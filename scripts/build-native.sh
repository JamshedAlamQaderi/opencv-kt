#!/usr/bin/env bash
# Builds libopencvkt for one Kotlin target from the `opencv/` submodule.
#
#   scripts/build-native.sh <target> [--jobs N] [--clean]
#
# Targets (Kotlin names):
#   linuxX64 linuxArm64 mingwX64                         (Linux host; uses Kotlin/Native's own clang + sysroot)
#   macosArm64 iosArm64 iosSimulatorArm64 iosX64         (macOS host with Xcode)
#   androidNativeArm64 androidNativeArm32 androidNativeX64 androidNativeX86   (Linux host; Kotlin/Native's bundled NDK toolchain)
#
# Outputs:
#   native/prebuilt/<konan_target>/libopencvkt.a          static lib embedded into modules/core's klib (Kotlin/Native)
#   native/prebuilt/jni/<platform>/<lib>                  JNI shared lib for JVM desktop / Android, when applicable:
#       linuxX64 -> jni/linux-x64/libopencvkt.so          macosArm64 -> jni/macos-arm64/libopencvkt.dylib
#       linuxArm64 -> jni/linux-arm64/libopencvkt.so      mingwX64   -> jni/windows-x64/opencvkt.dll
#       androidNative<abi> -> jni/android/<abi>/libopencvkt.so
#
# Environment overrides: CMAKE, NINJA, JAVA_HOME, ANDROID_NDK_HOME, KONAN_DATA_DIR, OPENCVKT_ANDROID_API (default 24)
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET="${1:-}"
shift || true
JOBS="$( (nproc || sysctl -n hw.ncpu) 2>/dev/null || echo 4)"
CLEAN=0
while [[ $# -gt 0 ]]; do
  case "$1" in
    --jobs) JOBS="$2"; shift 2 ;;
    --clean) CLEAN=1; shift ;;
    *) echo "unknown option: $1" >&2; exit 2 ;;
  esac
done

die() { echo "error: $*" >&2; exit 1; }
[[ -n "$TARGET" ]] || die "usage: $0 <target> [--jobs N] [--clean]"
[[ -f "$ROOT/opencv/CMakeLists.txt" ]] || die "opencv submodule missing: run 'git submodule update --init --depth 1'"

# ---------------------------------------------------------------- tools
find_tool() { # name, extra search dirs...
  local name="$1"; shift
  command -v "$name" 2>/dev/null && return 0
  local d; for d in "$@"; do [[ -x "$d/$name" ]] && { echo "$d/$name"; return 0; }; done
  return 1
}
SDK_DIR="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}}"
SDK_CMAKE_BIN="$(ls -d "$SDK_DIR"/cmake/*/bin 2>/dev/null | sort -V | tail -1 || true)"
CMAKE="${CMAKE:-$(find_tool cmake "$SDK_CMAKE_BIN" /opt/homebrew/bin /usr/local/bin || true)}"
NINJA="${NINJA:-$(find_tool ninja "$SDK_CMAKE_BIN" /opt/homebrew/bin /usr/local/bin || true)}"
[[ -n "$CMAKE" ]] || die "cmake not found (install it or the Android SDK cmake package)"
[[ -n "$NINJA" ]] || die "ninja not found"

KONAN_DEPS="${KONAN_DATA_DIR:-$HOME/.konan}/dependencies"
konan_dep() { # glob -> newest match
  ls -d $KONAN_DEPS/$1 2>/dev/null | sort -V | tail -1 || true
}
need_konan() {
  [[ -n "$1" ]] || die "Kotlin/Native toolchain dependency '$2' not found in $KONAN_DEPS.
Run any native build once so Kotlin/Native downloads it, e.g.: ./kotlin build -m core -p $TARGET"
}

ndk_home() {
  local ndk="${ANDROID_NDK_HOME:-${ANDROID_NDK_ROOT:-}}"
  [[ -z "$ndk" ]] && ndk="$(ls -d "$SDK_DIR"/ndk/* 2>/dev/null | sort -V | tail -1 || true)"
  [[ -d "$ndk" ]] || die "Android NDK not found (set ANDROID_NDK_HOME)"
  echo "$ndk"
}

java_include_dirs() {
  local home="${JAVA_HOME:-}"
  if [[ -z "$home" ]]; then
    home="$(java -XshowSettings:properties -version 2>&1 | awk -F'= ' '/java.home/ {print $2}' | head -1)"
  fi
  [[ -f "$home/include/jni.h" ]] || die "JDK with include/jni.h not found (set JAVA_HOME)"
  local md; md="$(dirname "$(find "$home/include" -name jni_md.h | head -1)")"
  echo "$home/include;$md"
}

# ---------------------------------------------------------------- target matrix
TOOLCHAIN_ARGS=()
JNI_DIR=""          # relative to native/prebuilt/jni
JNI_INCLUDES=""
case "$TARGET" in
  linuxX64|linuxArm64)
    if [[ "$TARGET" == linuxX64 ]]; then KONAN=linux_x64; TRIPLE=x86_64-unknown-linux-gnu; JNI_DIR=linux-x64
    else KONAN=linux_arm64; TRIPLE=aarch64-unknown-linux-gnu; JNI_DIR=linux-arm64; fi
    LLVM="$(konan_dep 'llvm-*-essentials-*')"; need_konan "$LLVM" "llvm"
    GCC="$(konan_dep "$TRIPLE-gcc-*")"; need_konan "$GCC" "$TRIPLE-gcc"
    TOOLCHAIN_ARGS=(-DCMAKE_TOOLCHAIN_FILE="$ROOT/native/cmake/toolchains/konan-linux.cmake"
                    -DKONAN_LLVM_HOME="$LLVM" -DKONAN_GCC_HOME="$GCC" -DKONAN_TRIPLE="$TRIPLE")
    JNI_INCLUDES="$(java_include_dirs)"
    ;;
  mingwX64)
    KONAN=mingw_x64; JNI_DIR=windows-x64
    LLVM="$(konan_dep 'llvm-*-essentials-*')"; need_konan "$LLVM" "llvm"
    MSYS="$(konan_dep 'msys2-mingw-w64-x86_64-*')"; need_konan "$MSYS" "msys2-mingw-w64-x86_64"
    TOOLCHAIN_ARGS=(-DCMAKE_SYSTEM_NAME=Windows -DCMAKE_SYSTEM_PROCESSOR=AMD64
                    -DCMAKE_C_COMPILER="$LLVM/bin/clang" -DCMAKE_CXX_COMPILER="$LLVM/bin/clang++"
                    -DCMAKE_C_COMPILER_TARGET=x86_64-w64-mingw32 -DCMAKE_CXX_COMPILER_TARGET=x86_64-w64-mingw32
                    -DCMAKE_SYSROOT="$MSYS" -DCMAKE_AR="$LLVM/bin/llvm-ar" -DCMAKE_RC_COMPILER=false
                    "-DCMAKE_EXE_LINKER_FLAGS=-fuse-ld=lld" "-DCMAKE_SHARED_LINKER_FLAGS=-fuse-ld=lld")
    # Cross-compiling from Linux: the host JDK has no win32/jni_md.h, so use our minimal one.
    JNI_INCLUDES="$(java_include_dirs | cut -d';' -f1);$ROOT/native/third_party/jni/win32"
    ;;
  macosArm64)
    KONAN=macos_arm64; JNI_DIR=macos-arm64
    TOOLCHAIN_ARGS=(-DCMAKE_OSX_ARCHITECTURES=arm64 -DCMAKE_OSX_DEPLOYMENT_TARGET=11.0)
    JNI_INCLUDES="$(java_include_dirs)"
    ;;
  iosArm64|iosSimulatorArm64|iosX64)
    case "$TARGET" in
      iosArm64) KONAN=ios_arm64; SDK=iphoneos; ARCH=arm64 ;;
      iosSimulatorArm64) KONAN=ios_simulator_arm64; SDK=iphonesimulator; ARCH=arm64 ;;
      iosX64) KONAN=ios_x64; SDK=iphonesimulator; ARCH=x86_64 ;;
    esac
    TOOLCHAIN_ARGS=(-DCMAKE_SYSTEM_NAME=iOS -DCMAKE_OSX_SYSROOT="$SDK" -DCMAKE_OSX_ARCHITECTURES="$ARCH"
                    -DCMAKE_OSX_DEPLOYMENT_TARGET=14.0 -DCMAKE_XCODE_ATTRIBUTE_ONLY_ACTIVE_ARCH=NO)
    ;;
  androidNativeArm64|androidNativeArm32|androidNativeX64|androidNativeX86)
    case "$TARGET" in
      androidNativeArm64) KONAN=android_arm64; ABI=arm64-v8a ;;
      androidNativeArm32) KONAN=android_arm32; ABI=armeabi-v7a ;;
      androidNativeX64) KONAN=android_x64; ABI=x86_64 ;;
      androidNativeX86) KONAN=android_x86; ABI=x86 ;;
    esac
    # Kotlin/Native links androidNative binaries with its own NDK standalone toolchain (clang 8, API 21,
    # its own libc++). Objects from a newer NDK reference libc++/compiler-rt symbols it doesn't have,
    # so compile with that exact toolchain. The JNI .so built here is what Android apps load, too.
    KN_NDK="$(konan_dep 'target-toolchain-*-android_ndk')"; need_konan "$KN_NDK" "target-toolchain-*-android_ndk"
    JNI_DIR="android/$ABI"
    case "$ABI" in
      arm64-v8a) GCC_TRIPLE=aarch64-linux-android ;; armeabi-v7a) GCC_TRIPLE=arm-linux-androideabi ;;
      x86_64) GCC_TRIPLE=x86_64-linux-android ;; x86) GCC_TRIPLE=i686-linux-android ;;
    esac
    CLANG_TARGET="${GCC_TRIPLE/arm-linux-androideabi/armv7a-linux-androideabi}21"
    # CMake recognises a standalone toolchain by a `bin/<triple>-gcc`, which the clang-only K/N toolchain lacks:
    # mirror it via symlinks and add a stub that only answers CMake's -dumpversion / -dumpmachine probes.
    KN_NDK_MIRROR="$ROOT/native/build/.kn-android-toolchain"
    if [[ ! -x "$KN_NDK_MIRROR/bin/$GCC_TRIPLE-gcc" ]]; then
      rm -rf "$KN_NDK_MIRROR"; mkdir -p "$KN_NDK_MIRROR/bin"
      for e in "$KN_NDK"/*; do [[ "$(basename "$e")" == bin ]] || ln -s "$e" "$KN_NDK_MIRROR/"; done
      for e in "$KN_NDK"/bin/*; do ln -s "$e" "$KN_NDK_MIRROR/bin/"; done
      for t in aarch64-linux-android arm-linux-androideabi x86_64-linux-android i686-linux-android; do
        printf '#!/bin/sh\ncase "$1" in -dumpversion) echo 4.9.x ;; -dumpmachine) echo %s ;; *) echo "stub: use clang" >&2; exit 1 ;; esac\n' "$t" \
          > "$KN_NDK_MIRROR/bin/$t-gcc"
        chmod +x "$KN_NDK_MIRROR/bin/$t-gcc"
      done
    fi
    # CMake's standalone mode passes --target without the API level; ours comes later on the command line and wins.
    TOOLCHAIN_ARGS=(-DCMAKE_SYSTEM_NAME=Android -DCMAKE_SYSTEM_VERSION=21
                    -DCMAKE_ANDROID_STANDALONE_TOOLCHAIN="$KN_NDK_MIRROR" -DCMAKE_ANDROID_ARCH_ABI="$ABI"
                    "-DCMAKE_C_FLAGS=--target=$CLANG_TARGET" "-DCMAKE_CXX_FLAGS=--target=$CLANG_TARGET -static-libstdc++"
                    "-DCMAKE_SHARED_LINKER_FLAGS=--target=$CLANG_TARGET -static-libstdc++"
                    "-DCMAKE_EXE_LINKER_FLAGS=--target=$CLANG_TARGET -static-libstdc++"
                    -DCMAKE_ASM_COMPILER="$KN_NDK/bin/clang" "-DCMAKE_ASM_FLAGS=--target=$CLANG_TARGET"
                    -DANDROID_ABI="$ABI")
    [[ "$ABI" == armeabi-v7a ]] && TOOLCHAIN_ARGS+=(-DCMAKE_ANDROID_ARM_NEON=ON)
    ;;
  *) die "unknown target '$TARGET'" ;;
esac

BUILD="$ROOT/native/build/$TARGET"
INSTALL="$BUILD/opencv-install"
OUT_STATIC="$ROOT/native/prebuilt/$KONAN"
[[ $CLEAN -eq 1 ]] && rm -rf "$BUILD"
mkdir -p "$BUILD" "$OUT_STATIC"

echo "==> [$TARGET] cmake=$CMAKE jobs=$JOBS"

# ---------------------------------------------------------------- 1. OpenCV (static)
"$CMAKE" -S "$ROOT/opencv" -B "$BUILD/opencv" -G Ninja -DCMAKE_MAKE_PROGRAM="$NINJA" \
  -C "$ROOT/native/cmake/opencv-options.cmake" \
  -DCMAKE_INSTALL_PREFIX="$INSTALL" \
  "${TOOLCHAIN_ARGS[@]}"
"$CMAKE" --build "$BUILD/opencv" --parallel "$JOBS"
"$CMAKE" --install "$BUILD/opencv" >/dev/null

OPENCV_DIR="$(dirname "$(find "$INSTALL" -name OpenCVConfig.cmake | head -1)")"
[[ -n "$OPENCV_DIR" ]] || die "OpenCVConfig.cmake not found under $INSTALL"

# ---------------------------------------------------------------- 2. shim + JNI
BUILD_JNI=OFF; [[ -n "$JNI_DIR" ]] && BUILD_JNI=ON
"$CMAKE" -S "$ROOT/native" -B "$BUILD/opencvkt" -G Ninja -DCMAKE_MAKE_PROGRAM="$NINJA" \
  -DCMAKE_BUILD_TYPE=Release -DOpenCV_DIR="$OPENCV_DIR" -DOPENCV_KT_ROOT="$ROOT" \
  -DOPENCVKT_BUILD_JNI="$BUILD_JNI" -DOPENCVKT_JNI_INCLUDE_DIRS="$JNI_INCLUDES" \
  "${TOOLCHAIN_ARGS[@]}"
"$CMAKE" --build "$BUILD/opencvkt" --parallel "$JOBS"

# ---------------------------------------------------------------- 3. merge static archives -> libopencvkt.a
SHIM_A="$(find "$BUILD/opencvkt" -maxdepth 1 -name 'libopencvkt_shim.a' | head -1)"
OPENCV_ARCHIVES=()   # (no `mapfile`: macOS ships bash 3.2)
while IFS= read -r archive; do OPENCV_ARCHIVES+=("$archive"); done < <(find "$INSTALL" -name '*.a' | sort)
[[ -n "$SHIM_A" && ${#OPENCV_ARCHIVES[@]} -gt 0 ]] || die "static archives not found"
MERGED="$OUT_STATIC/libopencvkt.a"
rm -f "$MERGED"
if [[ "$KONAN" == macos_* || "$KONAN" == ios_* ]]; then
  libtool -static -no_warning_for_no_symbols -o "$MERGED" "$SHIM_A" "${OPENCV_ARCHIVES[@]}"
else
  AR="${LLVM:+$LLVM/bin/llvm-ar}"
  [[ -z "$AR" && -n "${KN_NDK:-}" ]] && AR="$KN_NDK/bin/llvm-ar"
  AR="${AR:-ar}"
  { echo "create $MERGED"; for a in "$SHIM_A" "${OPENCV_ARCHIVES[@]}"; do echo "addlib $a"; done; echo "save"; echo "end"; } | "$AR" -M
fi
# Drop debug info (the NDK toolchain always compiles with -g, which makes the archive ~7x larger).
STRIP=""
if [[ "$KONAN" == macos_* || "$KONAN" == ios_* ]]; then STRIP="strip"
elif [[ -n "${KN_NDK:-}" ]]; then STRIP="$KN_NDK/bin/llvm-strip"
fi
if [[ -n "$STRIP" ]]; then
  if [[ "$STRIP" == strip ]]; then "$STRIP" -S "$MERGED" 2>/dev/null || true; else "$STRIP" --strip-debug "$MERGED"; fi
fi
echo "==> static: $MERGED ($(du -h "$MERGED" | cut -f1))"

# ---------------------------------------------------------------- 4. JNI shared library
if [[ -n "$JNI_DIR" ]]; then
  OUT_JNI="$ROOT/native/prebuilt/jni/$JNI_DIR"
  mkdir -p "$OUT_JNI"
  LIB="$(find "$BUILD/opencvkt" -maxdepth 1 \( -name 'libopencvkt.so' -o -name 'libopencvkt.dylib' -o -name 'opencvkt.dll' \) | head -1)"
  [[ -n "$LIB" ]] || die "JNI library not produced"
  cp "$LIB" "$OUT_JNI/"
  if [[ -n "$STRIP" ]]; then
    if [[ "$STRIP" == strip ]]; then "$STRIP" -x "$OUT_JNI/$(basename "$LIB")"; else "$STRIP" --strip-unneeded "$OUT_JNI/$(basename "$LIB")"; fi
  fi
  LIB="$OUT_JNI/$(basename "$LIB")"
  echo "==> jni:    $OUT_JNI/$(basename "$LIB") ($(du -h "$LIB" | cut -f1))"
fi

# ---------------------------------------------------------------- 5. refresh cinterop .def files
"$ROOT/scripts/generate-cinterop-defs.sh" >/dev/null
