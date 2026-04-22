package com.example.provenylab.outbox;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OutboxPublisher {
  private final OutboxClaimService claimService;
  private final JdbcTemplate jdbc;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final String topic;
  private final int maxRetries;

  public OutboxPublisher(
      OutboxClaimService claimService,
      JdbcTemplate jdbc,
      KafkaTemplate<String, String> kafkaTemplate,
      @Value("${outbox.topic:ledger.outbox.v1}") String topic,
      @Value("${outbox.max-retries:5}") int maxRetries) {
    this.claimService = claimService;
    this.jdbc = jdbc;
    this.kafkaTemplate = kafkaTemplate;
    this.topic = topic;
    this.maxRetries = maxRetries;
  }

  public List<Map<String, Object>> publishOnce(String publisherId, int batchSize) {
    List<OutboxRow> rows = claimService.claim(publisherId, batchSize);
    List<Map<String, Object>> results = new ArrayList<>();
    for (OutboxRow row : rows) {
      long start = System.nanoTime();
      try {
        kafkaTemplate.send(topic, row.aggregateId(), row.payloadJson()).get(10, TimeUnit.SECONDS);
        markPublished(row.id());
        long millis = Duration.ofNanos(System.nanoTime() - start).toMillis();
        results.add(Map.of("id", row.id(), "status", "PUBLISHED", "publishDurationMs", millis));
      } catch (Exception e) {
        markFailedOrRetry(row.id(), e);
        results.add(
            Map.of(
                "id",
                row.id(),
                "status",
                "RETRY_OR_FAILED",
                "error",
                e.getClass().getSimpleName()));
      }
    }
    return results;
  }

  private void markPublished(long id) {
    jdbc.update(
        "UPDATE outbox_event SET status='PUBLISHED', published_at=now(), last_error=NULL WHERE id=?",
        id);
  }

  private void markFailedOrRetry(long id, Exception e) {
    jdbc.update(
        "UPDATE outbox_event SET status=CASE WHEN retry_count+1>=? THEN 'FAILED' ELSE 'PENDING' END, retry_count=retry_count+1, next_retry_at=now()+interval '5 seconds', processing_owner=NULL, processing_started_at=NULL, last_error=? WHERE id=?",
        maxRetries,
        e.getMessage(),
        id);
  }
}
