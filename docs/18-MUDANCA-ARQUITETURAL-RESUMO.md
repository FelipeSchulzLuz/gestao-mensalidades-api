# 🚨 IMPORTANTE: Mudança Arquitetural - Leia Antes de Codificar!

> **Data:** 2025-10-05  
> **Impacto:** 🔴 CRÍTICO - Toda implementação futura afetada  
> **Status:** ✅ Aprovado - Implementação iniciará em breve

---

## 🎯 TL;DR (Resumo Executivo)

### O Que Mudou?

```
❌ ANTES (Arquitetura Limitada):
gestao-mensalidades-api = Sistema de Assinaturas
└── Entidade "Plan" para planos mensais/anuais

✅ AGORA (Arquitetura Escalável):
gestao-mensalidades-api = Plataforma de Comércio Híbrida Multi-Tenant
├── Entidade "Product" (genérica)
├── Entidade "Price" (múltiplos preços por produto)
├── Suporte a Assinaturas (recorrente)
└── Suporte a E-commerce (pagamento único)
```

### Por Que Mudamos?

**Problema:** Arquitetura antiga não permitia vender produtos avulsos (doces, cursos, etc)

**Solução:** Generalizar para modelo **Product + Price** (igual ao Stripe)

### Impacto para Você (Desenvolvedor)

🚫 **NÃO** use mais a palavra "Plano" no código  
✅ **USE** "Product" e "Price" em todo lugar  
✅ **CONSIDERE** que produtos podem ser SUBSCRIPTION ou ONE_TIME  
✅ **LEMBRE** que checkout pode ser 'subscription' ou 'payment'

---

## 📋 Checklist Rápido

Antes de escrever QUALQUER código novo, responda:

- [ ] Li o documento [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md)?
- [ ] Entendi a diferença entre Product (o quê) e Price (como vender)?
- [ ] Sei qual ProductType usar (SUBSCRIPTION ou ONE_TIME)?
- [ ] Considerei o commerce_mode da organização?
- [ ] Planejei webhook diferente para subscription vs payment?

Se qualquer resposta for "não", **PARE** e leia a documentação primeiro!

---

## 🏗️ Novo Modelo Mental

### Conceito 1: Product = Abstração Genérica

```
Um Product é QUALQUER COISA que você vende:
✅ Plano de Assinatura Mensal
✅ Doce de Chocolate (unitário)
✅ Curso Online (pagamento único)
✅ Consulta (agendamento único)

Todos são Products! O que muda é o TYPE.
```

### Conceito 2: Price = Como Vender

```
Um Product pode ter MÚLTIPLOS Prices:

Product: "Plano Premium"
├── Price 1: R$ 99/mês (RECURRING, month)
├── Price 2: R$ 990/ano (RECURRING, year)
└── Price 3: R$ 29 trial (ONE_TIME)

Product: "Doce de Festa"
└── Price 1: R$ 5,00 (ONE_TIME)
```

### Conceito 3: CommerceMode = Configuração da Organização

```
Organization "SaaS XYZ"
└── commerce_mode: SUBSCRIPTIONS_ONLY
    → Frontend mostra "Página de Planos"
    → Só aceita checkout.session com mode='subscription'

Organization "Loja de Doces"
└── commerce_mode: ECOMMERCE_ONLY
    → Frontend mostra "Grade de Produtos"
    → Só aceita checkout.session com mode='payment'

Organization "Plataforma Híbrida"
└── commerce_mode: HYBRID
    → Frontend tem AMBAS as visões
    → Aceita AMBOS os tipos de checkout
```

---

## 🚦 Guia de Decisão Rápido

### Quando Criar um Produto?

```java
// ERRADO (terminologia antiga)
Plan plano = new Plan("Plano Mensal");

// CERTO (nova arquitetura)
Product produto = new Product(
    "Plano Mensal",
    ProductType.SUBSCRIPTION  // ← Importante!
);
```

### Quando Criar um Preço?

```java
// Um produto pode ter MÚLTIPLOS preços
Product produto = findProduct(123);

Price precoMensal = new Price(
    produto,
    99.90,
    "BRL",
    PriceType.RECURRING,  // ← Importante!
    "month"               // ← Intervalo (só para RECURRING)
);

Price precoAnual = new Price(
    produto,
    990.00,
    "BRL",
    PriceType.RECURRING,
    "year"
);
```

### Quando Criar Checkout?

```java
// SUBSCRIPTION (assinatura recorrente)
if (product.getType() == ProductType.SUBSCRIPTION) {
    SessionCreateParams params = SessionCreateParams.builder()
        .setMode(SessionCreateParams.Mode.SUBSCRIPTION)  // ← Crucial!
        .addLineItem(...)
        .build();
}

// ONE_TIME (pagamento único - e-commerce)
if (product.getType() == ProductType.ONE_TIME) {
    SessionCreateParams params = SessionCreateParams.builder()
        .setMode(SessionCreateParams.Mode.PAYMENT)  // ← Crucial!
        .addLineItem(...)
        .build();
}
```

### Quando Processar Webhook?

```java
if ("subscription".equals(session.getMode())) {
    // Criar registro em tabela "subscriptions"
    handleSubscriptionCheckout(session);
} else if ("payment".equals(session.getMode())) {
    // Criar registro em tabela "orders"
    handlePaymentCheckout(session);
}
```

---

## 📊 Comparação Visual

### Schema Antes vs Depois

```sql
-- ❌ ANTES (Rígido)
CREATE TABLE plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    price DECIMAL(10,2),
    interval VARCHAR(20)  -- 'month', 'year'
);

-- ✅ DEPOIS (Flexível)
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT,
    name VARCHAR(255),
    type VARCHAR(20),  -- 'SUBSCRIPTION' ou 'ONE_TIME'
    metadata JSONB     -- Dados customizados
);

CREATE TABLE prices (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT,
    amount DECIMAL(10,2),
    type VARCHAR(20),           -- 'RECURRING' ou 'ONE_TIME'
    recurring_interval VARCHAR(20)  -- 'month', 'year' (ou NULL)
);
```

---

## 🎓 Exemplos Práticos

### Exemplo 1: Criar Produto de Assinatura

```java
// 1. Criar produto
Product produto = new Product();
produto.setOrganization(organization);
produto.setName("Plano Premium");
produto.setDescription("Acesso completo à plataforma");
produto.setType(ProductType.SUBSCRIPTION);  // ← Assinatura
produto.setIsActive(true);

productRepository.save(produto);

// 2. Criar preço mensal
Price precoMensal = new Price();
precoMensal.setProduct(produto);
precoMensal.setAmount(new BigDecimal("99.90"));
precoMensal.setCurrency("BRL");
precoMensal.setType(PriceType.RECURRING);  // ← Recorrente
precoMensal.setRecurringInterval("month");
precoMensal.setStripePriceId(stripePrice.getId());

priceRepository.save(precoMensal);

// 3. Criar checkout
String checkoutUrl = checkoutService.createCheckoutSession(
    user,
    precoMensal.getId(),
    CheckoutMode.SUBSCRIPTION  // ← Mode importante!
);
```

### Exemplo 2: Criar Produto de E-commerce

```java
// 1. Criar produto
Product doce = new Product();
doce.setOrganization(organization);
doce.setName("Brigadeiro Gourmet");
doce.setDescription("Brigadeiro artesanal de chocolate belga");
doce.setType(ProductType.ONE_TIME);  // ← Pagamento único
doce.setMetadata(Map.of(
    "category", "doces",
    "weight", "50g",
    "allergens", List.of("leite", "soja")
));

productRepository.save(doce);

// 2. Criar preço unitário
Price preco = new Price();
preco.setProduct(doce);
preco.setAmount(new BigDecimal("5.00"));
preco.setCurrency("BRL");
preco.setType(PriceType.ONE_TIME);  // ← Pagamento único
preco.setRecurringInterval(null);   // ← NULL para ONE_TIME

priceRepository.save(preco);

// 3. Criar checkout (carrinho com múltiplos itens)
List<CartItem> cart = List.of(
    new CartItem(doce, preco, 3)  // 3 unidades
);

String checkoutUrl = checkoutService.createCheckoutSession(
    user,
    cart,
    CheckoutMode.PAYMENT  // ← Mode importante!
);
```

---

## ⚠️ Armadilhas Comuns

### ❌ Armadilha 1: Usar "Plan" no código

```java
// ❌ ERRADO
Plan plan = new Plan("Mensal");

// ✅ CERTO
Product product = new Product("Mensal", ProductType.SUBSCRIPTION);
Price price = new Price(product, 99.90, PriceType.RECURRING, "month");
```

### ❌ Armadilha 2: Esquecer o Type

```java
// ❌ ERRADO (não especifica tipo)
Product product = new Product("Algo");

// ✅ CERTO
Product product = new Product("Algo", ProductType.SUBSCRIPTION);
// ou
Product product = new Product("Algo", ProductType.ONE_TIME);
```

### ❌ Armadilha 3: Mode errado no Checkout

```java
// ❌ ERRADO (product é ONE_TIME mas mode é SUBSCRIPTION)
if (product.getType() == ProductType.ONE_TIME) {
    session.setMode("subscription");  // 💥 ERRO!
}

// ✅ CERTO
if (product.getType() == ProductType.ONE_TIME) {
    session.setMode("payment");  // ✅ Correto
}
```

### ❌ Armadilha 4: Webhook não distingue

```java
// ❌ ERRADO (sempre cria subscription)
@EventHandler("checkout.session.completed")
public void handle(Event event) {
    createSubscription(event);  // 💥 E se for pagamento único?
}

// ✅ CERTO
@EventHandler("checkout.session.completed")
public void handle(Event event) {
    Session session = extractSession(event);
    
    if ("subscription".equals(session.getMode())) {
        createSubscription(session);
    } else if ("payment".equals(session.getMode())) {
        createOrder(session);
    }
}
```

---

## 📚 Leitura Obrigatória

Antes de implementar QUALQUER feature:

1. **[17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md)** - Documento completo (30 min)
2. **[02-ARQUITETURA.md](./02-ARQUITETURA.md)** - Clean Architecture (15 min)
3. **[04-STRIPE.md](./04-STRIPE.md)** - Integração Stripe (15 min)

**Total:** 1 hora de leitura = Economiza 10 horas de refatoração! 🎯

---

## 🚀 Próximos Passos

### Para o Time

1. ✅ Ler este documento
2. ✅ Ler [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md)
3. ⏸️ Aguardar migration 003 (nova estrutura de dados)
4. ⏸️ Implementar entidades Product e Price
5. ⏸️ Atualizar serviços existentes
6. ⏸️ Testar fluxos de checkout

### Para o Tech Lead

1. ✅ Criar migration 003 (organizations, products, prices, orders)
2. ⏸️ Definir ordem de implementação das fases
3. ⏸️ Atualizar checklist de desenvolvimento
4. ⏸️ Code review garantindo nova terminologia

---

## 💬 Perguntas Frequentes

### "Preciso refatorar código existente?"

**Resposta:** Depende. Se você já implementou algo com "Plan", aguarde orientação. Provavelmente vamos criar adaptador temporário durante transição.

### "Posso ainda usar 'Plan' no frontend?"

**Resposta:** SIM! No frontend você pode mostrar "Planos" como UI/UX. Mas no backend, use Product + Price.

### "E se minha organização só vende assinaturas?"

**Resposta:** Perfeito! Você define `commerce_mode = SUBSCRIPTIONS_ONLY` e só cria produtos do tipo SUBSCRIPTION. A arquitetura suporta isso perfeitamente.

### "Isso afeta o MVP?"

**Resposta:** NÃO! O MVP continua sendo apenas assinaturas. Mas agora a fundação está preparada para e-commerce futuro sem refatoração dolorosa.

### "Quando começa a implementação?"

**Resposta:** Após aplicação da migration 002 (Firebase Auth) e criação da migration 003 (nova estrutura).

---

## 🆘 Precisa de Ajuda?

- **Dúvidas sobre arquitetura:** Ver [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md)
- **Dúvidas sobre implementação:** Perguntar no canal #dev
- **Dúvidas sobre Stripe:** Ver [04-STRIPE.md](./04-STRIPE.md)
- **Código exemplo:** Ver seção "Exemplos Práticos" acima

---

## ✅ Confirmação de Leitura

Antes de começar a codificar, confirme:

- [ ] Li este documento completo
- [ ] Li [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md)
- [ ] Entendi a diferença entre Product e Price
- [ ] Entendi os dois tipos de checkout (subscription vs payment)
- [ ] Sei como processar webhooks para cada tipo
- [ ] Estou pronto para usar a nova terminologia

**Se marcou todos:** Parabéns! Você está pronto! 🎉

**Se falta algum:** Volte e leia a documentação marcada ⬆️

---

**Documento criado em:** 2025-10-05  
**Última atualização:** 2025-10-05  
**Revisão obrigatória:** Antes de qualquer implementação  
**Próxima atualização:** Após migration 003 aplicada
