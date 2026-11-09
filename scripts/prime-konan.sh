#!/usr/bin/env bash
# Makes Kotlin/Native download its toolchain dependencies (clang/lld, Linux sysroots, MinGW sysroot) into ~/.konan,
# which scripts/build-native.sh reuses to compile OpenCV with the exact toolchain Kotlin/Native links with.
#   scripts/prime-konan.sh <konan_target>      e.g. linux_x64, linux_arm64, mingw_x64
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET="${1:?usage: $0 <konan_target>}"

# Compiling any native module makes the Kotlin Toolchain provision the Kotlin/Native distribution.
(cd "$ROOT" && ./kotlin build -m flann -p linuxX64 >/dev/null)

CACHE="${KOTLIN_TOOLCHAIN_CACHE:-$HOME/.cache/JetBrains/Kotlin}"
KONANC="$(ls -d "$CACHE"/extract.cache/*kotlin-native-prebuilt-*/bin/kotlinc-native 2>/dev/null | sort -V | tail -1)"
[[ -x "$KONANC" ]] || { echo "kotlinc-native not found under $CACHE" >&2; exit 1; }

TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
echo 'fun main() {}' > "$TMP/hello.kt"
"$KONANC" "$TMP/hello.kt" -target "$TARGET" -o "$TMP/hello" >/dev/null
echo "Kotlin/Native dependencies for $TARGET are in ${KONAN_DATA_DIR:-$HOME/.konan}/dependencies"
