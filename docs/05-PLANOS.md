# 📋 Arquitetura de Gerenciamento de Planos

> ⚠️ **STATUS: DEPRECATED - Documento Obsoleto**  
> **Este documento descreve a arquitetura ANTIGA baseada em "Planos".**  
> 
> **📖 Para a arquitetura ATUAL, leia:**  
> - **[17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md)** - Arquitetura completa Products + Prices  
> - **[18-MUDANCA-ARQUITETURAL-RESUMO.md](./18-MUDANCA-ARQUITETURAL-RESUMO.md)** - Resumo da mudança  
>
> **🔄 Mudança Principal:**  
> `Plan` → `Product` + `Price` (múltiplos preços por produto)
>
> **Manter este documento apenas como referência histórica.**

---

## 🎯 Estratégia Recomendada: Banco de Dados + Admin Panel

### Decisão de Design

**✅ Usar Banco de Dados como fonte principal**

**Razões:**
1. **Flexibilidade:** Admin pode criar/editar/desativar planos sem deploy
2. **Sincronização:** Integração bidirecional com Stripe
3. **Auditoria:** Histórico de mudanças
4. **Testes:** Fácil criar planos de teste
5. **Escalabilidade:** Adicionar novos planos sem código

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                      STRIPE DASHBOARD                        │
│  (Produtos e Preços criados manualmente ou via API)         │
└─────────────────────────────────────────────────────────────┘
                            ↕ Sincronização
┌─────────────────────────────────────────────────────────────┐
│                    BANCO DE DADOS (PostgreSQL)               │
│  Table: plans                                                │
│  - id, name, description, price, interval                    │
│  - stripePriceId (vinculado ao Stripe)                       │
│  - isActive, isFeatured, displayOrder                        │
└─────────────────────────────────────────────────────────────┘
                            ↕ CRUD
┌─────────────────────────────────────────────────────────────┐
│                      ADMIN PANEL (Web)                       │
│  - Listar planos                                             │
│  - Criar novo plano (cria no Stripe automaticamente)        │
│  - Editar plano (sincroniza com Stripe)                     │
│  - Ativar/Desativar plano                                   │
│  - Destacar plano                                            │
│  - Ordenar planos                                            │
└─────────────────────────────────────────────────────────────┘
                            ↕ API REST
┌─────────────────────────────────────────────────────────────┐
│                      FRONTEND (React/Vue)                    │
│  - Exibe planos ativos ordenados                            │
│  - Permite assinatura                                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 Modelo de Dados (Plan Entity)

```java
@Entity
@Table(name = "plans")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 100)
    private String name;           // "Plano Básico", "Plano Pro"
    
    @Size(max = 500)
    private String description;    // Descrição do plano
    
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;      // 99.90, 990.00
    
    @NotNull
    @Size(min = 3, max = 3)
    private String currency;       // BRL, USD
    
    @NotNull
    @Enumerated(EnumType.STRING)
    private BillingInterval interval; // MONTH, YEAR
    
    @Column(unique = true)
    private String stripePriceId;  // price_xxxxxxxxxxxxx
    
    private String stripeProductId; // prod_xxxxxxxxxxxxx
    
    @NotNull
    private Boolean isActive = true; // Exibir no frontend?
    
    private Boolean isFeatured = false; // Destacar como "Mais Popular"?
    
    private Integer displayOrder = 0; // Ordem de exibição (0 = primeiro)
    
    // Recursos do plano (JSON ou tabela separada)
    @Column(columnDefinition = "jsonb")
    private String features;       // ["Recurso 1", "Recurso 2", ...]
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

---

## 🔧 Fluxos de Operação

### 1. **Criar Novo Plano (via Admin)**

```
Admin → POST /api/admin/plans
  {
    "name": "Plano Premium",
    "description": "Acesso completo",
    "price": 149.90,
    "currency": "BRL",
    "interval": "MONTH",
    "features": ["Recurso 1", "Recurso 2"]
  }

↓ Backend:
1. Valida dados
2. Cria PRODUTO no Stripe (se não existir)
3. Cria PREÇO no Stripe
4. Salva no banco com stripePriceId
5. Retorna plano criado

Resultado:
- Plano disponível imediatamente no frontend
- Sincronizado com Stripe
```

### 2. **Editar Plano (via Admin)**

```
Admin → PUT /api/admin/plans/{id}
  {
    "description": "Nova descrição",
    "isActive": true,
    "isFeatured": true,
    "displayOrder": 1
  }

↓ Backend:
1. Atualiza no banco
2. Se mudou preço/nome: atualiza no Stripe
3. Retorna plano atualizado

Resultado:
- Mudanças refletidas imediatamente
- Histórico mantido
```

### 3. **Desativar Plano (via Admin)**

```
Admin → PATCH /api/admin/plans/{id}/deactivate

↓ Backend:
1. Define isActive = false
2. Plano deixa de aparecer no frontend
3. Assinaturas existentes continuam funcionando

Resultado:
- Novos usuários não veem o plano
- Assinantes atuais não são afetados
```

### 4. **Sincronizar com Stripe (Webhook)**

```
Stripe → Webhook: product.updated / price.updated

↓ Backend:
1. Recebe evento
2. Busca plano por stripePriceId
3. Atualiza dados no banco

Resultado:
- Mudanças no Stripe refletidas no sistema
```

---

## 🎨 Endpoints da API

### Público (Frontend)

```
GET /api/plans
  → Lista planos ativos, ordenados por displayOrder
  → Retorna apenas planos com isActive = true
  
GET /api/plans/{id}
  → Detalhes de um plano específico
```

### Admin (Protegido - apenas ADMIN)

```
GET /api/admin/plans
  → Lista TODOS os planos (ativos e inativos)
  → Inclui métricas (qtd assinantes, receita)

POST /api/admin/plans
  → Cria novo plano (e produto/preço no Stripe)

PUT /api/admin/plans/{id}
  → Atualiza plano

PATCH /api/admin/plans/{id}/activate
  → Ativa plano

PATCH /api/admin/plans/{id}/deactivate
  → Desativa plano

DELETE /api/admin/plans/{id}
  → Remove plano (soft delete - isActive = false)

POST /api/admin/plans/{id}/sync
  → Força sincronização com Stripe

POST /api/admin/plans/sync-all
  → Sincroniza todos os planos com Stripe

GET /api/admin/plans/{id}/subscribers
  → Lista assinantes do plano

GET /api/admin/plans/metrics
  → Métricas gerais (MRR, ARR, conversão)
```

---

## 🔐 Permissões

```java
// SecurityConfig.java
http
  .authorizeHttpRequests(auth -> auth
    // Público
    .requestMatchers(GET, "/api/plans/**").permitAll()
    
    // Admin apenas
    .requestMatchers("/api/admin/plans/**").hasRole("ADMIN")
    
    // Usuário autenticado
    .requestMatchers("/api/subscriptions/**").authenticated()
  )
```

---

## 💾 Configuração Inicial (.env)

**Apenas IDs de produtos base (opcional):**

```env
# IDs dos produtos Stripe (para referência, não obrigatório)
STRIPE_PRODUCT_MONTHLY=prod_SgzMGAVP7sdb7s
STRIPE_PRODUCT_YEARLY=prod_Sj9inVJYHvyr0M
```

**Seed inicial no banco:**

```sql
-- Script de inicialização (data.sql ou Flyway)
INSERT INTO plans (name, description, price, currency, interval, stripe_price_id, stripe_product_id, is_active, is_featured, display_order)
VALUES 
  ('Plano Mensal', 'Acesso mensal completo', 99.90, 'BRL', 'MONTH', 'price_xxxxx', 'prod_SgzMGAVP7sdb7s', true, false, 1),
  ('Plano Anual', 'Acesso anual com desconto', 990.00, 'BRL', 'YEAR', 'price_yyyyy', 'prod_Sj9inVJYHvyr0M', true, true, 0);
```

---

## 🎛️ Admin Panel (Frontend)

### Tela de Gerenciamento de Planos

```
┌─────────────────────────────────────────────────────────┐
│  GERENCIAR PLANOS                        [+ Novo Plano] │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  🏆 Plano Anual                    [Editar] [Desativar] │
│     R$ 990,00/ano                                        │
│     ⭐ Destacado | 🟢 Ativo | 📊 123 assinantes        │
│     Ordem: ↑ ↓                                           │
│                                                          │
│  📦 Plano Mensal                   [Editar] [Desativar] │
│     R$ 99,90/mês                                         │
│     🟢 Ativo | 📊 456 assinantes                        │
│     Ordem: ↑ ↓                                           │
│                                                          │
│  💼 Plano Premium (INATIVO)        [Editar] [Ativar]    │
│     R$ 199,90/mês                                        │
│     🔴 Inativo | 📊 0 assinantes                        │
│                                                          │
└─────────────────────────────────────────────────────────┘

📊 Métricas
- MRR (Monthly Recurring Revenue): R$ 45.284,40
- ARR (Annual Recurring Revenue): R$ 543.412,80
- Total de Assinantes: 579
- Taxa de Conversão: 12.5%
```

### Formulário de Criação/Edição

```
┌─────────────────────────────────────────────────────────┐
│  CRIAR NOVO PLANO                                        │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  Nome do Plano *                                         │
│  [Plano Premium                                      ]   │
│                                                          │
│  Descrição                                               │
│  [Acesso completo a todos os recursos               ]   │
│                                                          │
│  Preço *                         Moeda *                 │
│  [199.90                    ]    [BRL ▼]                │
│                                                          │
│  Intervalo *                                             │
│  ○ Mensal  ● Anual                                      │
│                                                          │
│  Recursos (um por linha)                                │
│  [Recurso 1                                          ]   │
│  [Recurso 2                                          ]   │
│  [+ Adicionar recurso]                                  │
│                                                          │
│  ☑ Ativo (exibir no frontend)                          │
│  ☑ Destacado (marcar como "Mais Popular")              │
│                                                          │
│  Ordem de Exibição                                       │
│  [0                              ] (0 = primeiro)       │
│                                                          │
│  [Cancelar]              [Criar e Sincronizar Stripe]   │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## 🔄 Sincronização Bidirecional

### Stripe → Sistema (Webhooks)

```java
@PostMapping("/api/webhooks/stripe")
public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
    Event event = stripe.webhooks.constructEvent(payload, signature, webhookSecret);
    
    switch (event.getType()) {
        case "price.created":
        case "price.updated":
            syncPriceFromStripe(event.getData().getObject());
            break;
        case "product.updated":
            syncProductFromStripe(event.getData().getObject());
            break;
    }
    
    return ResponseEntity.ok("Webhook processed");
}
```

### Sistema → Stripe (Service)

```java
@Service
public class PlanService {
    
    public Plan createPlan(CreatePlanRequest request) {
        // 1. Criar produto no Stripe
        Product stripeProduct = stripeService.createProduct(request.getName());
        
        // 2. Criar preço no Stripe
        Price stripePrice = stripeService.createPrice(
            stripeProduct.getId(),
            request.getPrice(),
            request.getCurrency(),
            request.getInterval()
        );
        
        // 3. Salvar no banco
        Plan plan = new Plan();
        plan.setName(request.getName());
        plan.setStripePriceId(stripePrice.getId());
        plan.setStripeProductId(stripeProduct.getId());
        // ... outros campos
        
        return planRepository.save(plan);
    }
}
```

---

## 📈 Vantagens da Abordagem

### Para o Admin:
- ✅ Criar/editar planos sem deploy
- ✅ Testar diferentes preços facilmente
- ✅ Ativar/desativar planos sazonais
- ✅ Destacar planos específicos
- ✅ Ver métricas em tempo real
- ✅ Auditoria de mudanças

### Para o Desenvolvedor:
- ✅ Código desacoplado de configurações
- ✅ Testes facilitados
- ✅ Sincronização automática com Stripe
- ✅ Histórico de mudanças
- ✅ Rollback fácil

### Para o Sistema:
- ✅ Escalável
- ✅ Flexível
- ✅ Auditável
- ✅ Testável
- ✅ Mantível

---

## 🎯 Recomendação Final

### Implementar em Fases:

**Fase 1 (MVP):**
- ✅ Entidade Plan no banco (JÁ FEITO)
- ✅ Endpoints públicos (GET /api/plans)
- ✅ Seed inicial com 2 planos

**Fase 2 (Admin Basic):**
- CRUD completo de planos (admin)
- Ativar/desativar
- Ordenação

**Fase 3 (Sincronização):**
- Criar produto/preço no Stripe ao criar plano
- Webhooks para sincronizar mudanças
- Endpoint de sincronização forçada

**Fase 4 (Métricas):**
- Dashboard com MRR/ARR
- Análise de conversão
- Relatórios

---

## 💡 Casos de Uso Reais

### Cenário 1: Black Friday
```
Admin → Cria "Plano Anual Black Friday"
       - Preço: R$ 790,00 (desconto de R$ 200)
       - isFeatured: true
       - displayOrder: 0 (primeiro)
       - isActive: true

→ Plano aparece no topo do site
→ Após Black Friday: isActive = false
→ Assinantes mantêm o preço promocional
```

### Cenário 2: Teste A/B de Preços
```
Admin → Duplica "Plano Mensal"
       - Plano A: R$ 99,90
       - Plano B: R$ 89,90
       
→ Exibe aleatoriamente para usuários
→ Analisa conversão
→ Desativa o pior performático
```

### Cenário 3: Plano Empresarial
```
Admin → Cria "Plano Empresarial"
       - Preço: R$ 2.990,00/mês
       - Features: ["Suporte prioritário", "API", ...]
       - displayOrder: 0
       
→ Disponível imediatamente
→ Sem deploy
```

---

**Conclusão:** Use banco de dados como fonte principal + admin panel. É a solução mais flexível, escalável e profissional. 🚀
