# Compiles for Linux with Kotlin/Native's OWN toolchain (clang from ~/.konan + the gcc-8.3 / glibc-2.19 sysroot).
#
# Why: Kotlin/Native links linuxX64/linuxArm64 binaries against that old sysroot. A static library compiled with
# the host's gcc 13+/glibc 2.3x headers references newer libstdc++/glibc symbol versions and fails to link.
# Bonus: the JNI .so built this way runs on any glibc >= 2.19 (CentOS 7 era) distro.
#
# Required -D variables (scripts/build-native.sh fills them in):
#   KONAN_LLVM_HOME   ~/.konan/dependencies/llvm-<ver>-x86_64-linux-essentials-<n>
#   KONAN_GCC_HOME    ~/.konan/dependencies/<arch>-unknown-linux-gnu-gcc-8.3.0-glibc-<ver>-kernel-4.9-2
#   KONAN_TRIPLE      x86_64-unknown-linux-gnu | aarch64-unknown-linux-gnu
set(CMAKE_SYSTEM_NAME Linux)
if(KONAN_TRIPLE MATCHES "^aarch64")
    set(CMAKE_SYSTEM_PROCESSOR aarch64)
else()
    set(CMAKE_SYSTEM_PROCESSOR x86_64)
endif()

# Forward the variables to try_compile() projects.
list(APPEND CMAKE_TRY_COMPILE_PLATFORM_VARIABLES KONAN_LLVM_HOME KONAN_GCC_HOME KONAN_TRIPLE)

set(CMAKE_C_COMPILER "${KONAN_LLVM_HOME}/bin/clang")
set(CMAKE_CXX_COMPILER "${KONAN_LLVM_HOME}/bin/clang++")
set(CMAKE_C_COMPILER_TARGET "${KONAN_TRIPLE}")
set(CMAKE_CXX_COMPILER_TARGET "${KONAN_TRIPLE}")
set(CMAKE_AR "${KONAN_LLVM_HOME}/bin/llvm-ar" CACHE FILEPATH "")
# The K/N LLVM bundle has no llvm-ranlib: build the archive index with `llvm-ar s` instead.
set(CMAKE_RANLIB ":" CACHE FILEPATH "")
foreach(lang C CXX ASM)
    set(CMAKE_${lang}_ARCHIVE_FINISH "<CMAKE_AR> s <TARGET>")
endforeach()

set(CMAKE_SYSROOT "${KONAN_GCC_HOME}/${KONAN_TRIPLE}/sysroot")
set(_konan_flags "--gcc-toolchain=${KONAN_GCC_HOME}")
set(CMAKE_C_FLAGS_INIT "${_konan_flags}")
set(CMAKE_CXX_FLAGS_INIT "${_konan_flags}")
set(CMAKE_EXE_LINKER_FLAGS_INIT "${_konan_flags} -fuse-ld=lld")
set(CMAKE_SHARED_LINKER_FLAGS_INIT "${_konan_flags} -fuse-ld=lld")
set(CMAKE_MODULE_LINKER_FLAGS_INIT "${_konan_flags} -fuse-ld=lld")

set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_PACKAGE BOTH)
