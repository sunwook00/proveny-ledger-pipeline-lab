#!/usr/bin/env bash
set -euo pipefail
BASE=${BASE:-http://localhost:8080}
curl -s -X POST "$BASE/test/idempotency" | python3 -m json.tool
