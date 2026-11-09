## Summary

<!-- What does this PR change, and why? Link the issue if there is one: "Closes #123". -->

## Type of change

- [ ] New bindings (OpenCV functions / classes)
- [ ] Bug fix
- [ ] Build / native toolchain / CI
- [ ] Documentation
- [ ] OpenCV submodule update
- [ ] Other:

## Modules touched

<!-- e.g. imgproc, core -->

## Checklist (see CONTRIBUTING.md → "Pull request contract")

- [ ] One logical change; unrelated refactors are in separate PRs
- [ ] New bindings have all four layers: C header, C++ shim wrapped in `ocvkt::guard`, JNI glue, expect bridge with JVM/Android + native actuals, and the public Kotlin API
- [ ] KDoc on every new public declaration (what it does, the `cv::` function it maps to, parameters, return value, exceptions)
- [ ] Tests added/updated in `modules/<m>/test/`, including an error path
- [ ] README module table updated (if a module gained bindings)
- [ ] No generated or binary files committed (`native/build/`, `native/prebuilt/`, `modules/*/cinterop@native/`, staged natives, `build/`)
- [ ] Breaking changes to the public API or C ABI are described below

## Platforms tested

<!-- Tick only what you actually ran. "Builds" = ./kotlin build passes; "Tests" = ./kotlin test passes. -->

| Platform | Builds | Tests |
|---|---|---|
| JVM (host: …) | [ ] | [ ] |
| linuxX64 | [ ] | [ ] |
| macosArm64 | [ ] | [ ] |
| iOS simulator | [ ] | [ ] |
| Android (device/emulator) | [ ] | [ ] |
| Windows (JVM / mingwX64) | [ ] | [ ] |

Native library rebuilt with `scripts/build-native.sh` for: <!-- e.g. linuxX64 -->

## Breaking changes

<!-- "None", or describe what changes for users and how to migrate. -->

## Notes for reviewers

<!-- Anything that needs special attention, open questions, follow-ups. -->
