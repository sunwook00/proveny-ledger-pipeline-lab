package com.example.provenylab.ledger;
import java.time.Instant;
public record LedgerEntryRow(String ledgerId,String passportId,long seq,String eventCategory,String eventAction,String actorRole,String actorId,Instant occurredAt,String payloadJson,String payloadCanonical,String dataHash,String prevHash,String entryHash,String idempotencyKey) {}
