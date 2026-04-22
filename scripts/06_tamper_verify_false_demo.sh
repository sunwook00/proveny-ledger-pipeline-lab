#!/usr/bin/env bash
set -euo pipefail

BASE=${BASE:-http://localhost:8080}

json_get() {
  python3 -c "import json,sys; print(json.load(sys.stdin)['$1'])"
}

json_total_entries() {
  python3 -c "import json,sys; print(json.load(sys.stdin).get('totalEntries', 0))"
}

json_valid() {
  python3 -c "import json,sys; print(str(json.load(sys.stdin).get('valid')).lower())"
}

echo "==> Create fake MINT event"
SERIAL="TAMPER-$(date +%s)-$RANDOM"
MINT=$(curl -s -X POST "$BASE/fake/products/mint" \
  -H 'Content-Type: application/json' \
  -d "{\"serialNumber\":\"$SERIAL\"}")

echo "$MINT" | python3 -m json.tool

PASSPORT=$(echo "$MINT" | json_get passportId)
echo "passportId=$PASSPORT"

echo
echo "==> Publish outbox events until this passport has at least one ledger entry"

TOTAL=0

for i in $(seq 1 10); do
  echo "publish attempt=$i"

  curl -s -X POST "$BASE/outbox/publish-once?publisherId=tamper-demo&batchSize=200" |
    python3 -m json.tool || true

  sleep 2

  VERIFY=$(curl -s "$BASE/ledgers/passports/$PASSPORT/verify")
  TOTAL=$(echo "$VERIFY" | json_total_entries)

  echo "current totalEntries=$TOTAL"

  if [ "$TOTAL" -ge 1 ]; then
    break
  fi
done

if [ "$TOTAL" -lt 1 ]; then
  echo
  echo "ERROR: ledger_entry was not appended for passportId=$PASSPORT"
  echo "Outbox metrics:"
  curl -s "$BASE/outbox/metrics" | python3 -m json.tool || true

  echo
  echo "Hint: check app logs, Kafka topic, and outbox status."
  exit 1
fi

echo
echo "==> Before tamper"
BEFORE=$(curl -s "$BASE/ledgers/passports/$PASSPORT/verify")
echo "$BEFORE" | python3 -m json.tool

BEFORE_VALID=$(echo "$BEFORE" | json_valid)

if [ "$BEFORE_VALID" != "true" ]; then
  echo "ERROR: expected verify valid=true before tamper"
  exit 1
fi

echo
echo "==> Tamper seq=1 payload"
TAMPER=$(curl -s -X POST "$BASE/test/tamper/$PASSPORT/1")
echo "$TAMPER" | python3 -m json.tool

UPDATED=$(echo "$TAMPER" | python3 -c "import json,sys; print(json.load(sys.stdin).get('updated', 0))")

if [ "$UPDATED" -lt 1 ]; then
  echo "ERROR: tamper update did not modify any ledger_entry row"
  exit 1
fi

echo
echo "==> After tamper"
AFTER=$(curl -s "$BASE/ledgers/passports/$PASSPORT/verify")
echo "$AFTER" | python3 -m json.tool

AFTER_VALID=$(echo "$AFTER" | json_valid)

if [ "$AFTER_VALID" != "false" ]; then
  echo "ERROR: expected verify valid=false after tamper"
  exit 1
fi

echo
echo "SUCCESS: tampered payload was detected by Verify API."
