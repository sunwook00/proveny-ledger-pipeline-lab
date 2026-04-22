package com.example.provenylab.ledger;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LedgerQueryService {
  private final JdbcTemplate jdbc;

  public LedgerQueryService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<LedgerEntryRow> findByPassportId(String passportId) {
    return jdbc.query(
        """
      SELECT ledger_id,passport_id,seq,event_category,event_action,actor_role,actor_id,occurred_at,payload_json::text AS payload_json,payload_canonical,data_hash,prev_hash,entry_hash,idempotency_key
      FROM ledger_entry WHERE passport_id=? ORDER BY seq
      """,
        (rs, i) ->
            new LedgerEntryRow(
                rs.getString("ledger_id"),
                rs.getString("passport_id"),
                rs.getLong("seq"),
                rs.getString("event_category"),
                rs.getString("event_action"),
                rs.getString("actor_role"),
                rs.getString("actor_id"),
                ((Timestamp) rs.getTimestamp("occurred_at")).toInstant(),
                rs.getString("payload_json"),
                rs.getString("payload_canonical"),
                rs.getString("data_hash"),
                rs.getString("prev_hash"),
                rs.getString("entry_hash"),
                rs.getString("idempotency_key")),
        passportId);
  }
}
