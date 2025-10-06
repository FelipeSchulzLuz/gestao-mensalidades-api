-- Flyway V2 - Add Firebase Auth support
-- Origin: database/migrations/002_add_firebase_auth_support.sql

-- 1. Adicionar coluna firebase_uid
ALTER TABLE users ADD COLUMN IF NOT EXISTS firebase_uid VARCHAR(128) UNIQUE;

-- 2. Criar índice para firebase_uid
CREATE INDEX IF NOT EXISTS idx_user_firebase_uid ON users(firebase_uid);

-- 3. Tornar a coluna password nullable (agora é opcional, dependendo do método de auth)
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;

-- 4. Adicionar constraint para garantir que usuário tenha pelo menos um método de auth
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_user_has_auth_method'
    ) THEN
        ALTER TABLE users 
        ADD CONSTRAINT chk_user_has_auth_method 
        CHECK (password IS NOT NULL OR firebase_uid IS NOT NULL);
    END IF;
END $$;

-- 5. Comentários nas colunas para documentação
COMMENT ON COLUMN users.password IS 'Senha hasheada com BCrypt (apenas para autenticação JWT própria). NULL se usa apenas Firebase.';
COMMENT ON COLUMN users.firebase_uid IS 'UID do usuário no Firebase Authentication. NULL se usa apenas autenticação JWT própria.';
