package com.example.provenylab.outbox;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxClaimService {
  private final JdbcTemplate jdbc;

  public OutboxClaimService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Transactional
  public List<OutboxRow> claim(String publisherId, int batchSize) {
    return jdbc.query(
        """
      WITH claimed AS (SELECT id FROM outbox_event WHERE status='PENDING' AND (next_retry_at IS NULL OR next_retry_at <= now()) ORDER BY id LIMIT ? FOR UPDATE SKIP LOCKED)
      UPDATE outbox_event e SET status='PROCESSING', processing_owner=?, processing_started_at=now()
      FROM claimed WHERE e.id=claimed.id
      RETURNING e.id,e.aggregate_id,e.aggregate_type,e.event_type,e.payload::text AS payload_json,e.idempotency_key,e.created_at
      """,
        (rs, i) ->
            new OutboxRow(
                rs.getLong("id"),
                rs.getString("aggregate_id"),
                rs.getString("aggregate_type"),
                rs.getString("event_type"),
                rs.getString("payload_json"),
                rs.getString("idempotency_key"),
                rs.getTimestamp("created_at").toInstant()),
        batchSize,
        publisherId);
  }
}
