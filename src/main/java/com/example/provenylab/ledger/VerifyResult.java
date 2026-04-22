package com.example.provenylab.ledger;

public record VerifyResult(
    String passportId, boolean valid, Long invalidAtSeq, String reason, int totalEntries) {}
