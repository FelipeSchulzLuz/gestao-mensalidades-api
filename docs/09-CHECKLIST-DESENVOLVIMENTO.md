# 📋 Checklist Completo - Sistema de Gestão de Mensalidades

## 📊 Progresso Geral

```
Fase 1: Setup do Projeto           ████████████████████ 100% ✅
Fase 2: Banco de Dados              ████████████████████ 100% ✅
Fase 3: Autenticação & Security     ████████████████████ 100% ✅
Fase 4: Integração Stripe Base      ░░░░░░░░░░░░░░░░░░░░   0% 🔜
Fase 5: Módulo Auth (/auth)         ░░░░░░░░░░░░░░░░░░░░   0% 
Fase 6: Módulo Usuários             ░░░░░░░░░░░░░░░░░░░░   0% 
Fase 7: Módulo Planos (CRÍTICO)     ░░░░░░░░░░░░░░░░░░░░   0% 
Fase 8: Checkout & Assinaturas      ░░░░░░░░░░░░░░░░░░░░   0% 
Fase 9: Portal do Cliente           ░░░░░░░░░░░░░░░░░░░░   0% 
Fase 10: Webhooks (CRÍTICO)         ░░░░░░░░░░░░░░░░░░░░   0% 
Fase 11: Tratamento de Erros        ░░░░░░░░░░░░░░░░░░░░   0% 
Fase 12: Testes                     ░░░░░░░░░░░░░░░░░░░░   0% 
Fase 13: Documentação               ███████░░░░░░░░░░░░░  35% 🚧
Fase 14: Produção                   ░░░░░░░░░░░░░░░░░░░░   0% 

Progresso Total: 21.4% (3/14 fases)
```

## 🎯 Próximos Passos Recomendados

1. ✅ **Fase 3**: ~~Implementar Spring Security + JWT~~ **COMPLETO!**
2. **Fase 4**: Configurar Stripe SDK e serviços base ← **PRÓXIMO**
3. **Fase 7**: **CRÍTICO** - Implementar gerenciamento de planos (admin + público + sincronização)
4. **Fase 10**: **CRÍTICO** - Implementar webhooks (assinaturas + sincronização de planos)

## 📖 Documentação de Referência

- `docs/17-ARQUITETURA-HIBRIDA.md` - Arquitetura Products + Prices (atual)
- `docs/04-STRIPE.md` - Guia completo do Stripe CLI e webhooks
- `docs/03-DOCKER.md` - Instruções para rodar PostgreSQL, pgAdmin, Redis
- `CREDENCIAIS.md` - Todas credenciais e acessos (protegido no .gitignore)
- `docs/99-STATUS.md` - Status detalhado do projeto

---

## ✅ FASE 2: BANCO DE DADOS E MODELAGEM

### 2.1 Configuração do PostgreSQL
- [x] Instalar PostgreSQL localmente ou configurar Docker ✅
- [x] Criar banco de dados de desenvolvimento ✅
- [x] Configurar connection pool no application.yml ✅
- [x] Habilitar Hibernate DDL auto (validate em prod) ✅
- [x] Criar script de inicialização do banco (database/init.sql) ✅
- [x] Criar docker-compose.yml com PostgreSQL, pgAdmin e Redis ✅
- [x] Documentar setup do Docker (docs/03-DOCKER.md) ✅
- [x] Configurar portas não convencionais para evitar conflitos ✅
- [ ] Configurar migrations com Flyway ou Liquibase (Opcional - MVP usa DDL auto) Visão Geral
Este checklist detalha todos os passos necessários para desenvolver o sistema de gestão de mensalidades seguindo as melhores práticas do Stripe e padrões enterprise de grandes empresas.

---

## ✅ FASE 1: SETUP DO PROJETO E CONFIGURAÇÃO INICIAL

### 1.1 Configuração do Projeto Spring Boot
- [x] Criar projeto Spring Boot usando Spring Initializr (Java 21 ✅)
- [x] Configurar Maven/Gradle com todas as dependências ✅
  ```xml
  <!-- Dependências essenciais -->
  ✅ spring-boot-starter-web
  ✅ spring-boot-starter-data-jpa
  ✅ spring-boot-starter-security
  ✅ spring-boot-starter-validation
  ✅ postgresql (runtime)
  ✅ lombok
  ✅ stripe-java (v30.0.0)
  ✅ jjwt (v0.12.5 - api, impl, jackson)
  ✅ spring-boot-starter-test
  ✅ mockito-core (incluído no spring-boot-starter-test)
  ```
- [x] Configurar estrutura de pastas seguindo Clean Architecture: ✅
  ```
  src/main/java/br/com/gestaomensalidades/gestao_mensalidades_api/
  ✅ domain/          # Entidades de domínio
     ✅ entities/     # User, Plan, Subscription
     ✅ enums/        # BillingInterval, SubscriptionStatus
  ✅ application/     # Casos de uso e serviços
     ✅ services/     # AuthService, UserService, etc
  ✅ infrastructure/  # Implementações técnicas
     ✅ persistence/  # Repositórios JPA
     ✅ security/     # Configuração Spring Security
     ✅ stripe/       # Integrações Stripe
  ✅ presentation/    # Controllers REST
     ✅ controllers/  # AuthController, UserController, etc
     ✅ dto/          # DTOs de request/response
     ✅ mappers/      # Conversores Entity ↔ DTO
  ```
- [x] Criar arquivo `application.yml` com profiles (dev, prod) ✅
- [x] Configurar variáveis de ambiente para secrets ✅

### 1.2 Configuração de Segurança
- [x] Adicionar dependência do Spring Security ✅
- [x] Criar arquivo `.env.example` com variáveis necessárias ✅
- [x] Configurar `.gitignore` para proteger secrets ✅
- [x] Documentar processo de configuração de variáveis de ambiente ✅

### 1.3 Configuração do Git
- [x] Inicializar repositório Git ✅
- [x] Criar `.gitignore` apropriado para Java/Spring Boot ✅
- [ ] Configurar branch strategy (main, develop, feature/*)
- [x] Adicionar README.md inicial ✅

---

## ✅ FASE 2: BANCO DE DADOS E MODELAGEM

### 2.1 Configuração do PostgreSQL
- [ ] Instalar PostgreSQL localmente ou configurar Docker
- [ ] Criar banco de dados de desenvolvimento
- [ ] Configurar connection pool no application.yml
- [ ] Habilitar Hibernate DDL auto (validate em prod)
- [ ] Configurar migrations com Flyway ou Liquibase

### 2.2 Modelagem de Entidades

#### Entity: User
- [x] Criar entidade `User` com anotações JPA ✅
  ```java
  ✅ id (Long)
  ✅ name (String, @NotBlank, @Size)
  ✅ email (String, @Email, unique)
  ✅ password (String, hasheado)
  ✅ stripeCustomerId (String, unique)
  ✅ createdAt, updatedAt (Timestamp)
  ✅ isActive (Boolean)
  ✅ role (String) - USER/ADMIN
  ```
- [x] Adicionar índices em email e stripeCustomerId ✅
- [x] Implementar Lombok (@Getter, @Setter, @Builder, @Entity, @Table) ✅

#### Entity: Plan
- [x] Criar entidade `Plan` ✅
  ```java
  ✅ id (Long)
  ✅ name (String, @NotBlank, @Size)
  ✅ description (String, @Size)
  ✅ price (BigDecimal, @DecimalMin)
  ✅ currency (String, default "BRL")
  ✅ interval (Enum: BillingInterval)
  ✅ stripePriceId (String, unique)
  ✅ stripeProductId (String) - Produto Stripe associado
  ✅ isActive (Boolean) - Controla visibilidade no frontend
  ✅ isFeatured (Boolean) - Plano destaque "Mais Popular"
  ✅ displayOrder (Integer) - Ordem de exibição (0 = primeiro)
  ✅ features (String/JSONB) - Lista de recursos do plano
  ✅ createdAt, updatedAt
  ```
- [x] Criar Enum `BillingInterval` (MONTH, YEAR) ✅
- [x] Adicionar validações de preço (@DecimalMin, @NotNull) ✅
- [ ] **IMPORTANTE**: Adicionar campo `stripeProductId` (necessário para sincronização)
- [ ] **IMPORTANTE**: Adicionar campo `features` (JSONB ou TEXT) para recursos do plano
- [ ] Adicionar constraints: `CHECK (displayOrder >= 0)`, `CHECK (price >= 0)`

#### Entity: Subscription
- [x] Criar entidade `Subscription` ✅
  ```java
  ✅ id (Long)
  ✅ userId (FK -> User, @ManyToOne)
  ✅ planId (FK -> Plan, @ManyToOne)
  ✅ stripeSubscriptionId (String, unique)
  ✅ status (Enum: SubscriptionStatus)
  ✅ currentPeriodStart (LocalDateTime)
  ✅ currentPeriodEnd (LocalDateTime)
  ✅ cancelAtPeriodEnd (Boolean)
  ✅ canceledAt (LocalDateTime)
  ✅ trialEnd (LocalDateTime) - Período de teste
  ✅ createdAt, updatedAt
  ```
- [x] Criar Enum `SubscriptionStatus` (7 status + métodos úteis) ✅
- [x] Configurar relacionamentos JPA (@ManyToOne, FetchType.LAZY) ✅
- [x] Adicionar índices em stripeSubscriptionId, userId, status ✅

### 2.3.1 Estratégia de Gerenciamento de Planos
> **📌 IMPORTANTE - DECISÃO ARQUITETURAL**
> 
> **Planos gerenciados via Banco de Dados + Admin Panel (NÃO hardcoded no .env)**
> 
> Razões:
> - ✅ Admin pode criar/editar/desativar planos SEM deploy
> - ✅ Sincronização bidirecional com Stripe (webhooks)
> - ✅ Testes A/B, promoções, planos sazonais
> - ✅ Métricas em tempo real (MRR, ARR, conversão)
> - ✅ Histórico e auditoria de mudanças
> 
> O que vai no .env:
> - Apenas IDs de produtos Stripe (referência, opcional)
> - `STRIPE_PRODUCT_MONTHLY=prod_SgzMGAVP7sdb7s`
> - `STRIPE_PRODUCT_YEARLY=prod_Sj9inVJYHvyr0M`
> 
> O que vai no Banco:
> - Todos detalhes dos planos (nome, preço, descrição, features)
> - Flags de controle (isActive, isFeatured, displayOrder)
> - Sincronização (stripePriceId, stripeProductId)
> 
> 📖 Consulte `docs/17-ARQUITETURA-HIBRIDA.md` para arquitetura Products + Prices

### 2.3 Repositories
- [x] Criar `UserRepository extends JpaRepository` ✅
  - ✅ `Optional<User> findByEmail(String email)`
  - ✅ `Optional<User> findByStripeCustomerId(String customerId)`
  - ✅ `boolean existsByEmail(String email)`
  - ✅ `Optional<User> findActiveUserByEmail(String email)`
  - ✅ `List<User> findByIsActive(Boolean isActive)`
  - ✅ `long countActiveUsers()`
- [x] Criar `PlanRepository` ✅
  - ✅ `List<Plan> findByIsActiveTrue()`
  - ✅ `Optional<Plan> findByStripePriceId(String priceId)`
  - ✅ `List<Plan> findByInterval(BillingInterval interval)`
  - ✅ `List<Plan> findFeaturedPlans()`
  - ✅ `long countActivePlans()`
  - [ ] **ADICIONAR**: `List<Plan> findByIsActiveTrueOrderByDisplayOrderAsc()` - Essencial para listagem ordenada
  - [ ] **ADICIONAR**: `Optional<Plan> findByStripeProductId(String productId)` - Para sincronização via webhook
  - [ ] **ADICIONAR**: `@Query("SELECT COUNT(s) FROM Subscription s WHERE s.plan.id = :planId AND s.status = 'ACTIVE'")` 
    `long countActiveSubscriptionsByPlanId(@Param("planId") Long planId)` - Métricas admin
- [x] Criar `SubscriptionRepository` ✅
  - ✅ `Optional<Subscription> findByUserIdAndStatus(Long userId, Status)`
  - ✅ `Optional<Subscription> findByStripeSubscriptionId(String subId)`
  - ✅ `Optional<Subscription> findActiveSubscriptionByUserId(Long userId)`
  - ✅ `boolean hasActiveSubscription(Long userId)`
  - ✅ `BigDecimal calculateMonthlyRecurringRevenue()` - Cálculo de MRR
  - ✅ `List<Subscription> findExpiringSoon(LocalDateTime date)`

---

## ✅ FASE 3: SISTEMA DE AUTENTICAÇÃO E SEGURANÇA

### 🎉 FASE COMPLETA - 2025-10-05

**Commit:** 14 commits criados (Conventional Commits pattern)  
**Arquivos:** 19 arquivos criados (domain, application, infrastructure, presentation)  
**Build:** ✅ Compilação successful com Java 21

### 3.1 Spring Security Configuration ✅
- [x] Criar classe `SecurityConfig` (moderna - sem WebSecurityConfigurerAdapter) ✅
- [x] Configurar CORS dinamicamente (application.yml) ✅
- [x] Desabilitar CSRF (stateless JWT) ✅
- [x] Configurar filtros de autenticação JWT ✅
- [x] Definir rotas públicas vs protegidas ✅
  ```java
  Públicas: /api/auth/**, /api/plans (GET), /api/webhooks/stripe
  Protegidas: /api/** (todas as outras)
  Admin: /api/admin/** (ROLE_ADMIN)
  ```

### 3.2 JWT Implementation ✅
- [x] Criar `JwtTokenProvider` para geração e validação de tokens ✅
  - `generateToken(User user)` ✅
  - `validateToken(String token)` ✅
  - `getUserIdFromToken(String token)` ✅
  - `getEmailFromToken(String token)` ✅
- [x] Configurar secret key via .env (512-bit HS512) ✅
- [x] Definir tempo de expiração do token (24h) ✅
- [x] Criar filtro `JwtAuthenticationFilter` (OncePerRequestFilter) ✅
- [x] Usar JJWT 0.12.5 (parser(), verifyWith() API) ✅

### 3.3 Password Hashing ✅
- [x] Configurar BCryptPasswordEncoder bean ✅
- [x] Implementar hash de senha no registro ✅
- [x] Força mínima da senha: 8 caracteres (@Size validation) ✅
- [x] Validação via Jakarta Bean Validation ✅

### 3.4 User Details Service ✅
- [x] Implementar `CustomUserDetailsService implements UserDetailsService` ✅
- [x] Carregar usuário por email ou ID ✅
- [x] Mapear UserRole enum → authorities (ROLE_USER, ROLE_ADMIN) ✅

### 3.5 Domain Layer ✅
- [x] `UserRole` enum (USER, ADMIN) com getAuthority() ✅
- [x] `InvalidCredentialsException` (401) ✅
- [x] `UserAlreadyExistsException` (409) ✅
- [x] `UserNotFoundException` (404) ✅
- [x] User entity refatorada (String role → UserRole enum) ✅

### 3.6 Application Layer ✅
- [x] `UserRepositoryPort` (hexagonal architecture) ✅
- [x] `RegisterRequestDTO` (name, email, password + validation) ✅
- [x] `LoginRequestDTO` (email, password) ✅
- [x] `AuthResponseDTO` (accessToken, tokenType, expiresIn) ✅
- [x] `RegisterUserUseCase` (email check, BCrypt, JWT) ✅
- [x] `LoginUseCase` (AuthenticationManager, JWT) ✅

### 3.7 Presentation Layer ✅
- [x] `AuthController` (POST /api/auth/register, /api/auth/login) ✅
- [x] `GlobalExceptionHandler` (@RestControllerAdvice) ✅
  - UserAlreadyExistsException → 409 ✅
  - InvalidCredentialsException → 401 ✅
  - UserNotFoundException → 404 ✅
  - MethodArgumentNotValidException → 400 (field errors) ✅
  - Exception → 500 ✅

### 3.8 Infrastructure ✅
- [x] `JwtAuthenticationEntryPoint` (JSON 401 responses) ✅
- [x] `UserRepositoryAdapter` (implements UserRepositoryPort) ✅
- [x] Gradle auto-download Java 21 (foojay-resolver-convention) ✅
- [x] Docker network conflict resolvido (172.28.0.0/16) ✅

### 3.9 Testes Pendentes ⏳
- [ ] Testar POST /api/auth/register (201 Created)
- [ ] Testar POST /api/auth/login (200 OK + JWT)
- [ ] Testar rota protegida com token (200 OK)
- [ ] Testar rota protegida sem token (401)
- [ ] Testar rota admin sem ROLE_ADMIN (403)

**Nota:** Docker precisa estar rodando em 192.168.1.12 para testes

---

## ✅ FASE 4: INTEGRAÇÃO COM STRIPE - CONFIGURAÇÃO BASE

### 4.1 SDK do Stripe
- [ ] Adicionar dependência `stripe-java` no pom.xml/build.gradle
- [ ] Criar classe `StripeConfig` para inicializar SDK
  ```java
  @Configuration
  public class StripeConfig {
      @PostConstruct
      void init() {
          Stripe.apiKey = stripeSecretKey;
      }
  }
  ```
- [ ] Configurar variáveis de ambiente:
  - `STRIPE_SECRET_KEY` (sk_test_... para dev)
  - `STRIPE_PUBLISHABLE_KEY` (pk_test_...)
  - `STRIPE_WEBHOOK_SECRET` (whsec_...)

### 4.2 Stripe Service Base
- [ ] Criar `StripeCustomerService`
  - `String createCustomer(String name, String email)`
  - `Customer getCustomer(String customerId)`
- [ ] Criar `StripeProductService`
  - `Product createProduct(String name, String description)`
  - `Price createPrice(Product, amount, currency, interval)`
- [ ] Implementar tratamento de exceções do Stripe
- [ ] Adicionar logging para todas operações Stripe

### 4.3 Configuração no Dashboard do Stripe
- [ ] Criar conta no Stripe (modo test)
- [ ] Configurar informações da empresa
- [ ] Criar produtos e prices no Dashboard
- [ ] Anotar IDs dos prices criados
- [ ] Configurar Customer Portal settings:
  - [ ] Habilitar atualização de métodos de pagamento
  - [ ] Configurar política de cancelamento
  - [ ] Personalizar branding (logo, cores)
  - [ ] Definir URL de retorno padrão

---

## ✅ FASE 5: MÓDULO DE AUTENTICAÇÃO (/auth)

### ⚠️ NOTA: Esta fase foi consolidada na Fase 3

A Fase 3 já implementou todo o módulo de autenticação seguindo Clean Architecture.

### 5.1 DTOs ✅
- [x] Criar `RegisterRequestDTO` ✅
  ```java
  - name (@NotBlank)
  - email (@Email)
  - password (@Size(min=8))
  ```
- [x] Criar `LoginRequestDTO` ✅
  ```java
  - email (@Email)
  - password (@NotBlank)
  ```
- [x] Criar `AuthResponseDTO` ✅
  ```java
  - accessToken
  - tokenType (Bearer)
  - expiresIn (24h)
  ```

### 5.2 Use Cases Layer ✅ (Clean Architecture)
- [x] Criar `RegisterUserUseCase` ✅
  - `execute(RegisterRequestDTO)` → AuthResponseDTO
    - ✅ Validar se email já existe (UserRepositoryPort)
    - ✅ Hash da senha (BCryptPasswordEncoder)
    - ⏳ Criar Customer no Stripe (Fase 4)
    - ✅ Salvar User (com role USER por padrão)
    - ✅ Gerar JWT token (JwtTokenProvider)
  - [x] Criar `LoginUseCase` ✅
  - `execute(LoginRequestDTO)` → AuthResponseDTO
    - ✅ Validar credenciais (AuthenticationManager)
    - ✅ Gerar JWT token

### 5.3 Controller ✅
- [x] Criar `AuthController` ✅
  - `POST /api/auth/register`
    - Input: RegisterRequestDTO
    - Output: 201 Created + AuthResponseDTO
    - ✅ Validações: email único, senha forte
    - ✅ Exception: UserAlreadyExistsException (409)
  - `POST /api/auth/login`
    - Input: LoginRequestDTO
    - Output: 200 OK + AuthResponseDTO
    - ✅ Validações: credenciais corretas
    - ✅ Exception: InvalidCredentialsException (401)

### 5.4 Testes ⏳
- [ ] Testar registro com dados válidos
- [ ] Testar registro com email duplicado
- [ ] Testar login com credenciais válidas
- [ ] Testar login com credenciais inválidas
- [ ] Verificar criação de Customer no Stripe

---

## ✅ FASE 6: MÓDULO DE USUÁRIOS (/users)

### 6.1 DTOs
- [ ] Criar `UserResponseDTO` (sem senha)
  ```java
  - id, name, email
  - stripeCustomerId
  - createdAt
  ```

### 6.2 Service Layer
- [ ] Criar `UserService`
  - `UserResponseDTO getCurrentUser(Authentication auth)`
  - `UserResponseDTO updateUser(Long id, UpdateUserDTO)`

### 6.3 Controller
- [ ] Criar `UserController`
  - `GET /users/me` (@AuthenticationPrincipal)
    - Output: 200 OK + UserResponseDTO
    - Requer autenticação JWT

### 6.4 Testes
- [ ] Testar acesso sem autenticação (401)
- [ ] Testar acesso com token válido
- [ ] Testar acesso com token expirado

---

## ✅ FASE 7: MÓDULO DE PLANOS (/plans)

### 7.1 DTOs Públicos (Frontend)
- [ ] Criar `PlanResponseDTO`
  ```java
  - id, name, description
  - price, currency, interval
  - stripePriceId, stripeProductId
  - isActive, isFeatured, displayOrder
  - features (List<String>)
  - createdAt, updatedAt
  ```

### 7.2 DTOs Admin
- [ ] Criar `CreatePlanRequestDTO`
  ```java
  - name (@NotBlank, @Size(max=100))
  - description (@Size(max=500))
  - price (@NotNull, @DecimalMin("0.01"))
  - currency (@NotNull, @Size(3))
  - interval (@NotNull - MONTH/YEAR)
  - features (List<String>)
  - isActive (Boolean, default true)
  - isFeatured (Boolean, default false)
  - displayOrder (Integer, default 0)
  ```
- [ ] Criar `UpdatePlanRequestDTO`
  ```java
  - name, description (opcionais)
  - price (requer criação de novo price no Stripe)
  - isActive, isFeatured, displayOrder
  - features
  ```
- [ ] Criar `PlanMetricsDTO`
  ```java
  - planId, planName
  - activeSubscriptions (count)
  - mrr (Monthly Recurring Revenue)
  - arr (Annual Recurring Revenue)
  - conversionRate
  ```

### 7.3 Service Layer - PlanService (Público)
- [ ] Criar `PlanService`
  - `List<PlanResponseDTO> getAllActivePlans()`
    - Retorna apenas planos com isActive=true
    - Ordenados por displayOrder ASC
  - `PlanResponseDTO getPlanById(Long id)`
    - Retorna 404 se não encontrado ou inativo

### 7.4 Service Layer - PlanAdminService
- [ ] Criar `PlanAdminService`
  - `List<PlanResponseDTO> getAllPlans(boolean includeInactive)`
    - Lista TODOS os planos (ativos e inativos)
  - `PlanResponseDTO createPlan(CreatePlanRequestDTO request)`
    - Validar dados de entrada
    - Criar produto no Stripe (StripeProductService)
    - Criar preço no Stripe (StripePriceService)
    - Salvar no banco com stripePriceId e stripeProductId
    - Retornar plano criado
  - `PlanResponseDTO updatePlan(Long id, UpdatePlanRequestDTO request)`
    - Buscar plano existente
    - Atualizar campos permitidos
    - Se mudou preço: criar novo price no Stripe
    - Atualizar produto no Stripe se necessário
    - Salvar alterações no banco
  - `void activatePlan(Long id)`
    - Define isActive=true
  - `void deactivatePlan(Long id)`
    - Define isActive=false (soft delete)
    - Assinaturas existentes não são afetadas
  - `void deletePlan(Long id)`
    - Soft delete (isActive=false)
    - Validar se não há assinaturas ativas
  - `PlanResponseDTO syncPlanWithStripe(Long id)`
    - Força sincronização com Stripe
    - Busca dados atualizados do produto/preço
    - Atualiza no banco
  - `void syncAllPlansWithStripe()`
    - Sincroniza todos os planos com Stripe
  - `List<UserResponseDTO> getPlanSubscribers(Long planId)`
    - Lista todos assinantes do plano
  - `PlanMetricsDTO getPlanMetrics(Long planId)`
    - Retorna métricas do plano (assinantes, MRR, ARR)
  - `PlanMetricsDTO getAllPlansMetrics()`
    - Retorna métricas gerais de todos os planos

### 7.5 Service Layer - StripePlanSyncService
- [ ] Criar `StripePlanSyncService`
  - `void syncPriceFromStripe(String stripePriceId)`
    - Busca preço no Stripe
    - Atualiza plano no banco se existir
    - Cria plano se não existir
  - `void syncProductFromStripe(String stripeProductId)`
    - Busca produto no Stripe
    - Atualiza planos relacionados
  - `void handlePriceCreatedWebhook(Event event)`
    - Processa webhook price.created
  - `void handlePriceUpdatedWebhook(Event event)`
    - Processa webhook price.updated
  - `void handleProductUpdatedWebhook(Event event)`
    - Processa webhook product.updated

### 7.6 Controller - PlanController (Público)
- [ ] Criar `PlanController`
  - `GET /api/plans` (Público)
    - Output: 200 OK + List<PlanResponseDTO>
    - Retorna apenas planos ativos, ordenados
  - `GET /api/plans/{id}` (Público)
    - Output: 200 OK + PlanResponseDTO
    - Retorna 404 se inativo ou não encontrado

### 7.7 Controller - PlanAdminController
- [ ] Criar `PlanAdminController`
  - `GET /api/admin/plans` (ADMIN)
    - Query param: includeInactive (boolean)
    - Output: 200 OK + List<PlanResponseDTO>
  - `GET /api/admin/plans/{id}` (ADMIN)
    - Output: 200 OK + PlanResponseDTO
  - `POST /api/admin/plans` (ADMIN)
    - Input: CreatePlanRequestDTO
    - Output: 201 Created + PlanResponseDTO
    - Cria plano e sincroniza com Stripe
  - `PUT /api/admin/plans/{id}` (ADMIN)
    - Input: UpdatePlanRequestDTO
    - Output: 200 OK + PlanResponseDTO
  - `PATCH /api/admin/plans/{id}/activate` (ADMIN)
    - Output: 200 OK + PlanResponseDTO
  - `PATCH /api/admin/plans/{id}/deactivate` (ADMIN)
    - Output: 200 OK + PlanResponseDTO
  - `DELETE /api/admin/plans/{id}` (ADMIN)
    - Output: 204 No Content
    - Soft delete (isActive=false)
  - `POST /api/admin/plans/{id}/sync` (ADMIN)
    - Output: 200 OK + PlanResponseDTO
    - Força sincronização com Stripe
  - `POST /api/admin/plans/sync-all` (ADMIN)
    - Output: 200 OK + { message, syncedCount }
  - `GET /api/admin/plans/{id}/subscribers` (ADMIN)
    - Output: 200 OK + List<UserResponseDTO>
  - `GET /api/admin/plans/{id}/metrics` (ADMIN)
    - Output: 200 OK + PlanMetricsDTO
  - `GET /api/admin/plans/metrics` (ADMIN)
    - Output: 200 OK + PlanMetricsDTO (agregado)

### 7.8 Validações e Regras de Negócio
- [ ] Validar unicidade de stripePriceId
- [ ] Validar se produto existe no Stripe antes de criar plano
- [ ] Impedir exclusão de planos com assinaturas ativas
- [ ] Validar preço mínimo (ex: R$ 0.50)
- [ ] Validar moeda (BRL, USD, EUR)
- [ ] Validar intervalo (MONTH, YEAR)
- [ ] Validar displayOrder (não negativos)

### 7.9 Seed Inicial de Planos
- [ ] Criar `data.sql` ou migration com planos iniciais
  ```sql
  INSERT INTO plans (name, description, price, currency, interval, 
                     stripe_price_id, stripe_product_id, is_active, 
                     is_featured, display_order)
  VALUES 
    ('Plano Mensal', 'Acesso completo mensal', 99.90, 'BRL', 'MONTH', 
     'price_xxxxx', 'prod_SgzMGAVP7sdb7s', true, false, 1),
    ('Plano Anual', 'Acesso anual com desconto', 990.00, 'BRL', 'YEAR', 
     'price_yyyyy', 'prod_Sj9inVJYHvyr0M', true, true, 0);
  ```
- [ ] Testar seed em ambiente de desenvolvimento

### 7.10 Repositório - Queries Adicionais
- [ ] Adicionar queries ao `PlanRepository`
  - `List<Plan> findByIsActiveTrueOrderByDisplayOrderAsc()`
  - `List<Plan> findByIsFeaturedTrueAndIsActiveTrue()`
  - `Optional<Plan> findByStripeProductId(String productId)`
  - `List<Plan> findByPriceBetween(BigDecimal min, BigDecimal max)`
  - `@Query("SELECT COUNT(s) FROM Subscription s WHERE s.plan.id = :planId AND s.status = 'ACTIVE'")`
    `long countActiveSubscriptionsByPlanId(@Param("planId") Long planId)`

### 7.11 Admin Role Implementation
- [ ] Adicionar campo `role` em User (ROLE_USER, ROLE_ADMIN) ✅ (JÁ FEITO)
- [ ] Criar enum `UserRole` (USER, ADMIN)
- [ ] Proteger endpoints `/api/admin/**` com `@PreAuthorize("hasRole('ADMIN')")`
- [ ] Criar usuário admin inicial (seed ou migration)
  ```sql
  INSERT INTO users (name, email, password, role, is_active)
  VALUES ('Admin', 'admin@example.com', '{bcrypt_hash}', 'ADMIN', true);
  ```

### 7.2 Testes
- [ ] Testar listagem pública (apenas ativos, ordenados)
- [ ] Testar criação de plano (cria no Stripe + banco)
- [ ] Testar atualização de plano (sincroniza com Stripe)
- [ ] Testar ativação/desativação
- [ ] Testar sincronização com Stripe
- [ ] Testar métricas (MRR, ARR, assinantes)
- [ ] Testar proteção de endpoints admin (403 para não-admin)
- [ ] Testar webhooks de sincronização (price.updated, product.updated)

---

## ✅ FASE 8: MÓDULO DE ASSINATURAS - CHECKOUT

### 8.1 DTOs
- [ ] Criar `CreateCheckoutSessionRequestDTO`
  ```java
  - priceId (stripePriceId do Plan)
  ```
- [ ] Criar `CheckoutSessionResponseDTO`
  ```java
  - url (URL do Stripe Checkout)
  - sessionId
  ```
- [ ] Criar `SubscriptionResponseDTO`
  ```java
  - id, status
  - currentPeriodStart, currentPeriodEnd
  - plan (nested PlanResponseDTO)
  - cancelAtPeriodEnd
  ```

### 8.2 Service Layer - StripeCheckoutService
- [ ] Criar método `createCheckoutSession(User, String priceId)`
  ```java
  1. Buscar Plan por stripePriceId
  2. Validar se plano existe e está ativo
  3. Criar CheckoutSession no Stripe:
     - customer: user.stripeCustomerId
     - line_items: [{ price: priceId, quantity: 1 }]
     - mode: "subscription"
     - success_url: "{frontend}/success?session_id={CHECKOUT_SESSION_ID}"
     - cancel_url: "{frontend}/cancel"
  4. Retornar session.url
  ```

### 8.3 Service Layer - SubscriptionService
- [ ] Criar `SubscriptionService`
  - `SubscriptionResponseDTO getCurrentSubscription(User)`
    - Buscar subscription ativa do usuário
    - Popular dados do plano
  - `SubscriptionResponseDTO createSubscription(User, Plan, stripeSubId)`
    - Chamado pelo webhook
  - `void updateSubscription(stripeSubId, status, periodEnd)`
    - Chamado pelo webhook
  - `void cancelSubscription(User)`
    - Marca para cancelar no final do período
  - `void upgradePlan(User, Long newPlanId)`
    - Cria novo checkout para upgrade
  - `void downgradePlan(User, Long newPlanId)`
    - Agenda downgrade para final do período

### 8.4 Controller
- [ ] Criar `SubscriptionController`
  - `POST /api/subscriptions/create-checkout-session` (Auth)
    - Input: CreateCheckoutSessionRequestDTO
    - Output: 200 OK + CheckoutSessionResponseDTO
  - `GET /api/subscriptions/me` (Auth)
    - Output: 200 OK + SubscriptionResponseDTO
    - Retorna 404 se não tiver assinatura ativa
  - `GET /api/subscriptions/history` (Auth)
    - Output: 200 OK + List<SubscriptionHistoryDTO>
    - Histórico de todas assinaturas do usuário

### 8.5 Validações e Regras de Negócio
- [ ] Validar se usuário já tem assinatura ativa antes de criar checkout
- [ ] Permitir apenas um plano ativo por usuário
- [ ] Retornar erro 400 se priceId inválido
- [ ] Validar se plano está ativo antes de criar checkout
- [ ] Impedir checkout se usuário já tem assinatura do mesmo plano
- [ ] Logs detalhados de cada operação

---

## ✅ FASE 9: MÓDULO DE PORTAL DO CLIENTE

### 9.1 DTOs
- [ ] Criar `CustomerPortalResponseDTO`
  ```java
  - url (URL do Stripe Customer Portal)
  ```

### 9.2 Service Layer
- [ ] Criar `StripePortalService`
  - `String createPortalSession(String customerId, String returnUrl)`
    ```java
    BillingPortalSession session = BillingPortalSession.create(
      BillingPortalSessionCreateParams.builder()
        .setCustomer(customerId)
        .setReturnUrl(returnUrl)
        .build()
    );
    return session.getUrl();
    ```

### 9.3 Controller
- [ ] Adicionar ao `SubscriptionController`
  - `POST /subscriptions/manage-portal-session` (Auth)
    - Input: Opcional { returnUrl }
    - Output: 200 OK + CustomerPortalResponseDTO
    - Usar user.stripeCustomerId

### 9.4 Configuração no Stripe Dashboard
- [ ] Configurar Customer Portal:
  - [ ] Habilitar "Cancel subscription"
  - [ ] Habilitar "Update payment methods"
  - [ ] Configurar mensagem de cancelamento
  - [ ] Definir URL de retorno padrão

---

## ✅ FASE 10: SISTEMA DE WEBHOOKS (CRÍTICO)

### 10.1 Webhook Signature Verification
- [ ] Criar `StripeWebhookService`
  - `Event constructEvent(payload, signature, secret)`
    ```java
    // Verificar assinatura ANTES de processar
    try {
      Event event = Webhook.constructEvent(
        payload, 
        signatureHeader, 
        webhookSecret
      );
      return event;
    } catch (SignatureVerificationException e) {
      throw new UnauthorizedException("Invalid signature");
    }
    ```

### 10.2 Event Handlers - Subscription Events
- [ ] Implementar handler para `checkout.session.completed`
  ```java
  1. Extrair session do evento
  2. Buscar User por stripeCustomerId
  3. Extrair subscription ID do session
  4. Buscar Plan por stripePriceId
  5. Criar Subscription no banco:
     - user, plan, stripeSubscriptionId
     - status: ACTIVE
     - currentPeriodEnd extraído do Stripe
  6. Log de sucesso
  ```

- [ ] Implementar handler para `invoice.payment_succeeded`
  ```java
  1. Extrair invoice do evento
  2. Atualizar currentPeriodEnd da Subscription
  3. Garantir status ACTIVE
  4. Log de renovação bem-sucedida
  ```

- [ ] Implementar handler para `invoice.payment_failed`
  ```java
  1. Extrair invoice do evento
  2. Atualizar status para PAST_DUE
  3. (Opcional) Enviar email de notificação
  4. Log de falha de pagamento
  ```

- [ ] Implementar handler para `customer.subscription.deleted`
  ```java
  1. Extrair subscription do evento
  2. Atualizar status para CANCELED
  3. Definir canceledAt
  4. Log de cancelamento
  ```

- [ ] Implementar handler para `customer.subscription.updated`
  ```java
  1. Atualizar dados da subscription (status, período)
  2. Detectar mudanças de plano (upgrade/downgrade)
  3. Atualizar cancelAtPeriodEnd se mudou
  4. Log de atualização
  ```

### 10.2.1 Event Handlers - Plan Sync Events
- [ ] Implementar handler para `price.created`
  ```java
  1. Extrair price do evento
  2. Buscar produto relacionado
  3. Criar plano no banco (se não existir)
  4. Associar stripePriceId e stripeProductId
  5. Definir isActive=false (admin precisa ativar)
  6. Log de sincronização
  ```

- [ ] Implementar handler para `price.updated`
  ```java
  1. Extrair price do evento
  2. Buscar plano por stripePriceId
  3. Atualizar dados (se campos permitidos mudaram)
  4. Log de atualização
  ```

- [ ] Implementar handler para `product.created`
  ```java
  1. Extrair product do evento
  2. Log de novo produto (não cria plano automaticamente)
  ```

- [ ] Implementar handler para `product.updated`
  ```java
  1. Extrair product do evento
  2. Buscar planos com stripeProductId
  3. Atualizar dados dos planos (nome, descrição)
  4. Log de sincronização
  ```

- [ ] Implementar handler para `product.deleted`
  ```java
  1. Extrair product do evento
  2. Buscar planos relacionados
  3. Desativar planos (isActive=false)
  4. Log de desativação
  ```

### 10.3 Controller
- [ ] Criar `WebhookController`
  - `POST /webhooks/stripe` (Público, SEM autenticação JWT)
    ```java
    @PostMapping("/webhooks/stripe")
    public ResponseEntity<Void> handleStripeWebhook(
      @RequestBody String payload,
      @RequestHeader("Stripe-Signature") String signature
    ) {
      Event event = webhookService.constructEvent(payload, signature);
      webhookService.handleEvent(event);
      return ResponseEntity.ok().build();
    }
    ```
- [ ] Desabilitar CSRF para `/webhooks/stripe` no SecurityConfig
- [ ] Configurar para aceitar raw request body (não parsear JSON)

### 10.4 Teste de Webhooks
- [ ] Instalar Stripe CLI: `stripe login`
- [ ] Configurar webhook forwarding: `stripe listen --forward-to localhost:8800/api/webhooks/stripe`
- [ ] Copiar webhook secret (whsec_...) e atualizar .env
- [ ] Testar eventos de assinatura:
  ```bash
  stripe trigger checkout.session.completed
  stripe trigger invoice.payment_succeeded
  stripe trigger invoice.payment_failed
  stripe trigger customer.subscription.deleted
  stripe trigger customer.subscription.updated
  ```
- [ ] Testar eventos de sincronização de planos:
  ```bash
  stripe trigger price.created
  stripe trigger price.updated
  stripe trigger product.updated
  stripe trigger product.deleted
  ```
- [ ] Verificar logs da aplicação para cada evento
- [ ] Validar que dados foram atualizados no banco corretamente
- [ ] Consultar `docs/04-STRIPE.md` para guia completo

### 10.5 Idempotência e Logs
- [ ] Criar tabela `ProcessedWebhookEvent` (id do evento Stripe)
- [ ] Verificar se evento já foi processado antes
- [ ] Adicionar logs estruturados para cada evento
- [ ] Implementar retry logic para falhas temporárias

---

## ✅ FASE 11: TRATAMENTO DE ERROS E VALIDAÇÕES

### 11.1 Exception Handling
- [ ] Criar exception customizadas:
  - `ResourceNotFoundException`
  - `DuplicateResourceException`
  - `StripeServiceException`
  - `UnauthorizedException`
  - `ValidationException`

- [ ] Criar `@ControllerAdvice` global
  ```java
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(e) {
    return new ResponseEntity<>(ErrorResponse, 404);
  }
  // ... outros handlers
  ```

### 11.2 Validações
- [ ] Usar Bean Validation em todos DTOs
  - `@NotBlank`, `@Email`, `@Size`, `@Min`, `@Max`
- [ ] Validar entradas em Services (regras de negócio)
- [ ] Validar respostas do Stripe (null checks)

### 11.3 Error Response DTO
- [ ] Criar `ErrorResponseDTO`
  ```java
  - timestamp
  - status
  - error
  - message
  - path
  ```

---

## ✅ FASE 12: TESTES UNITÁRIOS E DE INTEGRAÇÃO

### 12.1 Testes Unitários (JUnit 5 + Mockito)
- [ ] Testar `AuthService`
  - Mock de UserRepository
  - Mock de StripeCustomerService
  - Verificar lógica de registro e login
- [ ] Testar `PlanService`
  - Mock de PlanRepository
  - Verificar listagem de planos ativos
  - Verificar ordenação por displayOrder
- [ ] Testar `PlanAdminService`
  - Mock de PlanRepository
  - Mock de StripeProductService
  - Mock de StripePriceService
  - Verificar criação de plano (banco + Stripe)
  - Verificar atualização de plano
  - Verificar ativação/desativação
  - Verificar validações (plano com assinaturas ativas)
- [ ] Testar `StripePlanSyncService`
  - Mock de Stripe API
  - Verificar sincronização de preço
  - Verificar sincronização de produto
  - Verificar criação de plano a partir do Stripe
- [ ] Testar `SubscriptionService`
  - Mock de SubscriptionRepository
  - Mock de PlanRepository
  - Verificar criação e atualização
  - Verificar regra de um plano por usuário
- [ ] Testar `StripeWebhookService`
  - Mock de Event
  - Verificar processamento de eventos de assinatura
  - Verificar processamento de eventos de sincronização
  - Verificar idempotência (evento duplicado)

### 12.2 Testes de Integração
- [ ] Configurar TestContainers para PostgreSQL
- [ ] Testar fluxo completo de registro
  - Criar usuário → Verificar Customer no Stripe
- [ ] Testar fluxo completo de checkout
  - Criar checkout → Verificar Session no Stripe
- [ ] Testar fluxo completo de criação de plano (admin)
  - Criar plano → Verificar Product e Price no Stripe
- [ ] Testar sincronização de planos
  - Webhook price.updated → Verificar atualização no banco
- [ ] Testar webhook endpoint com payload real do Stripe
- [ ] Testar proteção de endpoints admin (403 para não-admin)
- [ ] Testar validação de plano inativo no checkout
- [ ] Usar `@SpringBootTest` e `MockMvc`

### 12.3 Coverage
- [ ] Configurar JaCoCo para code coverage
- [ ] Meta: mínimo 80% de cobertura
- [ ] Excluir DTOs e configs do coverage

---

## ✅ FASE 13: DOCUMENTAÇÃO DA API

### 13.1 Swagger/OpenAPI
- [ ] Adicionar dependência `springdoc-openapi-ui`
- [ ] Configurar `@OpenAPIDefinition` na classe principal
  - Título: "API de Gestão de Mensalidades"
  - Descrição: Sistema de assinaturas com Stripe
  - Versão: 1.0
- [ ] Configurar autenticação JWT no Swagger
  - `@SecurityScheme(name = "bearer-jwt", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")`
- [ ] Adicionar anotações nos Controllers:
  - `@Tag(name = "Planos", description = "Endpoints de gerenciamento de planos")`
  - `@Operation(summary = "Listar planos ativos")`
  - `@ApiResponse(responseCode = "200", description = "Planos listados com sucesso")`
  - `@ApiResponse(responseCode = "401", description = "Não autenticado")`
  - `@ApiResponse(responseCode = "403", description = "Acesso negado - Admin apenas")`
  - `@Parameter(description = "ID do plano")`
- [ ] Documentar todos endpoints públicos e protegidos
- [ ] Adicionar exemplos de request/response nos DTOs
- [ ] Acessar Swagger UI em: `http://localhost:8800/swagger-ui.html`

### 13.2 README.md
- [ ] Criar documentação completa:
  - Descrição do projeto
  - Pré-requisitos (Java 21, PostgreSQL 15, Stripe CLI, Docker)
  - Instruções de instalação
  - Variáveis de ambiente necessárias
  - Como rodar localmente com Docker
  - Como usar `./scripts/run-make.sh start` para iniciar ambiente
  - Como configurar webhooks com Stripe CLI
  - Estrutura do projeto (Clean Architecture)
  - Endpoints disponíveis (link para Swagger)
  - Arquitetura de gerenciamento de planos (link para docs/17-ARQUITETURA-HIBRIDA.md)

### 13.3 Documentação Stripe
- [ ] Documentar IDs necessários (prices, products) ✅ (JÁ FEITO - ver CREDENCIAIS.md)
- [ ] Documentar configuração do Customer Portal
- [ ] Criar guia de troubleshooting de webhooks ✅ (JÁ FEITO - ver docs/04-STRIPE.md)
- [ ] Documentar fluxo completo de desenvolvimento local ✅ (JÁ FEITO - ver docs/04-STRIPE.md)
- [ ] Documentar comandos do Stripe CLI ✅ (JÁ FEITO - ver docs/04-STRIPE.md)

### 13.4 Documentação Adicional
- [ ] Atualizar `docs/17-ARQUITETURA-HIBRIDA.md` com implementação completa Products+Prices
- [ ] Criar diagramas de fluxo (opcional)
  - Fluxo de registro → checkout → webhook
  - Fluxo de criação de plano (admin) → Stripe → banco
  - Fluxo de sincronização webhook → banco
- [ ] Documentar decisões arquiteturais importantes
- [ ] Criar guia de contribuição (CONTRIBUTING.md)

---

## ✅ FASE 14: CONFIGURAÇÕES DE PRODUÇÃO

### 14.1 Profiles
- [ ] Criar `application-prod.yml`
  - Desabilitar DDL auto (validate)
  - Configurar pool de conexões
  - Habilitar SSL para banco
  - Configurar logs em arquivo

### 14.2 HTTPS
- [ ] Certificado SSL/TLS válido
- [ ] Configurar server.ssl.* no application.yml
- [ ] Testar webhook com HTTPS

### 14.3 Variáveis de Ambiente
- [ ] Todas secrets como variáveis de ambiente:
  - `DATABASE_URL`
  - `STRIPE_SECRET_KEY` (live)
  - `STRIPE_WEBHOOK_SECRET` (live)
  - `JWT_SECRET`
- [ ] Documentar em `.env.example`

### 14.4 Monitoramento e Logs
- [ ] Configurar Logback para produção
- [ ] Adicionar logging estruturado (JSON)
- [ ] Configurar rotação de logs
- [ ] (Opcional) Integrar com Sentry ou similar

### 14.5 Stripe Live Mode
- [ ] Criar produtos e prices em live mode
- [ ] Configurar webhook endpoint em live mode
- [ ] Testar com cartão real (ou modo test no início)
- [ ] Configurar limites de taxa (rate limiting)

### 14.6 Backup e Recovery
- [ ] Configurar backup automático do PostgreSQL
- [ ] Testar restauração de backup
- [ ] Documentar procedimento de recovery

---

## 🎯 EXTRAS E MELHORIAS (PÓS-MVP)

### Features Adicionais - Planos
- [ ] **Features dos Planos (JSON ou Tabela)**
  - [ ] Criar entidade `PlanFeature` ou usar campo JSONB
  - [ ] Endpoints para gerenciar features por plano
  - [ ] Exibir features na listagem pública
- [ ] **Comparação de Planos**
  - [ ] Endpoint `GET /api/plans/compare?ids=1,2,3`
  - [ ] Retornar tabela comparativa de features
- [ ] **Histórico de Preços**
  - [ ] Tabela `PlanPriceHistory`
  - [ ] Registrar mudanças de preço ao longo do tempo
  - [ ] Endpoint admin para visualizar histórico
- [ ] **Planos Customizados (Enterprise)**
  - [ ] Planos sem preço fixo (negociação)
  - [ ] Campo `isCustom` em Plan
  - [ ] Processo de solicitação via formulário
- [ ] **Testes A/B de Preços**
  - [ ] Sistema para exibir preços diferentes
  - [ ] Tracking de conversão por variante
  - [ ] Dashboard de análise de resultados

### Features Adicionais - Assinaturas
- [ ] Sistema de notificações por email
  - [ ] Email de boas-vindas (após registro)
  - [ ] Email de confirmação de assinatura
  - [ ] Email de renovação (7 dias antes)
  - [ ] Email de falha de pagamento
  - [ ] Email de cancelamento
  - [ ] Email de upgrade/downgrade
- [ ] **Cupons de Desconto**
  - [ ] Integrar com Stripe Coupons
  - [ ] Endpoint `POST /api/coupons/validate`
  - [ ] Aplicar cupom no checkout
  - [ ] Admin: CRUD de cupons
- [ ] **Período de Teste Gratuito**
  - [ ] Configurar `trial_period_days` no checkout
  - [ ] Campo `trialEnd` em Subscription ✅ (JÁ EXISTE)
  - [ ] Email ao final do trial
  - [ ] Conversão trial → paid tracking
- [ ] **Upgrade/Downgrade Proativo**
  - [ ] Endpoint `POST /api/subscriptions/change-plan`
  - [ ] Calcular proration com Stripe
  - [ ] Atualizar assinatura imediatamente ou no final do período
- [ ] **Reativação de Assinatura Cancelada**
  - [ ] Endpoint `POST /api/subscriptions/reactivate`
  - [ ] Validar se ainda está no período ativo

### Features Adicionais - Admin
- [ ] **Dashboard Administrativo Completo**
  - [ ] Métricas gerais (MRR, ARR, churn rate, LTV)
  - [ ] Gráficos de crescimento de receita
  - [ ] Distribuição de assinaturas por plano
  - [ ] Taxa de conversão por plano
  - [ ] Assinaturas criadas/canceladas por período
- [ ] **Gerenciamento de Usuários (Admin)**
  - [ ] Listar todos usuários
  - [ ] Buscar por email, nome, stripeCustomerId
  - [ ] Ver histórico de assinaturas por usuário
  - [ ] Ativar/desativar usuário
  - [ ] Dar/remover role ADMIN
- [ ] **Relatórios Exportáveis**
  - [ ] Exportar lista de assinantes (CSV/Excel)
  - [ ] Relatório de receita mensal/anual
  - [ ] Relatório de churn
  - [ ] Relatório de novos assinantes
- [ ] **Logs de Auditoria**
  - [ ] Tabela `AuditLog`
  - [ ] Registrar todas ações admin
  - [ ] Endpoint para visualizar logs
  - [ ] Filtros por usuário, ação, data
- [ ] **Sistema de Permissões Granular (RBAC)**
  - [ ] Entidade `Role` e `Permission`
  - [ ] Permissões: PLAN_CREATE, PLAN_UPDATE, USER_MANAGE, etc
  - [ ] Verificação com `@PreAuthorize("hasPermission(...)")`

### Otimizações
- [ ] Cache com Redis
  - [ ] Cache de planos ativos
  - [ ] Cache de dados do usuário
- [ ] Rate Limiting
- [ ] Compressão de responses (gzip)
- [ ] Paginação em listagens

### Segurança Adicional
- [ ] Implementar refresh tokens
- [ ] 2FA (Two-Factor Authentication)
- [ ] Logout de todas sessões
- [ ] Detecção de anomalias
- [ ] IP whitelisting para webhooks

---

## 📊 CHECKLIST DE QUALIDADE FINAL

### Code Quality
- [ ] Todos os princípios SOLID aplicados
- [ ] Código segue convenções Java (Checkstyle)
- [ ] Sem warnings do compilador
- [ ] Sem código comentado
- [ ] Documentação JavaDoc em classes públicas
- [ ] Clean Architecture respeitada (domain não depende de infrastructure)
- [ ] DTOs separados de entidades (sem exposição de dados internos)

### Testes
- [ ] 80%+ de cobertura de testes
- [ ] Todos testes passando
- [ ] Testes unitários para services e utils
- [ ] Testes de integração E2E funcionais
- [ ] Webhooks testados com Stripe CLI
- [ ] Testes de segurança (403 para não-admin, 401 para não-autenticado)
- [ ] Testes de validação (campos obrigatórios, formatos)
- [ ] Testes de sincronização de planos

### Segurança
- [ ] Todas senhas hasheadas com BCrypt
- [ ] JWT tokens seguros (512 bits) ✅ (JÁ CONFIGURADO)
- [ ] Webhook signatures validadas ANTES de processar
- [ ] Inputs validados em todos endpoints
- [ ] HTTPS em produção
- [ ] Secrets não commitadas no Git ✅ (JÁ CONFIGURADO - .gitignore)
- [ ] CORS configurado corretamente ✅ (JÁ CONFIGURADO - .env)
- [ ] Endpoints admin protegidos com @PreAuthorize("hasRole('ADMIN')")
- [ ] Rate limiting configurado (opcional)

### Documentação
- [ ] README completo com instruções claras
- [ ] Swagger UI acessível em `/swagger-ui.html`
- [ ] Variáveis de ambiente documentadas ✅ (JÁ FEITO - .env.example)
- [ ] Guia de desenvolvimento local ✅ (JÁ FEITO - docs/04-STRIPE.md)
- [ ] Guia Docker ✅ (JÁ FEITO - docs/03-DOCKER.md)
- [ ] Arquitetura de planos documentada ✅ (JÁ FEITO - docs/17-ARQUITETURA-HIBRIDA.md)
- [ ] Credenciais e senhas documentadas ✅ (JÁ FEITO - CREDENCIAIS.md)
- [ ] Guia de deploy criado

### Performance
- [ ] Queries otimizadas (sem N+1)
- [ ] Índices no banco criados ✅ (JÁ FEITO - User, Plan, Subscription)
- [ ] Connection pool configurado ✅ (JÁ FEITO - HikariCP)
- [ ] FetchType.LAZY em relacionamentos ✅ (JÁ FEITO)
- [ ] Tempo de resposta < 500ms (p95)
- [ ] Paginação implementada em listagens grandes (admin)
- [ ] Cache Redis configurado (opcional)

### Arquitetura e Boas Práticas
- [ ] Clean Architecture mantida
- [ ] Services não chamam controllers
- [ ] Controllers apenas delegam para services
- [ ] Repositories apenas no infrastructure
- [ ] Entidades de domínio não dependem de framework
- [ ] DTOs para todas requests/responses
- [ ] Mappers para conversão Entity ↔ DTO
- [ ] Exception handling global implementado
- [ ] Logging adequado (INFO, WARN, ERROR)
- [ ] Transações gerenciadas corretamente (@Transactional)

---

## 🚀 DEPLOY E GO-LIVE

### Pré-Deploy
- [ ] Code review completo
- [ ] Todos testes passando em staging
- [ ] Variáveis de ambiente configuradas
- [ ] Banco de dados de produção criado
- [ ] Backup strategy definida

### Deploy
- [ ] Deploy em ambiente de staging
- [ ] Testes em staging com dados reais
- [ ] Migração de dados (se necessário)
- [ ] Deploy em produção
- [ ] Smoke tests em produção

### Pós-Deploy
- [ ] Monitorar logs por 24h
- [ ] Verificar webhooks funcionando
- [ ] Testar fluxo completo end-to-end
- [ ] Criar subscription de teste
- [ ] Documentar incidentes e soluções

### Stripe Live Mode
- [ ] Ativar webhook endpoint live
- [ ] Testar com transação real de baixo valor
- [ ] Configurar alertas no Stripe Dashboard
- [ ] Habilitar Radar (anti-fraude)
- [ ] Configurar limites e regras de negócio

---

## 📚 RECURSOS E REFERÊNCIAS

### Documentação Oficial
- [Stripe Subscriptions Guide](https://docs.stripe.com/billing/subscriptions)
- [Stripe Webhooks Best Practices](https://docs.stripe.com/webhooks/best-practices)
- [Stripe Customer Portal](https://docs.stripe.com/customer-management)
- [Spring Boot Security](https://spring.io/guides/gs/securing-web/)
- [Spring Data JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Boas Práticas
- Clean Architecture (Robert C. Martin)
- SOLID Principles
- 12 Factor App
- REST API Design Best Practices

### Ferramentas Úteis
- Stripe CLI: `stripe listen --forward-to localhost:8080/webhooks/stripe`
- Postman Collection (criar para testar API)
- Docker Compose (para ambiente local)

---

## ✨ NOTAS IMPORTANTES

### Segurança Stripe
⚠️ **CRÍTICO**: Sempre validar webhook signatures antes de processar eventos
⚠️ **CRÍTICO**: Nunca expor Stripe Secret Key no frontend
⚠️ **CRÍTICO**: Usar HTTPS em produção para webhooks

### Idempotência
💡 Sempre verificar se eventos de webhook já foram processados (usar event.id)

### Tratamento de Erros
💡 Sempre retornar 200 OK para webhooks, mesmo em caso de erro (após logar)
💡 Stripe irá retentar webhooks que não receberem 2xx

### Testes
💡 Usar Stripe test mode e test cards durante desenvolvimento
💡 Stripe CLI é essencial para testar webhooks localmente

---

**Última atualização**: 2025-10-05
**Versão**: 1.0
**Status**: Pronto para desenvolvimento ✅
