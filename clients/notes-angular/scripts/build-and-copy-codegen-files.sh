#!/bin/bash

NOTES_SERVICE=notes-notes_service-0.0.0+auto.tgz


cd ../../../notes-app/target/generated-sources/typescript/
npm i
npm run build
npm pack
cp "${NOTES_SERVICE}" ../../../../clients/notes-angular/"${NOTES_SERVICE}"
cd ../../../../clients/notes-angular/
npm install "${NOTES_SERVICE}"
