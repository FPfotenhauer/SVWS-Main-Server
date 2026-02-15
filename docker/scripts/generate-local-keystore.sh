#!/usr/bin/env sh
set -e

CERT_DIR="$(dirname "$0")/../certs"
KEYSTORE="$CERT_DIR/localhost.p12"
STORE_PASS="svwskeystore"

mkdir -p "$CERT_DIR"

keytool -genkeypair \
  -alias localhost \
  -keyalg RSA \
  -keysize 2048 \
  -validity 365 \
  -storetype PKCS12 \
  -keystore "$KEYSTORE" \
  -storepass "$STORE_PASS" \
  -keypass "$STORE_PASS" \
  -dname "CN=localhost, OU=local, O=local, L=local, S=local, C=DE"

openssl pkcs12 -in "$KEYSTORE" -clcerts -nokeys \
  -out "$CERT_DIR/server.crt" \
  -passin pass:"$STORE_PASS"

openssl pkcs12 -in "$KEYSTORE" -nocerts -nodes \
  -out "$CERT_DIR/server.key" \
  -passin pass:"$STORE_PASS"

chmod 600 "$CERT_DIR/server.key"

echo "Keystore written to $KEYSTORE"
echo "PEM files written to $CERT_DIR/server.crt and $CERT_DIR/server.key"
