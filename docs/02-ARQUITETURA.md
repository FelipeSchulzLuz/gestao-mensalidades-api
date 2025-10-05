# 🏗️ Arquitetura do Sistema - Gestão de Mensalidades API

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Clean Architecture](#clean-architecture)
3. [Estrutura de Camadas](#estrutura-de-camadas)
4. [Fluxo de Dados](#fluxo-de-dados)
5. [Padrões de Projeto](#padrões-de-projeto)
6. [Tecnologias Principais](#tecnologias-principais)
7. [Integração com Stripe](#integração-com-stripe)
8. [Segurança](#segurança)

---

## 🎯 Visão Geral

Este projeto implementa uma API RESTful para **gerenciamento de assinaturas e mensalidades** integrada ao **Stripe** para processamento de pagamentos. O sistema segue os princípios da **Clean Architecture**, garantindo:

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

Veja detalhes completos em **[docs/05-PLANOS.md](./05-PLANOS.md)**.

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
- **[05-PLANOS.md](./05-PLANOS.md)** - Arquitetura de gerenciamento de planos
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
