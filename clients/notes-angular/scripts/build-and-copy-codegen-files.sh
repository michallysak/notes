#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../../../.." && pwd)"
GEN_TS_DIR="$ROOT_DIR/notes/notes-app/target/generated-sources/typescript"
CLIENT_DIR="$ROOT_DIR/notes/clients/notes-angular"
NOTES_SERVICE=notes-notes_service-0.0.0+auto.tgz

cd "$GEN_TS_DIR"
npm i
npm run build
cd dist
npm pack
cp "$NOTES_SERVICE" "$CLIENT_DIR/$NOTES_SERVICE"
cd "$CLIENT_DIR"
npm install "$NOTES_SERVICE"
