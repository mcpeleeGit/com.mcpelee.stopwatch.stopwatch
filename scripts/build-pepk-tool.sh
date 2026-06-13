#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PEPK_DIR="$ROOT_DIR/tools/pepk"
SRC_JAR="$PEPK_DIR/pepk-src.jar"
OUT_JAR="$PEPK_DIR/pepk.jar"
WORK_DIR="$ROOT_DIR/build/pepk-src"
CLASSES_DIR="$ROOT_DIR/build/pepk-classes"
COMPILE_LOG="$ROOT_DIR/build/pepk-javac.log"

if [[ ! -f "$SRC_JAR" ]]; then
  mkdir -p "$PEPK_DIR"
  curl -L --fail --silent --show-error \
    "https://www.gstatic.com/play-apps-publisher-rapid/signing-tool/prod/pepk-src.jar" \
    -o "$SRC_JAR"
fi

rm -rf "$WORK_DIR" "$CLASSES_DIR"
mkdir -p "$WORK_DIR" "$CLASSES_DIR"

(
  cd "$WORK_DIR"
  jar xf "$SRC_JAR"
)

if ! find "$WORK_DIR" -name "*.java" -print0 \
  | xargs -0 javac -nowarn -encoding UTF-8 -source 11 -target 11 -d "$CLASSES_DIR" \
    2> "$COMPILE_LOG"; then
  cat "$COMPILE_LOG" >&2
  exit 1
fi

RESOURCE_DIR="$CLASSES_DIR/com/google/wireless/android/vending/developer/signing/tools/extern/export"
mkdir -p "$RESOURCE_DIR"
cat > "$RESOURCE_DIR/help.txt" <<'HELP'
ExportEncryptedPrivateKeyTool

Required RSA AES wrapping flags:
  --keystore <path>
  --alias <alias>
  --output <path>
  --rsa-aes-encryption
  --encryption-key-path <play-console-public-key.pem>

Optional:
  --keystore-pass <password>
  --key-pass <password>
  --include-cert true

Prefer scripts/export-play-app-signing-key.sh so passwords can be prompted securely.
HELP
cat > "$RESOURCE_DIR/license.txt" <<'LICENSE'
Google PEPK source is provided by Google for Play App Signing key export.
See the license headers in pepk-src.jar for the original source license.
LICENSE

(
  cd "$CLASSES_DIR"
  jar cfe "$OUT_JAR" \
    com.google.wireless.android.vending.developer.signing.tools.extern.export.ExportEncryptedPrivateKeyTool \
    .
)

printf 'Built %s\n' "$OUT_JAR"
