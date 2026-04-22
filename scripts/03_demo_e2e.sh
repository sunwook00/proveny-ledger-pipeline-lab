#!/usr/bin/env bash
set -euo pipefail
BASE=${BASE:-http://localhost:8080}
json_get() { python3 -c "import json,sys; print(json.load(sys.stdin)['$1'])"; }
SERIAL="SN-$(date +%s)"
MINT=$(curl -s -X POST "$BASE/fake/products/mint" -H 'Content-Type: application/json' -d "{\"serialNumber\":\"$SERIAL\"}")
echo "$MINT" | python3 -m json.tool
PASSPORT=$(echo "$MINT" | json_get passportId)
curl -s -X POST "$BASE/outbox/publish-once?publisherId=pub-A&batchSize=10" | python3 -m json.tool
sleep 3
curl -s "$BASE/ledgers/passports/$PASSPORT/verify" | python3 -m json.tool
curl -s -X POST "$BASE/fake/passports/$PASSPORT/transfer" -H 'Content-Type: application/json' -d '{"toOwnerId":"user-2"}' | python3 -m json.tool
curl -s -X POST "$BASE/outbox/publish-once?publisherId=pub-A&batchSize=10" | python3 -m json.tool
sleep 3
curl -s "$BASE/ledgers/passports/$PASSPORT/entries" | python3 -m json.tool
curl -s "$BASE/ledgers/passports/$PASSPORT/verify" | python3 -m json.tool
