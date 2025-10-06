# 🔧 Como Aplicar a Migration 002 - Guia Passo a Passo

## ✅ SQL JÁ COPIADO PARA CLIPBOARD!

O script já copiou o SQL. Agora siga estes passos:

---

## 📋 Passo 1: Abrir DBeaver

1. Abra o **DBeaver**
2. Se não tiver instalado: https://dbeaver.io/download/

---

## 🔌 Passo 2: Conectar ao Banco

### Se já tem a conexão:
- Clique duas vezes na conexão existente
- Nome: `gestao_mensalidades` ou `PostgreSQL - 192.168.1.12`

### Se precisa criar a conexão:
1. Clique em **"Nova Conexão"** (ícone de plug com +)
2. Selecione **PostgreSQL**
3. Clique em **"Avançar"**
4. Preencha:
   ```
   Host: 192.168.1.12
   Port: 54320
   Database: gestao_mensalidades
   Usuário: admin
   Senha: admin123
   ```
5. Clique em **"Testar Conexão"**
6. Se OK, clique em **"Concluir"**

---

## 📝 Passo 3: Abrir SQL Editor

1. Clique com botão direito na conexão
2. Selecione **"Editor SQL"** > **"Novo Script SQL"**
3. Ou use atalho: **Ctrl + ]**

---

## 📋 Passo 4: Colar o SQL

1. No editor SQL que abriu, **cole o SQL** (Ctrl+V)
2. O SQL já está no clipboard! Basta colar.

O SQL que será colado:

```sql
-- Migration: Adicionar suporte para autenticação híbrida (JWT + Firebase)
-- Data: 2025-10-05

-- 1. Adicionar coluna firebase_uid
ALTER TABLE users ADD COLUMN IF NOT EXISTS firebase_uid VARCHAR(128) UNIQUE;

-- 2. Criar índice
CREATE INDEX IF NOT EXISTS idx_user_firebase_uid ON users(firebase_uid);

-- 3. Tornar password nullable
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;

-- 4. Adicionar constraint
ALTER TABLE users 
ADD CONSTRAINT chk_user_has_auth_method 
CHECK (password IS NOT NULL OR firebase_uid IS NOT NULL);

-- 5. Adicionar organization_id (se não existir)
ALTER TABLE users ADD COLUMN IF NOT EXISTS organization_id BIGINT;

-- 6. Comentários
COMMENT ON COLUMN users.password IS 'Senha hasheada com BCrypt...';
COMMENT ON COLUMN users.firebase_uid IS 'UID do usuário no Firebase...';
```

---

## ▶️ Passo 5: Executar o SQL

1. Com o SQL colado no editor
2. Pressione **Ctrl + Enter** (ou clique no botão ▶️ laranja)
3. Aguarde a execução (deve levar 1-2 segundos)

---

## ✅ Passo 6: Verificar Resultado

Se tudo correu bem, você verá:

```
✅ NOTICE: Migration aplicada com sucesso! Coluna firebase_uid criada.
✅ NOTICE: Coluna password agora é nullable (suporta Firebase-only users).
✅ NOTICE: Constraint chk_user_has_auth_method criada...
```

---

## 🔍 Passo 7: Verificar Colunas (Opcional)

Execute esta query para confirmar:

```sql
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    character_maximum_length
FROM information_schema.columns 
WHERE table_name = 'users' 
AND column_name IN ('password', 'firebase_uid', 'organization_id')
ORDER BY column_name;
```

**Resultado esperado:**

| column_name      | data_type         | is_nullable | character_maximum_length |
|------------------|-------------------|-------------|--------------------------|
| firebase_uid     | character varying | YES         | 128                      |
| organization_id  | bigint            | YES         | NULL                     |
| password         | character varying | YES         | 255                      |

---

## 🚀 Passo 8: Testar Aplicação

Agora você pode rodar a aplicação:

```powershell
./gradlew bootRun
```

Ou compilar primeiro:

```powershell
./gradlew clean build
```

---

## ⚠️ Problemas Comuns

### ❌ Erro: "column already exists"

**Solução:** Coluna já existe! Migration já foi aplicada antes.

```sql
-- Verificar se já existe
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'users' 
AND column_name = 'firebase_uid';
```

### ❌ Erro: "cannot drop not-null constraint"

**Solução:** Verificar se há usuários sem password:

```sql
-- Ver usuários
SELECT id, email, password IS NULL as sem_password 
FROM users;

-- Se tiver usuários sem password, atualizar:
UPDATE users SET password = 'temporary' WHERE password IS NULL;
```

### ❌ Erro: "constraint already exists"

**Solução:** Constraint já existe! Pode ignorar.

---

## 📞 Precisa de Ajuda?

Se der qualquer erro:

1. **Copie a mensagem de erro completa**
2. **Tire um screenshot** (se possível)
3. **Me envie** e eu te ajudo a resolver!

---

## ✅ Migration Aplicada? Próximo Passo:

Configurar Firebase:

1. Acesse https://console.firebase.google.com/
2. Crie um projeto
3. Baixe o service account key
4. Salve em: `src/main/resources/serviceAccountKey.json`
5. Configure o `.env`:
   ```env
   FIREBASE_PROJECT_ID=seu-projeto-id
   FIREBASE_SERVICE_ACCOUNT_KEY_PATH=serviceAccountKey.json
   ```

Leia: `IMPLEMENTACAO_COMPLETA.md` para instruções detalhadas!

---

**Boa sorte! 🚀**
