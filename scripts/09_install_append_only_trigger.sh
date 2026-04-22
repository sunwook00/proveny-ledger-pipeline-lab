#!/usr/bin/env bash
set -euo pipefail
docker exec -i proveny-lab-postgres psql -U proveny -d proveny_lab < sql/install_append_only_trigger.sql
