#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PEPK_JAR="$ROOT_DIR/tools/pepk/pepk.jar"

usage() {
  cat <<'USAGE'
Export an existing app signing private key for Google Play App Signing.

Required:
  --keystore PATH          Existing app signing JKS keystore
  --alias NAME             Key alias inside the keystore
  --encryption-key PATH    PEM encoded public key downloaded from Play Console
  --output PATH            Output file to upload to Play Console

Optional:
  --include-cert           Write a zip containing encryptedPrivateKey and certificate.pem

Passwords:
  By default the tool prompts securely for passwords.
  For non-interactive use, set:
    RELEASE_STORE_PASSWORD
    RELEASE_KEY_PASSWORD

Example:
  scripts/export-play-app-signing-key.sh \
    --keystore /secure/old-app-signing.jks \
    --alias upload \
    --encryption-key /secure/play-encryption-public-key.pem \
    --output build/play-app-signing/encrypted-private-key.pepk
USAGE
}

KEYSTORE=""
ALIAS=""
ENCRYPTION_KEY=""
OUTPUT=""
INCLUDE_CERT="false"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --keystore)
      KEYSTORE="${2:-}"
      shift 2
      ;;
    --alias)
      ALIAS="${2:-}"
      shift 2
      ;;
    --encryption-key)
      ENCRYPTION_KEY="${2:-}"
      shift 2
      ;;
    --output)
      OUTPUT="${2:-}"
      shift 2
      ;;
    --include-cert)
      INCLUDE_CERT="true"
      shift
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      printf 'Unknown argument: %s\n\n' "$1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

if [[ -z "$KEYSTORE" || -z "$ALIAS" || -z "$ENCRYPTION_KEY" || -z "$OUTPUT" ]]; then
  usage >&2
  exit 2
fi

if [[ ! -f "$PEPK_JAR" ]]; then
  "$ROOT_DIR/scripts/build-pepk-tool.sh"
fi

if [[ ! -f "$KEYSTORE" ]]; then
  printf 'Keystore not found: %s\n' "$KEYSTORE" >&2
  exit 1
fi

if [[ ! -f "$ENCRYPTION_KEY" ]]; then
  printf 'Encryption public key not found: %s\n' "$ENCRYPTION_KEY" >&2
  exit 1
fi

mkdir -p "$(dirname "$OUTPUT")"
rm -f "$OUTPUT"

ARGS=(
  --keystore "$KEYSTORE"
  --alias "$ALIAS"
  --output "$OUTPUT"
  --rsa-aes-encryption
  --encryption-key-path "$ENCRYPTION_KEY"
)

if [[ "$INCLUDE_CERT" == "true" ]]; then
  ARGS+=(--include-cert true)
fi

if [[ -n "${RELEASE_STORE_PASSWORD:-}" ]]; then
  ARGS+=(--keystore-pass "$RELEASE_STORE_PASSWORD")
fi

if [[ -n "${RELEASE_KEY_PASSWORD:-}" ]]; then
  ARGS+=(--key-pass "$RELEASE_KEY_PASSWORD")
fi

java -jar "$PEPK_JAR" "${ARGS[@]}"
chmod 600 "$OUTPUT"
printf 'Created encrypted key export: %s\n' "$OUTPUT"
