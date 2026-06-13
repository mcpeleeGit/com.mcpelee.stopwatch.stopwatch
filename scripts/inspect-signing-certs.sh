#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'USAGE'
Inspect signing certificate fingerprints.

Usage:
  scripts/inspect-signing-certs.sh --keystore /path/to/key.jks [--pem /path/to/cert.pem]

Optional:
  --alias NAME      Show a specific alias only

The keystore password is prompted by keytool unless RELEASE_STORE_PASSWORD is set.
Compare SHA-256 with Play Console > Setup > App integrity.
USAGE
}

KEYSTORE=""
PEM=""
ALIAS=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --keystore)
      KEYSTORE="${2:-}"
      shift 2
      ;;
    --pem)
      PEM="${2:-}"
      shift 2
      ;;
    --alias)
      ALIAS="${2:-}"
      shift 2
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

if [[ -z "$KEYSTORE" ]]; then
  usage >&2
  exit 2
fi

if [[ ! -f "$KEYSTORE" ]]; then
  printf 'Keystore not found: %s\n' "$KEYSTORE" >&2
  exit 1
fi

KEYTOOL_ARGS=(-list -v -keystore "$KEYSTORE")
if [[ -n "$ALIAS" ]]; then
  KEYTOOL_ARGS+=(-alias "$ALIAS")
fi
if [[ -n "${RELEASE_STORE_PASSWORD:-}" ]]; then
  KEYTOOL_ARGS+=(-storepass "$RELEASE_STORE_PASSWORD")
fi

printf '== Keystore ==\n%s\n\n' "$KEYSTORE"
keytool "${KEYTOOL_ARGS[@]}" \
  | awk '
      /Alias name:/ || /별칭 이름:/ { print }
      /Entry type:/ || /항목 유형:/ { print }
      /Owner:/ || /소유자:/ { print }
      /Issuer:/ || /발급자:/ { print }
      /Serial number:/ || /일련 번호:/ { print }
      /SHA1:/ { print }
      /SHA256:/ { print }
    '

if [[ -n "$PEM" ]]; then
  if [[ ! -f "$PEM" ]]; then
    printf '\nPEM certificate not found: %s\n' "$PEM" >&2
    exit 1
  fi

  printf '\n== PEM certificate ==\n%s\n\n' "$PEM"
  openssl x509 -in "$PEM" -noout -subject -issuer -serial -fingerprint -sha1
  openssl x509 -in "$PEM" -noout -fingerprint -sha256
fi
