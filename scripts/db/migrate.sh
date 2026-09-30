#!/usr/bin/env bash
# Aplica database/migrations/*.sql via Flyway (mesmo mecanismo do Spring na TASK 3.1).
# Uso: ./scripts/db/migrate.sh [--reset]
#   --reset  derruba o volume pgdata e recria do zero (DEV local apenas).
set -euo pipefail
cd "$(dirname "$0")/../.."

if [ "${1:-}" = "--reset" ]; then
  echo "RESET: removendo volume pgdata..."
  docker compose down -v
fi
docker compose up -d db
docker compose --profile tools run --rm migrate migrate
echo OK
