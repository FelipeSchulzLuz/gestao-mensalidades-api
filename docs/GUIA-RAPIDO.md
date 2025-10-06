# ⚡ Guia Rápido de Referência

> **Comandos, decisões e links mais usados no dia a dia**  
> Bookmark esta página para acesso instantâneo!

---

## 🚀 Comandos Mais Usados

### Docker & Ambiente

```bash
# Fluxo padronizado (recomendado)
./scripts/run-make.sh start

# Subir todos os serviços
docker compose up -d

# Ver logs em tempo real
docker compose logs -f

# Parar tudo
docker compose down

# Aplicar migrations via Flyway
docker compose run --rm flyway
```

### Gradle & Aplicação

```bash
# Compilar
./gradlew build

# Rodar aplicação
./gradlew bootRun

# Rodar testes
./gradlew test

# Limpar build
./gradlew clean
```

### Stripe CLI

```bash
# Login
stripe login

# Testar webhooks localmente
stripe listen --forward-to localhost:8080/api/webhooks/stripe

# Disparar evento teste
stripe trigger payment_intent.succeeded
```

### Git

```bash
# Commits convencionais
git commit -m "feat: add product creation endpoint"
git commit -m "fix: correct webhook handler"
git commit -m "docs: update architecture"
git commit -m "refactor: rename Plan to Product"
git commit -m "chore: update dependencies"
```

---

## 🏗️ Decisões Arquiteturais Rápidas

### ✅ USE ISTO (Nova Arquitetura)

```java
// ✅ Entidades
Product product = new Product();
Price price = new Price();
Organization org = new Organization();
Order order = new Order();

// ✅ Enums
ProductType.SUBSCRIPTION
ProductType.ONE_TIME

PriceType.RECURRING
PriceType.ONE_TIME

CommerceMode.SUBSCRIPTIONS_ONLY
CommerceMode.ECOMMERCE_ONLY
CommerceMode.HYBRID

// ✅ Checkout
if (product.getType() == ProductType.SUBSCRIPTION) {
    session.setMode("subscription");
}
if (product.getType() == ProductType.ONE_TIME) {
    session.setMode("payment");
}

// ✅ Webhook
if ("subscription".equals(session.getMode())) {
    createSubscription(session);
} else if ("payment".equals(session.getMode())) {
    createOrder(session);
}
```

### ❌ NÃO USE ISTO (Arquitetura Antiga)

```java
// ❌ OBSOLETO
Plan plan = new Plan();

// ❌ ERRADO
session.setMode("subscription"); // sem checar ProductType

// ❌ ERRADO
createSubscription(session); // sem checar mode
```

---

## 🗂️ Estrutura de Pacotes

```
src/main/java/br/com/gestaomensalidades/
├── domain/
│   ├── entities/        → User, Product, Price, Organization, Order
│   ├── enums/           → ProductType, PriceType, CommerceMode
│   └── repositories/    → Interfaces JPA
│
├── application/
│   ├── services/        → Lógica de negócio
│   ├── usecases/        → Casos de uso específicos
│   └── dto/             → Request/Response DTOs
│
├── infrastructure/
│   ├── persistence/     → Implementações JPA
│   ├── security/        → JWT, Firebase Auth
│   ├── stripe/          → Integração Stripe
│   └── config/          → Configurações Spring
│
└── presentation/
    ├── controllers/     → REST Controllers
    ├── dto/             → Request/Response específicos
    └── mappers/         → Entity ↔ DTO
```

---

## 🎯 Regras de Ouro

### 1. Terminologia
- ✅ **Product** (o que você vende)
- ✅ **Price** (como você vende)
- ❌ ~~Plan~~ (obsoleto)

### 2. Checkout
- **SUBSCRIPTION** → `mode: 'subscription'` → Tabela `subscriptions`
- **ONE_TIME** → `mode: 'payment'` → Tabela `orders`

### 3. Webhooks
```java
String mode = session.getMode();
if ("subscription".equals(mode)) {
    // Rota para subscriptions
} else if ("payment".equals(mode)) {
    // Rota para orders
}
```

### 4. Migrations
- **Sempre** sequenciais (002, 003, 004...)
- **Sempre** idempotentes
- **Sempre** com rollback possível
- **Nunca** alterar migrations aplicadas

### 5. Commits
```
tipo(escopo): descrição

feat: nova funcionalidade
fix: correção de bug
docs: documentação
refactor: refatoração
test: testes
chore: tarefas
```

---

## 📖 Links Rápidos para Docs

### 🚨 Antes de Codificar
→ [18-MUDANCA-ARQUITETURAL-RESUMO.md](./18-MUDANCA-ARQUITETURAL-RESUMO.md) (10 min)

### 🏗️ Arquitetura
→ [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md) (30 min referência)  
→ [02-ARQUITETURA.md](./02-ARQUITETURA.md) (Clean Architecture)

### 💳 Stripe
→ [04-STRIPE.md](./04-STRIPE.md) (webhooks, CLI, checkout)

### 🗄️ Database
→ [08-MIGRATIONS-BOAS-PRATICAS.md](./08-MIGRATIONS-BOAS-PRATICAS.md)  
→ [10-COMO-APLICAR-MIGRATION.md](./10-COMO-APLICAR-MIGRATION.md)

### 🐳 Docker
→ [03-DOCKER.md](./03-DOCKER.md) (troubleshooting)

### 📋 Tarefas
→ [09-CHECKLIST-DESENVOLVIMENTO.md](./09-CHECKLIST-DESENVOLVIMENTO.md)  
→ [STATUS-GERAL.md](./STATUS-GERAL.md)

### 🚀 Deploy
→ [12-DEPLOY-SERVIDOR.md](./12-DEPLOY-SERVIDOR.md)

---

## 🔧 Troubleshooting Rápido

### Application não sobe
```bash
# 1. Verificar se Docker está rodando
docker ps

# 2. Verificar se migrations foram aplicadas
docker compose logs flyway

# 3. Executar migrations e reiniciar
./scripts/run-make.sh start

# 4. Verificar logs
docker compose logs -f app
```

### Erro de migration
```bash
# Executar migrations via Flyway
docker compose run --rm flyway

# Verificar histórico de migrations aplicadas
docker exec -i $(docker ps -qf "name=gestao-postgres") \
  psql -U gestao_app_user -d gestao_mensalidades_dev -c \
  "SELECT version, description, installed_on FROM flyway_schema_history;"

# Ou conectar no DBeaver para inspecionar manualmente
```

### Docker não conecta
```powershell
# Recriar containers
docker-compose down
docker-compose up -d --build

# Ver logs
docker-compose logs -f postgres
```

### Stripe webhooks não funcionam
```powershell
# 1. Verificar se Stripe CLI está rodando
stripe listen --forward-to localhost:8080/api/webhooks/stripe

# 2. Verificar .env
# STRIPE_WEBHOOK_SECRET deve estar configurado

# 3. Testar manualmente
stripe trigger payment_intent.succeeded
```

### Erro de compilação Java
```powershell
# Limpar e recompilar
.\gradlew clean build

# Se persistir, verificar Java version
java -version  # Deve ser 21

# Invalidar cache IDE (IntelliJ)
# File → Invalidate Caches / Restart
```

---

## 🎨 Padrões de Código

### DTOs
```java
// Request
public record CreateProductRequest(
    String name,
    String description,
    ProductType type
) {}

// Response
public record ProductResponse(
    Long id,
    String name,
    ProductType type,
    LocalDateTime createdAt
) {}
```

### Services
```java
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository repository;
    private final ProductMapper mapper;
    
    public ProductResponse create(CreateProductRequest request) {
        Product product = mapper.toEntity(request);
        Product saved = repository.save(product);
        return mapper.toResponse(saved);
    }
}
```

### Controllers
```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService service;
    
    @PostMapping
    public ResponseEntity<ProductResponse> create(
        @Valid @RequestBody CreateProductRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(service.create(request));
    }
}
```

### Repositories
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByOrganizationId(Long organizationId);
    
    Optional<Product> findByIdAndOrganizationId(Long id, Long organizationId);
    
    @Query("SELECT p FROM Product p WHERE p.type = :type AND p.isActive = true")
    List<Product> findActiveByType(@Param("type") ProductType type);
}
```

---

## 📊 Métricas do Projeto

**Progresso:** ~25% (3 de 14 fases)  
**Documentos:** 20 arquivos  
**Linhas de código:** ~5.000 (estimativa)  
**Commits:** 15+  
**Migrations:** 2 criadas (1 aplicada)

---

## 🆘 Ajuda Imediata

### Não sei por onde começar
1. [LEIA-ME.md](./LEIA-ME.md) → Escolha seu perfil
2. [STATUS-GERAL.md](./STATUS-GERAL.md) → Veja o status atual

### Vou codificar agora
1. [18-MUDANCA-ARQUITETURAL-RESUMO.md](./18-MUDANCA-ARQUITETURAL-RESUMO.md) → Leia ANTES
2. Este guia → Consulte enquanto codifica
3. [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md) → Referência detalhada

### Erro no código
1. Seção "Troubleshooting Rápido" acima ↑
2. [03-DOCKER.md](./03-DOCKER.md) ou [04-STRIPE.md](./04-STRIPE.md)
3. Git log / Git blame para entender mudança

### Code review
1. Verificar terminologia (Product/Price, NÃO Plan)
2. Verificar checkout mode (subscription vs payment)
3. Verificar Clean Architecture (camadas corretas)
4. [LEIA-ME.md](./LEIA-ME.md) seção "Code Review"

---

## 🔄 Última Atualização

**Data:** 2025-10-05  
**Autor:** Time de Desenvolvimento  
**Próxima revisão:** Após Migration 003 aplicada

---

**💡 Dica:** Mantenha este guia sempre aberto enquanto desenvolve!
