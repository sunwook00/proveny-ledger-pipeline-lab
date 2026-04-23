# Fresh Clone Smoke Test

## Goal

Verify that the repository works from a fresh clone environment, not only from the original local workspace.

## Fresh Clone Environment

- Path: `~/Desktop/fresh clone/proveny-ledger-pipeline-lab-smoke`
- Infra ports:
  - PostgreSQL: `55432`
  - Kafka: `19092`
- App port:
  - Spring Boot: `18080`

## Steps

### 1. Clone and run infrastructure

```bash
git clone https://github.com/sunwook00/proveny-ledger-pipeline-lab.git proveny-ledger-pipeline-lab-smoke
cd proveny-ledger-pipeline-lab-smoke

POSTGRES_PORT=55432 KAFKA_PORT=19092 docker compose -f compose.yaml up -d
./gradlew test

2. Run application
DB_URL=jdbc:postgresql://localhost:55432/proveny_lab \
KAFKA_BOOTSTRAP_SERVERS=localhost:19092 \
SERVER_PORT=18080 \
./gradlew bootRun

3. Health check
curl http://localhost:18080/actuator/health

4. Verification scripts
BASE=http://localhost:18080 ./scripts/03_demo_e2e.sh
BASE=http://localhost:18080 ./scripts/04_skip_locked_demo.sh
BASE=http://localhost:18080 ./scripts/05_idempotency_demo.sh
BASE=http://localhost:18080 ./scripts/06_tamper_verify_false_demo.sh
