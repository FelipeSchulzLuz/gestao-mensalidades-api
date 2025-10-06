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

## 📋 Descrição dos Scripts

### 🗑️ `recreate-database` (.ps1 / .sh)

**Função:** Recria o banco de dados do ZERO com todas migrations aplicadas.

**⚠️ DESTRUTIVO:** Apaga TODOS os dados!

**Quando usar:**
- ✅ Desenvolvimento local sem dados importantes
- ✅ Primeira configuração do ambiente
- ✅ Quando quer garantir estado limpo do banco
- ✅ Depois de mudar várias migrations

**Não usar em:**
- ❌ Produção
- ❌ Staging com dados importantes
- ❌ Quando tem dados de teste que quer manter

**Processo:**
1. Para container PostgreSQL
2. Remove volume do banco (apaga tudo)
3. Recria container e volume
4. Migrations rodam automaticamente via docker-entrypoint-initdb.d/
5. Verifica se colunas foram criadas

---

### 🔄 `apply-migration-existing-db` (.ps1 / .sh)

**Função:** Aplica migration SEM dropar o banco.

**NÃO destrutivo:** Preserva dados existentes!

**Quando usar:**
- ✅ Staging/Produção
- ✅ Desenvolvimento com dados importantes
- ✅ Aplicar APENAS a nova migration
- ✅ Simular comportamento de produção

**Processo:**
1. Copia arquivo SQL para dentro do container
2. Executa migration via `psql` dentro do container
3. Verifica se colunas foram criadas

---

### 🐳 `apply-migration-docker` (.ps1 / .sh)

**Função:** Aplica migration diretamente via pipe no Docker.

**Quando usar:**
- ✅ Alternativa rápida ao apply-migration-existing-db
- ✅ Quando não quer copiar arquivo para container
- ✅ Para automação via CI/CD

**Processo:**
1. Localiza container PostgreSQL automaticamente
2. Faz pipe do SQL diretamente para `psql` no container
3. Verifica colunas criadas

---

### 🔧 `install-psql.ps1` (Apenas Windows)

**Função:** Auxilia instalação do cliente PostgreSQL no Windows.

**Métodos:**
1. Chocolatey
2. Scoop
3. Instalador oficial

**Nota:** Não necessário no Linux (use `apt install postgresql-client`)

---

### 📋 `apply-migration-002.ps1` (Apenas Windows)

**Função:** Menu interativo com múltiplas opções.

**Opções:**
1. Copiar SQL para clipboard (usar no DBeaver/pgAdmin)
2. Aplicar via psql (se instalado)
3. Aplicar via Docker
4. Aplicar via .NET/Npgsql (experimental)

**Quando usar:** 
- ✅ Desenvolvimento Windows sem Docker configurado
- ✅ Quando tem psql instalado
- ✅ Para usar DBeaver/pgAdmin manualmente

---

## 🎯 Qual Script Usar?

### 🖥️ Desenvolvimento Local (Windows)

```powershell
# Sem dados importantes? Use este:
.\scripts\recreate-database.ps1

# Com dados que quer manter?
.\scripts\apply-migration-existing-db.ps1

# Docker não funciona? Use menu:
.\scripts\apply-migration-002.ps1
```

---

### 🐧 Servidor Linux (Produção/Staging)

```bash
# SEMPRE use este (não destrutivo):
./scripts/apply-migration-existing-db.sh

# OU este (mais simples):
./scripts/apply-migration-docker.sh

# NUNCA use este em produção:
# ❌ ./scripts/recreate-database.sh
```

---

## 🚀 Workflow Recomendado

### Dev Local (primeira vez)

```powershell
# Windows
git clone ...
cd gestao-mensalidades-api
docker-compose up -d postgres
.\scripts\recreate-database.ps1
.\gradlew bootRun
```

```bash
# Linux
git clone ...
cd gestao-mensalidades-api
chmod +x scripts/*.sh
docker-compose up -d postgres
./scripts/recreate-database.sh
./gradlew bootRun
```

---

### Nova Migration Criada

```powershell
# Windows
git pull
.\scripts\recreate-database.ps1  # Se não tem dados importantes
# OU
.\scripts\apply-migration-existing-db.ps1  # Se quer manter dados
.\gradlew bootRun
```

```bash
# Linux
git pull
./scripts/apply-migration-existing-db.sh
./gradlew bootRun
```

---

### Deploy Servidor (primeira vez)

```bash
# No servidor Linux
git clone ...
cd gestao-mensalidades-api
chmod +x scripts/*.sh

# Configurar .env com valores de produção
nano .env

# Subir banco
docker-compose up -d postgres

# Aplicar migrations
./scripts/recreate-database.sh  # OK na primeira vez

# Subir aplicação
docker-compose up -d --build
```

---

### Deploy Servidor (atualização)

```bash
# No servidor Linux
cd gestao-mensalidades-api
git pull

# BACKUP PRIMEIRO!
docker exec gestao-postgres pg_dump -U gestao_app_user gestao_mensalidades > backup_$(date +%Y%m%d).sql

# Aplicar nova migration (NÃO destrutivo)
./scripts/apply-migration-existing-db.sh

# Atualizar aplicação
docker-compose up -d --build

# Verificar logs
docker-compose logs -f app
```

---

## 🔒 Segurança

### ✅ SEMPRE:
- Fazer backup antes de aplicar migration em produção
- Testar migration em staging primeiro
- Usar scripts `.sh` no servidor Linux
- Verificar colunas após aplicar migration

### ❌ NUNCA:
- Usar `recreate-database` em produção/staging
- Aplicar migration sem backup
- Executar scripts sem ler o código antes
- Ignorar erros de migration

---

## 🆘 Troubleshooting

### "Permission denied" ao executar script .sh

```bash
chmod +x scripts/*.sh
./scripts/nome-do-script.sh
```

### "Container not found"

```bash
# Verificar containers rodando
docker ps

# Subir postgres
docker-compose up -d postgres
```

### "psql: command not found" (dentro do script)

Scripts `.sh` usam Docker, não precisam psql instalado!

### Migration já foi aplicada

Normal! Script detecta e avisa. Use `recreate-database.sh` se quiser começar do zero.

---

## 📚 Mais Informações

- **Boas práticas:** Ver `docs/08-MIGRATIONS-BOAS-PRATICAS.md`
- **Deploy servidor:** Ver `DEPLOY_SERVIDOR.md`
- **Aplicação manual:** Ver `COMO_APLICAR_MIGRATION.md`

---

## 🎓 Resumo

| Script | Windows | Linux | Destrutivo | Uso |
|--------|---------|-------|------------|-----|
| `recreate-database` | `.ps1` | `.sh` | ✅ SIM | Dev local |
| `apply-migration-existing-db` | `.ps1` | `.sh` | ❌ NÃO | Produção |
| `apply-migration-docker` | `.ps1` | `.sh` | ❌ NÃO | Qualquer |
| `install-psql` | `.ps1` | ❌ N/A | N/A | Dev Windows |
| `apply-migration-002` | `.ps1` | ❌ N/A | Depende | Dev Windows |

**Dica:** Em caso de dúvida, use `apply-migration-existing-db`! É seguro! ✅
