package com.example.provenylab.ledger;

import com.example.provenylab.common.JsonCanonicalizer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class LedgerVerificationService {
  private final LedgerQueryService queryService;
  private final JsonCanonicalizer canonicalizer;
  private final LedgerHashService hashService;

  public LedgerVerificationService(
      LedgerQueryService queryService,
      JsonCanonicalizer canonicalizer,
      LedgerHashService hashService) {
    this.queryService = queryService;
    this.canonicalizer = canonicalizer;
    this.hashService = hashService;
  }

  public VerifyResult verify(String passportId) {
    List<LedgerEntryRow> entries = queryService.findByPassportId(passportId);
    String prev = null;
    long expectedSeq = 1;
    for (LedgerEntryRow e : entries) {
      if (e.seq() != expectedSeq)
        return invalid(passportId, e.seq(), "seq gap: expected " + expectedSeq, entries.size());
      String canonical = canonicalizer.canonicalize(canonicalizer.parseJson(e.payloadJson()));
      String dataHash = hashService.dataHash(canonical);
      if (!dataHash.equals(e.dataHash()))
        return invalid(passportId, e.seq(), "data_hash mismatch", entries.size());
      if (!Objects.equals(prev, e.prevHash()))
        return invalid(passportId, e.seq(), "prev_hash mismatch", entries.size());
      LedgerEvent event =
          new LedgerEvent(
              e.passportId(),
              e.eventCategory(),
              e.eventAction(),
              e.actorRole(),
              e.actorId(),
              e.occurredAt(),
              Map.of(),
              e.idempotencyKey(),
              1);
      String recalculated = hashService.entryHash(e.prevHash(), e.dataHash(), e.seq(), event);
      if (!recalculated.equals(e.entryHash()))
        return invalid(passportId, e.seq(), "entry_hash mismatch", entries.size());
      prev = e.entryHash();
      expectedSeq++;
    }
    return new VerifyResult(passportId, true, null, null, entries.size());
  }

  private VerifyResult invalid(String passportId, long seq, String reason, int total) {
    return new VerifyResult(passportId, false, seq, reason, total);
  }
}
