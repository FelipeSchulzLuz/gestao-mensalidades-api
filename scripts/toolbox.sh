#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPO="FelipeSchulzLuz/gestao-mensalidades-api"
cd "$REPO_ROOT"

usage() {
  cat <<'EOF'
Usage: toolbox.sh <command> [args]

Quick helper for repository maintenance — secrets, DB and CI prep.

Commands:
  start                Alias for start-with-flyway (recommended quick-start)
  start-with-flyway    Copy .env.docker->.env (creates from .env.docker.example if needed)
  secrets:prompt       Interactive prompt to set missing GitHub Actions secrets (uses gh)
  secrets:import       Scan .env files and import suggested secrets (interactive)
  db:apply-migration   Apply Flyway migration (uses docker-compose / Makefile)
  db:recreate          Recreate the development database (DESTRUCTIVE)
  merge-env            Merge .env templates (.env.docker + .env.example) into .env (dry-run by default)
  run-make [target]    Run make in repo root (default: start)
  help, --help, -h     Show this help

Examples:
  ./scripts/toolbox.sh start-with-flyway
  ./scripts/toolbox.sh start
  ./scripts/toolbox.sh secrets:import
  ./scripts/toolbox.sh secrets:prompt

Requirements:
  - gh (GitHub CLI) installed and authenticated for secrets automation
  - docker and docker compose for local infra commands

Security notes:
  - Never commit sensitive values in files tracked by Git. Use GitHub Secrets for CI.
  - For SARIF upload, provide a Fine-grained PAT with permission "Security events: Write"
    and store it as SECURITY_UPLOAD_TOKEN if your org restricts GITHUB_TOKEN.

EOF
}

ensure_gh() {
  if ! command -v gh >/dev/null 2>&1; then
    echo "Error: gh (GitHub CLI) not found. Install it first: https://cli.github.com/manual/installation" >&2
    exit 1
  fi
}

cmd_secrets_prompt() {
  ensure_gh
  echo "Prompting for missing secrets..."
  read -rp "DOCKER_USERNAME: " DOCKER_USERNAME
  read -rp "DOCKER_PASSWORD: " -s DOCKER_PASSWORD; echo
  read -rp "DOCKER_REGISTRY (default docker.io): " DOCKER_REGISTRY
  DOCKER_REGISTRY=${DOCKER_REGISTRY:-docker.io}
  read -rp "SONAR_TOKEN (leave empty if none): " SONAR_TOKEN
  read -rp "SECURITY_UPLOAD_TOKEN (leave empty if none): " SECURITY_UPLOAD_TOKEN
  read -rp "NVD_API_KEY (leave empty if none): " NVD_API_KEY

  printf '%s' "$DOCKER_USERNAME" | gh secret set DOCKER_USERNAME --repo "$REPO" --body -
  printf '%s' "$DOCKER_PASSWORD" | gh secret set DOCKER_PASSWORD --repo "$REPO" --body -
  printf '%s' "$DOCKER_REGISTRY" | gh secret set DOCKER_REGISTRY --repo "$REPO" --body -
  [[ -n "$SONAR_TOKEN" ]] && printf '%s' "$SONAR_TOKEN" | gh secret set SONAR_TOKEN --repo "$REPO" --body -
  [[ -n "$SECURITY_UPLOAD_TOKEN" ]] && printf '%s' "$SECURITY_UPLOAD_TOKEN" | gh secret set SECURITY_UPLOAD_TOKEN --repo "$REPO" --body -
  [[ -n "$NVD_API_KEY" ]] && printf '%s' "$NVD_API_KEY" | gh secret set NVD_API_KEY --repo "$REPO" --body -
  echo "Secrets set."
}

cmd_secrets_import() {
  ensure_gh
  if [[ -x "./scripts/import_envs_to_github_secrets.sh" ]]; then
    ./scripts/import_envs_to_github_secrets.sh
  elif [[ -f "./scripts/import_envs_to_github_secrets.sh" ]]; then
    bash ./scripts/import_envs_to_github_secrets.sh
  else
    echo "import_envs_to_github_secrets.sh not found in ./scripts."
    echo "See scripts/README.md for manual instructions or create secrets via 'gh secret set'";
    exit 1
  fi
}

cmd_db_apply_migration() {
  MIGRATION_FILE=${1-}
  if [[ -z "$MIGRATION_FILE" ]]; then
    echo "Usage: toolbox.sh db:apply-migration path/to/migration.sql" >&2
    exit 1
  fi
  if [[ -x "./scripts/apply-migration-existing-db.sh" ]]; then
    ./scripts/apply-migration-existing-db.sh "$MIGRATION_FILE"
  elif [[ -f "./scripts/apply-migration-existing-db.sh" ]]; then
    bash ./scripts/apply-migration-existing-db.sh "$MIGRATION_FILE"
  else
    echo "apply-migration-existing-db.sh not found in ./scripts. Please run migrations via 'docker compose run --rm flyway' or 'make apply-migrations'";
    exit 1
  fi
}

cmd_db_recreate() {
  if [[ -x "./scripts/recreate-database.sh" ]]; then
    ./scripts/recreate-database.sh
  elif [[ -f "./scripts/recreate-database.sh" ]]; then
    bash ./scripts/recreate-database.sh
  else
    echo "recreate-database.sh not found in ./scripts. To recreate DB manually, stop containers and remove volumes, then start postgres again. See scripts/README.md";
    exit 1
  fi
}

cmd_start_with_flyway() {
  if [[ -x "./scripts/start-with-flyway.sh" ]]; then
    ./scripts/start-with-flyway.sh
  elif [[ -f "./scripts/start-with-flyway.sh" ]]; then
    bash ./scripts/start-with-flyway.sh
  else
    echo "start-with-flyway.sh not found. Running embedded fallback: prepare .env, start infra, run flyway, and start app."

    # Copy .env template if no .env exists
    if [[ ! -f "${REPO_ROOT}/.env" ]]; then
      if [[ -f "${REPO_ROOT}/.env.docker" ]]; then
        cp "${REPO_ROOT}/.env.docker" "${REPO_ROOT}/.env"
        echo "Copied .env.docker -> .env"
      elif [[ -f "${REPO_ROOT}/.env.docker.example" ]]; then
        echo "⚠️  .env.docker not found. Creating from template..."
        cp "${REPO_ROOT}/.env.docker.example" "${REPO_ROOT}/.env.docker"
        cp "${REPO_ROOT}/.env.docker" "${REPO_ROOT}/.env"
        echo "⚠️  IMPORTANT: Edit .env.docker with REAL values before running!"
        echo "⚠️  Template has placeholder values that won't work."
        exit 1
      fi
    fi

    echo "Bringing up infra services (postgres, redis, pgadmin)..."
    docker compose up -d postgres redis pgadmin

    echo "Running Flyway migrations..."
    docker compose run --rm flyway

    echo "Starting application service..."

    # Check if host ports used by pgadmin or app are already in use to avoid Docker bind errors
    CONFLICT_PORTS=()
    if ss -ltn | grep -q ':18080\b'; then
      CONFLICT_PORTS+=(18080)
    fi
    if ss -ltn | grep -q ':10080\b'; then
      CONFLICT_PORTS+=(10080)
    fi

    if [[ ${#CONFLICT_PORTS[@]} -gt 0 ]]; then
      echo "Error: the following host ports are already in use: ${CONFLICT_PORTS[*]}"
      echo "This usually means another container or process is bound to these ports."
      echo "To inspect which container/process is using a port run:"
      echo "  sudo lsof -i :<port> || ss -lptn | grep ':<port>'"
      echo "Common fixes:"
      echo "  - Stop the conflicting container(s): docker stop <container> (e.g. gestao-pgadmin)"
      echo "  - Or stop via compose: docker compose stop <service>"
      echo "  - Or change the host port mapping in docker-compose.yml for app or pgadmin"
      exit 1
    fi

    docker compose up -d app
  fi
}

cmd_merge_env() {
  # Delegate to merge_env_templates.sh if present
  if [[ -x "./scripts/merge_env_templates.sh" ]]; then
    ./scripts/merge_env_templates.sh "$@"
  elif [[ -f "./scripts/merge_env_templates.sh" ]]; then
    bash ./scripts/merge_env_templates.sh "$@"
  else
    echo "merge_env_templates.sh not found in ./scripts. See scripts/README.md for guidance on creating .env from templates."
    exit 1
  fi
}

cmd_run_make() {
  if [[ $# -eq 0 ]]; then
    make start
  else
    make "$@"
  fi
}

case ${1-} in
  start) shift; cmd_start_with_flyway "$@" ;;
  secrets:prompt) shift; cmd_secrets_prompt "$@" ;;
  secrets:import) shift; cmd_secrets_import "$@" ;;
  db:apply-migration) shift; cmd_db_apply_migration "$@" ;;
  db:recreate) shift; cmd_db_recreate "$@" ;;
  merge-env) shift; cmd_merge_env "$@" ;;
  start-with-flyway) shift; cmd_start_with_flyway "$@" ;;
  run-make) shift; cmd_run_make "$@" ;;
  help|--help|-h) usage ;;
  "") usage ;;
  *) echo "Unknown command: $1"; usage; exit 1 ;;
esac
