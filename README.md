# opencv-kt

Kotlin Multiplatform bindings for [OpenCV](https://github.com/opencv/opencv) **5.1.0**, compiled from source
(the `opencv/` git submodule), split into one Kotlin module per OpenCV module, and built with the
[Kotlin Toolchain](https://github.com/JetBrains/kotlin-toolchain) (`./kotlin`).

It does **not** wrap OpenCV's official Android SDK, iOS framework or Java bindings. Like
[opencv-kmp](https://github.com/Enaium/opencv-kmp), every platform goes through one C ABI:

```
              ┌────────────────────── common Kotlin API (Mat, Imgproc, Imgcodecs, ...) ──────────────────────┐
              │                                                                                               │
  src@jvmAndAndroid: JNI  ──►  modules/<m>/native/jni.cpp ─┐                 src@native: cinterop (.def generated
  (JVM desktop + Android)                                  ▼                 by scripts/generate-cinterop-defs.sh)
                                          modules/<m>/include/opencvkt/<m>.h  ◄────────────────────────┘
                                          modules/<m>/native/shim.cpp   (extern "C", catches all C++ exceptions)
                                                           │
                                         static OpenCV built from the opencv/ submodule
```

## Platforms

| Kotlin target | Binary | How it's loaded |
|---|---|---|
| `jvm` (linux-x64, linux-arm64, macos-arm64, windows-x64) | `libopencvkt.{so,dylib}` / `opencvkt.dll` | `NativeLibraryLoader` (see below) |
| `android` | `libopencvkt.so` per ABI | `System.loadLibrary` (app's `jniLibs`), else extracted from `opencvkt-natives-android`'s resources |
| `iosArm64`, `iosSimulatorArm64`, `iosX64`, `macosArm64` | `libopencvkt.a` | linked statically (embedded in `opencvkt-core`'s klib) |
| `linuxX64`, `linuxArm64`, `mingwX64` | `libopencvkt.a` | linked statically |
| `androidNativeArm64/Arm32/X64/X86` | `libopencvkt.a` | linked statically |

## Modules

Group: `com.jamshedalamqaderi.kmp.opencvkt` · packages: `com.jamshedalamqaderi.kmp.opencvkt.<module>`

| Artifact | OpenCV module | Bindings |
|---|---|---|
| `opencvkt-core` | core | `Mat`, `Scalar`, `Size`, `Point`, `CvType`, `OpenCv.version` |
| `opencvkt-imgproc` | imgproc | `cvtColor`, `gaussianBlur`, `resize`, `threshold`, `canny` |
| `opencvkt-imgcodecs` | imgcodecs | `imread`, `imwrite`, `imdecode`, `imencode` |
| `opencvkt-photo` | photo | `inpaint`, `fastNlMeansDenoising` |
| `opencvkt-flann`, `opencvkt-geometry`, `opencvkt-videoio`, `opencvkt-highgui`, `opencvkt-features`, `opencvkt-stereo`, `opencvkt-objdetect`, `opencvkt-calib`, `opencvkt-dnn`, `opencvkt-video`, `opencvkt-ptcloud`, `opencvkt-stitching` | same names | module scaffolding only — native code is compiled in, Kotlin bindings still to be written |
| `opencvkt` | — | umbrella: depends on all of the above |
| `opencvkt-natives-<platform>` | — | runtime-only JNI binary: `linux-x64`, `linux-arm64`, `macos-arm64`, `windows-x64`, `android` |

OpenCV 5 dropped `ml` (moved to opencv_contrib), renamed `features2d` → `features`, and added `geometry` / `ptcloud`.

## Using it

```kotlin
// build.gradle.kts (Gradle consumers)
kotlin {
    sourceSets {
        commonMain.dependencies { implementation("com.jamshedalamqaderi.kmp.opencvkt:opencvkt:<version>") }
        // the native binary for JVM / Android is a separate, runtime-only artifact:
        jvmMain.dependencies { runtimeOnly("com.jamshedalamqaderi.kmp.opencvkt:opencvkt-natives-linux-x64:<version>") }
        androidMain.dependencies { runtimeOnly("com.jamshedalamqaderi.kmp.opencvkt:opencvkt-natives-android:<version>") }
    }
}
```

```kotlin
Imgcodecs.imread("in.jpg").use { src ->
    Mat().use { gray ->
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
        Imgcodecs.imwrite("gray.png", gray)
    }
}
```

`Mat` lives in native memory: always `close()` it or use `use {}`.

### Getting the JVM binary without the natives artifact

`NativeLibraryLoader` tries, in order:

1. `-Dopencvkt.library=/path/to/libopencvkt.so` — explicit file
2. `-Dopencvkt.library.path=/dir` or env `OPENCVKT_LIBRARY_PATH=/dir` — e.g. an unzipped
   `opencvkt-<version>-jni-<platform>.zip` from the GitHub release
3. classpath resource `opencvkt/natives/<platform>/<file>` — what `opencvkt-natives-*` ships; you can also drop the file
   into your own app's `resources/` at that path
4. `System.loadLibrary("opencvkt")` (`java.library.path`)

## Building

Full per-OS setup (Linux, macOS, Windows), troubleshooting and the repository layout:
**[docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)**. Quick start on Linux:

Requirements: JDK 17+, CMake ≥ 3.16 + Ninja (the Android SDK's `cmake` package works), and per target:
Kotlin/Native's own toolchain for Linux/MinGW/androidNative (`scripts/prime-konan.sh <konan_target>` fetches it), Xcode for Apple.

```bash
git clone --recurse-submodules --shallow-submodules https://github.com/JamshedAlamQaderi/opencv-kt.git
cd opencv-kt
scripts/prime-konan.sh linux_x64     # downloads Kotlin/Native's clang + glibc 2.19 sysroot
scripts/build-native.sh linuxX64     # OpenCV + shims -> native/prebuilt/linux_x64/libopencvkt.a + jni/linux-x64/libopencvkt.so
scripts/stage-natives.sh             # copy JNI libs into natives/* modules
./kotlin test -p jvm -p linuxX64
./kotlin run -m jvm-app              # samples/jvm-app
```

Or skip compiling OpenCV: `scripts/fetch-natives.sh <version> linux_x64 jni-linux-x64` downloads the release assets.

The cinterop `.def` files are generated (cinterop needs absolute paths) and git-ignored; both scripts above refresh
them. If you only need Kotlin/Native klibs without binaries, run `scripts/generate-cinterop-defs.sh` once after cloning.

Linux and Android binaries are compiled with Kotlin/Native's own toolchains (clang + glibc 2.19 / libstdc++ 8 sysroot
for Linux, its bundled NDK for Android, API 21), so the static libraries link into Kotlin/Native executables, the Linux
JNI `.so` runs on any glibc ≥ 2.19 distro, and the Android `.so` is 16 KB-page aligned.

### Layout

```
opencv/                        OpenCV 5.1.0 (git submodule)
native/CMakeLists.txt          builds libopencvkt from every modules/*/native/*.cpp
native/cmake/opencv-options.cmake   which OpenCV modules/3rd-party libs are built
native/include/                prelude.h (shared C ABI types) + internal C++/JNI helpers
modules/<m>/include/opencvkt/<m>.h  the module's C ABI
modules/<m>/native/shim.cpp    C ABI -> OpenCV C++
modules/<m>/native/jni.cpp     JNI -> C ABI
modules/<m>/src/               common Kotlin API + `expect object <M>Bridge`
modules/<m>/src@jvmAndAndroid/ JNI actuals
modules/<m>/src@native/        cinterop actuals
modules/<m>/cinterop@native/   generated .def (scripts/generate-cinterop-defs.sh; absolute paths, core embeds libopencvkt.a)
natives/                       runtime-only JNI artifacts
templates/                     shared module settings (Kotlin version, publishing)
```

### Adding bindings to a module

1. Declare the C function in `modules/<m>/include/opencvkt/<m>.h`.
2. Implement it in `modules/<m>/native/shim.cpp`, wrapping the body in `ocvkt::guard` (C++ exceptions must not cross the C ABI).
3. Add the JNI wrapper in `modules/<m>/native/jni.cpp`.
4. Add it to the `expect object <M>Bridge` and its JNI + cinterop actuals, then expose it in the common API.
5. Rebuild natives (`scripts/build-native.sh <target>`); it also regenerates the cinterop `.def` files.

## Publishing

`.github/workflows/publish.yaml` runs on tags `vX.Y.Z` / `vX.Y.Z-dev-N`: builds natives for every target, publishes all
modules with `./kotlin publish mavenCentral` (manual release in the Central Portal by default), and attaches the native
archives to a GitHub Release. Repository secrets: `SONATYPE_USERNAME`, `SONATYPE_PASSWORD` (Central Portal user token),
`GPG_PRIVATE_KEY` (ASCII-armored), `GPG_PASSWORD`.

## Contributing

Contributions are welcome, especially bindings for the placeholder modules. Read
**[CONTRIBUTING.md](CONTRIBUTING.md)** for the conventions and the pull-request contract; PRs use the
[PR template](.github/PULL_REQUEST_TEMPLATE.md).

## License

Apache-2.0. OpenCV is Apache-2.0 as well.
