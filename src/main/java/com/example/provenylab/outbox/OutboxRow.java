package com.example.provenylab.outbox;

import java.time.Instant;

public record OutboxRow(
    long id,
    String aggregateId,
    String aggregateType,
    String eventType,
    String payloadJson,
    String idempotencyKey,
    Instant createdAt) {}
