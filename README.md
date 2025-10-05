# 🎯 Gestão de Mensalidades API

> **API REST para gerenciamento de assinaturas e mensalidades com integração Stripe**

Sistema completo para gestão de assinaturas recorrentes desenvolvido com Java 21 + Spring Boot 3.5.6. Perfeito para ONGs, academias, escolas e pequenos negócios que precisam automatizar cobranças recorrentes.

---

## 🚀 Quick Start

**Nunca rodou o projeto?** Comece aqui:

👉 **[docs/01-INICIO-RAPIDO.md](docs/01-INICIO-RAPIDO.md)** - Guia completo de configuração e execução

---

## 📚 Documentação

| Documento | Descrição |
|-----------|-----------|
| **[CHECKLIST_DESENVOLVIMENTO.md](CHECKLIST_DESENVOLVIMENTO.md)** | ✅ Checklist completo das 14 fases de desenvolvimento |
| **[docs/01-INICIO-RAPIDO.md](docs/01-INICIO-RAPIDO.md)** | 🏁 Guia de início rápido (Docker + Stripe + API) |
| **[docs/02-ARQUITETURA.md](docs/02-ARQUITETURA.md)** | 🏗️ Arquitetura Clean Architecture e padrões |
| **[docs/03-DOCKER.md](docs/03-DOCKER.md)** | 🐳 Configuração Docker (PostgreSQL, pgAdmin, Redis) |
| **[docs/04-STRIPE.md](docs/04-STRIPE.md)** | 💳 Integração com Stripe e webhooks |
| **[docs/05-PLANOS.md](docs/05-PLANOS.md)** | 📦 Arquitetura de gerenciamento de planos |
| **[docs/99-STATUS.md](docs/99-STATUS.md)** | 📊 Status atual do projeto e progresso |

---

## 🛠️ Stack Tecnológica

**Backend:** Java 21, Spring Boot 3.5.6, Spring Security (JWT), Gradle  
**Database:** PostgreSQL 15, Redis 7  
**Pagamentos:** Stripe Java SDK 30.0.0  
**Arquitetura:** Clean Architecture (Domain, Application, Infrastructure, Presentation)

---

## ⚡ Executar em 3 Passos

### 1️⃣ Iniciar Docker (PostgreSQL + Redis)

```powershell
.\scripts\start-dev.ps1
```

### 2️⃣ Iniciar Stripe CLI (webhooks locais)

```powershell
.\scripts\start-stripe.ps1
```

### 3️⃣ Rodar a API

```powershell
./gradlew bootRun
```

**Acesso:**
- API: http://localhost:8800/api
- pgAdmin: http://localhost:8800 (admin@gestao.local)
- PostgreSQL: localhost:54320

---

## 📁 Estrutura do Projeto

```
gestao-mensalidades-api/
├── docs/                          # 📚 Documentação completa
│   ├── 01-INICIO-RAPIDO.md       # Guia de início rápido
│   ├── 02-ARQUITETURA.md         # Arquitetura do sistema
│   ├── 03-DOCKER.md              # Guia Docker
│   ├── 04-STRIPE.md              # Integração Stripe
│   ├── 05-PLANOS.md              # Gerenciamento de planos
│   └── 99-STATUS.md              # Status do projeto
├── scripts/                       # 🔧 Scripts utilitários
│   ├── start-dev.ps1             # Inicia Docker (PostgreSQL, Redis, pgAdmin)
│   └── start-stripe.ps1          # Inicia Stripe CLI (webhooks)
├── src/                           # 💻 Código fonte
│   └── main/java/.../
│       ├── domain/               # Entidades e regras de domínio
│       ├── application/          # Casos de uso e lógica de negócio
│       ├── infrastructure/       # Implementações (JPA, Stripe, Security)
│       └── presentation/         # Controllers REST
├── CHECKLIST_DESENVOLVIMENTO.md   # ✅ Checklist de desenvolvimento
├── .env.example                   # 📝 Template de variáveis de ambiente
└── README.md                      # 📖 Este arquivo

### 3. Executar a Aplicação

```bash
# Linux/Mac
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

---

## ✨ Funcionalidades

- ✅ Autenticação JWT segura
- ✅ Gerenciamento de usuários e perfis
- ✅ Catálogo de planos (admin panel + sincronização Stripe)
- ✅ Checkout integrado com Stripe
- ✅ Portal do cliente para autogerenciamento
- ✅ Webhooks para sincronização automática
- ✅ Suporte a múltiplos métodos de pagamento
- ✅ Métricas de planos (MRR/ARR)

---

## 🧪 Testes e Build

```powershell
# Executar testes
./gradlew test

# Build do projeto
./gradlew clean build

# JAR gerado em:
# build/libs/gestao-mensalidades-api-0.0.1-SNAPSHOT.jar
```

---

## 📊 Status do Projeto

**Progresso Geral:** 14.3% (2/14 fases completas)

✅ **Fase 1:** Setup do Projeto (100%)  
✅ **Fase 2:** Modelagem do Banco (100%)  
⏳ **Fase 3:** Autenticação e Segurança (próxima)

**Detalhes:** Veja [docs/99-STATUS.md](docs/99-STATUS.md) para progresso completo.

---

## 🔧 Scripts Úteis

**Iniciar ambiente Docker (PostgreSQL + Redis + pgAdmin):**
```powershell
.\scripts\start-dev.ps1
```

**Iniciar Stripe CLI (webhooks locais):**
```powershell
.\scripts\start-stripe.ps1
```

**Simular eventos Stripe:**
```powershell
stripe trigger checkout.session.completed
stripe trigger invoice.payment_succeeded
```

---

## 🤝 Contribuindo

Para contribuir com o projeto, consulte o **[CHECKLIST_DESENVOLVIMENTO.md](CHECKLIST_DESENVOLVIMENTO.md)** para ver as tarefas pendentes e em andamento.

---

## 📝 Licença

Este projeto está sob a licença MIT.

---

**Status**: 🚧 Em Desenvolvimento Ativo  
**Última atualização**: 2025-01-05
