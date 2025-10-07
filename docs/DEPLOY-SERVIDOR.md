# 🚀 Guia de Deploy - Servidor de Produção

## 📦 O QUE COPIAR PARA O SERVIDOR

### ✅ Arquivos Obrigatórios:

```
gestao-mensalidades-api/
├── docker-compose.yml              ← Config dos containers
├── Dockerfile                      ← Build da aplicação
├── .env                           ← IMPORTANTE: valores de produção!
├── build.gradle                   ← Dependências
├── settings.gradle                ← Config Gradle
├── gradlew                        ← Gradle Wrapper
├── gradlew.bat                    ← Gradle Wrapper (Windows)
├── gradle/                        ← Wrapper files
├── database/
│   ├── init.sql                   ← Schema inicial
│   └── migrations/
│       ├── 001_*.sql
│       └── 002_add_firebase_auth_support.sql
├── docker/
│   └── pgadmin/
│       └── servers.json
└── src/                           ← Código-fonte completo
```

### ❌ NÃO copiar (apenas dev local):

```
✅ scripts/*.sh                   ← Copiar scripts Bash (run-make.sh, start-with-flyway.sh)
❌ build/                          ← Será gerado no servidor
❌ .gradle/                        ← Cache local
❌ .git/                           ← Opcional (pode copiar se quiser)
❌ .idea/                          ← IDE
```

### ✅ Copiar (scripts para Linux):

```
✅ scripts/*.sh                    ← Scripts Bash para servidor Linux
  - NOTE: legacy helper scripts were consolidated into `scripts/toolbox.sh`.
  - Use `./scripts/toolbox.sh` or the Makefile targets on the server to run these tasks.
```

---

## 🔐 IMPORTANTE: Arquivo .env de Produção

Crie um `.env` NO SERVIDOR com valores seguros:

```env
# ========================================
# PRODUÇÃO - VALORES SEGUROS
# ========================================

# Spring Profile
SPRING_PROFILE=prod

# Database (PostgreSQL no Docker)
DB_HOST=postgres
DB_PORT=5432
DB_NAME=gestao_mensalidades
DB_USERNAME=gestao_app_user
DB_PASSWORD=SENHA_SUPER_FORTE_AQUI_123!@#

# JWT
JWT_SECRET=CHAVE_SUPER_SEGURA_256_BITS_OU_MAIS_AQUI_1234567890
JWT_EXPIRATION=86400000

# Stripe (CHAVES LIVE!)
STRIPE_SECRET_KEY=sk_live_SUA_CHAVE_LIVE_AQUI
STRIPE_PUBLISHABLE_KEY=pk_live_SUA_CHAVE_LIVE_AQUI
STRIPE_WEBHOOK_SECRET=whsec_SUA_CHAVE_WEBHOOK_AQUI

# Firebase
FIREBASE_PROJECT_ID=seu-projeto-id
FIREBASE_SERVICE_ACCOUNT_KEY_PATH=/app/config/serviceAccountKey.json

# Frontend
FRONTEND_URL=https://seu-dominio.com
CORS_ALLOWED_ORIGINS=https://seu-dominio.com,https://app.seu-dominio.com

# Redis
REDIS_PASSWORD=SENHA_REDIS_FORTE_AQUI

# pgAdmin
PGADMIN_PASSWORD=SENHA_PGADMIN_FORTE_AQUI
```

---

## 📋 Passo a Passo: Deploy via Docker

### 1️⃣ Preparar Servidor

```bash
# Instalar Docker e Docker Compose
sudo apt update
sudo apt install -y docker.io docker-compose

# Verificar
docker --version
docker-compose --version
```

### 2️⃣ Copiar Arquivos

**Opção A: Via Git (Recomendado)**
```bash
# No servidor
cd /opt
git clone https://github.com/FelipeSchulzLuz/gestao-mensalidades-api.git
cd gestao-mensalidades-api
```

**Opção B: Via SCP (do Windows)**
```powershell
# No Windows (PowerShell)
# Comprimir projeto (sem node_modules, build, etc)
Compress-Archive -Path C:\freelancer\manager\gestao-mensalidades-api\* -DestinationPath gestao-api.zip

# Copiar para servidor
scp gestao-api.zip usuario@IP_SERVIDOR:/opt/

# No servidor
cd /opt
unzip gestao-api.zip
```

**Opção C: Via SFTP (FileZilla)**
- Conecte no servidor
- Vá para `/opt/gestao-mensalidades-api`
- Arraste os arquivos

### 3️⃣ Configurar .env

```bash
# No servidor
cd /opt/gestao-mensalidades-api
nano .env

# Cole os valores de produção (ver acima)
# Ctrl+O para salvar, Ctrl+X para sair
```

### 4️⃣ Configurar Firebase

```bash
# Criar pasta de configs
mkdir -p /opt/gestao-mensalidades-api/config

# Copiar service account key para servidor
# No Windows:
scp serviceAccountKey.json usuario@IP_SERVIDOR:/opt/gestao-mensalidades-api/config/

# Verificar permissões
chmod 600 /opt/gestao-mensalidades-api/config/serviceAccountKey.json
```

### 5️⃣ Ajustar docker-compose.yml para Produção

```bash
nano docker-compose.yml
```

Ajustar:
- Portas (80:8080 ao invés de 8800:8080)
- Remover pgAdmin (opcional)
- Adicionar volumes para Firebase:

```yaml
  app:
    volumes:
      - ./config:/app/config:ro  # ← Adicionar esta linha
```

### 6️⃣ Dar Permissão de Execução aos Scripts

```bash
# Tornar scripts executáveis
chmod +x scripts/*.sh
```

### 7️⃣ Aplicar Migration (Primeira vez)

**Opção A: Database limpo (recomendado primeira vez)**
```bash
# Recreate DB via toolbox or manually using docker-compose
./scripts/toolbox.sh db:recreate
```

**Opção B: Database existente**
```bash
# Run Flyway migrations via Makefile or docker-compose
make apply-migrations
# or
docker compose run --rm flyway
```

### 8️⃣ Iniciar Aplicação

```bash
# Build e start
docker-compose up -d --build

# Ver logs
docker-compose logs -f

# Verificar status
docker-compose ps
```

### 9️⃣ Verificar se Funcionou

```bash
# Testar health check
curl http://localhost:8080/actuator/health

# Testar login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@test.com","password":"senha123"}'
```

---

## 🔄 Atualizar Aplicação (Após Deploy Inicial)

```bash
# No servidor
cd /opt/gestao-mensalidades-api

# Parar aplicação
docker-compose down

# Atualizar código (se via Git)
git pull origin master

# Ou reenviar arquivos via SCP/SFTP

# Rebuild e restart
docker-compose up -d --build

# Ver logs
docker-compose logs -f app
```

---

## 🐛 Troubleshooting

### ❌ Erro: "Cannot connect to database"

**Solução:**
```bash
# Verificar se PostgreSQL está rodando
docker-compose ps

# Ver logs do PostgreSQL
docker-compose logs postgres

# Recriar banco
docker-compose down
docker volume rm gestao-mensalidades-api_postgres_data
docker-compose up -d
```

### ❌ Erro: "Firebase not initialized"

**Solução:**
```bash
# Verificar se arquivo existe
ls -la /opt/gestao-mensalidades-api/config/serviceAccountKey.json

# Verificar permissões
chmod 600 /opt/gestao-mensalidades-api/config/serviceAccountKey.json

# Verificar .env
cat .env | grep FIREBASE
```

### ❌ Erro: "Port already in use"

**Solução:**
```bash
# Ver o que está usando a porta
sudo lsof -i :8080

# Matar processo
sudo kill -9 PID

# Ou mudar porta no docker-compose.yml
ports:
  - "8081:8080"  # Usar 8081 no lugar de 8080
```

---

## 📊 Monitoramento

### Ver Logs em Tempo Real
```bash
docker-compose logs -f app
```

### Ver Uso de Recursos
```bash
docker stats
```

### Backup do Banco
```bash
docker exec gestao-postgres pg_dump -U gestao_app_user gestao_mensalidades > backup.sql
```

### Restaurar Backup
```bash
cat backup.sql | docker exec -i gestao-postgres psql -U gestao_app_user gestao_mensalidades
```

---

## 🔒 Segurança em Produção

### ✅ Checklist:

- [ ] `.env` com senhas fortes
- [ ] Chaves Stripe LIVE (não test)
- [ ] HTTPS configurado (Nginx + Let's Encrypt)
- [ ] Firewall configurado (apenas portas necessárias)
- [ ] Backups automáticos do banco
- [ ] Logs centralizados
- [ ] Monitoring (Sentry, New Relic, etc)
- [ ] Service account key com permissões mínimas
- [ ] CORS configurado apenas para domínios reais
- [ ] pgAdmin desabilitado em produção (ou protegido)

---

## 🎯 Resumo

**Para copiar para servidor:**
```
✅ docker-compose.yml
✅ Dockerfile
✅ .env (criar com valores prod)
✅ database/ (migrations)
✅ src/ (código)
✅ build.gradle, gradlew, etc
✅ serviceAccountKey.json (na pasta config)

❌ scripts/ (não precisa)
❌ build/ (não precisa)
❌ .gradle/ (não precisa)
```

**Comando único no servidor:**
```bash
cd /opt/gestao-mensalidades-api
docker-compose up -d --build
```

Pronto! 🚀
