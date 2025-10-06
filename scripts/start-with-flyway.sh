#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

ENV_TEMPLATE=.env.docker
ENV_FILE=.env
FORCE=${FORCE:-0}

# Copy .env if not exists or if FORCE=1
if [ -f "$ENV_FILE" ] && [ "$FORCE" -ne 1 ]; then
  echo ".env already exists. To overwrite, run: FORCE=1 ./scripts/start-with-flyway.sh"
else
  if [ ! -f "$ENV_TEMPLATE" ]; then
    echo "Template $ENV_TEMPLATE not found. Aborting." >&2
    exit 1
  fi
  cp "$ENV_TEMPLATE" "$ENV_FILE"
  echo "Copied $ENV_TEMPLATE -> $ENV_FILE"
fi

# Ensure docker compose network/services are available
echo "Bringing up postgres and redis and pgadmin (detached)..."
docker compose up -d postgres redis pgadmin

# Run Flyway migrations via compose
echo "Running Flyway migrations..."
docker compose run --rm flyway

# Start the application
echo "Starting application (app)..."
docker compose up -d app

echo "Done. Use 'docker compose logs -f app' to follow logs."
