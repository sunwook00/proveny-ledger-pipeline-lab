# Technical Notes

## 1. Transactional Outbox

DB 저장과 Kafka 발행은 서로 다른 시스템에서 일어납니다.

비즈니스 데이터는 저장됐는데 Kafka 발행 전에 서버가 죽으면 원장 기록 이벤트가 유실될 수 있습니다.

이를 줄이기 위해 비즈니스 이벤트를 먼저 `outbox_event`에 저장하고, 별도 Publisher가 Kafka로 발행합니다.

## 2. Outbox Status

```text
PENDING → PROCESSING → PUBLISHED
                    ↘ FAILED
PROCESSING timeout → PENDING
```

- `PENDING`: 아직 발행되지 않은 이벤트
- `PROCESSING`: Publisher가 claim해서 처리 중인 이벤트
- `PUBLISHED`: Kafka 발행 성공
- `FAILED`: 재시도 초과 또는 복구 불가

## 3. SKIP LOCKED

여러 Publisher가 동시에 같은 PENDING row를 claim하면 중복 발행이 생길 수 있습니다.

`FOR UPDATE SKIP LOCKED`를 사용하면 이미 다른 트랜잭션이 lock한 row를 기다리지 않고 건너뜁니다.

## 4. Consumer Idempotency

Kafka는 같은 메시지를 다시 전달할 수 있습니다.

따라서 Consumer는 같은 이벤트가 두 번 들어와도 결과가 한 번만 반영되도록 만들어야 합니다.

이 저장소에서는 `ledger_entry.idempotency_key` unique constraint로 중복 반영을 막습니다.

## 5. Hash Chain

```text
data_hash = SHA256(payload_canonical)

entry_hash = SHA256(
  prev_hash
  + data_hash
  + seq
  + event_category
  + event_action
  + actor_role
  + actor_id
  + occurred_at
)
```

각 entry는 이전 entry의 hash를 `prev_hash`로 들고 있습니다.

중간 payload가 바뀌면 Verify API에서 체인 불일치를 감지할 수 있습니다.

## 6. Verify API

Verify API는 다음 항목을 순서대로 검증합니다.

1. seq 연속성
2. payload canonicalization
3. data_hash 재계산
4. prev_hash 연결
5. entry_hash 재계산

하나라도 불일치하면 `valid=false`를 반환합니다.

## 7. Recovery

Publisher가 이벤트를 PROCESSING으로 바꾼 뒤 Kafka 발행 전에 죽으면 해당 row가 stuck될 수 있습니다.

Recovery Scheduler는 timeout을 넘긴 PROCESSING row를 다시 PENDING으로 복구합니다.

재발행으로 인한 중복 가능성은 Consumer idempotency로 방어합니다.

## 8. Metrics

| 지표 | 의미 |
|---|---|
| pending.size | 아직 발행되지 않은 이벤트 수 |
| oldest.age | 가장 오래 기다린 PENDING 이벤트의 대기 시간 |
| processing.count | 처리 중인 이벤트 수 |
| failed.count | 실패 상태 이벤트 수 |

pending.size와 oldest.age가 먼저 올라가면 Kafka Consumer보다 Outbox Publisher 병목을 먼저 의심합니다.
