#!/usr/bin/env bash
# Wait for the database to accept connections before starting the app, so the
# backend never boots ahead of the database and never crashes on a cold start.
set -e

HOST="${DB_HOST:-db}"
PORT="${DB_PORT:-5432}"
USER="${DB_USERNAME:-synchrony}"

echo "Waiting for the database at ${HOST}:${PORT} ..."
i=0
until pg_isready -h "$HOST" -p "$PORT" -U "$USER" -q; do
  i=$((i + 1))
  if [ "$i" -ge 300 ]; then
    echo "Database wait timed out after ${i}s. Starting anyway."
    break
  fi
  sleep 1
done
echo "Database is ready. Starting the application."

exec java ${JAVA_OPTS:-} -jar /app/app.jar
