# Local Runbook for Mac

## 1. Required Tools

```bash
brew --version
git --version
java -version
docker --version
gh --version
```

## 2. Start Infrastructure

```bash
docker compose -f compose.yaml up -d
docker compose -f compose.yaml ps
```

## 3. Check PostgreSQL

```bash
docker compose -f compose.yaml exec -T postgres psql -U proveny -d proveny_lab -c "select now();"
```

## 4. Check Kafka Topics

```bash
docker compose -f compose.yaml exec -T kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list
```

## 5. Run Tests

```bash
./gradlew test
```

## 6. Run Application

```bash
./gradlew bootRun
```

## 7. Health Check

```bash
curl http://localhost:8080/actuator/health
```

Expected result:

```json
{"status":"UP"}
```

## 8. Verification Scripts

```bash
./scripts/03_demo_e2e.sh
./scripts/04_skip_locked_demo.sh
./scripts/05_idempotency_demo.sh
./scripts/06_tamper_verify_false_demo.sh
./scripts/07_recovery_demo.sh
./scripts/08_metrics_demo.sh
```
POSTGRES_PORT=55432 KAFKA_PORT=19092 docker compose -f compose.yaml up -d
DB_URL=jdbc:postgresql://localhost:55432/proveny_lab \
KAFKA_BOOTSTRAP_SERVERS=localhost:19092 \
SERVER_PORT=18080 \
./gradlew bootRun
curl http://localhost:18080/actuator/health
BASE=http://localhost:18080 ./scripts/03_demo_e2e.sh
BASE=http://localhost:18080 ./scripts/04_skip_locked_demo.sh
BASE=http://localhost:18080 ./scripts/05_idempotency_demo.sh
BASE=http://localhost:18080 ./scripts/06_tamper_verify_false_demo.sh
BASE=http://localhost:18080 ./scripts/07_recovery_demo.sh
BASE=http://localhost:18080 ./scripts/08_metrics_demo.sh
