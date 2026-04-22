package com.example.provenylab.ledger;

import java.time.Instant;
import java.util.Map;

public record LedgerEvent(
    String passportId,
    String eventCategory,
    String eventAction,
    String actorRole,
    String actorId,
    Instant occurredAt,
    Map<String, Object> payload,
    String idempotencyKey,
    int schemaVersion) {}
