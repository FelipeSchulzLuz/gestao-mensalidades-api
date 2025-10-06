# 🚀 Como Iniciar o Ambiente de Desenvolvimento

Este guia explica como iniciar todos os serviços necessários para desenvolvimento.

## 📋 Arquitetura do Ambiente

```
┌─────────────────────────────────────────────────────────────┐
│                   SERVIDOR (192.168.1.12)                    │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Docker Compose                                        │ │
│  │  - PostgreSQL (porta 54320)                            │ │
│  │  - pgAdmin (porta 8800)                                │ │
│  │  - Redis (porta 63790)                                 │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            ↕ Rede
┌─────────────────────────────────────────────────────────────┐
│               MÁQUINA LOCAL (Desenvolvimento)                │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Terminal 1: Stripe CLI                               │ │
│  │  - Webhooks listener                                   │ │
│  │  - Forward para localhost:8800                         │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Terminal 2: Spring Boot API                           │ │
│  │  - ./gradlew bootRun                                   │ │
│  │  - Porta 8800                                          │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Terminal 3: Frontend (futuro)                         │ │
│  │  - npm run dev                                         │ │
│  │  - Porta 3000                                          │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 Passo a Passo

### 1️⃣ Iniciar Docker no Servidor (192.168.1.12)

Recomendado: use o wrapper padronizado que invoca o Makefile para copiar `.env`, subir infra mínima e executar migrations via Flyway.

```bash
# Torne os scripts executáveis (uma vez)
chmod +x scripts/*.sh

# Fluxo recomendado (rápido e reprodutível)
./scripts/run-make.sh start
```

O wrapper faz (resumo):
- copia `.env.docker` → `.env` (se `.env` não existir)
- sobe serviços mínimos: `postgres`, `redis`, `pgadmin`
- executa migrations via Flyway (`docker compose run --rm flyway`)
- sobe a aplicação (`docker compose up -d app`)

Saída esperada (resumo):

PostgreSQL e Redis iniciados, migrations aplicadas com sucesso, aplicação disponível em `http://localhost:8800` (ou host/porta configurada no `.env`).

Se precisar executar manualmente, veja a seção "Fluxo manual" no final deste documento.

---


### 2️⃣ Iniciar Stripe CLI (Máquina Local)

Em um **terminal separado** na sua máquina local:

```bash
# Se tiver o script wrapper local (scripts/start-stripe.sh)
./scripts/start-stripe.sh

# Ou manualmente com Stripe CLI
stripe listen --forward-to localhost:8800/api/webhooks/stripe
```

**O que este script faz:**
- ✅ Verifica se Stripe CLI está instalado
- ✅ Verifica se você está autenticado
- ✅ Inicia o listener de webhooks
- ✅ Encaminha eventos para `localhost:8800/api/webhooks/stripe`
- ✅ Exibe o **webhook secret** (whsec_xxxxx)

**Saída esperada:**
```
==> Verificando instalação do Stripe CLI...
✓ Stripe CLI instalado!

==> Verificando autenticação...
✓ Autenticado no Stripe!

==============================================================================
  Iniciando Stripe Listener
==============================================================================

⚠ IMPORTANTE:
  1. O webhook secret (whsec_xxxxx) será exibido abaixo
  2. Copie e adicione ao arquivo .env:
     STRIPE_WEBHOOK_SECRET=whsec_xxxxx

Iniciando listener... (Pressione Ctrl+C para parar)

Ready! You are using Stripe API Version [2024-10-28.acacia]. 
Your webhook signing secret is whsec_1234567890abcdef (^C to quit)
```

**⚠️ COPIE O WEBHOOK SECRET!**

---

### 3️⃣ Atualizar .env com Webhook Secret

Edite o arquivo `.env` e adicione o secret que apareceu no terminal:

```env
# Stripe Configuration
STRIPE_SECRET_KEY=sk_test_51RlBmU...
STRIPE_PUBLISHABLE_KEY=pk_test_51RlBmU...
STRIPE_WEBHOOK_SECRET=whsec_1234567890abcdef  # 👈 COLE AQUI
STRIPE_PRODUCT_MONTHLY=prod_SgzMGAVP7sdb7s
STRIPE_PRODUCT_YEARLY=prod_Sj9inVJYHvyr0M
```

---

### 4️⃣ Iniciar API Spring Boot (Máquina Local)

Em **outro terminal** na sua máquina local, se preferir rodar a API localmente via Gradle:

```bash
# Build do projeto
./gradlew build

# Iniciar API
./gradlew bootRun
```

**Saída esperada:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v3.5.6)

2025-10-05 14:30:00.000  INFO --- [main] Application started on port 8800
```

---

### 5️⃣ Verificar Tudo Funcionando

#### Testar API (Health Check)
```powershell
curl http://localhost:8800/api/actuator/health
```

#### Testar Webhook do Stripe
Em outro terminal:
```powershell
stripe trigger checkout.session.completed
```

Você deve ver no terminal do Stripe:
```
[200] POST http://localhost:8800/api/webhooks/stripe
```

E nos logs da API:
```
INFO --- Processing Stripe webhook: checkout.session.completed
```

---

## 🎯 Comandos Rápidos

### Docker (Servidor)
```bash
# Status dos containers
docker compose ps

# Ver logs em tempo real
docker compose logs -f

# Parar containers
docker compose stop

# Reiniciar containers
docker compose restart

# Parar e remover containers
docker compose down
```

### Stripe CLI (Local)
```bash
# Login no Stripe
stripe login

# Iniciar listener (script wrapper)
./scripts/start-stripe.sh

# Iniciar listener (manual)
stripe listen --forward-to localhost:8800/api/webhooks/stripe

# Testar eventos
stripe trigger checkout.session.completed
stripe trigger invoice.payment_succeeded
stripe trigger customer.subscription.deleted

# Listar produtos
stripe products list

# Listar preços
stripe prices list
```

### Gradle (Local)
```bash
# Build
./gradlew build

# Run
./gradlew bootRun

# Testes
./gradlew test

# Clean + Build
./gradlew clean build

# Ver dependências
./gradlew dependencies
```

---

## 🐛 Troubleshooting

### "Docker não está rodando"
```powershell
# Inicie o Docker Desktop manualmente e aguarde alguns segundos
```

### "Stripe CLI não encontrado"
```powershell
# Instale usando Scoop (recomendado)
iwr -useb get.scoop.sh | iex
scoop bucket add stripe https://github.com/stripe/scoop-stripe-cli.git
scoop install stripe

# Ou baixe de: https://github.com/stripe/stripe-cli/releases/latest
```

### "Não autenticado no Stripe"
```powershell
stripe login
# Abrirá o navegador para autenticação
```

### "Webhook 404 Not Found"
- ✅ Verifique se a API está rodando em `localhost:8800`
- ✅ Verifique se o endpoint é `/api/webhooks/stripe`
- ✅ Verifique os logs da API para ver se recebeu a requisição

### "PostgreSQL Connection Refused"
```powershell
# Verifique se containers estão rodando
docker compose ps

# Se não estiverem, inicie
docker compose up -d

# Verifique logs do PostgreSQL
docker compose logs postgres
```

### "Porta 8800 já em uso"
```powershell
# Descobrir processo usando a porta
netstat -ano | findstr :8800

# Matar processo (substitua <PID> pelo número)
taskkill /PID <PID> /F
```

---

## 📚 Documentação Adicional

- **Docker:** `03-DOCKER.md` - Guia completo do Docker
- **Stripe:** `04-STRIPE.md` - Guia completo do Stripe CLI
- **Credenciais:** `11-CREDENCIAIS.md` - Todas senhas e acessos
- **Arquitetura:** `17-ARQUITETURA-HIBRIDA.md` - Products + Prices (híbrido)
- **Status:** `STATUS-GERAL.md` - Progresso consolidado do projeto

---

## 🎯 Workflow Típico de Desenvolvimento

### Início do Dia
1. ✅ Iniciar ambiente: `./scripts/run-make.sh start`
2. ✅ Iniciar Stripe: `stripe listen --forward-to localhost:8800/api/webhooks/stripe` (terminal separado)
3. ✅ Começar a codificar! (app já está rodando via wrapper)

### Durante o Desenvolvimento
- 💻 Editar código
- 🔄 API reinicia automaticamente (Spring DevTools)
- 🧪 Testar webhooks: `stripe trigger <evento>`
- 📊 Ver logs em tempo real

### Final do Dia
1. ✅ Parar API: `Ctrl+C` no terminal
2. ✅ Parar Stripe: `Ctrl+C` no terminal
3. ✅ Parar Docker: `docker compose stop` (opcional - pode deixar rodando)

---

## ⚡ Dicas de Produtividade

### Use Terminal com Abas
```
Tab 1: Git / Comandos gerais
Tab 2: stripe listen (webhooks)
Tab 3: docker compose logs -f (logs)
```

### Alias Úteis (PowerShell Profile)
```powershell
# Adicione ao arquivo: $PROFILE

function dcu { docker compose up -d }
function dcd { docker compose down }
function dcl { docker compose logs -f }
function grun { .\gradlew bootRun }
function gtest { .\gradlew test }
```

---

**Última Atualização:** 2025-10-05  
**Versão:** 2.0 (Scripts separados: Docker no servidor, Stripe local)
