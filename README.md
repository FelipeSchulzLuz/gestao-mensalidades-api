# 🎯 Gestão de Mensalidades API

> **Plataforma de comércio híbrida: assinaturas recorrentes + e-commerce integrados com Stripe**

Sistema completo desenvolvido com **Java 21 + Spring Boot 3.5.6** seguindo **Clean Architecture**. Suporta **assinaturas recorrentes** (SaaS, planos mensais) e **e-commerce** (vendas únicas) na mesma plataforma.

---

## 🚀 Quick Start (3 minutos)

### 1️⃣ Clone e Configure

```bash
git clone https://github.com/FelipeSchulzLuz/gestao-mensalidades-api.git
cd gestao-mensalidades-api
cp .env.example .env
# Edite .env com suas credenciais
```

### 2️⃣ Inicie o Ambiente

```bash
# Docker (PostgreSQL + Redis + pgAdmin)
docker-compose up -d

# Aplicar migrations
./gradlew flywayMigrate

# Iniciar API
./gradlew bootRun
```

### 3️⃣ Acesse

- **API:** http://localhost:8800/api
- **Health Check:** http://localhost:8800/actuator/health
- **pgAdmin:** http://localhost:5050 (ver `docs/CREDENCIAIS.md`)

---

## 📚 Documentação

### 🚪 Início Rápido

- **[docs/INDEX.md](docs/INDEX.md)** - 📖 Índice completo de toda documentação
- **[docs/INICIO-RAPIDO.md](docs/INICIO-RAPIDO.md)** - ⚡ Setup detalhado do ambiente
- **[docs/PROJECT-STATUS.md](docs/PROJECT-STATUS.md)** - � Status do projeto (80% backend completo)

### 🏗️ Arquitetura

- **[docs/ARQUITETURA.md](docs/ARQUITETURA.md)** - Clean Architecture (4 camadas)
- **[docs/ARQUITETURA-HIBRIDA.md](docs/ARQUITETURA-HIBRIDA.md)** - Modelo Products + Prices (Stripe)

### 🛠️ Desenvolvimento

- **[docs/CHECKLIST-DESENVOLVIMENTO.md](docs/CHECKLIST-DESENVOLVIMENTO.md)** - ✅ Checklist completo
- **[docs/JAKARTA-VALIDATIONS.md](docs/JAKARTA-VALIDATIONS.md)** - 📝 Guia de validações
- **[docs/DOCKER.md](docs/DOCKER.md)** - 🐳 Docker e containers
- **[docs/STRIPE.md](docs/STRIPE.md)** - 💳 Integração Stripe

**📁 Total:** 14 documentos organizados em [docs/](docs/)

---

## 🏗️ Arquitetura

### Clean Architecture (4 Camadas)

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │  ← Controllers REST
├─────────────────────────────────────────┤
│         Application Layer               │  ← Services, DTOs, Use Cases
├─────────────────────────────────────────┤
│         Domain Layer                    │  ← Entities, Business Rules
├─────────────────────────────────────────┤
│         Infrastructure Layer            │  ← JPA, Stripe, Security
└─────────────────────────────────────────┘
```

### Modelo Híbrido: Products + Prices

```
Organization (commerce_mode: SUBSCRIPTIONS | ECOMMERCE | HYBRID)
    │
    ├── Products (type: SUBSCRIPTION | ONE_TIME)
    │       └── Prices (type: RECURRING | ONE_TIME)
    │
    ├── Users
    ├── Subscriptions (assinaturas recorrentes)
    └── Orders (compras únicas)
```

**Checkout Inteligente:**
- Product `SUBSCRIPTION` → Stripe mode `subscription` → Tabela `subscriptions`
- Product `ONE_TIME` → Stripe mode `payment` → Tabela `orders`

📖 **Detalhes:** [docs/ARQUITETURA-HIBRIDA.md](docs/ARQUITETURA-HIBRIDA.md)

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologias |
|--------|-------------|
| **Backend** | Java 21, Spring Boot 3.5.6, Spring Security (JWT + Firebase) |
| **Build** | Gradle 8.5 |
| **Database** | PostgreSQL 15, Flyway (migrations), Redis 7 (cache) |
| **Pagamentos** | Stripe Java SDK 30.0.0 |
| **Validações** | Jakarta Bean Validation 3.0 |
| **Containerização** | Docker, Docker Compose |

---

## 📊 Status do Projeto

```
BACKEND CORE: ████████████████████░ 80%

✅ Domain Layer         100% ██████████
✅ Infrastructure       100% ██████████  
✅ Application Services 100% ██████████
✅ Application DTOs     100% ██████████
⏳ Presentation Layer     0% ░░░░░░░░░░
⏳ Testing                0% ░░░░░░░░░░
```

**Progresso detalhado:** [docs/PROJECT-STATUS.md](docs/PROJECT-STATUS.md)

---

## 📁 Estrutura do Projeto

```
gestao-mensalidades-api/
├── docs/                          # 📚 Documentação completa (14 arquivos)
│   ├── INDEX.md                  # Índice principal
│   ├── INICIO-RAPIDO.md          # Guia de início rápido
│   ├── ARQUITETURA.md            # Clean Architecture
│   ├── ARQUITETURA-HIBRIDA.md    # Products + Prices (modelo atual)
│   ├── DOCKER.md                 # Guia Docker
│   ├── STRIPE.md                 # Integração Stripe
│   ├── JAKARTA-VALIDATIONS.md    # Validações Jakarta Bean
│   └── PROJECT-STATUS.md         # Status do projeto (80% backend)
│
├── src/main/java/.../
│   ├── domain/                   # � Entidades (Organization, Product, Price, Order)
│   ├── application/              
│   │   ├── dto/                  # � DTOs com validações Jakarta (100%)
│   │   ├── mappers/              # 🟢 Conversores Entity ↔ DTO (100%)
│   │   └── services/             # 🟢 Services com multi-tenancy (100%)
│   ├── infrastructure/
│   │   ├── persistence/          # 🟢 Repositories JPA (100%)
│   │   └── security/             # 🟢 JWT + Firebase (100%)
│   └── presentation/
│       └── controllers/          # 🔴 REST Controllers (0% - próximo)
│
├── database/
│   └── migrations/               # 🟢 Flyway (V1, V2, V3 aplicadas)
│
├── .env.example                  # Template de variáveis
├── docker-compose.yml            # PostgreSQL + Redis + pgAdmin
└── README.md                     # Este arquivo
```

---

## ✨ Funcionalidades Implementadas

### ✅ Completo (80% Backend Core)
- ✅ **Domain Layer** - Entities (Organization, Product, Price, Order)
- ✅ **Infrastructure** - Repositories JPA, Security (JWT + Firebase)
- ✅ **Application Services** - ProductService, PriceService, OrderService
- ✅ **DTOs & Mappers** - Jakarta validations (30+ annotations)
- ✅ **Multi-tenancy** - Enforcement em todos os services
- ✅ **Migrations** - Flyway (3 migrations aplicadas)

### 🔄 Em Desenvolvimento (Próximo)
- ⏳ **REST Controllers** - ProductController, PriceController, OrderController
- ⏳ **Unit Tests** - Services, DTOs, Mappers
- ⏳ **Integration Tests** - API endpoints

---

## 🧪 Build e Testes

```bash
# Build completo
./gradlew clean build

# Pular testes (desenvolvimento)
./gradlew build -x test

# Executar apenas testes
./gradlew test

# JAR gerado em: build/libs/gestao-mensalidades-api-0.0.1-SNAPSHOT.jar
```

---

## � Docker

```bash
# Iniciar ambiente completo
docker-compose up -d

# Ver logs
docker-compose logs -f api

# Parar tudo
docker-compose down

# Reset completo (CUIDADO: apaga dados!)
docker-compose down -v
```

**Serviços:**
- PostgreSQL: `localhost:5432` (user: `gestao_user`, ver `.env`)
- Redis: `localhost:6379`
- pgAdmin: `http://localhost:5050` (ver `docs/CREDENCIAIS.md`)

---

## � Stripe (Desenvolvimento Local)

```bash
# 1. Login Stripe
stripe login

# 2. Iniciar webhooks
stripe listen --forward-to localhost:8800/api/webhooks/stripe

# 3. Simular eventos (em outro terminal)
stripe trigger checkout.session.completed
stripe trigger invoice.payment_succeeded
```

**Documentação:** [docs/STRIPE.md](docs/STRIPE.md)

---

## 🤝 Contribuindo

1. Leia [docs/INDEX.md](docs/INDEX.md) - Índice da documentação
2. Consulte [docs/CHECKLIST-DESENVOLVIMENTO.md](docs/CHECKLIST-DESENVOLVIMENTO.md) - Tarefas pendentes
3. Siga [docs/JAKARTA-VALIDATIONS.md](docs/JAKARTA-VALIDATIONS.md) - Padrões de validação

---

## 📝 Licença

Este projeto está sob a licença MIT.

---

**Repositório:** [github.com/FelipeSchulzLuz/gestao-mensalidades-api](https://github.com/FelipeSchulzLuz/gestao-mensalidades-api)  
**Status:** 🚧 Em Desenvolvimento Ativo (80% backend completo)  
**Última atualização:** 2025-10-06
