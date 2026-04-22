package com.example.provenylab.outbox;
import org.springframework.beans.factory.annotation.Value; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Component; import java.util.List;
@Component
public class OutboxRecoveryScheduler {
  private final JdbcTemplate jdbc; private final int timeoutSeconds;
  public OutboxRecoveryScheduler(JdbcTemplate jdbc,@Value("${outbox.recovery-timeout-seconds:180}") int timeoutSeconds){this.jdbc=jdbc;this.timeoutSeconds=timeoutSeconds;}
  @Scheduled(fixedDelayString="${outbox.recovery-interval-ms:30000}") public void scheduledRecover(){recover();}
  public List<Long> recover(){return jdbc.queryForList("UPDATE outbox_event SET status='PENDING', processing_owner=NULL, processing_started_at=NULL, retry_count=retry_count+1, next_retry_at=now() WHERE status='PROCESSING' AND processing_started_at < now() - (? * interval '1 second') RETURNING id",Long.class,timeoutSeconds);}
}
