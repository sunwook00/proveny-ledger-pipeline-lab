# Proveny Ledger Pipeline 검증 로그

이 저장소는 Proveny 팀 프로젝트에서 다룬 Ledger/Event Reliability Pipeline을 별도로 재현한 실습 저장소입니다.

## 실행 환경

- MacBook M4
- Java 21
- Docker Desktop
- PostgreSQL 16
- Apache Kafka 3.7.2
- Spring Boot
- Gradle Wrapper

## 검증 완료 체크리스트

- [x] ./gradlew test 성공
- [x] Spring Boot /actuator/health UP
- [x] E2E Outbox -> Kafka -> Ledger -> Verify valid=true
- [x] SKIP LOCKED duplicate claim prevention
- [x] Consumer idempotency
- [x] Tamper detection valid=false
- [x] Recovery Scheduler stuck PROCESSING recovery
- [x] Outbox metrics

## 검증 로그

| 항목 | 파일 |
|---|---|
| E2E Outbox-Kafka-Ledger-Verify | `docs/verification/01_e2e_outbox_kafka_ledger_verify.txt` |
| SKIP LOCKED 중복 claim 방지 | `docs/verification/02_skip_locked_duplicate_claim.txt` |
| Consumer 멱등 처리 | `docs/verification/03_consumer_idempotency.txt` |
| payload 변조 감지 | `docs/verification/04_tamper_verify_false.txt` |
| PROCESSING stuck 복구 | `docs/verification/05_recovery_stuck_processing.txt` |
| Outbox 상태 지표 | `docs/verification/06_outbox_metrics.txt` |

## 요약

이 repo에서는 Product/Workflow 전체를 구현하지 않고, 원장 기록 신뢰성과 관련된 최소 흐름만 재현했습니다.

핵심은 다음 네 가지입니다.

1. 이벤트 유실 방지: Transactional Outbox
2. 중복 방지: idempotency_key
3. 위변조 감지: SHA-256 hash chain + Verify API
4. 장애 복구: PROCESSING timeout recovery
