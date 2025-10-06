# 💳 Guia de Desenvolvimento com Stripe

> **💡 IMPORTANTE:** O Stripe CLI roda na sua **máquina local** (não no servidor Docker).  
> Use o comando `stripe listen --forward-to localhost:8800/api/webhooks/stripe` para webhooks locais.

## 📋 Índice

1. [Instalação da Stripe CLI](#instalação-da-stripe-cli)
2. [Configuração Inicial](#configuração-inicial)
3. [Webhooks Locais](#webhooks-locais)
4. [Testes de Eventos](#testes-de-eventos)
5. [Produtos e Preços](#produtos-e-preços)
6. [Comandos Úteis](#comandos-úteis)
7. [Troubleshooting](#troubleshooting)

---

## 🔧 Instalação da Stripe CLI

### Windows (Recomendado: Scoop)

```powershell
# Instalar Scoop (se ainda não tiver)
iwr -useb get.scoop.sh | iex

# Instalar Stripe CLI
scoop bucket add stripe https://github.com/stripe/scoop-stripe-cli.git
scoop install stripe
```

### Windows (Alternativa: Download direto)

1. Baixe de: https://github.com/stripe/stripe-cli/releases/latest
2. Extraia o arquivo `stripe.exe`
3. Adicione ao PATH ou use o caminho completo

### macOS (Homebrew)

```bash
brew install stripe/stripe-cli/stripe
```

### Linux

```bash
# Debian/Ubuntu
wget https://github.com/stripe/stripe-cli/releases/latest/download/stripe_linux_x86_64.tar.gz
tar -xvf stripe_linux_x86_64.tar.gz
sudo mv stripe /usr/local/bin/
```

### Verificar instalação

```bash
stripe --version
```

---

## 🔑 Configuração Inicial

### 1. Login na sua conta Stripe

```bash
stripe login
```

Isso abrirá seu navegador para autorizar o acesso. Após autorizar, você verá uma mensagem de sucesso no terminal.

### 2. Verificar autenticação

```bash
stripe config --list
```

Você deverá ver suas configurações, incluindo o **test mode secret key**.

---

## 🎯 Webhooks Locais (Desenvolvimento)

### Configuração do Projeto

**Porta da API:** `8800` (configurada no `.env`)  
**Endpoint de Webhook:** `/api/webhooks/stripe`

### Iniciar Listener de Webhooks

#### Opção 1: Forwarding para localhost (quando rodar API local)

```bash
stripe listen --thin-events '*' --forward-thin-to localhost:8800/api/webhooks/stripe
```

#### Opção 2: Forwarding para rede local (quando API estiver no Docker 192.168.1.12)

```bash
stripe listen --thin-events '*' --forward-thin-to http://192.168.1.12:8800/api/webhooks/stripe
```

### O que acontece:

1. O comando inicia um listener que captura todos os eventos do Stripe
2. Exibe o **webhook signing secret** (algo como `whsec_xxxxxxxxxxxxx`)
3. Encaminha eventos para sua aplicação local

### ⚠️ IMPORTANTE: Copiar o Webhook Secret

Quando você executar `stripe listen`, você verá algo assim:

```bash
> Ready! Your webhook signing secret is whsec_1234567890abcdefghijklmnopqrstuvwxyz (^C to quit)
```

**Copie esse `whsec_...` e adicione ao seu `.env`:**

```env
STRIPE_WEBHOOK_SECRET=whsec_1234567890abcdefghijklmnopqrstuvwxyz
```

---

## 🧪 Testes de Eventos

### Eventos Disponíveis para Teste

```bash
# Ver lista completa de eventos que podem ser testados
stripe trigger --help
```

### Eventos Importantes para Assinaturas

#### 1. Criar Customer
```bash
stripe trigger customer.created
```

#### 2. Assinatura Criada
```bash
stripe trigger customer.subscription.created
```

#### 3. Pagamento Bem-Sucedido
```bash
stripe trigger payment_intent.succeeded
```

#### 4. Assinatura Atualizada
```bash
stripe trigger customer.subscription.updated
```

#### 5. Assinatura Cancelada
```bash
stripe trigger customer.subscription.deleted
```

#### 6. Pagamento Falhou
```bash
stripe trigger payment_intent.payment_failed
```

#### 7. Invoice Criado
```bash
stripe trigger invoice.created
```

#### 8. Invoice Pago
```bash
stripe trigger invoice.paid
```

#### 9. Invoice Falhou
```bash
stripe trigger invoice.payment_failed
```

#### 10. Checkout Session Completado
```bash
stripe trigger checkout.session.completed
```

### Exemplo de Teste Completo (Fluxo de Assinatura)

```bash
# 1. Criar customer
stripe trigger customer.created

# 2. Completar checkout
stripe trigger checkout.session.completed

# 3. Criar assinatura
stripe trigger customer.subscription.created

# 4. Processar invoice
stripe trigger invoice.paid

# 5. Simular renovação mensal
stripe trigger customer.subscription.updated
```

---

## 📦 Produtos e Preços

### Seus Produtos Cadastrados

**Plano Mensal:**
```env
STRIPE_PRODUCT_MONTHLY=prod_SgzMGAVP7sdb7s
```

**Plano Anual:**
```env
STRIPE_PRODUCT_YEARLY=prod_Sj9inVJYHvyr0M
```

### Ver Detalhes dos Produtos

```bash
# Listar todos os produtos
stripe products list

# Ver detalhes de um produto específico
stripe products retrieve prod_SgzMGAVP7sdb7s
stripe products retrieve prod_Sj9inVJYHvyr0M
```

### Ver Preços dos Produtos

```bash
# Listar preços do produto mensal
stripe prices list --product prod_SgzMGAVP7sdb7s

# Listar preços do produto anual
stripe prices list --product prod_Sj9inVJYHvyr0M
```

### Criar um Novo Preço (se necessário)

```bash
# Preço mensal de R$ 99,00
stripe prices create \
  --product prod_SgzMGAVP7sdb7s \
  --unit-amount 9900 \
  --currency brl \
  --recurring interval=month

# Preço anual de R$ 990,00 (equivalente a R$ 82,50/mês)
stripe prices create \
  --product prod_Sj9inVJYHvyr0M \
  --unit-amount 99000 \
  --currency brl \
  --recurring interval=year
```

---

## 🛠️ Comandos Úteis

### Customers

```bash
# Listar customers
stripe customers list --limit 10

# Criar customer
stripe customers create --email teste@exemplo.com --name "João Silva"

# Ver detalhes
stripe customers retrieve cus_xxxxxxxxxxxxx

# Atualizar customer
stripe customers update cus_xxxxxxxxxxxxx --name "João Silva Updated"
```

### Subscriptions

```bash
# Listar assinaturas
stripe subscriptions list --limit 10

# Criar assinatura
stripe subscriptions create \
  --customer cus_xxxxxxxxxxxxx \
  --items[0][price]=price_xxxxxxxxxxxxx

# Cancelar assinatura
stripe subscriptions cancel sub_xxxxxxxxxxxxx
```

### Invoices

```bash
# Listar invoices
stripe invoices list --limit 10

# Ver detalhes
stripe invoices retrieve in_xxxxxxxxxxxxx

# Pagar invoice manualmente (teste)
stripe invoices pay in_xxxxxxxxxxxxx
```

### Checkout Sessions

```bash
# Listar checkout sessions
stripe checkout sessions list --limit 10

# Criar checkout session
stripe checkout sessions create \
  --mode subscription \
  --success-url "http://localhost:3000/success" \
  --cancel-url "http://localhost:3000/cancel" \
  --line-items[0][price]=price_xxxxxxxxxxxxx \
  --line-items[0][quantity]=1
```

### Logs em Tempo Real

```bash
# Ver todos os logs da API
stripe logs tail

# Filtrar apenas eventos de webhook
stripe logs tail --filter-event-type='*'
```

---

## 🔍 Workflow Completo de Desenvolvimento

### 1. Iniciar Ambiente

```powershell
# Terminal 1: Iniciar Docker (PostgreSQL, pgAdmin, Redis)
cd c:\freelancer\manager\gestao-mensalidades-api
docker compose up -d

# Terminal 2: Iniciar Stripe Listener
stripe listen --thin-events '*' --forward-thin-to localhost:8800/api/webhooks/stripe

# Terminal 3: Iniciar API Spring Boot (quando estiver pronta)
./gradlew bootRun
```

### 2. Testar Fluxo de Assinatura

```bash
# 1. Criar checkout session via API ou CLI
stripe checkout sessions create \
  --mode subscription \
  --success-url "http://192.168.1.12:3000/success?session_id={CHECKOUT_SESSION_ID}" \
  --cancel-url "http://192.168.1.12:3000/cancel" \
  --customer-email "teste@exemplo.com" \
  --line-items[0][price]=price_xxxxxxxxxxxxx \
  --line-items[0][quantity]=1

# 2. Acessar URL retornada e completar o checkout

# 3. Verificar eventos no listener (Terminal 2)

# 4. Verificar dados salvos no banco (pgAdmin: http://192.168.1.12:8800)
```

---

## 🐛 Troubleshooting

### Problema: "stripe: command not found"

**Solução:**
```powershell
# Windows: Adicionar ao PATH
$env:Path += ";C:\caminho\para\stripe"

# Ou usar caminho completo
C:\caminho\para\stripe.exe listen --forward-to localhost:8800/api/webhooks/stripe
```

### Problema: Webhook não está recebendo eventos

**Verificações:**

1. **API está rodando?**
   ```powershell
   curl http://localhost:8800/api/actuator/health
   ```

2. **Listener está ativo?**
   - Verifique se o terminal com `stripe listen` está rodando
   - Veja se aparecem logs quando você trigger eventos

3. **Porta correta?**
   - Certifique-se que está usando porta 8800 (não 8080)
   - Verifique o `.env`: `SERVER_PORT=8800`

4. **Endpoint correto?**
   - Deve ser: `/api/webhooks/stripe`
   - Não `/webhooks/stripe` (sem `/api`)

### Problema: Eventos não aparecem no banco

**Verificações:**

1. **Webhook Secret correto no .env?**
   ```env
   STRIPE_WEBHOOK_SECRET=whsec_xxxxxxxxxxxxx
   ```

2. **Controller implementado?**
   - Verifique se `StripeWebhookController` existe
   - Endpoint deve validar assinatura do webhook

3. **Ver logs da aplicação**
   ```bash
   # Se rodando via Gradle
   ./gradlew bootRun --console=plain

   # Se rodando no Docker
   docker compose logs -f app
   ```

### Problema: Erro de assinatura inválida (401)

**Causa:** Webhook secret está incorreto ou não foi configurado

**Solução:**
1. Copie o secret exibido quando executa `stripe listen`
2. Atualize no `.env`: `STRIPE_WEBHOOK_SECRET=whsec_xxxxx`
3. Reinicie a aplicação

---

## 📝 Checklist de Configuração

Antes de começar o desenvolvimento:

- [ ] ✅ Stripe CLI instalada e autenticada (`stripe login`)
- [ ] ✅ Produtos criados no Stripe Dashboard
- [ ] ✅ IDs dos produtos adicionados ao `.env`
- [ ] ✅ `stripe listen` rodando em um terminal
- [ ] ✅ Webhook secret copiado para `.env`
- [ ] ✅ API Spring Boot rodando na porta 8800
- [ ] ✅ PostgreSQL rodando (Docker Compose)
- [ ] ✅ Endpoint de webhook implementado
- [ ] ✅ Testes básicos funcionando (`stripe trigger`)

---

## 🎯 Próximos Passos

### Para Implementação:

1. **Criar Controller de Webhook**
   - Endpoint: `POST /api/webhooks/stripe`
   - Validar assinatura do Stripe
   - Processar eventos

2. **Implementar Handlers de Eventos**
   - `checkout.session.completed`
   - `customer.subscription.created`
   - `customer.subscription.updated`
   - `customer.subscription.deleted`
   - `invoice.paid`
   - `invoice.payment_failed`

3. **Testar Cada Evento**
   - Usar `stripe trigger` para cada tipo de evento
   - Verificar se dados são salvos corretamente no banco
   - Validar fluxo completo

---

## 🔗 Links Úteis

- **Stripe CLI Docs:** https://stripe.com/docs/stripe-cli
- **Dashboard (Test Mode):** https://dashboard.stripe.com/test/dashboard
- **Webhooks:** https://dashboard.stripe.com/test/webhooks
- **API Reference:** https://stripe.com/docs/api
- **Testing:** https://stripe.com/docs/testing

---

## 💡 Dicas Importantes

1. **Sempre use Test Mode em desenvolvimento**
   - Chaves começam com `sk_test_` e `pk_test_`
   - Use cartões de teste: https://stripe.com/docs/testing

2. **Mantenha o listener rodando**
   - Deixe um terminal dedicado para `stripe listen`
   - Veja os eventos em tempo real

3. **Valide SEMPRE a assinatura dos webhooks**
   - Use `stripe.Webhook.constructEvent()` no backend
   - Nunca confie em webhooks sem validação

4. **Use logs para debug**
   ```bash
   stripe logs tail
   ```

5. **Teste cenários de erro**
   - Pagamento falhou
   - Assinatura cancelada
   - Invoice não pago

---

**Ambiente:** Desenvolvimento (Test Mode)  
**Última atualização:** 05/01/2025
