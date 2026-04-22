#!/usr/bin/env bash
set -euo pipefail
BASE=${BASE:-http://localhost:8080}
curl -s "$BASE/outbox/metrics" | python3 -m json.tool
