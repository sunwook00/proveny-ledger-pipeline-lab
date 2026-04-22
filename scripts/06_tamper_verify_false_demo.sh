#!/usr/bin/env bash
set -euo pipefail
BASE=${BASE:-http://localhost:8080}
json_get(){ python3 -c "import json,sys; print(json.load(sys.stdin)['$1'])"; }
MINT=$(curl -s -X POST "$BASE/fake/products/mint" -H 'Content-Type: application/json' -d "{\"serialNumber\":\"TAMPER-$(date +%s)\"}")
PASSPORT=$(echo "$MINT" | json_get passportId)
curl -s -X POST "$BASE/outbox/publish-once?batchSize=10" >/dev/null
sleep 3
echo "Before tamper"; curl -s "$BASE/ledgers/passports/$PASSPORT/verify" | python3 -m json.tool
echo "Tamper"; curl -s -X POST "$BASE/test/tamper/$PASSPORT/1" | python3 -m json.tool
echo "After tamper"; curl -s "$BASE/ledgers/passports/$PASSPORT/verify" | python3 -m json.tool
