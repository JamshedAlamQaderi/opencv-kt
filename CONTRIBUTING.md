# Contributing to opencv-kt

Thanks for helping! This guide covers how the project is organised, the conventions every change follows, and the
contract a pull request has to meet before it can be merged.

- Setting up a machine and running the build/tests: **[docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)** (Linux, macOS, Windows)
- Project overview and usage: [README.md](README.md)

## Ways to contribute

- **Add bindings** for an OpenCV function or class. Twelve modules (`calib`, `dnn`, `features`, `flann`, `geometry`,
  `highgui`, `objdetect`, `ptcloud`, `stereo`, `stitching`, `video`, `videoio`) are still placeholders; any of them is
  a good place to start.
- **Test on a platform** we can't easily cover (macOS, iOS, Windows, Android devices) and report or fix what breaks.
- **Fix bugs and improve docs.**

For anything bigger than a handful of functions — a new module design, changing the C ABI conventions, build-system
changes — please open an issue first so we can agree on the approach before you invest time.

## Workflow

1. Fork the repository and clone it **with the submodule**:
   `git clone --recurse-submodules --shallow-submodules https://github.com/<you>/opencv-kt.git`
2. Create a branch from `main`: `feat/imgproc-morphology`, `fix/loader-windows`, `docs/dev-guide`, …
3. Make your change following the conventions below, with tests.
4. Run the checks from [Before you open a PR](#before-you-open-a-pr).
5. Open a pull request against `main` and fill in the template.

Commit messages: imperative, short subject line, scoped to the module when it helps, e.g.
`imgproc: bind morphologyEx and getStructuringElement`. Keep unrelated changes in separate PRs.

## How a binding is put together

Every OpenCV function goes through four layers. Using `Imgproc.threshold` as the example:

| Layer | File | What it does |
|---|---|---|
| C header | `modules/imgproc/include/opencvkt/imgproc.h` | Declares `ocvkt_imgproc_threshold(...)`, the C ABI shared by all platforms |
| C++ shim | `modules/imgproc/native/shim.cpp` | Calls `cv::threshold`, catches every C++ exception via `ocvkt::guard` |
| JNI glue | `modules/imgproc/native/jni.cpp` | `Java_..._ImgprocJni_threshold` → C function; throws `OpenCvException` on failure |
| Kotlin | `src/…/internal/ImgprocBridge.kt` (expect), `src@jvmAndAndroid/…/ImgprocJni.kt`, `src@native/…/ImgprocBridge.native.kt` (actuals), `src/…/Imgproc.kt` (public API) | Platform bridge + the API users call |

Then rebuild the native library for your platform (`scripts/build-native.sh <target>`), which also regenerates the
cinterop `.def` files, and add tests in `modules/<m>/test/`.

### Adding bindings to a placeholder module

A placeholder module has only `module.yaml` and `src/…/<Module>.kt`. To start binding it:

1. Create `modules/<m>/include/opencvkt/<m>.h` (copy the guard/`OCVKT_BEGIN_DECLS` structure from `imgproc.h`).
2. Create `modules/<m>/native/shim.cpp` and `modules/<m>/native/jni.cpp`. `native/CMakeLists.txt` picks up both
   automatically.
3. Add `aliases`-based source sets: `src@jvmAndAndroid/` and `src@native/` (the alias is already declared in
   `module.yaml`).
4. Run `scripts/build-native.sh <target>`. `scripts/generate-cinterop-defs.sh` notices the new header and generates
   `modules/<m>/cinterop@native/opencvkt_<m>.def`.
5. Replace the placeholder object's "Not implemented yet" KDoc with real documentation.

## Conventions

### C ABI (`include/opencvkt/*.h`, `native/shim.cpp`)

- Function names: `ocvkt_<module>_<snake_case_opencv_name>`, e.g. `ocvkt_imgproc_gaussian_blur`. Core's Mat
  functions use `ocvkt_mat_*`.
- Include `"opencvkt/prelude.h"` and wrap declarations in `OCVKT_BEGIN_DECLS` / `OCVKT_END_DECLS`; mark each
  function `OCVKT_API`.
- Only C types cross the boundary: fixed-width integers (`int32_t`, `int64_t`), `double`/`float`, `const char*`
  (UTF-8), pointers to caller-owned buffers with an explicit length, and opaque handles (`ocvkt_mat`).
- Functions that can fail return `ocvkt_status` (0 = OK). Functions that create an object return the handle and
  NULL on failure. Extra results go through out-pointers (see `ocvkt_imgproc_threshold`).
- **Every function body that can throw goes through `ocvkt::guard` / `ocvkt::guard_new`.** A C++ exception must
  never cross the C ABI: Kotlin/Native and JNI cannot unwind it and the process crashes.
- Ownership is explicit: whoever receives a new handle owns it; document it in the header comment.

### JNI (`native/jni.cpp`, `src@jvmAndAndroid/`)

- Declare entry points with `OCVKT_JNI(<mangled_class>, <ret>, <name>)`.
- Report failures with `ocvkt::jni::throw_if_failed` / `throw_if_null`, which raise `OpenCvException`.
- The Kotlin side is an `internal object <Module>Jni` with `@JvmStatic external fun` members that are **public**
  (Kotlin mangles the JVM names of `internal` members, which breaks the JNI lookup). Its `init` block calls
  `OpenCvNativeRuntime.ensureLoaded()`.
- Renaming a Kotlin package, class or function means renaming the C symbol too.

### Kotlin API (`src/`)

- Follow OpenCV's names in camelCase (`cv::GaussianBlur` → `gaussianBlur`), grouped in an object named after the
  module (`Imgproc`, `Imgcodecs`, `Photo`, …). Constants keep OpenCV's names (`COLOR_BGR2GRAY`).
- Output-parameter style: functions take a `dst: Mat` and let OpenCV allocate it, mirroring the C++ API and allowing
  buffer reuse. Return a value only where OpenCV does (e.g. `threshold` returns the computed threshold).
- Functions that return a new `Mat` must say in their KDoc that the caller owns it.
- Explicit API mode is on: every public declaration needs an explicit visibility and return type.
- **Every public declaration has KDoc**: what it does, the OpenCV function it maps to (`cv::...`), the meaning and
  valid range of each parameter, the return value, and which exceptions it throws. Internal bridge functions get a
  one-line doc naming their C function.
- Use `@InternalOpenCvApi` for anything that exists only to connect modules (like `Mat.nativeHandle`).
- No platform-specific types in the common API.

### Tests (`modules/<m>/test/`)

- Tests live in the common `test/` directory so they run on every platform.
- Cover at least: a normal call checked against a known result, and one error path (`assertFailsWith<OpenCvException>`).
- Keep tests self-contained: generate input Mats in code instead of committing image files.

## Before you open a PR

On Linux, the minimum set of checks is:

```bash
scripts/build-native.sh linuxX64      # only if you changed C/C++ or headers
scripts/stage-natives.sh
./kotlin build -p jvm -p android -p linuxX64
./kotlin test -p jvm -p linuxX64
```

On macOS use `macosArm64` instead of `linuxX64`. On Windows see [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md#windows).

## Pull request contract

A PR is ready to merge when all of the following hold. The [PR template](.github/PULL_REQUEST_TEMPLATE.md) repeats
this as a checklist.

1. **Scope:** one logical change; unrelated refactors go in their own PR.
2. **All four layers:** a new binding has the C header, shim (with `ocvkt::guard`), JNI glue, the expect bridge with
   both actuals, and the public Kotlin API. Partial bindings that only work on some platforms aren't accepted.
3. **Docs:** KDoc on every new public declaration; README's module table updated if a module gains bindings.
4. **Tests:** new or changed behaviour is tested in `modules/<m>/test/`, including an error path.
5. **Verified:** `./kotlin test` passes on JVM and on at least one native target you could run; the PR lists the
   platforms you actually ran it on.
6. **CI green:** the `CI` workflow passes.
7. **No generated or binary files committed:** nothing under `native/build/`, `native/prebuilt/`,
   `modules/*/cinterop@native/`, staged `natives/**` binaries, or `build/`.
8. **Compatibility:** breaking changes to the public API or the C ABI are called out in the PR description.
9. **OpenCV submodule:** bumped only in a dedicated PR, with the reason and the test results.

## Updating OpenCV

```bash
cd opencv && git fetch --depth 1 origin tag <new-tag> && git checkout <new-tag> && cd ..
git add opencv
```

Then rebuild the natives for at least one platform, run all tests, and check the C++ changes in OpenCV's release
notes for anything that affects existing shims (renamed functions, changed defaults, removed modules).

## Releases

Maintainers release by pushing a tag `vX.Y.Z` (or `vX.Y.Z-dev-N` for pre-releases); see the Publishing section of the
README. Contributors don't need to change versions.

## License

By contributing, you agree that your contributions are licensed under the Apache License 2.0, like the rest of the
project.
