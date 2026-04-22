#!/usr/bin/env bash
set -euo pipefail
BASE=${BASE:-http://localhost:8080}
curl -s -X POST "$BASE/fake/products/mint" -H 'Content-Type: application/json' -d "{\"serialNumber\":\"RECOVERY-$(date +%s)\"}" | python3 -m json.tool
curl -s -X POST "$BASE/test/make-stuck-processing" | python3 -m json.tool
curl -s "$BASE/outbox/metrics" | python3 -m json.tool
curl -s -X POST "$BASE/outbox/recover" | python3 -m json.tool
curl -s "$BASE/outbox/metrics" | python3 -m json.tool
