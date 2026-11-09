#!/usr/bin/env bash
# Downloads prebuilt natives from a GitHub release into native/prebuilt/ (instead of compiling OpenCV locally).
#
#   scripts/fetch-natives.sh <version> [target...]
#
# Targets: Kotlin/Native konan names (linux_x64, macos_arm64, ios_arm64, android_arm64, ...) and/or
#          jni-<platform> (jni-linux-x64, jni-macos-arm64, jni-windows-x64, jni-android-arm64-v8a, ...).
# Without targets, fetches everything attached to the release.
# Env: OPENCVKT_REPO (default JamshedAlamQaderi/opencv-kt)
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION="${1:?usage: $0 <version> [target...]}"; shift
REPO="${OPENCVKT_REPO:-JamshedAlamQaderi/opencv-kt}"
BASE="https://github.com/$REPO/releases/download/v$VERSION"
PRE="$ROOT/native/prebuilt"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT

TARGETS=("$@")
if [[ ${#TARGETS[@]} -eq 0 ]]; then
  TARGETS=(linux_x64 linux_arm64 mingw_x64 macos_arm64 ios_arm64 ios_simulator_arm64 ios_x64
           android_arm64 android_arm32 android_x64 android_x86
           jni-linux-x64 jni-linux-arm64 jni-macos-arm64 jni-windows-x64
           jni-android-arm64-v8a jni-android-armeabi-v7a jni-android-x86_64 jni-android-x86)
fi

for t in "${TARGETS[@]}"; do
  if [[ "$t" == jni-* ]]; then
    plat="${t#jni-}"
    asset="opencvkt-$VERSION-jni-$plat.zip"
    dest="$PRE/jni/$plat"
    [[ "$plat" == android-* ]] && dest="$PRE/jni/android/${plat#android-}"
    curl -fsSL -o "$TMP/$asset" "$BASE/$asset" || { echo "skip $t (no $asset)"; continue; }
    mkdir -p "$dest" && unzip -q -o "$TMP/$asset" -d "$dest"
  else
    asset="opencvkt-$VERSION-$t.tar.gz"
    curl -fsSL -o "$TMP/$asset" "$BASE/$asset" || { echo "skip $t (no $asset)"; continue; }
    mkdir -p "$PRE/$t" && tar -C "$PRE/$t" -xzf "$TMP/$asset"
  fi
  echo "fetched $t"
done
"$ROOT/scripts/generate-cinterop-defs.sh" >/dev/null
echo "Run scripts/stage-natives.sh to copy JNI libraries into natives/* modules."
