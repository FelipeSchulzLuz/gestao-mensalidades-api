-- ============================================================================
-- Flyway Migration V3: Hybrid Commerce Architecture
-- ============================================================================
-- Data: 2025-10-06
-- Descrição: Transição para arquitetura híbrida (Products + Prices + Organizations)
-- Documentação: docs/17-ARQUITETURA-HIBRIDA.md
-- ============================================================================

-- PARTE 1: CRIAR TABELA ORGANIZATIONS
CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    commerce_mode VARCHAR(30) NOT NULL DEFAULT 'SUBSCRIPTIONS_ONLY',
    stripe_account_id VARCHAR(255) UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_commerce_mode CHECK (commerce_mode IN ('SUBSCRIPTIONS_ONLY', 'ECOMMERCE_ONLY', 'HYBRID'))
);

CREATE INDEX idx_organization_slug ON organizations(slug);
CREATE INDEX idx_organization_active ON organizations(is_active);
CREATE INDEX idx_organization_stripe_account ON organizations(stripe_account_id) WHERE stripe_account_id IS NOT NULL;

COMMENT ON TABLE organizations IS 'Organizações (multi-tenant). Cada organização pode vender assinaturas, produtos avulsos, ou ambos.';

-- PARTE 2: CRIAR TABELA PRODUCTS
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    features JSONB,
    metadata JSONB,
    stripe_product_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_product_type CHECK (type IN ('SUBSCRIPTION', 'ONE_TIME'))
);

CREATE INDEX idx_product_organization ON products(organization_id);
CREATE INDEX idx_product_type ON products(type);
CREATE INDEX idx_product_active ON products(is_active);
CREATE INDEX idx_product_featured ON products(is_featured) WHERE is_featured = TRUE;
CREATE INDEX idx_product_display_order ON products(display_order);
CREATE INDEX idx_product_stripe ON products(stripe_product_id) WHERE stripe_product_id IS NOT NULL;

COMMENT ON TABLE products IS 'Produtos genéricos. Podem ser assinaturas (SUBSCRIPTION) ou produtos avulsos (ONE_TIME).';

-- PARTE 3: CRIAR TABELA PRICES
CREATE TABLE prices (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount >= 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    type VARCHAR(20) NOT NULL,
    recurring_interval VARCHAR(20),
    recurring_interval_count INT DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    stripe_price_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_price_type CHECK (type IN ('RECURRING', 'ONE_TIME')),
    CONSTRAINT chk_recurring_consistency CHECK (
        (type = 'RECURRING' AND recurring_interval IS NOT NULL) OR
        (type = 'ONE_TIME' AND recurring_interval IS NULL)
    ),
    CONSTRAINT chk_recurring_interval CHECK (
        recurring_interval IS NULL OR recurring_interval IN ('day', 'week', 'month', 'year')
    ),
    CONSTRAINT chk_recurring_count CHECK (
        recurring_interval_count IS NULL OR recurring_interval_count > 0
    )
);

CREATE INDEX idx_price_product ON prices(product_id);
CREATE INDEX idx_price_type ON prices(type);
CREATE INDEX idx_price_active ON prices(is_active);
CREATE INDEX idx_price_stripe ON prices(stripe_price_id) WHERE stripe_price_id IS NOT NULL;

COMMENT ON TABLE prices IS 'Preços dos produtos. Um produto pode ter múltiplos preços (mensal, anual, promoção, etc.)';

-- PARTE 4: CRIAR TABELA ORDERS
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    organization_id BIGINT NOT NULL REFERENCES organizations(id),
    items JSONB NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) DEFAULT 0,
    total DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    stripe_checkout_session_id VARCHAR(255) UNIQUE,
    paid_at TIMESTAMP,
    refunded_at TIMESTAMP,
    canceled_at TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_order_status CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED', 'CANCELED')),
    CONSTRAINT chk_order_totals CHECK (
        total = subtotal - discount AND subtotal >= 0 AND discount >= 0 AND total >= 0
    )
);

CREATE INDEX idx_order_user ON orders(user_id);
CREATE INDEX idx_order_organization ON orders(organization_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_created ON orders(created_at DESC);
CREATE INDEX idx_order_stripe_payment_intent ON orders(stripe_payment_intent_id) WHERE stripe_payment_intent_id IS NOT NULL;

COMMENT ON TABLE orders IS 'Pedidos de compra (e-commerce). Pode conter múltiplos produtos.';

-- PARTE 5: ATUALIZAR USERS
ALTER TABLE users ADD COLUMN IF NOT EXISTS organization_id BIGINT REFERENCES organizations(id);
CREATE INDEX IF NOT EXISTS idx_user_organization ON users(organization_id);
COMMENT ON COLUMN users.organization_id IS 'Organização à qual o usuário pertence (multi-tenant)';

-- PARTE 6: ATUALIZAR SUBSCRIPTIONS (se existir)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'subscriptions') THEN
        -- Renomear plan_id para plan_id_deprecated (se existir)
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'subscriptions' AND column_name = 'plan_id') THEN
            ALTER TABLE subscriptions RENAME COLUMN plan_id TO plan_id_deprecated;
        END IF;
        
        -- Adicionar price_id
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'subscriptions' AND column_name = 'price_id') THEN
            ALTER TABLE subscriptions ADD COLUMN price_id BIGINT REFERENCES prices(id);
        END IF;
        
        -- Criar índice
        IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE tablename = 'subscriptions' AND indexname = 'idx_subscription_price') THEN
            CREATE INDEX idx_subscription_price ON subscriptions(price_id);
        END IF;
        
        -- Comentários
        EXECUTE 'COMMENT ON COLUMN subscriptions.plan_id_deprecated IS ''⚠️ DEPRECATED: Antiga referência a plans. Use price_id agora.''';
        EXECUTE 'COMMENT ON COLUMN subscriptions.price_id IS ''Referência ao preço (recorrente) que o usuário está assinando''';
    END IF;
END $$;

-- PARTE 7: MIGRAÇÃO DE DADOS
-- Criar organização padrão
INSERT INTO organizations (name, slug, commerce_mode, is_active)
VALUES ('Organização Padrão', 'default', 'SUBSCRIPTIONS_ONLY', TRUE)
ON CONFLICT (slug) DO NOTHING;

-- Atualizar users sem organização
UPDATE users SET organization_id = (SELECT id FROM organizations WHERE slug = 'default' LIMIT 1) WHERE organization_id IS NULL;

-- Migrar plans → products (apenas se tabela plans existir)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'plans') THEN
        INSERT INTO products (organization_id, name, description, type, is_active, is_featured, display_order, features, stripe_product_id, created_at, updated_at)
        SELECT (SELECT id FROM organizations WHERE slug = 'default' LIMIT 1), p.name, p.description, 'SUBSCRIPTION', p.is_active, p.is_featured, p.display_order, p.features, p.stripe_product_id, p.created_at, p.updated_at
        FROM plans p
        WHERE NOT EXISTS (SELECT 1 FROM products pr WHERE pr.stripe_product_id = p.stripe_product_id AND p.stripe_product_id IS NOT NULL);
    END IF;
END $$;

-- Migrar plans → prices (apenas se tabela plans existir)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'plans') THEN
        INSERT INTO prices (product_id, amount, currency, type, recurring_interval, recurring_interval_count, is_active, stripe_price_id, created_at)
        SELECT pr.id, p.price, 'BRL', 'RECURRING', p.billing_period, 1, p.is_active, p.stripe_price_id, p.created_at
        FROM plans p
        INNER JOIN products pr ON pr.stripe_product_id = p.stripe_product_id
        WHERE NOT EXISTS (SELECT 1 FROM prices pc WHERE pc.stripe_price_id = p.stripe_price_id AND p.stripe_price_id IS NOT NULL);
    END IF;
END $$;

-- Atualizar subscriptions (apenas se existir)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'subscriptions') 
       AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'plans') THEN
        UPDATE subscriptions s SET price_id = pr.id
        FROM plans p
        INNER JOIN products prod ON prod.stripe_product_id = p.stripe_product_id
        INNER JOIN prices pr ON pr.product_id = prod.id
        WHERE s.plan_id_deprecated = p.id AND s.price_id IS NULL;
    END IF;
END $$;

-- PARTE 8: DEPRECAR PLANS (apenas se existir)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'plans') THEN
        EXECUTE 'COMMENT ON TABLE plans IS ''⚠️ DEPRECATED: Substituída por products + prices. Mantida temporariamente para rollback.''';
    END IF;
END $$;
