#!/usr/bin/env bash
# Copies JNI libraries from native/prebuilt/jni/ into the runtime-only natives/* modules so they get packaged:
#   native/prebuilt/jni/<platform>/<lib>       -> natives/jvm-<platform>/resources/opencvkt/natives/<platform>/<lib>
#   native/prebuilt/jni/android/<abi>/<lib>    -> natives/android/resources/opencvkt/natives/android-<abi>/<lib>
# Missing platforms are skipped (with a note), so this works with a partial set of binaries.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC="$ROOT/native/prebuilt/jni"

for plat in linux-x64 linux-arm64 macos-arm64 windows-x64; do
  dst="$ROOT/natives/jvm-$plat/resources/opencvkt/natives/$plat"
  mkdir -p "$dst"
  find "$dst" -type f ! -name .gitkeep -delete
  if compgen -G "$SRC/$plat/*opencvkt.*" >/dev/null; then
    cp "$SRC/$plat/"*opencvkt.* "$dst/"
    echo "staged jvm-$plat: $(ls "$dst" | grep -v .gitkeep)"
  else
    echo "skip   jvm-$plat (no binary in $SRC/$plat)"
  fi
done

for abi in arm64-v8a armeabi-v7a x86_64 x86; do
  dst="$ROOT/natives/android/resources/opencvkt/natives/android-$abi"
  mkdir -p "$dst"
  find "$dst" -type f ! -name .gitkeep -delete
  if [[ -f "$SRC/android/$abi/libopencvkt.so" ]]; then
    cp "$SRC/android/$abi/libopencvkt.so" "$dst/"
    echo "staged android/$abi"
  else
    echo "skip   android/$abi"
  fi
done
