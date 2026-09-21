#!/usr/bin/env bash
# One command to build and run the whole stack, detached, and verify it is up.
# Usage: ./run.sh
set -euo pipefail
cd "$(dirname "$0")"

# 1. Ensure a .env with a valid JWT secret exists.
if [ ! -f .env ]; then
  grep -v '^JWT_SECRET=' .env.example > .env
fi
if ! grep -q '^JWT_SECRET=.\+' .env; then
  if command -v openssl >/dev/null 2>&1; then
    SECRET=$(openssl rand -base64 48 | tr -d '\n')
  else
    SECRET=$(head -c 48 /dev/urandom | base64 | tr -d '\n')
  fi
  grep -v '^JWT_SECRET=' .env > .env.tmp && mv .env.tmp .env
  echo "JWT_SECRET=$SECRET" >> .env
fi

# 2. Build and start in the background so this shell stays free.
echo "Building and starting containers (first run downloads dependencies, a few minutes)..."
docker compose up -d --build

# 3. Wait for the backend to report healthy.
echo "Waiting for the backend..."
for _ in $(seq 1 120); do
  code=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/actuator/health || true)
  if [ "$code" = "200" ]; then echo "Backend is up."; break; fi
  sleep 2
done

# 4. Verify a demo login end to end.
if curl -s -X POST http://localhost:8080/api/auth/login \
      -H 'Content-Type: application/json' \
      -d '{"username":"officer","password":"Officer#2024"}' | grep -q token; then
  echo "Login check passed."
else
  echo "Login check not passing yet. The seed may still be running."
  echo "Follow it with: docker compose logs -f backend"
fi

cat <<'INFO'

============================================================
  Synchrony Dynamic Risk Assessment is running.

  Open:      http://localhost:8081
             (in Codespaces, open the forwarded port 8081)

  Officer:   officer / Officer#2024
  Applicant: maria   / Applicant#2024

  Logs:      docker compose logs -f backend
  Stop:      docker compose down          (add -v to reset the database)
============================================================
INFO
