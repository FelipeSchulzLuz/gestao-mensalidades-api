# 📜 Scripts de Gerenciamento do Banco de Dados

Este diretório contém scripts para gerenciar migrations e banco de dados.

---

## Scripts e fluxo recomendado

Observação sobre Windows

Os scripts PowerShell (`.ps1`) foram removidos deste repositório — a equipe padronizou o uso de Bash e containers. Se você estiver em Windows, use WSL2 para executar os scripts Bash, ou use os alvos do Makefile através do wrapper `scripts/run-make.sh`.

Linux/macOS (Bash - `.sh`)

Uso em **servidor Linux** ou desenvolvimento local Linux/macOS. Recomendamos usar o wrapper que invoca o Makefile para fluxo padronizado:

```bash
# Dar permissão de execução (primeira vez)
chmod +x scripts/*.sh

# Uso recomendado (vai copiar .env, rodar flyway e iniciar a app)
./scripts/run-make.sh start

# Alternativa: executar passo a passo
# 1) Preparar .env
cp .env.docker .env
# 2) Subir infra mínima
docker compose up -d postgres redis pgadmin
# 3) Rodar migrations via Flyway
docker compose run --rm flyway
# 4) Subir a aplicação
docker compose up -d app
```

---
```markdown
# � Scripts de Gerenciamento do Banco de Dados

Este diretório contém utilitários e um fluxo recomendado para aplicar migrations e gerenciar o banco localmente ou em servidores.

Objetivo deste documento: remover ambiguidade e fornecer passos exatos, copy/paste, para cenários comuns.

---

## Pré-requisitos

- Docker & Docker Compose (v2 recommended)
- Permissão para executar scripts no repositório (chmod +x)
- (Local dev) Java + Gradle are required only if you plan to run the app with Gradle (`./gradlew bootRun`)
- No Windows? Use WSL2 and o fluxo Bash abaixo. PowerShell scripts foram removidos do repositório.

Certifique-se de ter copiado o template de variáveis de ambiente antes de iniciar:

```bash
cp .env.docker .env    # revisão manual recomendada antes de subir em produção
```

---

## Fluxo recomendado (rápido) — use o wrapper

Este projeto tem um wrapper que invoca o Makefile e executa os passos padronizados: copiar `.env`, subir infra mínima, executar Flyway e iniciar a app.

```bash
# Torne os scripts executáveis (apenas 1x)
chmod +x scripts/*.sh

# Fluxo padronizado (recomendado)
./scripts/run-make.sh start

# O que esse comando faz (resumo):
# 1) copia .env.docker -> .env (se .env não existir)
# 2) sobe serviços mínimos: postgres, redis, pgadmin
# 3) executa migrations via Flyway (docker compose run --rm flyway)
# 4) sobe a aplicação (docker compose up -d app)
```

Use esse fluxo sempre que quiser uma maneira segura e reprodutível para dev/local.

---

## Fluxo manual (passo-a-passo)

Siga estes passos se preferir executar cada etapa manualmente.

1) Preparar variáveis de ambiente

```bash
cp .env.docker .env
# Edite .env se necessário
```

2) Subir infra mínima

```bash
docker compose up -d postgres redis pgadmin
```

3) Rodar migrations (usando o serviço `flyway` definido no `docker-compose.yml`)

```bash
# Este comando usa o serviço flyway conforme configurado no compose
docker compose run --rm flyway

# Alternativa: usar alvo Makefile
make apply-migrations
```

4) Subir a aplicação

```bash
docker compose up -d app
```

5) Verificar a aplicação e o banco

```bash
# Logs da app
docker compose logs -f app

# Verificar colunas/tabelas via psql dentro do container (exemplo)
docker exec -i $(docker ps -qf "name=gestao-postgres") \
	psql -U gestao_app_user -d gestao_mensalidades_dev -c "\d users"
```

---

## Makefile targets (resumo)

- `make start` — wrapper equivalente a `./scripts/run-make.sh start` (ci-safe quick-start)
- `make apply-migrations` — executa `docker compose run --rm flyway`
- `make ci-migrate` — roda `./gradlew flywayValidate` e, se passar, `docker compose run --rm flyway` (útil para CI)
- `make up` — sobe serviços definidos no compose
- `make clean` — remove containers/volumes usados no dev (CUIDADO: destrutivo)

Use `make help` para ver o conjunto completo de alvos se o Makefile estiver presente.

---

## Recomendações e regras importantes

- Sempre faça backup antes de aplicar migrations em staging/produção.
- Em produção/staging use `ci-migrate` (ou processo CI) e valide `flywayValidate` antes de migrar.
- Não rode `recreate-database` em produção — é destrutivo.
- Os scripts PowerShell foram removidos; no Windows use WSL2 para o fluxo Bash.

---

## Verificação rápida (sanity checks)

Com o banco rodando, verifique se a migration foi aplicada:

```bash
# Listar migrations aplicadas (se você tiver psql localmente):
docker exec -i $(docker ps -qf "name=gestao-postgres") \
	psql -U gestao_app_user -d gestao_mensalidades_dev -c "SELECT version, description, installed_on FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 10;"

# Verificar existência de coluna (exemplo firebase_uid em users)
docker exec -i $(docker ps -qf "name=gestao-postgres") \
	psql -U gestao_app_user -d gestao_mensalidades_dev -c "\d+ users"
```

---

## Troubleshooting comum

- "Permission denied" ao executar script

```bash
chmod +x scripts/*.sh
```

- "Container not found" / Postgres não está rodando

```bash
docker ps
docker compose up -d postgres
```

- `JAVA_HOME` / `./gradlew` erros

```
# Gradle wrapper precisa de Java no host quando você roda ./gradlew
# Para executar a aplicação via Gradle certifique-se que Java está instalado e JAVA_HOME configurado
echo $JAVA_HOME
java -version
```

- `flywayValidate` falha no CI

```
# Rode localmente: ./gradlew flywayValidate
# Reveja mensagens e corrija a migration antes de permitir deploy
```

- Ver logs do flyway/container

```bash
docker compose logs flyway
docker compose logs -f postgres
```

---

## Notas finais

- Este README tem como objetivo ser uma referência prática: comandos copy/paste, verificação e ações de recuperação.
- Se quiser, posso também criar exemplos de comandos para o CI (GitHub Actions/GitLab CI) que usem `make ci-migrate`.

``` 
git clone ...
