# Development guide

How to build, test and work on opencv-kt on Linux, macOS and Windows.

> **Status of each path.** Linux x86_64 is the reference setup and is verified end to end (JVM + `linuxX64` tests,
> both samples). The macOS and Windows instructions follow the same scripts but have **not been run yet** — if
> something doesn't work, please open an issue or a PR (see [CONTRIBUTING.md](../CONTRIBUTING.md)).

## How the build works (read this first)

There are two separate builds:

1. **Native build** — `scripts/build-native.sh <target>` compiles OpenCV from the `opencv/` submodule plus every
   module's C++ shim into `libopencvkt`:
   - `native/prebuilt/<konan_target>/libopencvkt.a`: static library, embedded into `opencvkt-core`'s Kotlin/Native klib
   - `native/prebuilt/jni/<platform>/libopencvkt.{so,dylib}` / `opencvkt.dll`: JNI library for JVM / Android

   It also regenerates `modules/*/cinterop@native/*.def` (cinterop needs absolute paths, so those files are
   generated and git-ignored). The first build of a target takes 10–20 minutes; later builds only recompile what
   changed.
2. **Kotlin build** — `./kotlin build` / `./kotlin test` (the Kotlin Toolchain wrapper; `kotlin.bat` on Windows).
   It downloads its own Kotlin, JDK and Android SDK pieces on first use.

You only need to redo step 1 when you change C/C++ code, a header, or the OpenCV submodule. If you only touch Kotlin
code, build the natives once and then iterate with `./kotlin test`.

Instead of compiling OpenCV you can download prebuilt natives from a GitHub release (once one exists):
`scripts/fetch-natives.sh <version> <targets...>`.

### Targets and where they can be built

| Kotlin target | `build-native.sh` target | Build host | JNI output |
|---|---|---|---|
| `linuxX64` | `linuxX64` | Linux x86_64 | `jni/linux-x64/libopencvkt.so` |
| `linuxArm64` | `linuxArm64` | Linux x86_64 (cross) | `jni/linux-arm64/libopencvkt.so` |
| `mingwX64` | `mingwX64` | Linux x86_64 (cross, experimental) | `jni/windows-x64/opencvkt.dll` |
| `androidNative*` | `androidNativeArm64` / `Arm32` / `X64` / `X86` | Linux x86_64 | `jni/android/<abi>/libopencvkt.so` |
| `macosArm64` | `macosArm64` | macOS (Xcode) | `jni/macos-arm64/libopencvkt.dylib` |
| `iosArm64`, `iosSimulatorArm64`, `iosX64` | same names | macOS (Xcode) | — |

Linux, MinGW and androidNative natives are compiled with **Kotlin/Native's own toolchains** (downloaded into
`~/.konan/dependencies`), because Kotlin/Native links those targets against its own old glibc / NDK, and
libraries built with your system compiler won't link. `scripts/prime-konan.sh <konan_target>` downloads them.

## Linux (x86_64)

Verified on Ubuntu-family distros; others should work the same.

### Prerequisites

- git, bash, curl, zip, unzip
- JDK 17+ with headers (`jni.h`); `JAVA_HOME` set, or `java` on `PATH`
- CMake ≥ 3.16 and Ninja (the Android SDK's `cmake` package also works; the script finds it in `~/Android/Sdk/cmake`)

```bash
# Debian / Ubuntu
sudo apt install git curl zip unzip cmake ninja-build openjdk-17-jdk
# Fedora
sudo dnf install git curl zip unzip cmake ninja-build java-17-openjdk-devel
# Arch
sudo pacman -S git curl zip unzip cmake ninja jdk17-openjdk
```

### Build and test

```bash
git clone --recurse-submodules --shallow-submodules https://github.com/JamshedAlamQaderi/opencv-kt.git
cd opencv-kt

scripts/prime-konan.sh linux_x64        # downloads Kotlin/Native's clang + sysroot (first time only)
scripts/build-native.sh linuxX64        # OpenCV + shims -> native/prebuilt/
scripts/stage-natives.sh                # copies JNI libs into natives/* (needed by samples / packaging)

./kotlin test -p jvm -p linuxX64        # all tests on JVM and Kotlin/Native
./kotlin run -m jvm-app                 # sample: writes gray.png / edges.png
./kotlin run -m linux-app               # same sample as a native executable
./kotlin run -m jvm-app -- ~/Pictures/photo.jpg
```

Useful variations:

```bash
./kotlin test -m imgproc -p jvm                                   # one module, one platform
./kotlin test -p jvm --include-classes='*ImgprocTest'             # one test class
scripts/build-native.sh linuxX64 --clean                          # rebuild from scratch
scripts/build-native.sh linuxX64 --jobs 4                         # limit parallelism (low RAM)
```

### Other targets from Linux

```bash
scripts/prime-konan.sh linux_arm64   && scripts/build-native.sh linuxArm64
scripts/prime-konan.sh android_arm64 && scripts/build-native.sh androidNativeArm64   # also builds the Android JNI .so
scripts/prime-konan.sh mingw_x64     && scripts/build-native.sh mingwX64             # experimental
```

Android apps: run `scripts/stage-natives.sh` after building the `androidNative*` targets so
`opencvkt-natives-android` contains the `.so` files. To try it on an emulator you need a matching ABI
(`androidNativeX64` for the usual x86_64 emulator images).

## macOS (Apple Silicon)

> Not run yet by the maintainers. The CI release workflow uses the same commands on `macos-latest`.

### Prerequisites

- Xcode (from the App Store) and its command line tools: `xcode-select --install`, then open Xcode once to accept the license
- [Homebrew](https://brew.sh), then: `brew install cmake ninja openjdk@17`
- Point `JAVA_HOME` at a JDK, e.g. `export JAVA_HOME=$(/usr/libexec/java_home -v 17)`

The scripts only need the bash 3.2 that ships with macOS.

### Build and test

```bash
git clone --recurse-submodules --shallow-submodules https://github.com/JamshedAlamQaderi/opencv-kt.git
cd opencv-kt

scripts/build-native.sh macosArm64          # static lib + libopencvkt.dylib
scripts/build-native.sh iosSimulatorArm64   # for iOS simulator tests
scripts/stage-natives.sh

./kotlin test -p jvm -p macosArm64
./kotlin test -p iosSimulatorArm64          # runs on an iOS simulator
```

For iOS devices, also build `iosArm64` (and `iosX64` for Intel-Mac simulators).

The sample apps are configured for Linux (`linux-app`, and `jvm-app` depends on the `linux-x64` natives module). On
macOS, run the sample with the locally built library instead:

```bash
./kotlin run -m jvm-app --jvm-args="-Dopencvkt.library.path=$PWD/native/prebuilt/jni"
```

Linux and Android natives can't be built on macOS; use a Linux machine, a Linux VM/container, or CI.

## Windows (x86_64)

> Not run yet by the maintainers. Treat this section as the intended setup; reports and fixes are very welcome.

The native build scripts are bash scripts designed for Linux and macOS, and the Windows library is cross-compiled
from Linux. On Windows, use **WSL 2** for native builds and either WSL or plain Windows for Kotlin work.

### Option A: everything inside WSL 2 (recommended)

1. Install WSL 2 with Ubuntu: `wsl --install -d Ubuntu` (PowerShell as admin), then reboot.
2. Inside Ubuntu, follow the [Linux](#linux-x86_64) instructions. Clone into the Linux filesystem (`~/…`), not
   `/mnt/c/…`; builds on `/mnt/c` are very slow.
3. This gives you the Linux targets. To produce the Windows JNI library as well (experimental):
   ```bash
   scripts/prime-konan.sh mingw_x64
   scripts/build-native.sh mingwX64     # -> native/prebuilt/jni/windows-x64/opencvkt.dll + mingw_x64/libopencvkt.a
   ```

### Option B: Kotlin development on Windows with a prebuilt DLL

If you only work on Kotlin code and want to run JVM tests natively on Windows:

1. Install Git for Windows (it includes Git Bash) and a JDK 17+.
2. Clone with the submodule:
   `git clone --recurse-submodules --shallow-submodules https://github.com/JamshedAlamQaderi/opencv-kt.git`
3. Get `opencvkt.dll`, either:
   - built in WSL (Option A, step 3) and copied to `native\prebuilt\jni\windows-x64\opencvkt.dll`, or
   - from a GitHub release: in Git Bash, `scripts/fetch-natives.sh <version> jni-windows-x64 mingw_x64`.
4. Run the JVM tests from `cmd` or PowerShell:
   ```bat
   kotlin.bat test -p jvm
   ```
   JVM tests look for the library in `native\prebuilt\jni\<platform>\` automatically.
5. For the `mingwX64` Kotlin/Native target, the static library must be at `native\prebuilt\mingw_x64\libopencvkt.a`;
   then run `scripts/generate-cinterop-defs.sh` in Git Bash and `kotlin.bat test -p mingwX64`.

## Working in an IDE

Open the project folder in IntelliJ IDEA with the **Kotlin Toolchain** plugin installed; it reads `project.yaml` and
the `module.yaml` files directly. Run `scripts/generate-cinterop-defs.sh` once so native source sets resolve the
cinterop packages (`com.jamshedalamqaderi.kmp.opencvkt.<module>.cinterop`).

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `UnsatisfiedLinkError: Could not load the opencv-kt native library` | No JNI library for your platform. Build it (`build-native.sh`), run `stage-natives.sh` for the samples, or pass `-Dopencvkt.library.path=<dir>`. The error lists every place that was searched. |
| `unresolved reference 'cinterop'` when compiling a native target | The `.def` files weren't generated. Run `scripts/generate-cinterop-defs.sh`. |
| `undefined symbol: ocvkt_...` when linking a native test/executable | No `libopencvkt.a` for that target yet: `scripts/build-native.sh <target>` (or `fetch-natives.sh`). |
| `Kotlin/Native toolchain dependency ... not found` | Run `scripts/prime-konan.sh <konan_target>` first. |
| `cmake not found` / `ninja not found` | Install them, or set `CMAKE=/path/to/cmake NINJA=/path/to/ninja`. |
| `JDK with include/jni.h not found` | Install a full JDK (not a JRE) and set `JAVA_HOME`. |
| Native build runs out of memory | `scripts/build-native.sh <target> --jobs 2`. |
| Changed C++ code but tests still see old behaviour | Re-run `scripts/build-native.sh <target>` (it rebuilds the library and refreshes the `.def` files, which makes the Kotlin Toolchain re-run cinterop) and, for JVM samples, `scripts/stage-natives.sh`. |

## Repository layout

```
opencv/                                OpenCV (git submodule)
native/CMakeLists.txt                  builds libopencvkt from every modules/*/native/*.cpp
native/cmake/opencv-options.cmake      which OpenCV modules / 3rd-party libraries / backends are built
native/cmake/toolchains/               CMake toolchain files (Kotlin/Native's Linux toolchain)
native/include/                        prelude.h (shared C ABI types) + internal C++/JNI helpers
modules/<m>/include/opencvkt/<m>.h     the module's C ABI
modules/<m>/native/{shim,jni}.cpp      C ABI → OpenCV C++, and JNI → C ABI
modules/<m>/src/                       common Kotlin API + expect bridge
modules/<m>/src@jvmAndAndroid/         JNI actuals        modules/<m>/src@native/   cinterop actuals
modules/<m>/test/                      common tests (run on every platform)
natives/                               runtime-only JNI artifacts (opencvkt-natives-*)
samples/                               jvm-app, linux-app
scripts/                               build-native, prime-konan, generate-cinterop-defs, stage/fetch/package-natives, set-version
templates/                             shared module settings (Kotlin version, publishing)
```
