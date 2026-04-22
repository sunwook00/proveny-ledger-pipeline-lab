package com.example.provenylab.ledger;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
public class LedgerController {
  private final LedgerQueryService queryService;
  private final LedgerVerificationService verificationService;
  private final JdbcTemplate jdbc;

  public LedgerController(
      LedgerQueryService queryService,
      LedgerVerificationService verificationService,
      JdbcTemplate jdbc) {
    this.queryService = queryService;
    this.verificationService = verificationService;
    this.jdbc = jdbc;
  }

  @GetMapping("/ledgers/passports/{passportId}/entries")
  public List<LedgerEntryRow> entries(@PathVariable String passportId) {
    return queryService.findByPassportId(passportId);
  }

  @GetMapping("/ledgers/passports/{passportId}/verify")
  public VerifyResult verify(@PathVariable String passportId) {
    return verificationService.verify(passportId);
  }

  @PostMapping("/test/tamper/{passportId}/{seq}")
  public Map<String, Object> tamper(@PathVariable String passportId, @PathVariable long seq) {
    int updated =
        jdbc.update(
            "UPDATE ledger_entry SET payload_json=jsonb_set(payload_json, '{tampered}', 'true'::jsonb, true) WHERE passport_id=? AND seq=?",
            passportId,
            seq);
    return Map.of(
        "updated",
        updated,
        "message",
        "Run verify. It should become false unless append-only trigger is already installed.");
  }

  @PostMapping("/test/install-append-only-trigger")
  public Map<String, Object> installAppendOnlyTrigger() {
    jdbc.execute(
        "CREATE OR REPLACE FUNCTION prevent_ledger_entry_mutation() RETURNS TRIGGER AS $$ BEGIN RAISE EXCEPTION 'ledger_entry is immutable: update/delete not allowed'; END; $$ LANGUAGE plpgsql");
    jdbc.execute("DROP TRIGGER IF EXISTS trg_ledger_entry_no_update ON ledger_entry");
    jdbc.execute("DROP TRIGGER IF EXISTS trg_ledger_entry_no_delete ON ledger_entry");
    jdbc.execute(
        "CREATE TRIGGER trg_ledger_entry_no_update BEFORE UPDATE ON ledger_entry FOR EACH ROW EXECUTE FUNCTION prevent_ledger_entry_mutation()");
    jdbc.execute(
        "CREATE TRIGGER trg_ledger_entry_no_delete BEFORE DELETE ON ledger_entry FOR EACH ROW EXECUTE FUNCTION prevent_ledger_entry_mutation()");
    return Map.of("installed", true);
  }
}
