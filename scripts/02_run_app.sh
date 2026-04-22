#!/usr/bin/env bash
set -euo pipefail
if [ ! -f ./gradlew ]; then gradle wrapper --gradle-version 8.10.2; fi
./gradlew bootRun
