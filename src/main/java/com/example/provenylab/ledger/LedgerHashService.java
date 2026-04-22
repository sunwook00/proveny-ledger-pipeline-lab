package com.example.provenylab.ledger;

import com.example.provenylab.common.HashUtil;
import org.springframework.stereotype.Component;

@Component
public class LedgerHashService {
  public String dataHash(String canonicalPayload) {
    return HashUtil.sha256(canonicalPayload);
  }

  public String entryHash(String prevHash, String dataHash, long seq, LedgerEvent event) {
    return HashUtil.sha256(
        (prevHash == null ? "" : prevHash)
            + dataHash
            + seq
            + event.eventCategory()
            + event.eventAction()
            + event.actorRole()
            + event.actorId()
            + event.occurredAt().toString());
  }
}
