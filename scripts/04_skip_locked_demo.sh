#!/usr/bin/env bash
set -euo pipefail
BASE=${BASE:-http://localhost:8080}
curl -s -X POST "$BASE/test/skip-locked?events=100&publishers=2&batchSize=60" | python3 -m json.tool
