# 🔐 Guia de Configuração de Variáveis de Ambiente

Este documento explica como configurar corretamente todas as variáveis de ambiente necessárias para o funcionamento da aplicação.

## 📋 Índice

1. [Configuração Rápida](#configuração-rápida)
2. [Variáveis Obrigatórias](#variáveis-obrigatórias)
3. [Variáveis Opcionais](#variáveis-opcionais)
4. [Configuração por Ambiente](#configuração-por-ambiente)
5. [Como Obter as Credenciais](#como-obter-as-credenciais)
6. [Troubleshooting](#troubleshooting)

---

## ⚡ Configuração Rápida

### 1. Copiar Template

```bash
cp .env.example .env
```

### 2. Editar o arquivo `.env`

```bash
# Linux/Mac
nano .env

# Windows
notepad .env
```

### 3. Preencher Valores Necessários

Veja as seções abaixo para detalhes de cada variável.

---

## 🔑 Variáveis Obrigatórias

### Spring Profile

```bash
SPRING_PROFILE=dev  # ou 'prod' para produção
```

**Valores aceitos**: `dev`, `prod`

**Descrição**: Define o perfil de execução da aplicação. Cada perfil tem configurações específicas (logs, DDL, etc).

---

### Banco de Dados

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=gestao_mensalidades_dev
DB_USERNAME=postgres
DB_PASSWORD=sua_senha_aqui
```

**Descrição**: Credenciais de acesso ao PostgreSQL.

**⚠️ Importante**:
- Em produção, use senhas fortes e únicas
- Nunca use senhas padrão como "postgres" ou "password"
- Considere usar um serviço gerenciado (AWS RDS, Azure Database)

---

### JWT (JSON Web Token)

```bash
JWT_SECRET=sua_chave_secreta_forte_aqui_minimo_256_bits
JWT_EXPIRATION=86400000  # 24 horas em milissegundos
```

**Descrição**: Configuração para geração e validação de tokens JWT.

**Como gerar uma chave forte**:

```bash
# Linux/Mac
openssl rand -base64 64

# PowerShell (Windows)
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))
```

**⚠️ Crítico em Produção**:
- Use uma chave com **mínimo 256 bits**
- **Nunca** reutilize a chave de desenvolvimento
- Guarde a chave em um cofre de senhas (Vault, AWS Secrets Manager, etc)
- Se a chave vazar, **todos os tokens ficam comprometidos**

---

### Stripe

```bash
# Modo TEST (desenvolvimento)
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLISHABLE_KEY=pk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Modo LIVE (produção)
# STRIPE_SECRET_KEY=sk_live_...
# STRIPE_PUBLISHABLE_KEY=pk_live_...
# STRIPE_WEBHOOK_SECRET=whsec_...
```

**Descrição**: Credenciais do Stripe para processar pagamentos.

**Diferença entre TEST e LIVE**:
- `sk_test_` / `pk_test_`: Usadas em desenvolvimento, não processam dinheiro real
- `sk_live_` / `pk_live_`: Usadas em produção, processam transações reais

**⚠️ Segurança Crítica**:
- **NUNCA** exponha a `STRIPE_SECRET_KEY` no frontend
- **NUNCA** commite chaves no Git
- Use webhook secret para validar eventos
- Em produção, use apenas chaves live

---

## 🎨 Variáveis Opcionais

### Frontend

```bash
FRONTEND_URL=http://localhost:3000
```

**Descrição**: URL do frontend para redirecionamentos após checkout.

**Padrão**: `http://localhost:3000`

---

### CORS

```bash
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

**Descrição**: Domínios permitidos para fazer requisições à API.

**Múltiplos domínios**:
```bash
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://app.exemplo.com,https://exemplo.com
```

**⚠️ Produção**: Especifique apenas domínios confiáveis, nunca use `*`

---

### Servidor

```bash
SERVER_PORT=8080
```

**Descrição**: Porta onde a aplicação irá rodar.

**Padrão**: `8080`

---

### SSL (Produção)

```bash
SSL_KEY_STORE_PATH=/path/to/keystore.p12
SSL_KEY_STORE_PASSWORD=sua_senha_do_keystore
```

**Descrição**: Configuração de certificado SSL/TLS.

**Quando usar**: Apenas em produção, para HTTPS.

---

## 🌍 Configuração por Ambiente

### Desenvolvimento Local

```bash
# .env
SPRING_PROFILE=dev
DB_HOST=localhost
DB_PORT=5432
DB_NAME=gestao_mensalidades_dev
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=dev-secret-change-in-production
JWT_EXPIRATION=86400000
STRIPE_SECRET_KEY=sk_test_seu_test_key
STRIPE_PUBLISHABLE_KEY=pk_test_seu_test_key
STRIPE_WEBHOOK_SECRET=whsec_use_stripe_cli
FRONTEND_URL=http://localhost:3000
CORS_ALLOWED_ORIGINS=http://localhost:3000
SERVER_PORT=8080
```

### Staging/Homologação

```bash
# .env
SPRING_PROFILE=prod
DB_HOST=staging-db.exemplo.com
DB_PORT=5432
DB_NAME=gestao_mensalidades_staging
DB_USERNAME=app_user
DB_PASSWORD=senha_forte_unica
JWT_SECRET=chave_256_bits_gerada_openssl
JWT_EXPIRATION=86400000
STRIPE_SECRET_KEY=sk_test_seu_test_key  # Ainda test mode
STRIPE_PUBLISHABLE_KEY=pk_test_seu_test_key
STRIPE_WEBHOOK_SECRET=whsec_staging_webhook
FRONTEND_URL=https://staging.exemplo.com
CORS_ALLOWED_ORIGINS=https://staging.exemplo.com
SERVER_PORT=8080
```

### Produção

```bash
# .env
SPRING_PROFILE=prod
DB_HOST=prod-db.exemplo.com
DB_PORT=5432
DB_NAME=gestao_mensalidades_prod
DB_USERNAME=app_user
DB_PASSWORD=senha_extremamente_forte
JWT_SECRET=chave_256_bits_armazenada_em_vault
JWT_EXPIRATION=86400000
STRIPE_SECRET_KEY=sk_live_seu_live_key  # LIVE MODE
STRIPE_PUBLISHABLE_KEY=pk_live_seu_live_key
STRIPE_WEBHOOK_SECRET=whsec_prod_webhook
FRONTEND_URL=https://app.exemplo.com
CORS_ALLOWED_ORIGINS=https://app.exemplo.com,https://exemplo.com
SERVER_PORT=8080
SSL_KEY_STORE_PATH=/etc/ssl/certs/keystore.p12
SSL_KEY_STORE_PASSWORD=senha_keystore_forte
```

---

## 🔍 Como Obter as Credenciais

### PostgreSQL

**Opção 1: Instalação Local**

```bash
# Criar banco de dados
psql -U postgres
CREATE DATABASE gestao_mensalidades_dev;
CREATE USER app_user WITH PASSWORD 'sua_senha';
GRANT ALL PRIVILEGES ON DATABASE gestao_mensalidades_dev TO app_user;
```

**Opção 2: Docker**

```bash
docker run --name postgres-dev \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=gestao_mensalidades_dev \
  -p 5432:5432 -d postgres:15
```

---

### Stripe

1. **Criar conta**: https://dashboard.stripe.com/register

2. **Ativar modo test**: 
   - Toggle "Test mode" no Dashboard

3. **Obter API Keys**:
   - Acesse: https://dashboard.stripe.com/test/apikeys
   - Copie:
     - Publishable key: `pk_test_...`
     - Secret key: `sk_test_...` (clique em "Reveal test key")

4. **Webhook Secret** (Desenvolvimento):
   ```bash
   # Instalar Stripe CLI
   brew install stripe/stripe-cli/stripe  # Mac
   # Ou baixe de: https://stripe.com/docs/stripe-cli
   
   # Login
   stripe login
   
   # Forward webhooks
   stripe listen --forward-to localhost:8080/api/webhooks/stripe
   
   # Copie o whsec_... exibido
   ```

5. **Webhook Secret** (Produção):
   - Acesse: https://dashboard.stripe.com/webhooks
   - Clique em "Add endpoint"
   - URL: `https://seu-dominio.com/api/webhooks/stripe`
   - Selecione eventos necessários
   - Copie o "Signing secret" (whsec_...)

---

### JWT Secret

```bash
# Gerar chave forte (Linux/Mac)
openssl rand -base64 64

# Gerar chave forte (Windows PowerShell)
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))

# Resultado esperado (exemplo):
# YourBase64EncodedSecretKeyHereWithMinimum256BitsOfEntropyForProductionSecurity==
```

---

## 🐛 Troubleshooting

### Erro: "Could not connect to database"

**Causa**: Credenciais do banco incorretas ou PostgreSQL não está rodando.

**Solução**:
1. Verificar se PostgreSQL está rodando:
   ```bash
   # Linux/Mac
   sudo systemctl status postgresql
   
   # Windows
   Get-Service postgresql*
   ```

2. Testar conexão manual:
   ```bash
   psql -h localhost -p 5432 -U postgres -d gestao_mensalidades_dev
   ```

3. Verificar valores em `.env`:
   - `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`

---

### Erro: "Invalid Stripe API key"

**Causa**: Chave do Stripe inválida ou não configurada.

**Solução**:
1. Verificar se a chave começa com `sk_test_` ou `sk_live_`
2. Garantir que não há espaços ou quebras de linha
3. Verificar se a chave não foi revogada no Dashboard

---

### Erro: "Webhook signature verification failed"

**Causa**: Webhook secret incorreto.

**Solução**:
1. **Desenvolvimento**: Usar Stripe CLI
   ```bash
   stripe listen --forward-to localhost:8080/api/webhooks/stripe
   ```
   
2. **Produção**: Verificar o signing secret no Dashboard do Stripe

3. Garantir que o valor em `.env` corresponde ao secret do endpoint

---

### Erro: "JWT token expired or invalid"

**Causa**: Token JWT expirado ou chave secreta mudou.

**Solução**:
1. Fazer login novamente para obter novo token
2. Verificar se `JWT_SECRET` não foi alterado (tokens antigos ficam inválidos)
3. Ajustar `JWT_EXPIRATION` se necessário

---

### Erro: "CORS policy blocked"

**Causa**: Frontend não está na lista de origens permitidas.

**Solução**:
1. Adicionar URL do frontend em `CORS_ALLOWED_ORIGINS`
2. Exemplo: `CORS_ALLOWED_ORIGINS=http://localhost:3000,https://app.exemplo.com`
3. Reiniciar a aplicação

---

## ✅ Checklist de Segurança

Antes de ir para produção, verifique:

- [ ] JWT_SECRET tem pelo menos 256 bits
- [ ] Senhas de banco são fortes e únicas
- [ ] STRIPE_SECRET_KEY é do tipo `sk_live_` (não test)
- [ ] Webhook secret está correto no Stripe
- [ ] CORS permite apenas domínios confiáveis
- [ ] SSL/TLS está configurado (HTTPS)
- [ ] Arquivo `.env` está no `.gitignore`
- [ ] Variáveis sensíveis estão em um cofre de senhas
- [ ] Backups do banco estão configurados
- [ ] Logs não expõem informações sensíveis

---

## 📚 Recursos Adicionais

- [Stripe API Keys](https://stripe.com/docs/keys)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [PostgreSQL Security](https://www.postgresql.org/docs/current/auth-pg-hba-conf.html)
- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)

---

**Última atualização**: 2025-10-05
