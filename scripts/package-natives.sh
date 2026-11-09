#!/usr/bin/env bash
# Packs native/prebuilt/ into per-target archives for a GitHub release:
#   dist/opencvkt-<version>-<konan_target>.tar.gz   static lib for Kotlin/Native   (libopencvkt.a)
#   dist/opencvkt-<version>-jni-<platform>.zip       JNI lib for JVM / Android       (drop-in for -Dopencvkt.library.path)
# Usage: scripts/package-natives.sh <version>
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION="${1:?usage: $0 <version>}"
PRE="$ROOT/native/prebuilt"
DIST="$ROOT/dist"
mkdir -p "$DIST"

for dir in "$PRE"/*/; do
  name="$(basename "$dir")"
  [[ "$name" == jni ]] && continue
  [[ -f "$dir/libopencvkt.a" ]] || continue
  tar -C "$dir" -czf "$DIST/opencvkt-$VERSION-$name.tar.gz" libopencvkt.a
  echo "$DIST/opencvkt-$VERSION-$name.tar.gz"
done

if [[ -d "$PRE/jni" ]]; then
  while IFS= read -r lib; do
    rel="$(dirname "${lib#"$PRE/jni/"}")"           # linux-x64 | android/arm64-v8a
    tag="${rel//\//-}"
    (cd "$(dirname "$lib")" && zip -q -j "$DIST/opencvkt-$VERSION-jni-$tag.zip" "$(basename "$lib")")
    echo "$DIST/opencvkt-$VERSION-jni-$tag.zip"
  done < <(find "$PRE/jni" -type f \( -name '*.so' -o -name '*.dylib' -o -name '*.dll' \))
fi
