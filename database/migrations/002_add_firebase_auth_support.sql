-- Migration: Adicionar suporte para autenticação híbrida (JWT + Firebase)
-- Data: 2025-10-05
-- Descrição: Adiciona coluna firebase_uid e ajusta constraints para suportar dois métodos de autenticação

-- 1. Adicionar coluna firebase_uid
ALTER TABLE users ADD COLUMN IF NOT EXISTS firebase_uid VARCHAR(128) UNIQUE;

-- 2. Criar índice para firebase_uid (melhor performance nas buscas)
CREATE INDEX IF NOT EXISTS idx_user_firebase_uid ON users(firebase_uid);

-- 3. Tornar a coluna password nullable (agora é opcional, dependendo do método de auth)
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;

-- 4. Adicionar constraint para garantir que usuário tenha pelo menos um método de auth
-- Usuário DEVE ter password OU firebase_uid (não pode ter ambos NULL)
ALTER TABLE users 
ADD CONSTRAINT chk_user_has_auth_method 
CHECK (password IS NOT NULL OR firebase_uid IS NOT NULL);

-- 5. Comentários nas colunas para documentação
COMMENT ON COLUMN users.password IS 'Senha hasheada com BCrypt (apenas para autenticação JWT própria). NULL se usa apenas Firebase.';
COMMENT ON COLUMN users.firebase_uid IS 'UID do usuário no Firebase Authentication. NULL se usa apenas autenticação JWT própria.';

-- Verificar se a migration foi aplicada corretamente
DO $$
BEGIN
    -- Verificar se firebase_uid existe
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'users' 
        AND column_name = 'firebase_uid'
    ) THEN
        RAISE NOTICE 'Migration aplicada com sucesso! Coluna firebase_uid criada.';
    ELSE
        RAISE EXCEPTION 'Erro: Coluna firebase_uid não foi criada!';
    END IF;
    
    -- Verificar se password é nullable
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'users' 
        AND column_name = 'password'
        AND is_nullable = 'YES'
    ) THEN
        RAISE NOTICE 'Coluna password agora é nullable (suporta Firebase-only users).';
    ELSE
        RAISE WARNING 'Aviso: Coluna password ainda é NOT NULL.';
    END IF;
    
    -- Verificar constraint
    IF EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE table_name = 'users' 
        AND constraint_name = 'chk_user_has_auth_method'
    ) THEN
        RAISE NOTICE 'Constraint chk_user_has_auth_method criada (garante pelo menos um método de auth).';
    ELSE
        RAISE WARNING 'Aviso: Constraint de validação não foi criada.';
    END IF;
END $$;
