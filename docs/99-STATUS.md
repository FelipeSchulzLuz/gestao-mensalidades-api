# 📊 Status do Projeto - Sistema de Gestão de Mensalidades

**Data de Atualização:** 2025-10-05  
**Versão:** 1.1.0  
**Progresso Geral:** 14.3% (2/14 fases completas)

---

## 🎯 Visão Geral

Sistema de gestão de mensalidades e assinaturas integrado com Stripe, desenvolvido em Java 21 + Spring Boot 3.5.6 seguindo Clean Architecture.

---

## 📈 Progresso por Fase

```
✅ Fase 1: Setup do Projeto              ████████████████████ 100%
✅ Fase 2: Banco de Dados                ████████████████████ 100%
🔜 Fase 3: Autenticação                  ░░░░░░░░░░░░░░░░░░░░   0%
🔜 Fase 4: Integração Stripe Base        ░░░░░░░░░░░░░░░░░░░░   0%
   Fase 5: Módulo Auth (/auth)           ░░░░░░░░░░░░░░░░░░░░   0%
   Fase 6: Módulo Usuários               ░░░░░░░░░░░░░░░░░░░░   0%
⭐ Fase 7: Módulo Planos (CRÍTICO)       ░░░░░░░░░░░░░░░░░░░░   0%
   Fase 8: Checkout & Assinaturas        ░░░░░░░░░░░░░░░░░░░░   0%
   Fase 9: Portal do Cliente             ░░░░░░░░░░░░░░░░░░░░   0%
⭐ Fase 10: Webhooks (CRÍTICO)           ░░░░░░░░░░░░░░░░░░░░   0%
   Fase 11: Tratamento de Erros          ░░░░░░░░░░░░░░░░░░░░   0%
   Fase 12: Testes                       ░░░░░░░░░░░░░░░░░░░░   0%
🚧 Fase 13: Documentação                 ██████░░░░░░░░░░░░░░  30%
   Fase 14: Produção                     ░░░░░░░░░░░░░░░░░░░░   0%
```

**Legenda:**
- ✅ Completo
- 🔜 Próximo
- ⭐ Crítico para MVP
- 🚧 Em andamento

---

## ✅ Fase 1: Setup do Projeto (100%)

### Completado
- ✅ Projeto Spring Boot criado (Java 21)
- ✅ Gradle configurado com todas dependências
- ✅ Clean Architecture implementada
- ✅ application.yml com profiles (dev, prod)
- ✅ .env.example criado
- ✅ .gitignore configurado
- ✅ README.md inicial

### Dependências Principais
- Spring Boot 3.5.6 (Web, Data JPA, Security, Validation)
- PostgreSQL 15
- Lombok
- Stripe Java SDK 30.0.0
- JWT (jjwt 0.12.5)
- Spring Boot Test + Mockito

### Estrutura de Pastas
```
src/main/java/br/com/gestaomensalidades/gestao_mensalidades_api/
├── domain/
│   ├── entities/        ✅ User, Plan, Subscription
│   └── enums/           ✅ BillingInterval, SubscriptionStatus
├── application/
│   └── services/        🔜 AuthService, PlanService, etc
├── infrastructure/
│   ├── persistence/     ✅ Repositories
│   ├── security/        🔜 SecurityConfig, JWT
│   └── stripe/          🔜 Integrações Stripe
└── presentation/
    ├── controllers/     🔜 REST Controllers
    ├── dto/             🔜 Request/Response DTOs
    └── mappers/         🔜 Entity ↔ DTO
```

---

## ✅ Fase 2: Banco de Dados e Modelagem (100%)

### Entidades Criadas

#### User
- ✅ Campos: id, name, email, password, stripeCustomerId
- ✅ Auditoria: createdAt, updatedAt
- ✅ Flags: isActive, role (USER/ADMIN)
- ✅ Índices: email (unique), stripeCustomerId (unique)

#### Plan
- ✅ Campos: id, name, description, price, currency, interval
- ✅ Stripe: stripePriceId, stripeProductId
- ✅ Controle: isActive, isFeatured, displayOrder
- ✅ Features: Campo para recursos do plano
- ✅ Índices: stripePriceId (unique)

#### Subscription
- ✅ Campos: id, userId, planId, stripeSubscriptionId, status
- ✅ Períodos: currentPeriodStart, currentPeriodEnd
- ✅ Controle: cancelAtPeriodEnd, canceledAt, trialEnd
- ✅ Índices: stripeSubscriptionId (unique), userId, status

### Enums
- ✅ `BillingInterval`: MONTH, YEAR
- ✅ `SubscriptionStatus`: ACTIVE, PAST_DUE, CANCELED, INCOMPLETE, INCOMPLETE_EXPIRED, TRIALING, UNPAID

### Repositories
- ✅ `UserRepository`: 6 queries customizadas
- ✅ `PlanRepository`: 5 queries + 3 pendentes para admin
- ✅ `SubscriptionRepository`: 6 queries incluindo cálculo de MRR

### Infraestrutura
- ✅ Docker Compose (PostgreSQL, pgAdmin, Redis)
- ✅ Portas não convencionais (54320, 8800, 63790)
- ✅ Scripts de inicialização (init.sql)
- ✅ HikariCP configurado

---

## 🔜 Fase 3: Autenticação e Segurança (0%)

### A Fazer
- [ ] SecurityConfig com Spring Security
- [ ] JwtTokenProvider (geração/validação)
- [ ] JwtAuthenticationFilter
- [ ] BCryptPasswordEncoder
- [ ] CustomUserDetailsService
- [ ] Configuração de CORS
- [ ] Definir rotas públicas vs protegidas

---

## 🔜 Fase 4: Integração Stripe Base (0%)

### A Fazer
- [ ] StripeConfig (inicializar SDK)
- [ ] StripeCustomerService
- [ ] StripeProductService
- [ ] StripePriceService
- [ ] Tratamento de exceções Stripe
- [ ] Logging de operações Stripe

---

## ⭐ Fase 7: Módulo de Planos - CRÍTICO (0%)

### Decisão Arquitetural
**✅ Planos gerenciados via Banco + Admin (NÃO .env)**

**Razões:**
1. Admin pode criar/editar/desativar sem deploy
2. Sincronização bidirecional com Stripe
3. Testes A/B e promoções dinâmicas
4. Métricas em tempo real (MRR, ARR)
5. Histórico e auditoria

**Consulte:** `05-PLANOS.md` para detalhes completos

### A Fazer

#### DTOs
- [ ] `PlanResponseDTO` (público)
- [ ] `CreatePlanRequestDTO` (admin)
- [ ] `UpdatePlanRequestDTO` (admin)
- [ ] `PlanMetricsDTO` (admin)

#### Services
- [ ] `PlanService` (público)
  - getAllActivePlans(), getPlanById()
- [ ] `PlanAdminService` (admin)
  - CRUD completo de planos
  - Sincronização com Stripe
  - Métricas (MRR, ARR, assinantes)
- [ ] `StripePlanSyncService`
  - Webhooks: price.created, price.updated, product.updated

#### Controllers
- [ ] `PlanController` (público)
  - GET /api/plans
  - GET /api/plans/{id}
- [ ] `PlanAdminController` (admin - requer ROLE_ADMIN)
  - 11 endpoints de gerenciamento
  - Métricas e relatórios

#### Repositório
- [ ] Adicionar queries de ordenação
- [ ] Adicionar queries de sincronização
- [ ] Queries de métricas

---

## ⭐ Fase 10: Webhooks - CRÍTICO (0%)

### A Fazer

#### Eventos de Assinatura
- [ ] checkout.session.completed
- [ ] invoice.payment_succeeded
- [ ] invoice.payment_failed
- [ ] customer.subscription.deleted
- [ ] customer.subscription.updated

#### Eventos de Sincronização de Planos
- [ ] price.created
- [ ] price.updated
- [ ] product.created
- [ ] product.updated
- [ ] product.deleted

#### Implementação
- [ ] StripeWebhookService (validação de assinatura)
- [ ] WebhookController (POST /api/webhooks/stripe)
- [ ] Idempotência (ProcessedWebhookEvent)
- [ ] Logging estruturado
- [ ] Testes com Stripe CLI

---

## 🚧 Fase 13: Documentação (30%)

### Completado
- ✅ README.md inicial
- ✅ GUIDE_DEV.md
- ✅ CHECKLIST_DESENVOLVIMENTO.md (atualizado)
- ✅ docs/05-PLANOS.md (novo)
- ✅ docs/04-STRIPE.md (guia completo)
- ✅ docs/03-DOCKER.md
- ✅ PORTAS_DOCKER.md
- ✅ CREDENCIAIS.md (protegido)
- ✅ .env.example
- ✅ start-dev.ps1 (script PowerShell)

### A Fazer
- [ ] Swagger/OpenAPI configurado
- [ ] JavaDoc em classes públicas
- [ ] Diagramas de fluxo
- [ ] Guia de contribuição
- [ ] Guia de deploy

---

## 📋 Arquivos de Documentação

| Arquivo | Propósito | Status |
|---------|-----------|--------|
| `README.md` | Introdução e setup básico | ✅ Completo |
| `GUIDE_DEV.md` | Guia de desenvolvimento original | ✅ Completo |
| `CHECKLIST_DESENVOLVIMENTO.md` | Checklist detalhado das 14 fases | ✅ Atualizado |
| `docs/99-STATUS.md` | Este arquivo - status atual | ✅ Atualizado |
| `docs/05-PLANOS.md` | Estratégia de gerenciamento de planos | ✅ Completo |
| `docs/04-STRIPE.md` | Guia Stripe CLI + webhooks | ✅ Completo |
| `docs/03-DOCKER.md` | Guia Docker completo | ✅ Completo |
| `PORTAS_DOCKER.md` | Referência de portas | ✅ Completo |
| `.env.example` | Template de variáveis | ✅ Completo |
| `CREDENCIAIS.md` | Credenciais (protegido) | ✅ Completo |
| `start-dev.ps1` | Script de inicialização | ✅ Completo |

---

## 🛠️ Ambiente de Desenvolvimento

### Configuração Atual
- **Java:** 21 (LTS)
- **Spring Boot:** 3.5.6
- **Gradle:** Groovy DSL
- **Banco:** PostgreSQL 15 (Docker, porta 54320)
- **Cache:** Redis 7 (Docker, porta 63790)
- **pgAdmin:** Web UI (porta 8800)
- **API:** Porta 8800
- **Network:** 192.168.1.12

### Variáveis de Ambiente
- ✅ DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
- ✅ JWT_SECRET (512 bits)
- ✅ STRIPE_SECRET_KEY, STRIPE_PUBLISHABLE_KEY
- ✅ STRIPE_PRODUCT_MONTHLY, STRIPE_PRODUCT_YEARLY
- 🔜 STRIPE_WEBHOOK_SECRET (após executar `stripe listen`)
- ✅ FRONTEND_URL, CORS_ALLOWED_ORIGINS

### Docker
- ✅ docker-compose.yml configurado
- ✅ Portas não convencionais
- ✅ Volumes persistentes
- ✅ Scripts de inicialização
- ✅ Documentação completa (docs/03-DOCKER.md)

### Stripe
- ✅ Conta test mode configurada
- ✅ Produtos criados (monthly, yearly)
- ✅ Chaves API configuradas
- ✅ Stripe CLI instalável (guia completo)
- 🔜 Webhook secret (obter via `stripe listen`)

---

## 📊 Métricas do Código

### Estrutura
- **Entidades de Domínio:** 3 (User, Plan, Subscription)
- **Enums:** 2 (BillingInterval, SubscriptionStatus)
- **Repositories:** 3 com 17+ queries customizadas
- **Services:** 0 (a fazer)
- **Controllers:** 0 (a fazer)
- **DTOs:** 0 (a fazer)

### Linhas de Código (Estimativa)
- **Produção:** ~500 linhas
- **Testes:** 0 linhas
- **Documentação:** ~3.000 linhas

### Coverage
- **Alvo:** 80%+
- **Atual:** 0% (testes não iniciados)

---

## 🚀 Próximos Passos Recomendados

### Imediatos (Esta Sprint)
1. **Fase 3:** Implementar Spring Security + JWT
2. **Fase 4:** Configurar Stripe SDK e serviços base
3. **Fase 5:** Criar módulo de autenticação (/auth)

### Críticos para MVP
4. **Fase 7:** Implementar gerenciamento completo de planos
   - Admin: CRUD + sincronização com Stripe
   - Público: Listagem ordenada de planos ativos
5. **Fase 10:** Implementar webhooks do Stripe
   - Assinaturas (checkout, invoice, subscription)
   - Sincronização de planos (price.*, product.*)

### Pós-MVP
6. Fase 8: Checkout e assinaturas
7. Fase 9: Customer Portal
8. Fase 11-12: Erros e testes
9. Fase 14: Configurações de produção

---

## 🔧 Comandos Úteis

### Iniciar Ambiente
```powershell
# Iniciar Docker + Stripe CLI + API (quando pronta)
.\start-dev.ps1

# Apenas Docker
.\start-dev.ps1 -OnlyDocker

# Ver ajuda
.\start-dev.ps1 -Help
```

### Docker
```bash
# Iniciar serviços
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar serviços
docker-compose down
```

### Stripe CLI
```bash
# Login
stripe login

# Webhooks para localhost
stripe listen --forward-to localhost:8800/api/webhooks/stripe

# Webhooks para rede
stripe listen --forward-to http://192.168.1.12:8800/api/webhooks/stripe

# Testar evento
stripe trigger checkout.session.completed
```

### Gradle
```bash
# Build
./gradlew build

# Run
./gradlew bootRun

# Testes
./gradlew test

# Coverage
./gradlew jacocoTestReport
```

---

## 📚 Recursos Úteis

### Documentação Interna
- [05-PLANOS.md](./05-PLANOS.md) - Estratégia completa de planos
- [04-STRIPE.md](./04-STRIPE.md) - Guia Stripe CLI
- [03-DOCKER.md](./03-DOCKER.md) - Setup Docker
- [CREDENCIAIS.md](./CREDENCIAIS.md) - Acesso aos serviços

### Documentação Externa
- [Stripe Billing](https://docs.stripe.com/billing)
- [Stripe Webhooks](https://docs.stripe.com/webhooks)
- [Spring Security](https://spring.io/guides/gs/securing-web/)
- [Spring Data JPA](https://spring.io/guides/gs/accessing-data-jpa/)

---

## 🎯 Decisões Arquiteturais Importantes

### 1. Gerenciamento de Planos
**Decisão:** Banco de dados + Admin (NÃO .env)  
**Razão:** Flexibilidade, sincronização, métricas, testes A/B  
**Documento:** docs/05-PLANOS.md

### 2. Clean Architecture
**Decisão:** Separação estrita em camadas (domain, application, infrastructure, presentation)  
**Razão:** Testabilidade, manutenibilidade, desacoplamento

### 3. Sincronização Bidirecional Stripe
**Decisão:** Sistema → Stripe (ao criar) + Stripe → Sistema (webhooks)  
**Razão:** Dados sempre sincronizados, suporte a mudanças no Dashboard Stripe

### 4. Soft Delete
**Decisão:** Flag `isActive` em vez de DELETE físico  
**Razão:** Histórico, auditoria, rollback fácil

### 5. Um Plano Por Usuário
**Decisão:** Usuário pode ter apenas 1 assinatura ativa  
**Razão:** Simplificação do MVP, menos edge cases

---

## 🐛 Issues Conhecidas

Nenhuma issue conhecida no momento.

---

## ✨ Melhorias Futuras

### Features Planejadas (Pós-MVP)
- [ ] Sistema de notificações por email
- [ ] Cupons de desconto (Stripe Coupons)
- [ ] Período de teste gratuito
- [ ] Upgrade/Downgrade de planos
- [ ] Dashboard admin com métricas
- [ ] Relatórios exportáveis (CSV/Excel)
- [ ] Logs de auditoria
- [ ] RBAC (Role-Based Access Control)
- [ ] Cache com Redis
- [ ] Rate limiting

### Otimizações
- [ ] Paginação em listagens grandes
- [ ] Cache de planos ativos
- [ ] Compressão de responses
- [ ] CDN para assets

---

**Última Atualização:** 2025-10-05  
**Próxima Revisão:** Após conclusão da Fase 3  
**Mantido por:** Equipe de Desenvolvimento
