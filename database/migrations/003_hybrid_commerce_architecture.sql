-- ============================================================================
-- Migration 003: Hybrid Commerce Architecture
-- ============================================================================
-- Data: 2025-10-06
-- Autor: Time de Desenvolvimento
-- Descrição: Transição de arquitetura limitada (Plans) para arquitetura
--            híbrida escalável (Products + Prices + Organizations)
--
-- Mudanças principais:
--   1. Criar tabela organizations (multi-tenant)
--   2. Criar tabela products (substitui plans - mais genérico)
--   3. Criar tabela prices (preços flexíveis - recorrentes ou únicos)
--   4. Criar tabela orders (suporte a e-commerce)
--   5. Migrar dados: plans → products + prices
--   6. Atualizar relacionamentos (subscriptions → prices)
--   7. Deprecar tabela plans (manter temporariamente para rollback)
--
-- Compatibilidade: PostgreSQL 15+
-- Rollback: Disponível (ver final do arquivo)
-- ============================================================================

-- ============================================================================
-- PARTE 1: CRIAR NOVAS TABELAS
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Tabela: organizations
-- Descrição: Suporte multi-tenant. Cada organização pode operar em modo
--            diferente (apenas assinaturas, apenas e-commerce, ou híbrido)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS organizations (
    id BIGSERIAL PRIMARY KEY,
    
    -- Identificação
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE, -- URL-friendly: "minha-empresa"
    
    -- Configuração de Negócio
    commerce_mode VARCHAR(30) NOT NULL DEFAULT 'SUBSCRIPTIONS_ONLY',
    -- Valores: 'SUBSCRIPTIONS_ONLY', 'ECOMMERCE_ONLY', 'HYBRID'
    
    -- Integração Stripe
    stripe_account_id VARCHAR(255) UNIQUE, -- Para Stripe Connect (futuro)
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Metadados
    metadata JSONB, -- Dados customizados flexíveis
    
    -- Auditoria
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_commerce_mode CHECK (
        commerce_mode IN ('SUBSCRIPTIONS_ONLY', 'ECOMMERCE_ONLY', 'HYBRID')
    )
);

-- Índices para organizations
CREATE INDEX idx_organization_slug ON organizations(slug);
CREATE INDEX idx_organization_active ON organizations(is_active);
CREATE INDEX idx_organization_stripe_account ON organizations(stripe_account_id) WHERE stripe_account_id IS NOT NULL;

-- Comentários
COMMENT ON TABLE organizations IS 'Organizações (multi-tenant). Cada organização pode vender assinaturas, produtos avulsos, ou ambos.';
COMMENT ON COLUMN organizations.commerce_mode IS 'Modo de operação: SUBSCRIPTIONS_ONLY (apenas planos), ECOMMERCE_ONLY (apenas produtos avulsos), HYBRID (ambos)';
COMMENT ON COLUMN organizations.metadata IS 'Dados customizados em JSON. Ex: {"industry": "education", "logo_url": "..."}';

-- ----------------------------------------------------------------------------
-- Tabela: products
-- Descrição: Substitui "plans". Mais genérico - pode ser assinatura ou produto único
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    
    -- Relacionamentos
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    
    -- Identificação
    name VARCHAR(255) NOT NULL,
    description TEXT,
    
    -- 🆕 TIPO DE PRODUTO (diferencial chave)
    type VARCHAR(20) NOT NULL,
    -- Valores: 'SUBSCRIPTION' (planos recorrentes) ou 'ONE_TIME' (produtos avulsos)
    
    -- Visibilidade e Ordenação
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE, -- Destacar na listagem
    display_order INT DEFAULT 0, -- Ordem de exibição (menor = primeiro)
    
    -- Features/Benefícios (para planos)
    features JSONB, -- Ex: ["10GB Storage", "Email Support", "Custom Domain"]
    
    -- Metadados Customizados
    metadata JSONB, -- Ex: {"category": "doces", "weight_kg": 0.5, "sku": "DOC-001"}
    
    -- Integração Stripe
    stripe_product_id VARCHAR(255) UNIQUE,
    
    -- Auditoria
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_product_type CHECK (type IN ('SUBSCRIPTION', 'ONE_TIME'))
);

-- Índices para products
CREATE INDEX idx_product_organization ON products(organization_id);
CREATE INDEX idx_product_type ON products(type);
CREATE INDEX idx_product_active ON products(is_active);
CREATE INDEX idx_product_featured ON products(is_featured) WHERE is_featured = TRUE;
CREATE INDEX idx_product_display_order ON products(display_order);
CREATE INDEX idx_product_stripe ON products(stripe_product_id) WHERE stripe_product_id IS NOT NULL;

-- Comentários
COMMENT ON TABLE products IS 'Produtos genéricos. Podem ser assinaturas (SUBSCRIPTION) ou produtos avulsos (ONE_TIME).';
COMMENT ON COLUMN products.type IS 'SUBSCRIPTION = planos recorrentes | ONE_TIME = produtos avulsos (e-commerce)';
COMMENT ON COLUMN products.features IS 'Array JSON de benefícios/features. Ex: ["Feature 1", "Feature 2"]';
COMMENT ON COLUMN products.metadata IS 'Dados customizados. Ex: categoria, peso, SKU, cor, tamanho, etc.';

-- ----------------------------------------------------------------------------
-- Tabela: prices
-- Descrição: Preços dos produtos. Um produto pode ter múltiplos preços
--            (ex: mensal, anual, promoção, diferentes moedas)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prices (
    id BIGSERIAL PRIMARY KEY,
    
    -- Relacionamentos
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    
    -- Valores
    amount DECIMAL(10, 2) NOT NULL CHECK (amount >= 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL', -- ISO 4217: BRL, USD, EUR, etc.
    
    -- 🆕 TIPO DE PREÇO
    type VARCHAR(20) NOT NULL,
    -- Valores: 'RECURRING' (cobrança recorrente) ou 'ONE_TIME' (cobrança única)
    
    -- 🆕 RECORRÊNCIA (apenas para RECURRING)
    recurring_interval VARCHAR(20), -- 'day', 'week', 'month', 'year'
    recurring_interval_count INT DEFAULT 1, -- Ex: 3 (para cobrar a cada 3 meses)
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Integração Stripe
    stripe_price_id VARCHAR(255) UNIQUE,
    
    -- Auditoria
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_price_type CHECK (type IN ('RECURRING', 'ONE_TIME')),
    CONSTRAINT chk_recurring_consistency CHECK (
        (type = 'RECURRING' AND recurring_interval IS NOT NULL) OR
        (type = 'ONE_TIME' AND recurring_interval IS NULL)
    ),
    CONSTRAINT chk_recurring_interval CHECK (
        recurring_interval IS NULL OR 
        recurring_interval IN ('day', 'week', 'month', 'year')
    ),
    CONSTRAINT chk_recurring_count CHECK (
        recurring_interval_count IS NULL OR recurring_interval_count > 0
    )
);

-- Índices para prices
CREATE INDEX idx_price_product ON prices(product_id);
CREATE INDEX idx_price_type ON prices(type);
CREATE INDEX idx_price_active ON prices(is_active);
CREATE INDEX idx_price_stripe ON prices(stripe_price_id) WHERE stripe_price_id IS NOT NULL;

-- Comentários
COMMENT ON TABLE prices IS 'Preços dos produtos. Um produto pode ter múltiplos preços (mensal, anual, promoção, etc.)';
COMMENT ON COLUMN prices.type IS 'RECURRING = cobrança recorrente (assinaturas) | ONE_TIME = cobrança única';
COMMENT ON COLUMN prices.recurring_interval IS 'Intervalo de cobrança para RECURRING: day, week, month, year. NULL para ONE_TIME.';
COMMENT ON COLUMN prices.recurring_interval_count IS 'Multiplicador do intervalo. Ex: 3 months = cobrar a cada 3 meses';

-- ----------------------------------------------------------------------------
-- Tabela: orders
-- Descrição: 🆕 NOVA - Registra compras de produtos (e-commerce)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    
    -- Relacionamentos
    user_id BIGINT NOT NULL REFERENCES users(id),
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    
    -- Items (pode ter múltiplos produtos)
    items JSONB NOT NULL, 
    -- Estrutura: [{"product_id": 1, "price_id": 2, "quantity": 3, "amount": 15.00}]
    
    -- Totais
    subtotal DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) DEFAULT 0,
    total DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    
    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    -- Valores: PENDING, PAID, FAILED, REFUNDED, CANCELED
    
    -- Pagamento
    payment_method VARCHAR(50), -- card, pix, boleto, etc.
    
    -- Integração Stripe
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    stripe_checkout_session_id VARCHAR(255) UNIQUE,
    
    -- Timestamps importantes
    paid_at TIMESTAMP,
    refunded_at TIMESTAMP,
    canceled_at TIMESTAMP,
    
    -- Metadados
    metadata JSONB, -- Ex: endereço de entrega, notas do cliente
    
    -- Auditoria
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_order_status CHECK (
        status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED', 'CANCELED')
    ),
    CONSTRAINT chk_order_totals CHECK (
        total = subtotal - discount AND
        subtotal >= 0 AND
        discount >= 0 AND
        total >= 0
    )
);

-- Índices para orders
CREATE INDEX idx_order_user ON orders(user_id);
CREATE INDEX idx_order_organization ON orders(organization_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created ON orders(created_at DESC);
CREATE INDEX idx_order_stripe_payment_intent ON orders(stripe_payment_intent_id) WHERE stripe_payment_intent_id IS NOT NULL;

-- Comentários
COMMENT ON TABLE orders IS 'Pedidos de compra (e-commerce). Pode conter múltiplos produtos.';
COMMENT ON COLUMN orders.items IS 'Array JSON com os items do pedido. Cada item tem product_id, price_id, quantity, amount.';
COMMENT ON COLUMN orders.status IS 'PENDING = aguardando pagamento | PAID = pago | FAILED = falhou | REFUNDED = reembolsado | CANCELED = cancelado';

-- ============================================================================
-- PARTE 2: ATUALIZAR TABELAS EXISTENTES
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Adicionar organization_id em users
-- ----------------------------------------------------------------------------
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id);

CREATE INDEX IF NOT EXISTS idx_user_organization ON users(organization_id);

COMMENT ON COLUMN users.organization_id IS 'Organização à qual o usuário pertence (multi-tenant)';

-- ----------------------------------------------------------------------------
-- Atualizar subscriptions: remover plan_id, adicionar price_id
-- ----------------------------------------------------------------------------
-- Renomear coluna antiga (se existir) para manter histórico
ALTER TABLE subscriptions 
RENAME COLUMN plan_id TO plan_id_deprecated;

-- Adicionar nova coluna price_id
ALTER TABLE subscriptions
ADD COLUMN IF NOT EXISTS price_id BIGINT REFERENCES prices(id);

CREATE INDEX IF NOT EXISTS idx_subscription_price ON subscriptions(price_id);

COMMENT ON COLUMN subscriptions.plan_id_deprecated IS '⚠️ DEPRECATED: Antiga referência a plans. Use price_id agora.';
COMMENT ON COLUMN subscriptions.price_id IS 'Referência ao preço (recorrente) que o usuário está assinando';

-- ============================================================================
-- PARTE 3: MIGRAÇÃO DE DADOS (plans → products + prices)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 3.1: Criar organização padrão (para dados existentes)
-- ----------------------------------------------------------------------------
INSERT INTO organizations (name, slug, commerce_mode, is_active)
VALUES ('Organização Padrão', 'default', 'SUBSCRIPTIONS_ONLY', TRUE)
ON CONFLICT (slug) DO NOTHING;

-- Pegar ID da organização padrão
DO $$
DECLARE
    default_org_id BIGINT;
BEGIN
    SELECT id INTO default_org_id FROM organizations WHERE slug = 'default' LIMIT 1;
    
    -- Atualizar users sem organização
    UPDATE users 
    SET organization_id = default_org_id 
    WHERE organization_id IS NULL;
    
    RAISE NOTICE 'Organização padrão criada/atualizada. ID: %', default_org_id;
END $$;

-- ----------------------------------------------------------------------------
-- 3.2: Migrar plans → products
-- ----------------------------------------------------------------------------
INSERT INTO products (
    organization_id,
    name,
    description,
    type,
    is_active,
    is_featured,
    display_order,
    features,
    stripe_product_id,
    created_at,
    updated_at
)
SELECT 
    (SELECT id FROM organizations WHERE slug = 'default' LIMIT 1), -- organization_id
    p.name,
    p.description,
    'SUBSCRIPTION', -- Todos os plans antigos são assinaturas
    p.is_active,
    p.is_featured,
    p.display_order,
    p.features, -- Assumindo que plans.features já era JSONB
    p.stripe_product_id,
    p.created_at,
    p.updated_at
FROM plans p
WHERE NOT EXISTS (
    SELECT 1 FROM products pr 
    WHERE pr.stripe_product_id = p.stripe_product_id 
    AND p.stripe_product_id IS NOT NULL
);

-- ----------------------------------------------------------------------------
-- 3.3: Migrar plans → prices
-- ----------------------------------------------------------------------------
INSERT INTO prices (
    product_id,
    amount,
    currency,
    type,
    recurring_interval,
    recurring_interval_count,
    is_active,
    stripe_price_id,
    created_at
)
SELECT 
    pr.id, -- product_id (do product que acabamos de criar)
    p.price,
    'BRL', -- Assumindo BRL como padrão
    'RECURRING',
    p.billing_period, -- Assumindo que era 'month', 'year', etc.
    1, -- interval_count padrão
    p.is_active,
    p.stripe_price_id,
    p.created_at
FROM plans p
INNER JOIN products pr ON pr.stripe_product_id = p.stripe_product_id
WHERE NOT EXISTS (
    SELECT 1 FROM prices pc 
    WHERE pc.stripe_price_id = p.stripe_price_id 
    AND p.stripe_price_id IS NOT NULL
);

-- ----------------------------------------------------------------------------
-- 3.4: Atualizar subscriptions: mapear plan_id_deprecated → price_id
-- ----------------------------------------------------------------------------
UPDATE subscriptions s
SET price_id = pr.id
FROM plans p
INNER JOIN products prod ON prod.stripe_product_id = p.stripe_product_id
INNER JOIN prices pr ON pr.product_id = prod.id
WHERE s.plan_id_deprecated = p.id
AND s.price_id IS NULL;

-- ============================================================================
-- PARTE 4: VALIDAÇÕES E VERIFICAÇÕES
-- ============================================================================

DO $$
DECLARE
    org_count INT;
    product_count INT;
    price_count INT;
    order_count INT;
    orphan_subscriptions INT;
BEGIN
    -- Contar registros criados
    SELECT COUNT(*) INTO org_count FROM organizations;
    SELECT COUNT(*) INTO product_count FROM products;
    SELECT COUNT(*) INTO price_count FROM prices;
    SELECT COUNT(*) INTO order_count FROM orders;
    
    -- Verificar subscriptions órfãs
    SELECT COUNT(*) INTO orphan_subscriptions 
    FROM subscriptions 
    WHERE price_id IS NULL;
    
    -- Relatório
    RAISE NOTICE '════════════════════════════════════════════════';
    RAISE NOTICE 'Migration 003 - Relatório de Execução';
    RAISE NOTICE '════════════════════════════════════════════════';
    RAISE NOTICE 'Organizations criadas: %', org_count;
    RAISE NOTICE 'Products migrados: %', product_count;
    RAISE NOTICE 'Prices migrados: %', price_count;
    RAISE NOTICE 'Orders na base: %', order_count;
    RAISE NOTICE 'Subscriptions sem price_id: %', orphan_subscriptions;
    RAISE NOTICE '════════════════════════════════════════════════';
    
    IF orphan_subscriptions > 0 THEN
        RAISE WARNING 'Existem % subscriptions sem price_id! Revisar migração.', orphan_subscriptions;
    ELSE
        RAISE NOTICE '✅ Todas subscriptions foram migradas com sucesso!';
    END IF;
    
    IF product_count = 0 THEN
        RAISE WARNING 'Nenhum produto foi migrado. Verifique se a tabela plans tinha dados.';
    END IF;
END $$;

-- ============================================================================
-- PARTE 5: COMENTÁRIOS FINAIS
-- ============================================================================

COMMENT ON TABLE plans IS '⚠️ DEPRECATED: Substituída por products + prices. Mantida temporariamente para rollback.';

-- ============================================================================
-- ROLLBACK (SE NECESSÁRIO)
-- ============================================================================
-- ATENÇÃO: Execute apenas se precisar reverter a migration!
--
-- -- Restaurar subscriptions.plan_id
-- ALTER TABLE subscriptions RENAME COLUMN price_id TO price_id_backup;
-- ALTER TABLE subscriptions RENAME COLUMN plan_id_deprecated TO plan_id;
--
-- -- Remover novas tabelas (CUIDADO: perde dados!)
-- DROP TABLE IF EXISTS orders CASCADE;
-- DROP TABLE IF EXISTS prices CASCADE;
-- DROP TABLE IF EXISTS products CASCADE;
-- DROP TABLE IF EXISTS organizations CASCADE;
--
-- -- Remover coluna em users
-- ALTER TABLE users DROP COLUMN IF EXISTS organization_id;
--
-- RAISE NOTICE 'Rollback concluído. Arquitetura antiga restaurada.';
-- ============================================================================
