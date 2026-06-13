# Play App Signing Private Key Export

This project includes a local wrapper for Google's PEPK source tool. It exports an existing app
signing private key from a JKS keystore, encrypts it with the PEM public key downloaded from Play
Console, and writes the file that Play Console asks you to upload.

The private key is never printed. Keep the keystore, passwords, and generated export file outside
version control.

## Inputs

- Existing app signing JKS keystore from the other app/repository
- Key alias in that keystore
- PEM encoded encryption public key downloaded from Play Console

Important: the keystore must contain the **app signing key**, not only the Play upload key. If Play
Console says the private key does not match the current app certificate, compare the keystore
certificate SHA-256 with Play Console > Setup > App integrity > App signing key certificate.

```bash
scripts/inspect-signing-certs.sh \
  --keystore /secure/old-app-signing.jks \
  --pem /secure/upload_certificate.pem
```

## Build the local PEPK tool

```bash
scripts/build-pepk-tool.sh
```

This downloads `pepk-src.jar` if needed and compiles it into:

```text
tools/pepk/pepk.jar
```

## Export the encrypted private key

Interactive password prompt:

```bash
scripts/export-play-app-signing-key.sh \
  --keystore /secure/old-app-signing.jks \
  --alias your_key_alias \
  --encryption-key /secure/play-encryption-public-key.pem \
  --output build/play-app-signing/encrypted-private-key.pepk
```

Non-interactive:

```bash
export RELEASE_STORE_PASSWORD='...'
export RELEASE_KEY_PASSWORD='...'

scripts/export-play-app-signing-key.sh \
  --keystore /secure/old-app-signing.jks \
  --alias your_key_alias \
  --encryption-key /secure/play-encryption-public-key.pem \
  --output build/play-app-signing/encrypted-private-key.pepk
```

Upload the generated `.pepk` file in Play Console where it asks for the encrypted private key.

## Notes

- The bundled Google source tool loads `jks` keystores. If your existing key is PKCS12, convert a
  temporary copy to JKS first with `keytool -importkeystore`.
- Use `--include-cert` only if Play Console specifically asks for a zip that also includes the
  signing certificate.
