#!/usr/bin/env bash
set -euo pipefail
docker version >/dev/null
docker compose up -d
until docker exec proveny-lab-postgres pg_isready -U proveny -d proveny_lab >/dev/null 2>&1; do sleep 2; echo "waiting postgres..."; done
until docker exec proveny-lab-kafka kafka-topics.sh --bootstrap-server localhost:9092 --list >/dev/null 2>&1; do sleep 3; echo "waiting kafka..."; done
docker exec proveny-lab-kafka kafka-topics.sh --bootstrap-server localhost:9092 --create --if-not-exists --topic ledger.outbox.v1 --partitions 3 --replication-factor 1 >/dev/null
docker exec proveny-lab-kafka kafka-topics.sh --bootstrap-server localhost:9092 --create --if-not-exists --topic ledger.outbox.v1.dlq --partitions 1 --replication-factor 1 >/dev/null
docker compose ps
