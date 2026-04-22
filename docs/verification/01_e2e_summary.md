# E2E Verification — Outbox → Kafka → Ledger → Verify

## Goal

Verify that a fake Product/Workflow event is stored in `outbox_event`,
published to Kafka, consumed by the ledger pipeline, appended to `ledger_entry`,
and verified by the Verify API.

## Result

- Spring Boot health check: `UP`
- MINT event created an `outbox_event` with `PENDING`
- Outbox Publisher changed it to `PUBLISHED`
- Ledger entry `seq=1` was appended with `eventAction=MINT`
- Verify API returned `valid=true`, `totalEntries=1`
- TRANSFER event created another `outbox_event`
- Outbox Publisher changed it to `PUBLISHED`
- Ledger entry `seq=2` was appended with `eventAction=TRANSFER_COMPLETED`
- `seq=2.prevHash` matched `seq=1.entryHash`
- Verify API returned `valid=true`, `totalEntries=2`

## Interview Explanation

This proves the minimum Ledger/Event Reliability Pipeline:

Product/Workflow change
→ outbox_event PENDING
→ Outbox Publisher
→ Kafka ledger.outbox.v1
→ Ledger Consumer
→ ledger_entry append
→ SHA-256 hash chain
→ Verify API valid=true

This repository is a reproduction lab created after the original team project
to verify and explain the Ledger/Event Reliability Pipeline.
