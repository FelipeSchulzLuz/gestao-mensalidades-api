# 🔒 Política de Segurança

## 🚨 Arquivos Sensíveis - NUNCA COMMITAR!

Os seguintes arquivos contêm dados sensíveis e **JAMAIS** devem ser commitados no Git:

### ❌ Proibido Commitar:
- `.env` - Valores de ambiente locais
- `.env.docker` - Valores Docker com credenciais reais
- `.env.local`, `.env.production`, `.env.development` - Qualquer variação de .env
- `docs/CREDENCIAIS.md` - Documento com credenciais
- `serviceAccountKey.json` - Chave privada do Firebase
- Qualquer arquivo com senhas, tokens ou API keys

### ✅ Permitido Commitar (templates):
- `.env.example` - Template com placeholders
- `.env.docker.example` - Template Docker com placeholders
- `CREDENCIAIS.md.example` - Template de documentação

## 🔐 Dados Sensíveis a Proteger

### Credenciais de Banco de Dados
- `DB_PASSWORD` - Senha do PostgreSQL
- `DB_USERNAME` - Usuário do banco
- `PGADMIN_PASSWORD` - Senha do pgAdmin
- `REDIS_PASSWORD` - Senha do Redis

### Chaves de API
- `STRIPE_API_KEY` - Secret key do Stripe (mesmo test keys!)
- `STRIPE_WEBHOOK_SECRET` - Secret de webhooks
- `JWT_SECRET` - Secret para assinatura de tokens

### Tokens e Credenciais
- Firebase service account key
- GitHub tokens
- Docker Hub credentials
- Qualquer API key de terceiros

## 🛡️ Como Usar Templates com Segurança

### 1. Setup Inicial (Desenvolvimento Local)

```bash
# 1. Copiar template com placeholders
cp .env.docker.example .env.docker

# 2. Editar .env.docker com valores REAIS
nano .env.docker  # ou seu editor preferido

# 3. Copiar para .env (usado pela aplicação)
cp .env.docker .env

# 4. VERIFICAR que .env.docker e .env estão no .gitignore
git status  # NÃO devem aparecer!
```

### 2. Gerar Senhas Seguras

```bash
# Linux/Mac - Senha aleatória
openssl rand -base64 32

# JWT Secret (256 bits)
openssl rand -base64 64

# PowerShell (Windows)
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

### 3. Produção

**NUNCA use os mesmos valores de desenvolvimento em produção!**

```bash
# Em produção, use:
# - Gerenciador de secrets (AWS Secrets Manager, Azure Key Vault, etc)
# - Variáveis de ambiente do servidor
# - Ferramentas de CI/CD com secrets
```

## 🔍 Verificar Vazamentos Antes de Commitar

### Pre-commit Check
```bash
# Verificar se há arquivos sensíveis staged
git status

# Verificar conteúdo antes de commitar
git diff --cached

# Procurar por senhas/tokens acidentalmente adicionados
git grep -i "password\|secret\|api_key\|token" -- "*.env*"
```

### Revisar .gitignore
```bash
# Garantir que .gitignore está correto
cat .gitignore | grep -E "\.env|CREDENCIAIS"

# Deve conter:
# .env
# .env.*
# .env.docker
# CREDENCIAIS.md
```

## 🚑 Vazamento Detectado - O Que Fazer?

### Se você commitou acidentalmente um arquivo sensível:

#### 1. Remover do último commit (se não deu push)
```bash
git reset HEAD~1
git add .gitignore  # Adicionar regra se faltou
git commit -m "fix: add missing .gitignore rules"
```

#### 2. Se já deu push (CRÍTICO!)
```bash
# 1. Remover do tracking
git rm --cached .env.docker

# 2. Atualizar .gitignore
echo ".env.docker" >> .gitignore

# 3. Commit
git add .gitignore
git commit -m "security: remove sensitive .env.docker from repository"

# 4. IMPORTANTE: TROCAR TODAS AS CREDENCIAIS VAZADAS!
# - Senhas de banco
# - API keys do Stripe
# - JWT secrets
# - Tokens de terceiros
```

#### 3. Limpar Histórico (Avançado)
```bash
# Use git-filter-repo ou BFG Repo-Cleaner
# ATENÇÃO: Reescreve histórico, coordenar com time!

# Exemplo com git-filter-repo:
git filter-repo --path .env.docker --invert-paths

# Forçar push (cuidado!)
git push origin master --force
```

#### 4. Revogar Credenciais Vazadas
- **Stripe**: Revogar keys em https://dashboard.stripe.com/apikeys
- **Firebase**: Revogar service account em Firebase Console
- **Banco de Dados**: Trocar senha do usuário
- **JWT**: Gerar novo secret (invalida todos tokens)

## 📋 Checklist de Segurança

Antes de cada commit:

- [ ] Revisar `git status` - nenhum arquivo `.env*` deve aparecer
- [ ] Verificar `git diff --cached` - sem senhas ou tokens
- [ ] Confirmar que `.gitignore` está atualizado
- [ ] Templates (`.example`) não contêm valores reais
- [ ] Documentação não contém credenciais
- [ ] Scripts não imprimem valores sensíveis em logs

## 📞 Reportar Vulnerabilidade de Segurança

Se você encontrar uma vulnerabilidade de segurança neste projeto:

1. **NÃO** abra uma issue pública
2. Envie email para: schulz.neo@gmail.com
3. Inclua:
   - Descrição detalhada da vulnerabilidade
   - Passos para reproduzir
   - Impacto potencial
   - Sugestões de correção (se possível)

## 📚 Referências

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [GitHub Security Best Practices](https://docs.github.com/en/code-security/getting-started/best-practices-for-securing-your-repository)
- [Stripe Security Best Practices](https://stripe.com/docs/security/guide)

---

**Última atualização:** 2025-10-06  
**Revisão:** Após detecção de .env.docker commitado
