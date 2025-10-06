# 🎯 Gestão de Mensalidades API

> **Plataforma de comércio híbrida: assinaturas recorrentes + e-commerce integrados com Stripe**

Sistema completo desenvolvido com Java 21 + Spring Boot 3.5.6 seguindo Clean Architecture. Suporta **assinaturas recorrentes** (SaaS, planos mensais) e **e-commerce** (vendas únicas) na mesma plataforma.

---

## 🚀 Quick Start

**Escolha seu perfil:**

### 👶 Novo no Projeto
👉 **[docs/LEIA-ME.md](docs/LEIA-ME.md)** - Porta de entrada única com fluxos guiados

### � Vou Desenvolver Agora
1. **[docs/18-MUDANCA-ARQUITETURAL-RESUMO.md](docs/18-MUDANCA-ARQUITETURAL-RESUMO.md)** (10 min - OBRIGATÓRIO)
2. **[docs/GUIA-RAPIDO.md](docs/GUIA-RAPIDO.md)** (comandos e referências)

### 🔧 Setup do Ambiente
�👉 **[docs/01-INICIO-RAPIDO.md](docs/01-INICIO-RAPIDO.md)** - Docker + Stripe + API

---

## 📚 Documentação Essencial

| Documento | Propósito | Quando Ler |
|-----------|-----------|------------|
| **[docs/LEIA-ME.md](docs/LEIA-ME.md)** | 🚪 Porta de entrada única | Primeiro acesso |
| **[docs/18-MUDANCA-ARQUITETURAL-RESUMO.md](docs/18-MUDANCA-ARQUITETURAL-RESUMO.md)** | 🚨 Leia antes de codificar | Antes de qualquer código |
| **[docs/17-ARQUITETURA-HIBRIDA.md](docs/17-ARQUITETURA-HIBRIDA.md)** | 🏗️ Arquitetura completa | Referência detalhada |
| **[docs/GUIA-RAPIDO.md](docs/GUIA-RAPIDO.md)** | ⚡ Comandos e padrões | Consulta diária |
| **[docs/STATUS-GERAL.md](docs/STATUS-GERAL.md)** | � Status consolidado | Ver progresso |
| **[docs/09-CHECKLIST-DESENVOLVIMENTO.md](docs/09-CHECKLIST-DESENVOLVIMENTO.md)** | ✅ Tarefas pendentes | Planejar trabalho |

**📁 Documentação completa:** 23 documentos organizados em [docs/](docs/)

---

## 🏗️ Arquitetura (Atualizada Out/2025)

### Modelo Híbrido: Products + Prices

```
Organization (commerce_mode: SUBSCRIPTIONS | ECOMMERCE | HYBRID)
    │
    ├── Products (type: SUBSCRIPTION | ONE_TIME)
    │       └── Prices (type: RECURRING | ONE_TIME)
    │
    ├── Users
    │
    ├── Subscriptions (assinaturas recorrentes)
    │
    └── Orders (compras únicas)
```

**Checkout Inteligente:**
- Product `SUBSCRIPTION` → Stripe mode `subscription` → Tabela `subscriptions`
- Product `ONE_TIME` → Stripe mode `payment` → Tabela `orders`

📖 **Detalhes:** [17-ARQUITETURA-HIBRIDA.md](docs/17-ARQUITETURA-HIBRIDA.md)

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
