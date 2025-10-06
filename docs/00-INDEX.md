# 📚 Índice da Documentação

> **Guia completo de navegação pela documentação do projeto**

Este documento serve como índice centralizado de toda a documentação do projeto. Leia os documentos na ordem sugerida para melhor compreensão.

---

## 📖 Ordem de Leitura Recomendada

### Para Novos Desenvolvedores

1. **README.md** (raiz) - Visão geral do projeto
2. **18-MUDANCA-ARQUITETURAL-RESUMO.md** - 🚨 **LEIA ANTES DE CODIFICAR!**
3. **17-ARQUITETURA-HIBRIDA.md** - 🆕 Arquitetura completa (30 min)
4. **01-INICIO-RAPIDO.md** - Configure e rode o projeto
5. **02-ARQUITETURA.md** - Entenda a arquitetura Clean
6. **09-CHECKLIST-DESENVOLVIMENTO.md** - Veja as tarefas

### Para Desenvolvimento Ativo

1. **18-MUDANCA-ARQUITETURAL-RESUMO.md** - 🚨 Resumo executivo da mudança
2. **17-ARQUITETURA-HIBRIDA.md** - 🔥 Especificação técnica completa
3. **09-CHECKLIST-DESENVOLVIMENTO.md** - Tarefas pendentes (será atualizado)
4. **99-STATUS.md** - Progresso atual
5. **04-STRIPE.md** - Referência de integração Stripe

### ✅ Arquitetura Confirmada: Java/Spring Boot + PostgreSQL

**Arquitetura oficial do projeto:**
- Backend: Java 21 + Spring Boot 3.5.6
- Database: PostgreSQL 15 (Docker)
- Autenticação: Híbrida (JWT + Firebase Auth)
- Pagamentos: Stripe (Products + Prices model)

### Para Configuração de Infraestrutura

1. **03-DOCKER.md** - Configuração completa Docker
2. **04-STRIPE.md** - Setup Stripe CLI e webhooks
3. **01-INICIO-RAPIDO.md** - Workflow completo

---

## 📂 Documentos Principais

### Raiz do Projeto

#### 📘 [README.md](../README.md)
**Descrição:** Documento principal do projeto com visão geral e quick start.  
**Quando usar:** Primeira vez no projeto, compartilhar com novos membros.  
**Conteúdo:** Stack tecnológica, comandos rápidos, estrutura, status.

#### 📝 .env.example
**Descrição:** Template de variáveis de ambiente.  
**Quando usar:** Primeira configuração, referência de variáveis necessárias.  
**Conteúdo:** Todas variáveis com valores exemplo/placeholder.

---

### Pasta docs/

#### 🏁 [01-INICIO-RAPIDO.md](./01-INICIO-RAPIDO.md)
**Descrição:** Guia passo a passo para iniciar o projeto pela primeira vez.  
**Quando usar:** Setup inicial, novos desenvolvedores, troubleshooting.  
**Conteúdo:**
- Diagrama de arquitetura (Docker server vs Stripe local)
- 5 passos para rodar o projeto
- Comandos úteis (Docker, Stripe, Gradle)
- Troubleshooting comum
- Workflow típico de desenvolvimento

**Tempo de leitura:** 10 minutos  
**Pré-requisitos:** Docker, Stripe CLI, Java 21

#### 🏗️ [02-ARQUITETURA.md](./02-ARQUITETURA.md)
**Descrição:** Arquitetura completa do sistema (Clean Architecture).  
**Quando usar:** Entender estrutura, planejar features, code review.

#### 🐳 [03-DOCKER.md](./03-DOCKER.md)
**Descrição:** Configuração e uso do Docker no projeto.  
**Quando usar:** Setup infraestrutura, troubleshooting containers.

#### 💳 [04-STRIPE.md](./04-STRIPE.md)
**Descrição:** Integração completa com Stripe (webhooks, CLI, API).  
**Quando usar:** Implementar pagamentos, debugar webhooks.

####  [07-AUTENTICACAO-HIBRIDA.md](./07-AUTENTICACAO-HIBRIDA.md)
**Descrição:** Sistema de autenticação JWT + Firebase Auth.  
**Quando usar:** Entender fluxo de autenticação, implementar endpoints.

#### 🗄️ [08-MIGRATIONS-BOAS-PRATICAS.md](./08-MIGRATIONS-BOAS-PRATICAS.md)
**Descrição:** Guia completo sobre database migrations.  
**Quando usar:** Criar migrations, aplicar mudanças de schema.  
**Conteúdo:** Filosofia, workflows, boas práticas, ferramentas.

#### ✅ [09-CHECKLIST-DESENVOLVIMENTO.md](./09-CHECKLIST-DESENVOLVIMENTO.md)
**Descrição:** Checklist completo das 14 fases de desenvolvimento.  
**Quando usar:** Planejar sprints, verificar progresso, identificar gaps.  
**Conteúdo:** 14 fases detalhadas, estimativas, dependências, status.

#### 📝 [10-COMO-APLICAR-MIGRATION.md](./10-COMO-APLICAR-MIGRATION.md)
**Descrição:** Guia visual passo a passo para aplicar migrations.  
**Quando usar:** Aplicar migration manualmente via DBeaver/pgAdmin.  
**Conteúdo:** Screenshots, troubleshooting, verificação.

#### 🔐 [11-CREDENCIAIS.md](./11-CREDENCIAIS.md) *(não commitado)*
**Descrição:** Credenciais sensíveis (senhas, chaves, tokens).  
**Quando usar:** Configuração local, onboarding de desenvolvedores.  
**Conteúdo:** Senhas PostgreSQL, chaves Stripe, JWT secrets.  
**⚠️ NUNCA commite este arquivo!**

#### 🚀 [12-DEPLOY-SERVIDOR.md](./12-DEPLOY-SERVIDOR.md)
**Descrição:** Guia completo de deploy em servidor de produção.  
**Quando usar:** Deploy inicial, atualização de produção.  
**Conteúdo:** Arquivos para copiar, .env produção, passos Docker, segurança.

#### 🔄 [13-HYBRID-AUTH-STATUS.md](./13-HYBRID-AUTH-STATUS.md)
**Descrição:** Status da implementação de autenticação híbrida.  
**Quando usar:** Verificar progresso de implementação Firebase Auth.

#### 📦 [14-IMPLEMENTACAO-COMPLETA.md](./14-IMPLEMENTACAO-COMPLETA.md)
**Descrição:** Resumo da implementação completa do sistema.  
**Quando usar:** Visão geral do que foi implementado, documentação de referência.

####  [16-PROGRESSO.md](./16-PROGRESSO.md)
**Descrição:** Tracking de progresso e próximos passos.  
**Quando usar:** Reuniões de status, planejamento de sprints.

#### 🏗️ [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md) 🆕 **FUNDAMENTAL**
**Descrição:** 🔥 **Nova arquitetura: De "Planos" para "Produtos + Preços"**  
**Quando usar:** **LEIA ANTES DE QUALQUER IMPLEMENTAÇÃO!**  
**Conteúdo:**
- **Mudança de paradigma:** Plan → Product + Price
- **Multi-tenant:** Organizações com commerce_mode
- **Híbrido:** Suporte a assinaturas + e-commerce
- **Modelo espelhado:** Alinhado com Stripe
- **Schema completo:** organizations, products, prices, orders
- **Fluxos de checkout:** subscription vs payment
- **Webhooks inteligentes:** Decisão baseada em mode
- **Checklist de migração:** 6 fases

**Tempo de leitura:** 30 minutos  
**Impacto:** 🔴 **CRÍTICO** - Refatoração arquitetural completa  
**Status:** ✅ Aprovado - Pronto para implementação  
**Pré-requisitos:** Entender 02-ARQUITETURA.md e 04-STRIPE.md

---

#### 🚨 [18-MUDANCA-ARQUITETURAL-RESUMO.md](./18-MUDANCA-ARQUITETURAL-RESUMO.md) **OBRIGATÓRIO**
**Descrição:** 🚨 **Resumo executivo: LEIA ANTES DE CODIFICAR!**  
**Quando usar:** **Antes de qualquer implementação, PRs, code reviews**  
**Conteúdo:**
- **TL;DR:** O que mudou e por quê
- **Checklist rápido:** 5 perguntas antes de codificar
- **Modelo mental:** Product, Price, CommerceMode
- **Guias de decisão:** Quando criar o quê
- **Comparação visual:** Schema antes vs depois
- **Exemplos práticos:** Código Java completo
- **Armadilhas comuns:** ❌ Erros vs ✅ Correto
- **FAQ:** Perguntas frequentes

**Tempo de leitura:** 5-10 minutos  
**Impacto:** 🚨 **BLOQUEANTE** - Evita refatoração e bugs  
**Status:** ✅ Aprovado - Leitura obrigatória para todos  
**Público:** Todos desenvolvedores (frontend, backend, fullstack)  
**Complemento:** Ver 17-ARQUITETURA-HIBRIDA.md para especificação completa

---

#### 📊 [99-STATUS.md](./99-STATUS.md)
**Descrição:** Status atual do projeto e próximos passos.  
**Quando usar:** Check rápido de progresso, definir prioridades.

---

#### 🏗️ [02-ARQUITETURA.md](./02-ARQUITETURA.md)
**Descrição:** Arquitetura completa do sistema (Clean Architecture).  
**Quando usar:** Antes de começar a programar, dúvidas sobre estrutura.  
**Conteúdo:**
- Visão geral e princípios Clean Architecture
- Estrutura das 4 camadas (Domain, Application, Infrastructure, Presentation)
- Diagramas de fluxo de dados
- Padrões de projeto aplicados (Repository, Adapter, DTO, Factory, Strategy)
- Tecnologias principais
- Integração com Stripe
- Segurança (JWT, validação de webhooks)

**Tempo de leitura:** 20 minutos  
**Pré-requisitos:** Conhecimento básico de Java e Spring Boot

---

#### 🐳 [03-DOCKER.md](./03-DOCKER.md)
**Descrição:** Guia completo de Docker (PostgreSQL, pgAdmin, Redis).  
**Quando usar:** Configurar infraestrutura, problemas com Docker, produção.  
**Conteúdo:**
- Pré-requisitos e instalação
- Configuração inicial (`.env.docker`)
- Mapeamento de portas (não convencionais: 54320, 8800, 63790)
- Comandos úteis (iniciar, parar, logs, backup)
- Acesso aos serviços (PostgreSQL, pgAdmin, Redis)
- Configuração para rede 192.168.1.12
- Troubleshooting detalhado
- Backup e restauração
- Monitoramento

**Tempo de leitura:** 15 minutos  
**Pré-requisitos:** Docker e Docker Compose instalados

---

#### 💳 [04-STRIPE.md](./04-STRIPE.md)
**Descrição:** Integração completa com Stripe (SDK, CLI, webhooks).  
**Quando usar:** Implementar features de pagamento, configurar webhooks, troubleshooting Stripe.  
**Conteúdo:**
- Setup Stripe CLI
- Fluxo de assinatura completo
- Lista de webhooks (10 eventos)
- Configuração de Customer Portal
- Simular eventos no desenvolvimento
- Idempotência e segurança
- Troubleshooting Stripe

**Tempo de leitura:** 15 minutos  
**Pré-requisitos:** Conta Stripe (test mode), Stripe CLI instalado

---

---

#### 📊 [99-STATUS.md](./99-STATUS.md)
**Descrição:** Status atual do projeto e progresso das fases.  
**Quando usar:** Planejamento, standup, verificar próximos passos.  
**Conteúdo:**
- Progresso visual (14.3% - 2/14 fases)
- Detalhamento de cada fase
- Fases completas (1 e 2)
- Próximas fases
- Decisões arquiteturais
- Comandos úteis
- Métricas do projeto

**Tempo de leitura:** 10 minutos  
**Atualização:** Automática conforme progresso

---

## 🔧 Scripts (pasta scripts/)

### run-make.sh (wrapper recomendado)
**Descrição:** Inicia ambiente Docker e aplica migrations via Flyway.  
**Quando usar:** Todo dia, antes de começar desenvolvimento.  
**Funcionalidades:**
- Copia .env.docker → .env (se necessário)
- Inicia containers PostgreSQL, pgAdmin, Redis
- Executa migrations via Flyway
- Sobe a aplicação

**Uso:**
```bash
chmod +x scripts/*.sh
./scripts/run-make.sh start
```

### Stripe CLI (manual)
**Descrição:** Inicia Stripe CLI para webhooks locais.  
**Quando usar:** Desenvolver/testar features de pagamento e webhooks.  
**Funcionalidades:**
- Listener de webhooks
- Forward para `localhost:8800/api/webhooks/stripe`
- Exibe webhook secret

**Uso:**
```bash
# Login primeiro
stripe login

# Iniciar listener
stripe listen --forward-to localhost:8800/api/webhooks/stripe

# Ou custom endpoint:
stripe listen --forward-to localhost:8800/api/webhooks/custom
```

---

## 📍 Mapa de Navegação Rápida

### Preciso configurar o ambiente pela primeira vez
→ **01-INICIO-RAPIDO.md**

### Preciso entender a arquitetura do código
→ **18-MUDANCA-ARQUITETURAL-RESUMO.md** → **17-ARQUITETURA-HIBRIDA.md** → **02-ARQUITETURA.md**

### Tenho problemas com Docker
→ **03-DOCKER.md** (seção Troubleshooting)

### Preciso implementar integração com Stripe
→ **04-STRIPE.md** + **17-ARQUITETURA-HIBRIDA.md**

### Quero saber o que fazer agora
→ **09-CHECKLIST-DESENVOLVIMENTO.md** + **99-STATUS.md**

### Vou começar a codificar agora
→ 🚨 **18-MUDANCA-ARQUITETURAL-RESUMO.md** (5 min) + **17-ARQUITETURA-HIBRIDA.md** (30 min)

### Preciso rodar o projeto rapidamente
→ **README.md** (seção "Executar em 3 Passos")

### Novos desenvolvedores entrando no time
→ **README.md** → **01-INICIO-RAPIDO.md** → **02-ARQUITETURA.md** → **CHECKLIST_DESENVOLVIMENTO.md**

---

## 🎯 Convenções de Nomenclatura

### Documentos Numerados
- **00-INDEX.md** - Índice (este arquivo)
- **01-XX.md** - Guias de início/setup
- **02-09.md** - Documentação técnica
- **10-89.md** - Reservado para futuras expansões
- **90-99.md** - Documentos auxiliares (status, logs, changelog)

### Documentos Não Numerados
- Ficam na raiz do projeto
- São essenciais e frequentemente acessados
- Exemplo: README.md, CHECKLIST_DESENVOLVIMENTO.md

---

## ✏️ Como Atualizar a Documentação

### Adicionar Novo Documento

1. Crie o arquivo em `docs/` com número sequencial apropriado
2. Adicione entrada neste INDEX.md
3. Atualize README.md se for documento importante
4. Adicione links cruzados em documentos relacionados

### Atualizar Documento Existente

1. Edite o arquivo
2. Atualize data de "Última atualização" no final
3. Se mudança significativa, atualize também o INDEX.md

### Mover/Renomear Documento

1. Mova/renomeie o arquivo
2. Atualize todos os links em outros documentos
3. Atualize INDEX.md com novo caminho
4. Considere criar redirect ou nota de deprecação

---

## 🔄 Documentos Relacionados

- **README.md** (raiz) - Ponto de entrada principal
- **CHECKLIST_DESENVOLVIMENTO.md** (raiz) - Tarefas e progresso
- **database/SETUP_POSTGRESQL.md** - Setup manual PostgreSQL
- **src/**/README.md** - Documentação específica de cada camada

---

## 📝 Status da Documentação

| Documento | Status | Última Atualização |
|-----------|--------|--------------------|
| README.md | ✅ Completo | 2025-10-05 |
| 00-INDEX.md | ✅ Atualizado | 2025-10-05 |
| 01-INICIO-RAPIDO.md | ✅ Completo | 2025-01-04 |
| 02-ARQUITETURA.md | ✅ Completo | 2025-01-05 |
| 03-DOCKER.md | ✅ Completo | 2025-01-05 |
| 04-STRIPE.md | ✅ Completo | 2025-01-04 |
| 07-AUTENTICACAO-HIBRIDA.md | ✅ Completo | 2025-10-05 |
| 08-MIGRATIONS-BOAS-PRATICAS.md | ✅ Completo | 2025-10-05 |
| 09-CHECKLIST-DESENVOLVIMENTO.md | ✅ Completo | 2025-10-05 |
| 10-COMO-APLICAR-MIGRATION.md | ✅ Completo | 2025-10-05 |
| 11-CREDENCIAIS.md | 🔒 Privado | 2025-10-05 |
| 12-DEPLOY-SERVIDOR.md | ✅ Completo | 2025-10-05 |
| 13-HYBRID-AUTH-STATUS.md | ✅ Completo | 2025-10-05 |
| 14-IMPLEMENTACAO-COMPLETA.md | ✅ Completo | 2025-10-05 |
| 16-PROGRESSO.md | ✅ Completo | 2025-10-05 |
| 17-ARQUITETURA-HIBRIDA.md | 🔴 CRÍTICO | 2025-10-05 |
| 18-MUDANCA-ARQUITETURAL-RESUMO.md | 🚨 OBRIGATÓRIO | 2025-10-05 |
| 99-STATUS.md | ✅ Completo | 2025-01-04 |

---

**Documentação reorganizada em:** 2025-10-05  
**Arquivos movidos da raiz para docs/:** 8 arquivos  
**Próxima revisão:** Após aplicação de migration 002
