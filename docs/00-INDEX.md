# 📚 Índice da Documentação

> **Guia completo de navegação pela documentação do projeto**

Este documento serve como índice centralizado de toda a documentação do projeto. Leia os documentos na ordem sugerida para melhor compreensão.

---

## 📖 Ordem de Leitura Recomendada

### Para Novos Desenvolvedores

1. **README.md** (raiz) - Visão geral do projeto
2. **01-INICIO-RAPIDO.md** - Configure e rode o projeto
3. **02-ARQUITETURA.md** - Entenda a arquitetura
4. **CHECKLIST_DESENVOLVIMENTO.md** (raiz) - Veja as tarefas

### Para Desenvolvimento Ativo

1. **CHECKLIST_DESENVOLVIMENTO.md** - Tarefas pendentes
2. **99-STATUS.md** - Progresso atual
3. **05-PLANOS.md** - Arquitetura de planos (próxima implementação)
4. **04-STRIPE.md** - Referência de integração Stripe

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

#### ✅ [CHECKLIST_DESENVOLVIMENTO.md](../CHECKLIST_DESENVOLVIMENTO.md)
**Descrição:** Checklist completo das 14 fases de desenvolvimento.  
**Quando usar:** Planejar sprints, verificar progresso, identificar gaps.  
**Conteúdo:** 14 fases detalhadas, estimativas, dependências, status visual.

#### 🔐 CREDENCIAIS.md *(não commitado)*
**Descrição:** Credenciais sensíveis (senhas, chaves, tokens).  
**Quando usar:** Configuração local, onboarding de desenvolvedores.  
**Conteúdo:** Senhas PostgreSQL, chaves Stripe, JWT secrets.  
**⚠️ NUNCA commite este arquivo!**

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

#### 📦 [05-PLANOS.md](./05-PLANOS.md)
**Descrição:** Arquitetura de gerenciamento de planos (admin panel + sincronização Stripe).  
**Quando usar:** Implementar Fase 7 (Módulo de Planos), entender estratégia de sincronização.  
**Conteúdo:**
- Decisão arquitetural (Database + Admin vs .env)
- Diagrama de arquitetura de planos
- 17 endpoints (11 admin + 2 públicos + 4 sincronização)
- 4 DTOs (PlanResponseDTO, CreatePlanRequestDTO, UpdatePlanRequestDTO, PlanMetricsDTO)
- 3 Services (PlanService, PlanAdminService, StripePlanSyncService)
- Estratégia de sincronização bidirecional
- Webhooks de sincronização (5 eventos)
- Casos de uso (Black Friday, A/B testing, métricas MRR/ARR)
- Mockups de admin panel

**Tempo de leitura:** 25 minutos  
**Pré-requisitos:** 02-ARQUITETURA.md, 04-STRIPE.md

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

### start-dev.ps1
**Descrição:** Inicia ambiente Docker (PostgreSQL, pgAdmin, Redis).  
**Quando usar:** Todo dia, antes de começar desenvolvimento.  
**Funcionalidades:**
- Verifica Docker instalado e rodando
- Inicia containers PostgreSQL, pgAdmin, Redis
- Health checks automáticos
- Exibe informações de acesso
- Mostra instruções para Stripe CLI

**Uso:**
```powershell
.\scripts\start-dev.ps1
```

### start-stripe.ps1
**Descrição:** Inicia Stripe CLI para webhooks locais.  
**Quando usar:** Desenvolver/testar features de pagamento e webhooks.  
**Funcionalidades:**
- Verifica Stripe CLI instalado
- Verifica autenticação Stripe
- Inicia listener de webhooks
- Forward para `localhost:8800/api/webhooks/stripe`
- Exibe webhook secret

**Uso:**
```powershell
.\scripts\start-stripe.ps1

# Ou custom endpoint:
.\scripts\start-stripe.ps1 -ForwardTo "localhost:8800/api/webhooks/custom"
```

---

## 📍 Mapa de Navegação Rápida

### Preciso configurar o ambiente pela primeira vez
→ **01-INICIO-RAPIDO.md**

### Preciso entender a arquitetura do código
→ **02-ARQUITETURA.md**

### Tenho problemas com Docker
→ **03-DOCKER.md** (seção Troubleshooting)

### Preciso implementar integração com Stripe
→ **04-STRIPE.md** + **05-PLANOS.md**

### Quero saber o que fazer agora
→ **CHECKLIST_DESENVOLVIMENTO.md** + **99-STATUS.md**

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
| README.md | ✅ Completo | 2025-01-05 |
| CHECKLIST_DESENVOLVIMENTO.md | ✅ Completo | 2025-01-05 |
| 00-INDEX.md | ✅ Completo | 2025-01-05 |
| 01-INICIO-RAPIDO.md | ✅ Completo | 2025-01-04 |
| 02-ARQUITETURA.md | ✅ Completo | 2025-01-05 |
| 03-DOCKER.md | ✅ Completo | 2025-01-05 |
| 04-STRIPE.md | ✅ Completo | 2025-01-04 |
| 05-PLANOS.md | ✅ Completo | 2025-01-04 |
| 99-STATUS.md | ✅ Completo | 2025-01-04 |

---

**Documentação organizada em:** 2025-01-05  
**Próxima revisão:** Após conclusão da Fase 7 (Planos)
