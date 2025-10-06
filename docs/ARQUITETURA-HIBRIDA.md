# 🏗️ Arquitetura Híbrida: Produtos & Preços (V1 → V2)

> **Evolução Arquitetural: De Sistema de Assinaturas para Plataforma de Comércio Híbrida Multi-Tenant**

---

## 🎯 Visão Geral

Esta documentação descreve a **mudança arquitetural fundamental** do projeto:

**Antes (Arquitetura Limitada):**
- ❌ Sistema focado apenas em "Planos de Assinatura"
- ❌ Entidade `Plan` rígida e específica
- ❌ Impossível vender produtos avulsos (e-commerce)
- ❌ Sem suporte multi-tenant

**Depois (Arquitetura Escalável):**
- ✅ Plataforma híbrida (assinaturas + e-commerce)
- ✅ Modelo genérico `Product` + `Price` (como Stripe)
- ✅ Suporte a múltiplas organizações (multi-tenant)
- ✅ Configuração por cliente (modo de operação)
- ✅ Fundação preparada para crescimento

---

## 🧩 Conceito Central: Generalização

### A Mudança de Paradigma

```
❌ PENSAMENTO ANTIGO (Limitado):
"Plano Mensal" é diferente de "Doce de Festa"
→ Entidades separadas, lógicas separadas

✅ PENSAMENTO NOVO (Generalizado):
Ambos são PRODUTOS, diferem apenas em:
- Como são precificados (recorrente vs. avulso)
- Como são vendidos (subscription vs. payment)
→ Uma entidade, múltiplas configurações
```

### Exemplo Prático

```java
// ❌ ANTES (Limitado)
Plan planoMensal = new Plan("Plano Mensal", 99.90, "MONTH");
// Como vender um doce? Não tem suporte!

// ✅ DEPOIS (Genérico)
Product assinatura = new Product("Plano Mensal", ProductType.SUBSCRIPTION);
Price precoMensal = new Price(assinatura, 99.90, PriceType.RECURRING, "month");

Product doce = new Product("Doce de Festa", ProductType.ONE_TIME);
Price precoDoce = new Price(doce, 5.00, PriceType.ONE_TIME, null);
```

---

## 📊 Nova Estrutura de Dados

### Diagrama Entidade-Relacionamento

```
┌─────────────────────────────────────────────────────────────┐
│                      ORGANIZATIONS                          │
│  - id (PK)                                                  │
│  - name                                                     │
│  - commerce_mode (ENUM: SUBSCRIPTIONS_ONLY | ECOMMERCE_ONLY | HYBRID) │
│  - stripe_account_id                                        │
│  - created_at                                               │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ 1:N
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                        PRODUCTS                             │
│  - id (PK)                                                  │
│  - organization_id (FK) → organizations.id                  │
│  - name                                                     │
│  - description                                              │
│  - type (ENUM: 'SUBSCRIPTION' | 'ONE_TIME')               │
│  - is_active                                                │
│  - is_featured                                              │
│  - display_order                                            │
│  - metadata (JSONB) // Dados customizados                  │
│  - stripe_product_id                                        │
│  - created_at, updated_at                                   │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ 1:N (Um produto pode ter múltiplos preços)
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                         PRICES                              │
│  - id (PK)                                                  │
│  - product_id (FK) → products.id                            │
│  - amount (decimal)                                         │
│  - currency (BRL, USD, EUR...)                              │
│  - type (ENUM: 'RECURRING' | 'ONE_TIME')                   │
│  - recurring_interval ('month', 'year', NULL)              │
│  - is_active                                                │
│  - stripe_price_id                                          │
│  - created_at                                               │
└─────────────────────────────────────────────────────────────┘
           │                                  │
           │                                  │
           ▼ (para SUBSCRIPTION)              ▼ (para ONE_TIME)
┌──────────────────────┐           ┌──────────────────────┐
│   SUBSCRIPTIONS      │           │       ORDERS         │
│  - user_id           │           │  - user_id           │
│  - price_id (FK)     │           │  - items (JSONB)     │
│  - status            │           │  - total_amount      │
│  - current_period_end│           │  - status            │
│  - stripe_sub_id     │           │  - stripe_payment_id │
└──────────────────────┘           └──────────────────────┘
```

---

## 🗂️ Modelo de Dados Detalhado

### Tabela: `organizations`

```sql
CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    
    -- 🆕 MODO DE COMÉRCIO
    commerce_mode VARCHAR(50) NOT NULL DEFAULT 'SUBSCRIPTIONS_ONLY',
    -- Valores: 'SUBSCRIPTIONS_ONLY', 'ECOMMERCE_ONLY', 'HYBRID'
    
    stripe_account_id VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    metadata JSONB, -- Configurações customizadas
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_commerce_mode CHECK (
        commerce_mode IN ('SUBSCRIPTIONS_ONLY', 'ECOMMERCE_ONLY', 'HYBRID')
    )
);

CREATE INDEX idx_organization_slug ON organizations(slug);
CREATE INDEX idx_organization_commerce_mode ON organizations(commerce_mode);
```

### Tabela: `products`

```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    
    name VARCHAR(255) NOT NULL,
    description TEXT,
    
    -- 🆕 TIPO DE PRODUTO
    type VARCHAR(20) NOT NULL,
    -- Valores: 'SUBSCRIPTION' (planos) ou 'ONE_TIME' (e-commerce)
    
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    
    -- 🆕 METADATA FLEXÍVEL
    metadata JSONB, -- Ex: { "category": "doces", "weight": "500g" }
    
    stripe_product_id VARCHAR(255) UNIQUE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_product_type CHECK (type IN ('SUBSCRIPTION', 'ONE_TIME'))
);

CREATE INDEX idx_product_organization ON products(organization_id);
CREATE INDEX idx_product_type ON products(type);
CREATE INDEX idx_product_active ON products(is_active);
CREATE INDEX idx_product_display_order ON products(display_order);
```

### Tabela: `prices`

```sql
CREATE TABLE prices (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    
    -- 🆕 TIPO DE PREÇO
    type VARCHAR(20) NOT NULL,
    -- Valores: 'RECURRING' (assinatura) ou 'ONE_TIME' (pagamento único)
    
    -- 🆕 INTERVALO DE RECORRÊNCIA (apenas para RECURRING)
    recurring_interval VARCHAR(20), -- 'month', 'year', 'week', 'day'
    -- NULL para preços ONE_TIME
    
    is_active BOOLEAN DEFAULT TRUE,
    stripe_price_id VARCHAR(255) UNIQUE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_price_type CHECK (type IN ('RECURRING', 'ONE_TIME')),
    CONSTRAINT chk_recurring_interval CHECK (
        (type = 'RECURRING' AND recurring_interval IS NOT NULL) OR
        (type = 'ONE_TIME' AND recurring_interval IS NULL)
    )
);

CREATE INDEX idx_price_product ON prices(product_id);
CREATE INDEX idx_price_type ON prices(type);
CREATE INDEX idx_price_active ON prices(is_active);
```

### Tabela: `subscriptions` (EXISTENTE - pequena modificação)

```sql
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    
    -- 🔄 MUDANÇA: FK para price ao invés de plan
    price_id BIGINT NOT NULL REFERENCES prices(id),
    
    status VARCHAR(50) NOT NULL,
    current_period_start TIMESTAMP,
    current_period_end TIMESTAMP,
    cancel_at_period_end BOOLEAN DEFAULT FALSE,
    
    stripe_subscription_id VARCHAR(255) UNIQUE,
    stripe_customer_id VARCHAR(255),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    canceled_at TIMESTAMP
);
```

### Tabela: `orders` (🆕 NOVA - para e-commerce)

```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    
    -- Items do pedido (JSONB flexível)
    items JSONB NOT NULL,
    /* Exemplo:
    [
        {
            "product_id": 123,
            "price_id": 456,
            "quantity": 2,
            "unit_amount": 5.00,
            "total": 10.00
        }
    ]
    */
    
    total_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    status VARCHAR(50) NOT NULL, -- 'PENDING', 'PAID', 'FAILED', 'REFUNDED'
    
    -- Stripe
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    stripe_checkout_session_id VARCHAR(255),
    
    -- Endereço de entrega (se aplicável)
    shipping_address JSONB,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP,
    
    CONSTRAINT chk_order_status CHECK (
        status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED', 'CANCELED')
    )
);

CREATE INDEX idx_order_user ON orders(user_id);
CREATE INDEX idx_order_organization ON orders(organization_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created_at ON orders(created_at DESC);
```

---

## 🔄 Fluxos de Checkout

### Fluxo 1: Assinatura (SUBSCRIPTION)

```java
// 1. Cliente escolhe produto tipo SUBSCRIPTION
Product product = productRepository.findById(productId);
if (product.getType() != ProductType.SUBSCRIPTION) {
    throw new IllegalArgumentException("Produto não é assinatura");
}

// 2. Cliente escolhe um price (ex: mensal ou anual)
Price price = priceRepository.findById(priceId);
if (price.getType() != PriceType.RECURRING) {
    throw new IllegalArgumentException("Preço não é recorrente");
}

// 3. Criar checkout session com mode: 'subscription'
SessionCreateParams params = SessionCreateParams.builder()
    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
    .setCustomer(user.getStripeCustomerId())
    .addLineItem(
        SessionCreateParams.LineItem.builder()
            .setPrice(price.getStripePriceId())
            .setQuantity(1L)
            .build()
    )
    .setSuccessUrl(frontendUrl + "/success?session_id={CHECKOUT_SESSION_ID}")
    .setCancelUrl(frontendUrl + "/cancel")
    .build();

Session session = Session.create(params);
return session.getUrl();
```

**Webhook:** `checkout.session.completed` → Criar registro em `subscriptions`

---

### Fluxo 2: E-commerce (ONE_TIME)

```java
// 1. Cliente escolhe produtos tipo ONE_TIME (pode ser múltiplos)
List<CartItem> cartItems = cart.getItems();

// 2. Validar todos são ONE_TIME
for (CartItem item : cartItems) {
    Product product = item.getProduct();
    if (product.getType() != ProductType.ONE_TIME) {
        throw new IllegalArgumentException("Carrinho contém produto de assinatura");
    }
}

// 3. Criar checkout session com mode: 'payment'
SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
    .setMode(SessionCreateParams.Mode.PAYMENT)
    .setCustomer(user.getStripeCustomerId());

// Adicionar line items
for (CartItem item : cartItems) {
    paramsBuilder.addLineItem(
        SessionCreateParams.LineItem.builder()
            .setPrice(item.getPrice().getStripePriceId())
            .setQuantity(item.getQuantity())
            .build()
    );
}

paramsBuilder
    .setSuccessUrl(frontendUrl + "/order/success?session_id={CHECKOUT_SESSION_ID}")
    .setCancelUrl(frontendUrl + "/cart");

Session session = Session.create(paramsBuilder.build());
return session.getUrl();
```

**Webhook:** `checkout.session.completed` → Criar registro em `orders`

---

## 🎛️ Configuração Multi-Tenant

### Enum: CommerceMode

```java
public enum CommerceMode {
    SUBSCRIPTIONS_ONLY,  // Apenas assinaturas (SaaS tradicional)
    ECOMMERCE_ONLY,      // Apenas e-commerce (loja virtual)
    HYBRID               // Ambos (plataforma completa)
}
```

### Lógica no Frontend

```typescript
// 1. Buscar configuração da organização
const org = await api.get('/api/organization/config');

// 2. Renderizar interface apropriada
switch (org.commerce_mode) {
    case 'SUBSCRIPTIONS_ONLY':
        return <PlansPage />; // Página de planos tradicional
        
    case 'ECOMMERCE_ONLY':
        return <ProductsGrid />; // Grade de produtos
        
    case 'HYBRID':
        return <UnifiedCommerce />; // Tabs ou navegação híbrida
}
```

### Lógica no Backend

```java
@GetMapping("/api/products")
public List<ProductResponseDTO> getProducts(
    @RequestParam(required = false) ProductType type,
    @AuthenticationPrincipal User user
) {
    Organization org = user.getOrganization();
    
    // Validar se organização suporta o tipo solicitado
    if (type == ProductType.ONE_TIME && 
        org.getCommerceMode() == CommerceMode.SUBSCRIPTIONS_ONLY) {
        throw new BadRequestException("Organização não suporta e-commerce");
    }
    
    if (type == ProductType.SUBSCRIPTION && 
        org.getCommerceMode() == CommerceMode.ECOMMERCE_ONLY) {
        throw new BadRequestException("Organização não suporta assinaturas");
    }
    
    // Buscar produtos
    return productService.findByOrganizationAndType(org, type);
}
```

---

## 🔧 Webhooks Inteligentes

### Handler Unificado

```java
@PostMapping("/webhooks/stripe")
public ResponseEntity<Void> handleStripeWebhook(
    @RequestBody String payload,
    @RequestHeader("Stripe-Signature") String signature
) {
    Event event = webhookService.constructEvent(payload, signature);
    
    switch (event.getType()) {
        case "checkout.session.completed":
            Session session = (Session) event.getDataObjectDeserializer()
                .getObject().orElseThrow();
            
            // 🎯 DECISÃO BASEADA NO MODE
            if ("subscription".equals(session.getMode())) {
                handleSubscriptionCheckout(session);
            } else if ("payment".equals(session.getMode())) {
                handlePaymentCheckout(session);
            }
            break;
            
        // ... outros eventos
    }
    
    return ResponseEntity.ok().build();
}

private void handleSubscriptionCheckout(Session session) {
    // 1. Extrair subscription_id
    String subId = session.getSubscription();
    
    // 2. Buscar dados no Stripe
    Subscription stripeSub = Subscription.retrieve(subId);
    
    // 3. Extrair price_id
    String stripePriceId = stripeSub.getItems().getData().get(0).getPrice().getId();
    
    // 4. Buscar price no banco
    Price price = priceRepository.findByStripePriceId(stripePriceId)
        .orElseThrow();
    
    // 5. Criar subscription no banco
    com.gestao.Subscription subscription = new com.gestao.Subscription();
    subscription.setUser(user);
    subscription.setPrice(price);
    subscription.setStatus("ACTIVE");
    subscription.setStripeSubscriptionId(subId);
    // ...
    
    subscriptionRepository.save(subscription);
}

private void handlePaymentCheckout(Session session) {
    // 1. Extrair payment_intent_id
    String piId = session.getPaymentIntent();
    
    // 2. Extrair line items
    SessionListLineItemsParams params = SessionListLineItemsParams.builder().build();
    LineItemCollection lineItems = session.listLineItems(params);
    
    // 3. Criar order
    Order order = new Order();
    order.setUser(user);
    order.setStripePaymentIntentId(piId);
    order.setStripeCheckoutSessionId(session.getId());
    order.setStatus("PAID");
    order.setTotalAmount(session.getAmountTotal() / 100.0);
    
    // 4. Montar items (JSONB)
    List<OrderItem> items = new ArrayList<>();
    for (LineItem item : lineItems.getData()) {
        Price price = priceRepository.findByStripePriceId(item.getPrice().getId())
            .orElseThrow();
        
        items.add(new OrderItem(
            price.getProduct().getId(),
            price.getId(),
            item.getQuantity(),
            item.getAmountTotal() / 100.0
        ));
    }
    order.setItems(items);
    
    orderRepository.save(order);
}
```

---

## 📋 Checklist de Migração

### Fase 1: Preparação (Documentação e Planejamento)

- [x] Criar `ARQUITETURA-HIBRIDA.md` ✅
- [x] Atualizar `ARQUITETURA.md` com novos conceitos ✅
- [x] Atualizar `CHECKLIST-DESENVOLVIMENTO.md` ✅
- [ ] Criar migration SQL para nova estrutura
- [ ] Atualizar diagramas de arquitetura

### Fase 2: Modelo de Dados

- [ ] Criar entidade `Organization`
- [ ] Modificar entidade `User` (adicionar `organization_id`)
- [ ] Criar enum `CommerceMode`
- [ ] Criar enum `ProductType`
- [ ] Criar enum `PriceType`
- [ ] Criar entidade `Product` (substituindo `Plan`)
- [ ] Criar entidade `Price`
- [ ] Modificar entidade `Subscription` (FK para `Price`)
- [ ] Criar entidade `Order` (nova)
- [ ] Criar repositórios correspondentes

### Fase 3: Lógica de Negócio

- [ ] Criar `ProductService`
- [ ] Criar `PriceService`
- [ ] Criar `OrganizationService`
- [ ] Modificar `SubscriptionService` (usar `Price`)
- [ ] Criar `OrderService` (novo)
- [ ] Criar `CheckoutService` unificado (detecta tipo)
- [ ] Atualizar `StripeWebhookService` (handler inteligente)

### Fase 4: API e DTOs

- [ ] Criar DTOs de `Product`
- [ ] Criar DTOs de `Price`
- [ ] Criar DTOs de `Order`
- [ ] Criar `ProductController`
- [ ] Criar `OrderController`
- [ ] Atualizar `SubscriptionController`
- [ ] Atualizar `WebhookController`

### Fase 5: Testes

- [ ] Testes unitários `ProductService`
- [ ] Testes unitários `CheckoutService` (ambos modos)
- [ ] Testes integração webhook (subscription)
- [ ] Testes integração webhook (payment)
- [ ] Testes multi-tenant (commerce_mode)

### Fase 6: Documentação

- [ ] Swagger atualizado com novos endpoints
- [ ] README.md atualizado
- [ ] Guia de migração de dados (se houver)

---

## 🚀 Benefícios da Nova Arquitetura

### Escalabilidade

✅ **Crescimento Orgânico**: V1 (assinaturas) → V2 (e-commerce) sem refatoração  
✅ **Multi-Tenant**: Cada organização opera de forma independente  
✅ **Configurável**: Modo de operação definido por cliente  

### Flexibilidade

✅ **Produtos Ilimitados**: Tanto assinaturas quanto produtos físicos/digitais  
✅ **Múltiplos Preços**: Um produto pode ter preços mensal, anual, etc  
✅ **Metadata JSONB**: Extensível sem alterar schema  

### Alinhamento com Stripe

✅ **Modelo Espelhado**: `Product` + `Price` igual ao Stripe  
✅ **Sincronização Fácil**: Estrutura compatível com webhooks  
✅ **Menos Impedância**: Tradução direta Stripe ↔ Banco  

---

## 📚 Próximos Passos

1. **Aplicar migration atual** (002 - Firebase Auth)
2. **Criar migration 003** (Arquitetura Híbrida)
3. **Implementar entidades** seguindo novo modelo
4. **Atualizar serviços** existentes
5. **Implementar checkout** unificado
6. **Testar fluxos** (subscription + payment)

---

**Documento criado em:** 2025-10-05  
**Autor:** Time de Arquitetura  
**Status:** ✅ Aprovado - Pronto para implementação  
**Próxima revisão:** Após migração de dados completa
