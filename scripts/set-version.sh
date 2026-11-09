#!/usr/bin/env bash
# Sets the published version for every module (templates/publish.module-template.yaml).
# Accepts a git tag like v0.1.0 / v0.1.0-dev-3 (leading "v" stripped), mirroring KTransport's tag scheme.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION="${1:?usage: $0 <version|tag>}"
VERSION="${VERSION#v}"
sed -i.bak -E "s|^(    version: ).*$|\1$VERSION|" "$ROOT/templates/publish.module-template.yaml"
rm -f "$ROOT/templates/publish.module-template.yaml.bak"
grep -n "^    version:" "$ROOT/templates/publish.module-template.yaml"
