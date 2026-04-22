package com.example.provenylab.ledger;
public record AppendResult(String result, String ledgerId, Long seq, String idempotencyKey) {
  public static AppendResult appended(String ledgerId, Long seq, String key){ return new AppendResult("APPENDED", ledgerId, seq, key); }
  public static AppendResult duplicateIgnored(String key){ return new AppendResult("DUPLICATE_IGNORED", null, null, key); }
}
