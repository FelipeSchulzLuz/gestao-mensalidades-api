# 🏗️ Arquitetura do Sistema - Gestão de Mensalidades API

> **📅 Última atualização:** 2025-10-05  
> **📖 Complemento:** Ver [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md) para arquitetura Products + Prices

---

## ⚡ Mudança Arquitetural Importante

> 🔄 **Outubro 2025:** Sistema evoluiu de **Planos** para **Products + Prices**
> 
> **Motivação:** Suportar assinaturas E e-commerce na mesma plataforma
> 
> **Leitura obrigatória:**
> - [18-MUDANCA-ARQUITETURAL-RESUMO.md](./18-MUDANCA-ARQUITETURAL-RESUMO.md) (10 min)
> - [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md) (30 min)

---

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Nova Arquitetura (Products + Prices)](#nova-arquitetura-products--prices)
3. [Clean Architecture](#clean-architecture)
4. [Estrutura de Camadas](#estrutura-de-camadas)
5. [Fluxo de Dados](#fluxo-de-dados)
6. [Padrões de Projeto](#padrões-de-projeto)
7. [Tecnologias Principais](#tecnologias-principais)
8. [Integração com Stripe](#integração-com-stripe)
9. [Segurança](#segurança)

---

## 🎯 Visão Geral

Este projeto implementa uma API RESTful para **gerenciamento de assinaturas e e-commerce** integrada ao **Stripe** para processamento de pagamentos. O sistema segue os princípios da **Clean Architecture**, garantindo:

- ✅ **Separação de Responsabilidades**: Cada camada tem uma única responsabilidade
- ✅ **Independência de Frameworks**: A lógica de negócio não depende de frameworks externos
- ✅ **Testabilidade**: Facilita testes unitários e de integração
- ✅ **Manutenibilidade**: Código organizado e fácil de manter
- ✅ **Escalabilidade**: Arquitetura preparada para crescimento

### Diagrama de Alto Nível

```
┌────────────────────────────────────────────────────────────┐
│                      CLIENTE (Frontend)                     │
└────────────────────────┬───────────────────────────────────┘
                         │ HTTP/REST
┌────────────────────────▼───────────────────────────────────┐
│              Presentation Layer (Controllers)               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │ AuthController│  │UserController│  │PlanController│    │
│  └──────────────┘  └──────────────┘  └──────────────┘    │
└────────────────────────┬───────────────────────────────────┘
                         │ DTOs
┌────────────────────────▼───────────────────────────────────┐
│              Application Layer (Use Cases)                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │RegisterUser  │  │CreatePlan    │  │Subscription  │    │
│  └──────────────┘  └──────────────┘  └──────────────┘    │
└────────────────────────┬───────────────────────────────────┘
                         │ Entities & Interfaces
┌────────────────────────▼───────────────────────────────────┐
│                   Domain Layer (Core)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │User Entity   │  │Plan Entity   │  │Subscription  │    │
│  └──────────────┘  └──────────────┘  └──────────────┘    │
└────────────────────────┬───────────────────────────────────┘
                         │ Implementations
┌────────────────────────▼───────────────────────────────────┐
│           Infrastructure Layer (Frameworks & Tools)         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │JPA/Hibernate │  │Stripe API    │  │Redis Cache   │    │
│  └──────────────┘  └──────────────┘  └──────────────┘    │
└────────────────────────┬───────────────────────────────────┘
                         │
              ┌──────────┴─────────┐
              │                    │
         PostgreSQL              Stripe
```

---

## 🆕 Nova Arquitetura (Products + Prices)

> **Esta seção descreve a arquitetura híbrida implementada em Outubro 2025.**  
> Para detalhes completos, ver [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md)

### Conceito Central

O sistema agora suporta **dois modelos de negócio** na mesma plataforma:

1. **Assinaturas Recorrentes** (SaaS, planos mensais/anuais)
2. **E-commerce** (vendas únicas, produtos físicos/digitais)

### Modelo de Dados Atualizado

```
┌────────────────────────────────────────────────────────────┐
│                       ORGANIZATION                          │
│  ┌──────────────────────────────────────────────────────┐ │
│  │ id, name, commerce_mode (SUBSCRIPTIONS/ECOMMERCE/   │ │
│  │ HYBRID), stripe_account_id                           │ │
│  └──────────────────────────────────────────────────────┘ │
└────────────────────────┬───────────────────────────────────┘
                         │
          ┌──────────────┼──────────────┐
          │                             │
┌─────────▼─────────────┐    ┌─────────▼─────────────┐
│       PRODUCT         │    │        USER           │
│ ┌───────────────────┐ │    │ ┌───────────────────┐ │
│ │ id, organization  │ │    │ │ id, organization  │ │
│ │ name, description │ │    │ │ email, password   │ │
│ │ type: SUBSCRIPTION│ │    │ │ firebase_uid      │ │
│ │       ONE_TIME    │ │    │ └───────────────────┘ │
│ │ metadata (JSONB)  │ │    └───────────┬───────────┘
│ └───────────────────┘ │                │
└─────────┬─────────────┘                │
          │                              │
          │                    ┌─────────┴─────────────┐
┌─────────▼─────────────┐      │                       │
│        PRICE          │  ┌───▼──────────┐   ┌───────▼──────┐
│ ┌───────────────────┐ │  │SUBSCRIPTION  │   │    ORDER     │
│ │ id, product_id    │ │  │ ┌──────────┐ │   │ ┌──────────┐ │
│ │ amount, currency  │ │  │ │price_id  │ │   │ │items []  │ │
│ │ type: RECURRING   │ │  │ │status    │ │   │ │total     │ │
│ │       ONE_TIME    │ │  │ │period    │ │   │ │status    │ │
│ │ recurring_interval│ │  │ └──────────┘ │   │ └──────────┘ │
│ │ stripe_price_id   │ │  └──────────────┘   └──────────────┘
│ └───────────────────┘ │   (recorrente)      (pagamento único)
└───────────────────────┘
```

### Fluxo de Checkout Híbrido

```java
// 1. Cliente seleciona produto
Product product = productRepository.findById(productId);

// 2. Sistema decide tipo de checkout baseado no ProductType
if (product.getType() == ProductType.SUBSCRIPTION) {
    // Checkout de assinatura
    SessionCreateParams params = SessionCreateParams.builder()
        .setMode(SessionCreateParams.Mode.SUBSCRIPTION)  // ← Importante
        .addLineItem(...)
        .build();
    
    // Webhook: checkout.session.completed → criar registro em subscriptions
    
} else if (product.getType() == ProductType.ONE_TIME) {
    // Checkout de e-commerce
    SessionCreateParams params = SessionCreateParams.builder()
        .setMode(SessionCreateParams.Mode.PAYMENT)  // ← Importante
        .addLineItem(...)
        .build();
    
    // Webhook: checkout.session.completed → criar registro em orders
}
```

### Webhook Handler Inteligente

```java
@PostMapping("/webhooks/stripe")
public ResponseEntity<Void> handleStripeWebhook(
    @RequestBody String payload,
    @RequestHeader("Stripe-Signature") String signature
) {
    Event event = Webhook.constructEvent(payload, signature, webhookSecret);
    
    if ("checkout.session.completed".equals(event.getType())) {
        Session session = extractSession(event);
        
        // Decisão baseada no mode da session
        if ("subscription".equals(session.getMode())) {
            subscriptionService.createFromCheckout(session);  // → tabela subscriptions
        } else if ("payment".equals(session.getMode())) {
            orderService.createFromCheckout(session);  // → tabela orders
        }
    }
    
    return ResponseEntity.ok().build();
}
```

### Entidades Principais

**Product** (substitui Plan):
```java
@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne
    private Organization organization;
    
    private String name;
    private String description;
    
    @Enumerated(EnumType.STRING)
    private ProductType type;  // SUBSCRIPTION ou ONE_TIME
    
    @Type(JsonType.class)
    private Map<String, Object> metadata;  // JSONB flexível
    
    private Boolean isActive;
}
```

**Price** (múltiplos preços por produto):
```java
@Entity
@Table(name = "prices")
public class Price {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne
    private Product product;
    
    private BigDecimal amount;
    private String currency;
    
    @Enumerated(EnumType.STRING)
    private PriceType type;  // RECURRING ou ONE_TIME
    
    private String recurringInterval;  // "month", "year" ou NULL
    
    private String stripePriceId;
}
```

### Commerce Mode por Organização

Cada organização pode operar em 3 modos:

```java
public enum CommerceMode {
    SUBSCRIPTIONS_ONLY,  // Apenas assinaturas (SaaS)
    ECOMMERCE_ONLY,      // Apenas e-commerce (loja online)
    HYBRID               // Ambos (plataforma completa)
}
```

**Exemplo de uso:**
```java
Organization org = user.getOrganization();

if (org.getCommerceMode() == CommerceMode.SUBSCRIPTIONS_ONLY) {
    // Mostrar apenas produtos do tipo SUBSCRIPTION
    // Bloquear criação de produtos ONE_TIME
}

if (org.getCommerceMode() == CommerceMode.HYBRID) {
    // Mostrar abas: "Planos" e "Produtos"
    // Permitir ambos os tipos
}
```

### Referência Completa

📖 Para schemas SQL completos, exemplos de código, e checklist de implementação:  
→ **[17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md)**

---

## 🧱 Clean Architecture

### Princípios Fundamentais

1. **Independência de Frameworks**: A lógica de negócio não conhece Spring Boot, JPA ou Stripe SDK
2. **Testabilidade**: Regras de negócio podem ser testadas sem UI, banco de dados ou servidor
3. **Independência de UI**: A UI pode mudar sem alterar a lógica de negócio
4. **Independência de Banco de Dados**: Pode trocar PostgreSQL por MySQL sem afetar as regras de negócio
5. **Independência de Agentes Externos**: Pode trocar Stripe por outro gateway de pagamento

### Regra de Dependência

```
┌─────────────────────────────────────────┐
│                                         │
│       ┌─────────────────────┐           │
│       │     Frameworks      │           │
│       │  (Infrastructure)   │           │
│       └──────────┬──────────┘           │
│                  │                      │
│       ┌──────────▼──────────┐           │
│       │   Controllers &     │           │
│       │   Adapters          │           │
│       │  (Presentation)     │           │
│       └──────────┬──────────┘           │
│                  │                      │
│       ┌──────────▼──────────┐           │
│       │    Use Cases        │           │
│       │   (Application)     │           │
│       └──────────┬──────────┘           │
│                  │                      │
│       ┌──────────▼──────────┐           │
│       │    Entities         │           │
│       │    (Domain)         │   CORE   │
│       └─────────────────────┘           │
│                                         │
└─────────────────────────────────────────┘

➡️ As dependências sempre apontam PARA DENTRO
➡️ O Domain não conhece nenhuma camada externa
```

---

## 📁 Estrutura de Camadas

### 1. Domain Layer (Núcleo)

**Localização:** `src/main/java/br/com/mensalidades/domain/`

**Responsabilidade:** Contém as **regras de negócio** e **entidades** do sistema.

```
domain/
├── entities/          # Entidades de negócio (POJO)
│   ├── User.java
│   ├── Plan.java
│   └── Subscription.java
├── enums/             # Enumerações do domínio
│   ├── PlanStatus.java
│   └── SubscriptionStatus.java
└── exceptions/        # Exceções de domínio
    ├── UserNotFoundException.java
    └── PlanInactiveException.java
```

**Características:**
- ✅ Não depende de nenhuma outra camada
- ✅ Não usa anotações do Spring ou JPA
- ✅ POJO puro com lógica de negócio
- ✅ Validações de domínio (invariantes)

**Exemplo:**
```java
public class Plan {
    private String id;
    private String name;
    private BigDecimal price;
    private PlanStatus status;
    
    // Regra de negócio: plano deve ter preço positivo
    public void setPrice(BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.price = price;
    }
    
    // Regra de negócio: plano inativo não pode ser usado
    public boolean canBeSubscribed() {
        return status == PlanStatus.ACTIVE;
    }
}
```

### 2. Application Layer (Casos de Uso)

**Localização:** `src/main/java/br/com/mensalidades/application/`

**Responsabilidade:** Orquestra o fluxo de dados entre camadas e executa casos de uso.

```
application/
├── usecases/          # Casos de uso (lógica de aplicação)
│   ├── RegisterUserUseCase.java
│   ├── CreatePlanUseCase.java
│   └── SubscribeToPlanUseCase.java
├── dto/               # Data Transfer Objects
│   ├── request/
│   │   ├── RegisterUserRequestDTO.java
│   │   └── CreatePlanRequestDTO.java
│   └── response/
│       ├── UserResponseDTO.java
│       └── PlanResponseDTO.java
└── ports/             # Interfaces (inversão de dependência)
    ├── in/            # Portas de entrada (usadas por controllers)
    │   └── RegisterUserInputPort.java
    └── out/           # Portas de saída (implementadas pela infra)
        ├── UserRepositoryPort.java
        └── StripeServicePort.java
```

**Características:**
- ✅ Coordena entidades do domínio
- ✅ Define interfaces para comunicação com infraestrutura
- ✅ Contém DTOs para comunicação com controllers
- ✅ Não conhece detalhes de implementação (JPA, Stripe SDK)

**Exemplo:**
```java
@Service
public class RegisterUserUseCase implements RegisterUserInputPort {
    
    private final UserRepositoryPort userRepository;
    private final StripeServicePort stripeService;
    private final PasswordEncoder passwordEncoder;
    
    public UserResponseDTO execute(RegisterUserRequestDTO request) {
        // 1. Validar entrada
        validateEmail(request.getEmail());
        
        // 2. Criar entidade de domínio
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // 3. Criar customer no Stripe
        String stripeCustomerId = stripeService.createCustomer(user);
        user.setStripeCustomerId(stripeCustomerId);
        
        // 4. Persistir
        user = userRepository.save(user);
        
        // 5. Retornar DTO
        return UserResponseDTO.from(user);
    }
}
```

### 3. Presentation Layer (Interface)

**Localização:** `src/main/java/br/com/mensalidades/presentation/`

**Responsabilidade:** Expõe endpoints REST e manipula requisições HTTP.

```
presentation/
├── controllers/       # REST Controllers
│   ├── AuthController.java
│   ├── UserController.java
│   ├── PlanController.java
│   └── WebhookController.java
└── config/            # Configurações de apresentação
    ├── WebConfig.java
    └── CorsConfig.java
```

**Características:**
- ✅ Usa anotações do Spring (`@RestController`, `@RequestMapping`)
- ✅ Valida entrada com Bean Validation (`@Valid`)
- ✅ Retorna status HTTP apropriados
- ✅ Não contém lógica de negócio

**Exemplo:**
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final RegisterUserInputPort registerUserUseCase;
    
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterUserRequestDTO request) {
        UserResponseDTO response = registerUserUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

### 4. Infrastructure Layer (Implementações)

**Localização:** `src/main/java/br/com/mensalidades/infrastructure/`

**Responsabilidade:** Implementa detalhes técnicos (banco de dados, APIs externas, etc).

```
infrastructure/
├── persistence/       # Implementação de repositórios
│   ├── entities/      # Entidades JPA (diferentes do Domain!)
│   │   ├── UserEntity.java
│   │   └── PlanEntity.java
│   ├── repositories/  # Spring Data JPA Repositories
│   │   ├── UserJpaRepository.java
│   │   └── PlanJpaRepository.java
│   └── adapters/      # Adaptadores para as portas
│       └── UserRepositoryAdapter.java
├── stripe/            # Integração com Stripe
│   ├── StripeServiceAdapter.java
│   └── WebhookHandler.java
├── cache/             # Implementação de cache (Redis)
│   └── RedisCacheService.java
└── security/          # Segurança (JWT, Spring Security)
    ├── JwtTokenProvider.java
    └── SecurityConfig.java
```

**Características:**
- ✅ Usa anotações do Spring (`@Repository`, `@Component`)
- ✅ Implementa interfaces definidas na Application Layer
- ✅ Pode ser substituída sem afetar outras camadas

**Exemplo:**
```java
@Component
public class UserRepositoryAdapter implements UserRepositoryPort {
    
    private final UserJpaRepository jpaRepository;
    
    @Override
    public User save(User user) {
        // Converte Domain Entity → JPA Entity
        UserEntity entity = UserEntity.from(user);
        
        // Persiste com JPA
        entity = jpaRepository.save(entity);
        
        // Converte JPA Entity → Domain Entity
        return entity.toDomainModel();
    }
}
```

---

## 🔄 Fluxo de Dados

### Exemplo: Registro de Usuário

```
1. Cliente                POST /api/auth/register
   │                      { "name": "João", "email": "joao@email.com", "password": "..." }
   │
   ▼
2. AuthController         Valida entrada (@Valid)
   │                      
   │
   ▼
3. RegisterUserUseCase    Executa lógica de aplicação
   │                      ├─ Cria User (domain entity)
   │                      ├─ Chama StripeService (cria Customer)
   │                      └─ Chama UserRepository (persiste)
   │
   ├────────────────────┐
   │                    │
   ▼                    ▼
4. StripeServiceAdapter UserRepositoryAdapter
   │                    │
   │ Stripe API         │ JPA/Hibernate
   │                    │
   └────────────────────┘
   │
   ▼
5. RegisterUserUseCase    Retorna UserResponseDTO
   │
   ▼
6. AuthController         ResponseEntity<UserResponseDTO> (201 Created)
   │
   ▼
7. Cliente                JSON: { "id": "...", "name": "João", ... }
```

---

## 🎨 Padrões de Projeto

### 1. Repository Pattern

**Objetivo:** Abstrair acesso a dados.

```java
// Interface (Application Layer)
public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
}

// Implementação (Infrastructure Layer)
@Component
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final UserJpaRepository jpaRepository;
    // ...
}
```

### 2. Adapter Pattern (Hexagonal Architecture)

**Objetivo:** Isolar dependências externas.

```java
// Interface (Application Layer)
public interface StripeServicePort {
    String createCustomer(User user);
    String createCheckoutSession(Plan plan, User user);
}

// Implementação (Infrastructure Layer)
@Component
public class StripeServiceAdapter implements StripeServicePort {
    private final Stripe stripe;
    // Usa Stripe SDK internamente
}
```

### 3. DTO Pattern

**Objetivo:** Transferir dados entre camadas sem expor entidades internas.

```java
// Request DTO (entrada)
public class RegisterUserRequestDTO {
    @NotBlank private String name;
    @Email private String email;
    @Size(min = 8) private String password;
}

// Response DTO (saída)
public class UserResponseDTO {
    private String id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    
    public static UserResponseDTO from(User user) { /* ... */ }
}
```

### 4. Factory Pattern

**Objetivo:** Criar objetos complexos.

```java
public class UserFactory {
    public static User create(String name, String email, String hashedPassword) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(name);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
```

### 5. Strategy Pattern (Webhooks)

**Objetivo:** Processar diferentes tipos de webhooks.

```java
public interface WebhookStrategy {
    void handle(Event event);
}

@Component
public class CheckoutSessionCompletedStrategy implements WebhookStrategy {
    public void handle(Event event) { /* ... */ }
}

public class WebhookProcessor {
    private Map<String, WebhookStrategy> strategies;
    
    public void process(Event event) {
        WebhookStrategy strategy = strategies.get(event.getType());
        strategy.handle(event);
    }
}
```

---

## 🛠️ Tecnologias Principais

| Camada           | Tecnologia                | Objetivo                          |
|------------------|---------------------------|-----------------------------------|
| **Framework**    | Spring Boot 3.5.6         | Base da aplicação                 |
| **Build**        | Gradle 8.x                | Gerenciamento de dependências     |
| **Linguagem**    | Java 21                   | Linguagem de programação          |
| **Database**     | PostgreSQL 15             | Banco de dados relacional         |
| **ORM**          | JPA/Hibernate             | Mapeamento objeto-relacional      |
| **Cache**        | Redis 7                   | Cache distribuído                 |
| **Security**     | Spring Security + JWT     | Autenticação e autorização        |
| **Pagamentos**   | Stripe Java SDK 30.0.0    | Processamento de pagamentos       |
| **Validação**    | Bean Validation (Hibernate Validator) | Validação de entrada |
| **Logging**      | SLF4J + Logback           | Logs estruturados                 |
| **Testing**      | JUnit 5 + Mockito         | Testes unitários e integração     |
| **Docs API**     | Swagger/OpenAPI 3         | Documentação automática da API    |

---

## 💳 Integração com Stripe

### Fluxo de Assinatura

```
1. Frontend solicita checkout
   │
   ▼
2. API cria Checkout Session (Stripe)
   │
   ▼
3. Stripe retorna URL de checkout
   │
   ▼
4. Frontend redireciona usuário
   │
   ▼
5. Usuário paga no Stripe Checkout
   │
   ▼
6. Stripe envia webhook (checkout.session.completed)
   │
   ▼
7. API processa webhook e ativa assinatura
   │
   ▼
8. Banco de dados atualizado
```

### Arquitetura de Sincronização (Planos)

Veja detalhes completos em **[docs/17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md)** (Products + Prices).

**Estratégia:** Sincronização bidirecional automática

- Admin cria/edita plano → API sincroniza com Stripe
- Stripe notifica mudança via webhook → API atualiza banco
- Garantia de consistência eventual

---

## 🔒 Segurança

### Autenticação JWT

```
1. Login: POST /api/auth/login
   ├─ Valida credenciais
   ├─ Gera JWT token (512-bit)
   └─ Retorna access_token
   
2. Requisições autenticadas:
   Header: Authorization: Bearer {token}
   ├─ JwtFilter valida token
   ├─ Extrai userId do token
   └─ Configura SecurityContext
```

### Proteção de Rotas

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/plans").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

### Validação de Webhooks

```java
@PostMapping("/api/webhooks/stripe")
public ResponseEntity<Void> handleWebhook(@RequestBody String payload, 
                                           @RequestHeader("Stripe-Signature") String signature) {
    // Valida assinatura do webhook
    Event event = Webhook.constructEvent(payload, signature, webhookSecret);
    
    // Processa evento
    webhookProcessor.process(event);
    
    return ResponseEntity.ok().build();
}
```

---

## 📚 Documentos Relacionados

- **[01-INICIO-RAPIDO.md](./01-INICIO-RAPIDO.md)** - Guia de início rápido
- **[03-DOCKER.md](./03-DOCKER.md)** - Configuração do Docker
- **[04-STRIPE.md](./04-STRIPE.md)** - Integração com Stripe
- **[17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md)** - Arquitetura Products + Prices
- **[../CHECKLIST_DESENVOLVIMENTO.md](../CHECKLIST_DESENVOLVIMENTO.md)** - Checklist de implementação

---

## 🎯 Benefícios da Arquitetura

✅ **Testabilidade**: Lógica de negócio testável sem dependências externas  
✅ **Manutenibilidade**: Código organizado e fácil de entender  
✅ **Escalabilidade**: Fácil adicionar novos recursos  
✅ **Flexibilidade**: Trocar tecnologias sem reescrever tudo  
✅ **Independência**: Camadas isoladas e desacopladas  
✅ **Qualidade**: Código limpo seguindo SOLID principles  

---

**Próximo:** Comece o desenvolvimento seguindo o **[CHECKLIST_DESENVOLVIMENTO.md](../CHECKLIST_DESENVOLVIMENTO.md)**! 🚀
