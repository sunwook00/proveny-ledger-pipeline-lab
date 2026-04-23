#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

echo "==> Starting PostgreSQL and Kafka"
docker compose -f compose.yaml up -d

echo "==> Waiting for PostgreSQL"
until docker compose -f compose.yaml exec -T postgres pg_isready -U proveny -d proveny_lab >/dev/null 2>&1; do
  echo "waiting postgres..."
  sleep 2
done

echo "==> Waiting for Kafka"
until docker compose -f compose.yaml exec -T kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list >/dev/null 2>&1; do
  echo "waiting kafka..."
  sleep 3
done

echo "==> Creating Kafka topics"
docker compose -f compose.yaml exec -T kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create --if-not-exists \
  --topic ledger.outbox.v1 \
  --partitions 3 \
  --replication-factor 1 >/dev/null

docker compose -f compose.yaml exec -T kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create --if-not-exists \
  --topic ledger.outbox.v1.dlq \
  --partitions 1 \
  --replication-factor 1 >/dev/null

echo "==> Kafka topics"
docker compose -f compose.yaml exec -T kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list

echo "==> Containers"
docker compose -f compose.yaml ps
