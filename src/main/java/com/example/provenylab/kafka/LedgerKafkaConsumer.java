package com.example.provenylab.kafka;

import com.example.provenylab.ledger.*;
import org.slf4j.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class LedgerKafkaConsumer {
  private static final Logger log = LoggerFactory.getLogger(LedgerKafkaConsumer.class);
  private final LedgerAppendService appendService;

  public LedgerKafkaConsumer(LedgerAppendService appendService) {
    this.appendService = appendService;
  }

  @KafkaListener(
      topics = "${outbox.topic:ledger.outbox.v1}",
      groupId = "${spring.kafka.consumer.group-id}")
  public void consume(String message) {
    AppendResult result = appendService.append(appendService.fromOutboxPayload(message));
    log.info("ledger event consumed: {}", result);
  }
}
