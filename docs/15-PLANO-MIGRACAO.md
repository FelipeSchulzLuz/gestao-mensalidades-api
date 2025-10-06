# 📋 Plano de Migração: Java/PostgreSQL → Firebase

> 📚 **STATUS: DOCUMENTO DE REFERÊNCIA HISTÓRICA**  
> **Decisão Final:** ✅ **Continuamos com Java/Spring Boot + PostgreSQL**  
> 
> **Este documento descreve um plano de migração para Firebase que NÃO foi executado.**  
> 
> **✅ Arquitetura Confirmada (ATUAL):**
> - **Backend:** Java 21 + Spring Boot 3
> - **Database:** PostgreSQL 15
> - **Auth:** JWT + Firebase Auth (híbrido)
> - **Pagamentos:** Stripe SDK
>
> **📖 Para a arquitetura implementada, leia:**  
> - [02-ARQUITETURA.md](./02-ARQUITETURA.md) - Clean Architecture  
> - [07-AUTENTICACAO-HIBRIDA.md](./07-AUTENTICACAO-HIBRIDA.md) - Autenticação híbrida  
> - [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md) - Products + Prices
>
> **Mantenha este documento apenas como referência para:**
> - Comparação de complexidade entre stacks
> - Análise de custos Firebase vs VPS
> - Futuros projetos que considerem Firebase

---

## ⚠️ AVISO: Conteúdo Abaixo É Apenas Referência

**NÃO execute as etapas descritas abaixo.**  
**A migração para Firebase foi DESCARTADA.**

---

## 📊 Sumário Executivo (REFERÊNCIA HISTÓRICA)

### Situação Atual (CONFIRMADA)

- ✅ **Backend Java Spring Boot** completo e em produção
- ✅ **PostgreSQL + Docker** configurado
- ✅ **Stripe SDK** integração direta
- ✅ **Autenticação Híbrida** (JWT + Firebase Auth)
- ✅ **Clean Architecture** implementada

### Situação Alternativa Considerada (NÃO IMPLEMENTADA)

Esta seção descreve uma análise de Firebase que foi estudada mas não implementada:

- ❌ **Frontend SPA** (React/Vue/Angular)
- ❌ **Firebase Authentication** (substituir JWT)
- ❌ **Cloud Firestore** (substituir PostgreSQL)
- ❌ **Cloud Functions TypeScript** (substituir Spring Boot)
- ❌ **Stripe Extension** (substituir SDK direto)
- ❌ **Firebase Hosting** (hospedagem automática)

### Impacto da Mudança

| Componente | Status | Ação |
|------------|--------|------|
| Backend Java Spring Boot | ✅ Completo | 🗑️ **DESCARTAR** |
| PostgreSQL + Docker | ✅ Configurado | 🗑️ **DESCARTAR** |
| Sistema JWT customizado | ✅ Implementado | 🗑️ **DESCARTAR** (Firebase Auth) |
| Stripe SDK Java | ✅ Integrado | 🗑️ **DESCARTAR** (Stripe Extension) |
| Documentação | ✅ Organizada | ✅ **MANTER + ATUALIZAR** |
| Conceitos Clean Architecture | ✅ Aplicados | ✅ **ADAPTAR** para Firebase |

---

## 🎯 Objetivos da Migração

### Objetivos Técnicos

1. ✅ **Eliminar complexidade de infraestrutura** (Docker, PostgreSQL, servidor)
2. ✅ **Reduzir tempo de desenvolvimento** (4-6 semanas → 3-5 dias)
3. ✅ **Automatizar integração Stripe** (80% do código eliminado)
4. ✅ **Escalar automaticamente** (serverless)
5. ✅ **Reduzir custos iniciais** ($45-230/mês → $0-10/mês)

### Objetivos de Negócio

1. ✅ **Time-to-market mais rápido** (lançar MVP em dias, não meses)
2. ✅ **Menor custo de manutenção** (infraestrutura gerenciada)
3. ✅ **Foco no produto** (não em infraestrutura)
4. ✅ **Escalabilidade garantida** (Google infrastructure)

---

## 📦 Estratégia de Migração

### Opção 1: Branch Separada (Recomendada) ⭐

**Vantagens:**
- ✅ Manter histórico Java como referência
- ✅ Possibilidade de reverter decisão
- ✅ Comparar implementações lado a lado
- ✅ Extrair lógica de negócio do código Java

**Desvantagens:**
- ⚠️ Dois códigos no mesmo repositório (temporário)

**Estrutura:**
```
gestao-mensalidades-api/
├── master (backend Java - frozen)
└── feature/firebase-migration (novo código Firebase)
```

### Opção 2: Novo Repositório

**Vantagens:**
- ✅ Separação total
- ✅ Histórico limpo

**Desvantagens:**
- ❌ Perda de contexto histórico
- ❌ Mais difícil comparar código

### Opção 3: Monorepo

**Vantagens:**
- ✅ Tudo em um lugar
- ✅ Compartilhar tipos/interfaces

**Desvantagens:**
- ❌ Mais complexo de gerenciar
- ❌ Builds separados

### ✅ Decisão: **Opção 1 - Branch Separada**

---

## 📅 Fases de Implementação

### Fase 0: Preparação (1 hora)

- [ ] Criar branch `feature/firebase-migration`
- [ ] Instalar Firebase CLI: `npm install -g firebase-tools`
- [ ] Criar projeto Firebase no console
- [ ] Instalar Stripe Extension
- [ ] Configurar variáveis de ambiente

### Fase 1: Configuração Inicial Firebase (2-3 horas)

#### 1.1 Setup do Projeto
- [ ] Executar `firebase init`
- [ ] Selecionar serviços: Authentication, Firestore, Functions, Hosting
- [ ] Configurar região (us-central1 ou southamerica-east1)
- [ ] Criar estrutura de pastas

#### 1.2 Configurar Firebase Authentication
- [ ] Habilitar Email/Password provider
- [ ] Habilitar Google OAuth (opcional)
- [ ] Configurar domínios autorizados
- [ ] Criar templates de email customizados

#### 1.3 Configurar Cloud Firestore
- [ ] Criar banco em modo Native
- [ ] Configurar regras de segurança iniciais
- [ ] Criar índices necessários
- [ ] Habilitar offline persistence (frontend)

#### 1.4 Instalar Stripe Extension
- [ ] Instalar extension: `Run Payments with Stripe`
- [ ] Configurar Stripe API keys (test + prod)
- [ ] Configurar webhook secret
- [ ] Testar sincronização de produtos

### Fase 2: Frontend SPA (4-6 horas)

#### 2.1 Escolher Framework
- [ ] **Opção A:** React + Vite + TailwindCSS
- [ ] **Opção B:** Vue 3 + Vite + TailwindCSS
- [ ] **Opção C:** Next.js (React) + TailwindCSS

#### 2.2 Setup do Frontend
- [ ] Criar projeto com Vite/Next.js
- [ ] Instalar Firebase SDK: `npm install firebase`
- [ ] Configurar Firebase config
- [ ] Criar contexto de autenticação
- [ ] Criar componentes base (Layout, Navbar, Footer)

#### 2.3 Implementar Autenticação
- [ ] Página de registro (`/register`)
- [ ] Página de login (`/login`)
- [ ] Página de recuperação de senha (`/forgot-password`)
- [ ] Profile page (`/profile`)
- [ ] Logout functionality
- [ ] Protected routes (HOC ou middleware)

#### 2.4 Implementar Planos
- [ ] Página de listagem de planos (`/plans`)
- [ ] Componente de card de plano
- [ ] Botão "Assinar" que cria checkout_session
- [ ] Listener em tempo real para receber URL
- [ ] Redirecionamento para Stripe Checkout

#### 2.5 Implementar Dashboard do Usuário
- [ ] Página de dashboard (`/dashboard`)
- [ ] Exibir status da assinatura
- [ ] Botão para gerenciar assinatura (portal do cliente)
- [ ] Histórico de pagamentos
- [ ] Informações da conta

### Fase 3: Cloud Functions (3-4 horas)

#### 3.1 Setup TypeScript
- [ ] Configurar TypeScript no diretório `functions/`
- [ ] Instalar dependências: `npm install firebase-admin firebase-functions`
- [ ] Configurar ESLint + Prettier
- [ ] Criar estrutura de pastas (src/, triggers/, callable/)

#### 3.2 Implementar Triggers
- [ ] **onCreate Auth:** Criar documento `users/{uid}` no Firestore
- [ ] **onCreate Auth:** Enviar email de boas-vindas
- [ ] **onWrite Subscription:** Atualizar custom claims (premium/free)
- [ ] **onWrite Subscription:** Atualizar documento do usuário
- [ ] **onCreate Payment:** Enviar email de confirmação
- [ ] **onDelete Auth:** Limpar dados do usuário (GDPR)

#### 3.3 Implementar Callable Functions
- [ ] `createPortalLink()` - Criar link do portal do cliente Stripe
- [ ] `cancelSubscription()` - Cancelar assinatura
- [ ] `updateSubscription()` - Alterar plano
- [ ] `getSubscriptionStatus()` - Obter status detalhado

#### 3.4 Implementar Scheduled Functions
- [ ] Relatório mensal de assinaturas ativas
- [ ] Limpeza de checkout_sessions expiradas
- [ ] Notificar usuários com assinaturas expirando

### Fase 4: Regras de Segurança (2 horas)

#### 4.1 Firestore Security Rules
- [ ] Regras para coleção `users/`
- [ ] Regras para coleção `customers/`
- [ ] Regras para subcoleção `checkout_sessions/`
- [ ] Regras para subcoleção `subscriptions/`
- [ ] Regras para coleção `products/`
- [ ] Regras para coleção `reports/` (admin only)

#### 4.2 Firebase Authentication Rules
- [ ] Configurar custom claims
- [ ] Validar roles (user, premium, admin)
- [ ] Configurar middleware de autenticação

### Fase 5: Integração Stripe (2-3 horas)

#### 5.1 Configurar Produtos no Stripe Dashboard
- [ ] Criar produto "Plano Mensal"
- [ ] Criar preço mensal (ex: R$ 29,90)
- [ ] Criar produto "Plano Anual"
- [ ] Criar preço anual (ex: R$ 299,00)
- [ ] Adicionar metadata `firebaseRole: premium`
- [ ] Sincronizar produtos com Firestore (automático via extensão)

#### 5.2 Testar Fluxo de Checkout
- [ ] Criar checkout session no Firestore
- [ ] Verificar criação automática da sessão no Stripe
- [ ] Redirecionar para Stripe Checkout
- [ ] Completar pagamento (modo test)
- [ ] Verificar webhook received
- [ ] Verificar subscription criada no Firestore
- [ ] Verificar custom claims atualizados

#### 5.3 Testar Portal do Cliente
- [ ] Criar link do portal via callable function
- [ ] Acessar portal do Stripe
- [ ] Testar cancelamento de assinatura
- [ ] Verificar sincronização no Firestore

### Fase 6: UI/UX (3-4 horas)

#### 6.1 Design System
- [ ] Definir paleta de cores
- [ ] Configurar TailwindCSS theme
- [ ] Criar componentes reutilizáveis (Button, Card, Input, etc.)
- [ ] Criar loading states
- [ ] Criar error states
- [ ] Implementar toasts/notificações

#### 6.2 Páginas
- [ ] Landing page (`/`)
- [ ] Pricing page (`/pricing`)
- [ ] About page (`/about`)
- [ ] Contact page (`/contact`)
- [ ] Terms of Service (`/terms`)
- [ ] Privacy Policy (`/privacy`)

#### 6.3 Responsividade
- [ ] Testar mobile (< 640px)
- [ ] Testar tablet (640px - 1024px)
- [ ] Testar desktop (> 1024px)
- [ ] Testar diferentes navegadores

### Fase 7: Testes (2-3 horas)

#### 7.1 Testes Manuais
- [ ] Fluxo completo: Registro → Login → Assinar → Pagamento → Dashboard
- [ ] Testar cancelamento de assinatura
- [ ] Testar falha de pagamento
- [ ] Testar acesso a rotas protegidas sem login
- [ ] Testar acesso a recursos premium sem assinatura

#### 7.2 Testes Automatizados (opcional)
- [ ] Unit tests (Cloud Functions)
- [ ] Integration tests (Firestore rules)
- [ ] E2E tests (Cypress ou Playwright)

### Fase 8: Deploy e Monitoramento (1-2 horas)

#### 8.1 Deploy
- [ ] Deploy Cloud Functions: `firebase deploy --only functions`
- [ ] Deploy Firestore Rules: `firebase deploy --only firestore:rules`
- [ ] Deploy Frontend: `firebase deploy --only hosting`
- [ ] Configurar domínio customizado (opcional)

#### 8.2 Monitoramento
- [ ] Configurar alertas no Firebase Console
- [ ] Configurar alertas no Stripe Dashboard
- [ ] Monitorar logs das Cloud Functions
- [ ] Monitorar uso do Firestore
- [ ] Configurar error tracking (Sentry opcional)

---

## 📂 Estrutura de Arquivos (Nova)

### Estrutura Completa

```
gestao-mensalidades-firebase/
├── .firebaserc                      # Configuração de projetos Firebase
├── firebase.json                    # Configuração geral do Firebase
├── firestore.rules                  # Regras de segurança Firestore
├── firestore.indexes.json           # Índices Firestore
├── storage.rules                    # Regras de segurança Storage (se usar)
│
├── functions/                       # Cloud Functions (Backend)
│   ├── package.json
│   ├── tsconfig.json
│   ├── .eslintrc.js
│   ├── src/
│   │   ├── index.ts                # Entry point
│   │   ├── config/
│   │   │   └── firebase.ts         # Firebase Admin config
│   │   ├── triggers/
│   │   │   ├── auth.triggers.ts    # Triggers de autenticação
│   │   │   ├── subscription.triggers.ts  # Triggers de assinatura
│   │   │   └── payment.triggers.ts # Triggers de pagamento
│   │   ├── callable/
│   │   │   ├── subscription.callable.ts  # Functions callable
│   │   │   └── portal.callable.ts
│   │   ├── scheduled/
│   │   │   └── reports.scheduled.ts
│   │   ├── utils/
│   │   │   ├── email.utils.ts
│   │   │   └── validation.utils.ts
│   │   └── types/
│   │       └── index.ts
│   └── lib/                        # Build output (gerado)
│
├── public/                          # Assets estáticos (Firebase Hosting)
│   ├── index.html
│   └── favicon.ico
│
└── src/                            # Frontend (React/Vue/Next.js)
    ├── main.tsx                    # Entry point
    ├── App.tsx
    │
    ├── config/
    │   └── firebase.config.ts      # Firebase SDK config
    │
    ├── contexts/
    │   └── AuthContext.tsx         # Context de autenticação
    │
    ├── hooks/
    │   ├── useAuth.ts              # Hook de autenticação
    │   ├── useSubscription.ts      # Hook de assinatura
    │   └── useCheckout.ts          # Hook de checkout
    │
    ├── components/
    │   ├── layout/
    │   │   ├── Navbar.tsx
    │   │   ├── Footer.tsx
    │   │   └── Layout.tsx
    │   ├── auth/
    │   │   ├── LoginForm.tsx
    │   │   ├── RegisterForm.tsx
    │   │   └── ProtectedRoute.tsx
    │   ├── subscription/
    │   │   ├── PlanCard.tsx
    │   │   ├── SubscriptionStatus.tsx
    │   │   └── CheckoutButton.tsx
    │   └── ui/
    │       ├── Button.tsx
    │       ├── Card.tsx
    │       └── Input.tsx
    │
    ├── pages/
    │   ├── Home.tsx
    │   ├── Login.tsx
    │   ├── Register.tsx
    │   ├── Pricing.tsx
    │   ├── Dashboard.tsx
    │   └── Profile.tsx
    │
    ├── services/
    │   ├── auth.service.ts         # Serviços de autenticação
    │   ├── subscription.service.ts # Serviços de assinatura
    │   └── firestore.service.ts    # Helpers Firestore
    │
    ├── types/
    │   ├── user.types.ts
    │   ├── subscription.types.ts
    │   └── product.types.ts
    │
    ├── styles/
    │   └── globals.css             # Estilos globais + Tailwind
    │
    └── utils/
        ├── formatters.ts           # Formatação de datas, moedas
        └── validators.ts           # Validação de formulários
```

---

## ✅ Checklist de Verificação

### Pré-Deploy

- [ ] Todas as variáveis de ambiente configuradas
- [ ] Stripe em modo test funcionando
- [ ] Regras de segurança Firestore validadas
- [ ] Custom claims funcionando
- [ ] Todos os fluxos testados manualmente
- [ ] Error handling implementado
- [ ] Loading states implementados
- [ ] Responsividade testada
- [ ] Performance otimizada (lazy loading, code splitting)
- [ ] SEO básico configurado (meta tags)

### Pós-Deploy

- [ ] Aplicação acessível via Firebase Hosting
- [ ] Firebase Authentication funcionando
- [ ] Firestore leitura/escrita funcionando
- [ ] Cloud Functions executando
- [ ] Stripe Extension sincronizando
- [ ] Webhooks Stripe recebidos
- [ ] Emails sendo enviados
- [ ] Monitoramento ativo
- [ ] Logs sendo coletados

---

## 🔍 Comparação: Antes vs. Depois

### Complexidade de Código

| Tarefa | Java + PostgreSQL | Firebase | Redução |
|--------|------------------|----------|---------|
| **Autenticação** | 8 arquivos, ~600 linhas | Firebase SDK, 0 linhas | **-100%** |
| **Criar Cliente Stripe** | 50 linhas | 0 linhas (extensão) | **-100%** |
| **Webhooks Stripe** | 200+ linhas | 0 linhas (extensão) | **-100%** |
| **CRUD Usuário** | 150 linhas (JPA) | 20 linhas (Firestore SDK) | **-87%** |
| **Checkout Stripe** | 100 linhas | 15 linhas (escrever doc) | **-85%** |
| **Total Backend** | ~1,400 linhas | ~200 linhas | **-86%** |

### Arquivos Criados

| Categoria | Java + PostgreSQL | Firebase |
|-----------|------------------|----------|
| **Backend** | 20 arquivos Java | 5-7 arquivos TypeScript |
| **Config** | 5 arquivos (yml, properties, gradle) | 2 arquivos (firebase.json, .env) |
| **Database** | Schema SQL, migrations | Regras Firestore (1 arquivo) |
| **Auth** | 8 arquivos (JWT, Security) | Firebase Auth (0 arquivos customizados) |
| **Total** | **33+ arquivos** | **8-10 arquivos** |

### Tempo de Desenvolvimento

| Fase | Java + PostgreSQL | Firebase | Economia |
|------|------------------|----------|----------|
| Setup inicial | 1-2 semanas | 1 dia | **-85%** |
| Autenticação | 3-5 dias | 2 horas | **-95%** |
| Database | 2-3 dias | 3 horas | **-90%** |
| Stripe | 5-7 dias | 4 horas | **-95%** |
| Deploy | 1-2 dias | 1 hora | **-92%** |
| **TOTAL** | **4-6 semanas** | **3-5 dias** | **-90%** |

---

## 🚨 Riscos e Mitigações

### Risco 1: Curva de Aprendizado Firebase

**Risco:** Time não conhece Firebase  
**Impacto:** Médio  
**Mitigação:**
- ✅ Documentação oficial excelente
- ✅ Comunidade grande e ativa
- ✅ Exemplos abundantes no GitHub
- ✅ Seguir este guia passo a passo

### Risco 2: Vendor Lock-in (Google)

**Risco:** Dependência do Firebase/Google Cloud  
**Impacto:** Baixo (comum em cloud)  
**Mitigação:**
- ✅ Firebase é baseado em padrões (Firestore = NoSQL standard)
- ✅ Cloud Functions = Node.js standard
- ✅ Possível migrar para AWS/Azure se necessário (trabalhoso mas viável)
- ✅ Abstrair lógica de negócio em funções puras

### Risco 3: Custos Inesperados

**Risco:** Uso além do tier gratuito  
**Impacto:** Baixo  
**Mitigação:**
- ✅ Configurar alertas de billing
- ✅ Limitar uso com quotas
- ✅ Monitorar métricas diariamente
- ✅ Otimizar queries Firestore (menos leituras)
- ✅ Usar cache quando possível

### Risco 4: Limitações Firestore

**Risco:** Firestore não suporta queries complexas  
**Impacto:** Baixo  
**Mitigação:**
- ✅ Desnormalizar dados quando necessário
- ✅ Usar Cloud Functions para agregações
- ✅ BigQuery para analytics complexos (se necessário)
- ✅ Modelar dados pensando em queries necessárias

---

## 💰 Análise de Custos

### Custos Previstos (Firebase Blaze Plan)

| Serviço | Uso Mensal (estimado) | Custo |
|---------|----------------------|-------|
| **Firebase Auth** | 1,000 usuários ativos | Grátis |
| **Cloud Firestore** | 50K leituras, 20K escritas, 1GB storage | $0.18 |
| **Cloud Functions** | 50K invocações, 100K GB-s | $0.50 |
| **Firebase Hosting** | 10GB transferência, 500 milhões operações | Grátis |
| **Cloud Storage** | 5GB armazenamento | Grátis |
| **TOTAL** | | **~$0.68/mês** |

### Comparação com Backend Java

| Item | Backend Java | Firebase | Economia |
|------|--------------|----------|----------|
| Servidor (VM) | $20-50/mês | $0 | **$20-50/mês** |
| PostgreSQL | $15-30/mês | $0.18 | **$15-30/mês** |
| Load Balancer | $15/mês | $0 | **$15/mês** |
| SSL | $0-10/ano | $0 | **$0-10/ano** |
| Backup | $5-10/mês | $0 | **$5-10/mês** |
| **TOTAL** | **$55-115/mês** | **~$0.68/mês** | **$54-114/mês** |

**Economia anual:** **$650-1,370/ano** 💰

---

## 📝 Documentação a Criar

### Documentos Técnicos

- [ ] **README.md** - Overview do projeto Firebase
- [ ] **SETUP.md** - Instruções de setup local
- [ ] **DEPLOY.md** - Processo de deploy
- [ ] **API.md** - Documentação das Cloud Functions
- [ ] **FIRESTORE_SCHEMA.md** - Esquema de dados Firestore
- [ ] **SECURITY_RULES.md** - Explicação das regras

### Documentos de Usuário

- [ ] **USER_GUIDE.md** - Guia do usuário final
- [ ] **ADMIN_GUIDE.md** - Guia do administrador
- [ ] **FAQ.md** - Perguntas frequentes

---

## 🎓 Recursos de Aprendizado

### Cursos Recomendados

1. **Firebase Fundamentals (Grátis)**
   - [Firebase Web Codelab](https://firebase.google.com/codelabs/firebase-web)
   - Duração: 2-3 horas

2. **Stripe Extension (Grátis)**
   - [Stripe Extension Docs](https://github.com/stripe/stripe-firebase-extensions)
   - Duração: 1 hora

3. **Firestore Data Modeling**
   - [Get to know Cloud Firestore](https://www.youtube.com/playlist?list=PLl-K7zZEsYLluG5MCVEzXAQ7ACZBCuZgZ)
   - Duração: 3 horas

### Tutoriais Recomendados

- [Build a subscription service with Firebase and Stripe](https://stripe.com/docs/payments/checkout/subscriptions/build-subscription-service)
- [Firebase Authentication with React](https://firebase.google.com/docs/auth/web/start)
- [Cloud Functions TypeScript](https://firebase.google.com/docs/functions/typescript)

---

## 🏁 Próximos Passos Imediatos

### 1. Decisão (Agora)

- [ ] Aprovar migração para Firebase
- [ ] Definir framework frontend (React/Vue/Next.js)
- [ ] Definir timeline (3-5 dias)

### 2. Setup (1-2 horas)

- [ ] Criar branch `feature/firebase-migration`
- [ ] Criar projeto Firebase
- [ ] Instalar Firebase CLI
- [ ] Executar `firebase init`

### 3. Desenvolvimento (3-4 dias)

- [ ] Seguir checklist das Fases 1-8
- [ ] Commits organizados (Conventional Commits)
- [ ] Testes contínuos

### 4. Deploy e Validação (1 dia)

- [ ] Deploy em ambiente de staging
- [ ] Testes finais
- [ ] Deploy em produção

---

## 📞 Suporte e Ajuda

### Em Caso de Dúvidas

1. **Firebase Console:** https://console.firebase.google.com
2. **Firebase Support:** https://firebase.google.com/support
3. **Stack Overflow:** Tag `firebase`
4. **Discord Firebase:** https://discord.gg/BN2cgc3
5. **Stripe Support:** https://support.stripe.com

---

## ✅ Aprovação e Início

**Status:** 🟡 Aguardando aprovação para iniciar

**Aprovação necessária:**
- [ ] Product Owner
- [ ] Tech Lead
- [ ] Time de Desenvolvimento

**Após aprovação:**
1. Criar branch `feature/firebase-migration`
2. Iniciar Fase 0 (Preparação)
3. Executar checklist passo a passo

---

**Última atualização:** Outubro 2025  
**Autor:** Sistema de IA  
**Versão:** 1.0
