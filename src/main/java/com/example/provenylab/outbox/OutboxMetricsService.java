package com.example.provenylab.outbox;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class OutboxMetricsService {
  private final JdbcTemplate jdbc;

  public OutboxMetricsService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Map<String, Object> metrics() {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("pending.size", n("SELECT count(*) FROM outbox_event WHERE status='PENDING'"));
    m.put("processing.count", n("SELECT count(*) FROM outbox_event WHERE status='PROCESSING'"));
    m.put("published.count", n("SELECT count(*) FROM outbox_event WHERE status='PUBLISHED'"));
    m.put("failed.count", n("SELECT count(*) FROM outbox_event WHERE status='FAILED'"));
    Double oldest =
        jdbc.queryForObject(
            "SELECT COALESCE(EXTRACT(EPOCH FROM (now() - min(created_at))),0) FROM outbox_event WHERE status='PENDING'",
            Double.class);
    m.put("oldest.age.seconds", oldest == null ? 0 : oldest);
    m.put(
        "status.distribution",
        jdbc.queryForList(
            "SELECT status,count(*) AS count FROM outbox_event GROUP BY status ORDER BY status"));
    return m;
  }

  private Number n(String sql) {
    Number n = jdbc.queryForObject(sql, Number.class);
    return n == null ? 0 : n;
  }
}
