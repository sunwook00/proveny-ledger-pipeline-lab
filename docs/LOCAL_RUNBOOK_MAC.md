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
docker exec proveny-lab-postgres psql -U proveny -d proveny_lab -c "select now();"
```

## 4. Check Kafka Topics

```bash
docker exec proveny-lab-kafka /opt/kafka/bin/kafka-topics.sh \
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
