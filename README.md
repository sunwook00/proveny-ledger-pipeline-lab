# Proveny Ledger Pipeline Lab

Proveny 팀 프로젝트에서 다룬 Ledger/Event Reliability Pipeline을 별도로 재현한 실습 저장소입니다.

원본 프로젝트 전체를 복제한 것이 아니라, 원장 기록 신뢰성과 관련된 핵심 흐름만 최소 범위로 구현했습니다.

## 재현 범위

이 저장소에서 다루는 범위는 다음과 같습니다.

- Transactional Outbox
- PENDING → PROCESSING → PUBLISHED / FAILED 상태 흐름
- Kafka 발행 및 Consumer 처리
- idempotency_key 기반 중복 반영 방지
- SHA-256 hash chain
- Verify API
- FOR UPDATE SKIP LOCKED 기반 claim
- PROCESSING stuck 복구
- Outbox 상태 지표 확인

## 전체 흐름

```text
Fake Product/Workflow Event
→ outbox_event PENDING
→ Outbox Publisher
→ Kafka ledger.outbox.v1
→ Ledger Consumer
→ ledger_entry append
→ SHA-256 hash chain
→ Verify API
```

## 실행 환경

- MacBook M4
- Java 21
- Spring Boot
- PostgreSQL 16
- Apache Kafka 3.7.2
- Docker Desktop
- Gradle Wrapper

## 빠른 실행

```bash
docker compose -f compose.yaml up -d

./gradlew test

./gradlew bootRun
```

다른 터미널에서 확인합니다.

```bash
curl http://localhost:8080/actuator/health
```

정상 응답은 다음과 같습니다.

```json
{"status":"UP"}
```

## 검증 스크립트

```bash
./scripts/03_demo_e2e.sh
./scripts/04_skip_locked_demo.sh
./scripts/05_idempotency_demo.sh
./scripts/06_tamper_verify_false_demo.sh
./scripts/07_recovery_demo.sh
./scripts/08_metrics_demo.sh
```

## 검증 결과

| 항목 | 결과 |
|---|---|
| E2E Outbox → Kafka → Ledger → Verify | 완료 |
| SKIP LOCKED 중복 claim 방지 | 완료 |
| Consumer idempotency | 완료 |
| payload 변조 감지 | 완료 |
| PROCESSING stuck 복구 | 완료 |
| Outbox 상태 지표 | 완료 |
| Fresh clone smoke test | 완료 |
검증 로그는 `docs/verification` 디렉토리에 남깁니다.

## 핵심 확인 파일

| 파일 | 설명 |
|---|---|
| `src/main/resources/schema.sql` | outbox_event, ledger_chain, ledger_entry 스키마 |
| `src/main/java/com/example/provenylab/outbox/OutboxClaimService.java` | SKIP LOCKED claim |
| `src/main/java/com/example/provenylab/outbox/OutboxPublisher.java` | Outbox 발행 흐름 |
| `src/main/java/com/example/provenylab/kafka/LedgerKafkaConsumer.java` | Kafka Consumer |
| `src/main/java/com/example/provenylab/ledger/LedgerAppendService.java` | ledger_entry append |
| `src/main/java/com/example/provenylab/ledger/LedgerHashService.java` | SHA-256 hash 계산 |
| `src/main/java/com/example/provenylab/ledger/LedgerVerificationService.java` | Verify API 검증 로직 |
| `src/main/java/com/example/provenylab/outbox/OutboxRecoveryScheduler.java` | stuck PROCESSING 복구 |
| `docs/verification` | 실행 결과 로그 |

## 설계 메모

### Transactional Outbox

비즈니스 데이터 저장과 Kafka 발행은 서로 다른 시스템이므로 하나의 트랜잭션으로 묶기 어렵습니다.

이 저장소에서는 비즈니스 이벤트를 먼저 `outbox_event`에 저장하고, Publisher가 이후 Kafka로 발행합니다.

### SKIP LOCKED

여러 Publisher가 동시에 실행될 때 같은 PENDING row를 중복 claim하지 않도록 `FOR UPDATE SKIP LOCKED`를 사용합니다.

### Idempotency

Kafka는 같은 메시지를 다시 전달할 수 있으므로 Consumer는 멱등해야 합니다.

`ledger_entry.idempotency_key`에 unique constraint를 두어 같은 이벤트가 두 번 들어와도 원장에는 한 번만 기록되도록 합니다.

### Hash Chain

각 ledger entry는 이전 entry의 hash를 `prev_hash`로 가지고, 현재 payload와 metadata로 `entry_hash`를 계산합니다.

중간 payload가 바뀌면 Verify API에서 `valid=false`가 됩니다.

### Recovery

Publisher가 PENDING row를 PROCESSING으로 바꾼 뒤 죽으면 stuck row가 생길 수 있습니다.

Recovery Scheduler는 timeout을 넘긴 PROCESSING row를 다시 PENDING으로 복구합니다.

## 원본 팀 프로젝트와의 관계

이 저장소는 원본 Proveny 팀 프로젝트 전체 저장소가 아닙니다.

원본 프로젝트에는 Auth/RBAC, Product/Workflow Core, GitOps/EKS 등 더 넓은 범위가 함께 포함되어 있었습니다.

이 저장소는 그중 Ledger/Event Reliability Pipeline을 이해하고 검증하기 위해 만든 별도 재현 실습입니다.
