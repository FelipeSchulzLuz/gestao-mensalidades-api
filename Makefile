SHELL := /bin/bash

ENV_TEMPLATE := .env.docker
ENV_FILE := .env

.PHONY: help start apply-migrations up clean

help:
	@echo "Usage: make <target>"
	@echo "Targets:"
	@echo "  start            Prepare env, run flyway migrations and start the app"
	@echo "  apply-migrations Run only Flyway migrations via docker-compose"
	@echo "  up               Start app service (assumes migrations applied)"
	@echo "  clean            Remove generated .env (if created by the Makefile)"

start: ## Copy .env and run migrations + app
	@./scripts/start-with-flyway.sh

apply-migrations: ## Run flyway migrate via docker compose
	@echo "Running Flyway migrations..."
	@docker compose run --rm flyway

.PHONY: ci-migrate
ci-migrate: ## Run gradle flywayValidate, then docker compose run --rm flyway if validation passes
	@echo "Running Gradle flywayValidate..."
	@./gradlew flywayValidate || (echo "flywayValidate failed" && exit 1)
	@echo "flywayValidate passed. Running Flyway migrations via docker-compose..."
	@docker compose run --rm flyway

up: ## Start the application service
	@echo "Starting app..."
	@docker compose up -d app

clean: ## Remove .env created by this Makefile
	@if [ -f "$(ENV_FILE)" ] ; then \
		read -p "Remove $(ENV_FILE)? [y/N] " ans; \
		if [[ $$ans =~ ^[Yy]$$ ]]; then rm -f $(ENV_FILE); echo "Removed $(ENV_FILE)"; else echo "Skipped"; fi \
	else echo "$(ENV_FILE) not found"; fi
