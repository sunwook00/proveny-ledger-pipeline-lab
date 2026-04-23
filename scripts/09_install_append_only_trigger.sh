#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

docker compose -f compose.yaml exec -T postgres \
  psql -U proveny -d proveny_lab < sql/install_append_only_trigger.sql
