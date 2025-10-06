# 📁 Estrutura de Documentação do Projeto

> **Reorganização completa em 2025-10-05**

---

## 🎯 Objetivo da Reorganização

Antes tínhamos **8 arquivos `.md` espalhados na raiz** do projeto, dificultando navegação e manutenção. Agora **toda documentação está centralizada em `docs/`** com numeração sequencial lógica.

---

## 📂 Estrutura Atual

```
gestao-mensalidades-api/
│
├── 📄 README.md                          ← ÚNICO .md na raiz (entrada principal)
├── 📄 .env.example                       ← Template de variáveis
├── 📄 docker-compose.yml
├── 📄 Dockerfile
├── 📄 build.gradle
│
├── 📁 docs/                              ← TODA documentação aqui
│   ├── 00-INDEX.md                       ← Índice de navegação
│   │
│   ├── 🚀 INÍCIO E SETUP
│   ├── 01-INICIO-RAPIDO.md              ← Guia de setup inicial
│   ├── 02-ARQUITETURA.md                ← Clean Architecture
│   ├── 03-DOCKER.md                     ← Configuração Docker
│   │
│   ├── 💳 INTEGRAÇÕES
│   ├── 04-STRIPE.md                     ← Stripe webhooks e CLI
│   ├── 05-PLANOS.md                     ← Gerenciamento de planos
│   │
│   ├── 🔐 AUTENTICAÇÃO
│   ├── 06-ARQUITETURA-FIREBASE.md       ← Referência Firebase
│   ├── 07-AUTENTICACAO-HIBRIDA.md       ← JWT + Firebase Auth
│   │
│   ├── 🗄️ DATABASE
│   ├── 08-MIGRATIONS-BOAS-PRATICAS.md   ← Filosofia migrations
│   │
│   ├── 📋 DESENVOLVIMENTO
│   ├── 09-CHECKLIST-DESENVOLVIMENTO.md  ← 14 fases do projeto
│   ├── 10-COMO-APLICAR-MIGRATION.md     ← Guia visual DBeaver
│   ├── 11-CREDENCIAIS.md                ← Senhas/chaves (não commitado)
│   │
│   ├── 🚀 DEPLOY
│   ├── 12-DEPLOY-SERVIDOR.md            ← Guia completo deploy
│   │
│   ├── 📊 STATUS E PROGRESSO
│   ├── 13-HYBRID-AUTH-STATUS.md         ← Status auth híbrida
│   ├── 14-IMPLEMENTACAO-COMPLETA.md     ← Resumo implementação
│   ├── 15-PLANO-MIGRACAO.md             ← Referência Firebase
│   ├── 16-PROGRESSO.md                  ← Tracking progresso
│   │
│   └── 99-STATUS.md                     ← Status rápido projeto
│
├── 📁 scripts/
│   ├── 📄 README.md                      ← Documentação dos scripts
│   ├── 📜 *.ps1                          ← Scripts Windows
│   └── 📜 *.sh                           ← Scripts Linux
│
├── 📁 database/
│   ├── 📄 init.sql
│   ├── 📄 SETUP_POSTGRESQL.md
│   └── 📁 migrations/
│       └── 002_add_firebase_auth_support.sql
│
└── 📁 src/
    └── main/java/...
```

---

## 🔢 Sistema de Numeração

### Intervalo 00-09: Setup e Arquitetura
- **00-INDEX.md** - Índice mestre
- **01-INICIO-RAPIDO.md** - Setup inicial
- **02-ARQUITETURA.md** - Clean Architecture
- **03-DOCKER.md** - Infraestrutura

### Intervalo 04-05: Integrações Externas
- **04-STRIPE.md** - Pagamentos
- **05-PLANOS.md** - Gerenciamento

### Intervalo 06-08: Autenticação e Database
- **06-ARQUITETURA-FIREBASE.md** - Referência
- **07-AUTENTICACAO-HIBRIDA.md** - JWT + Firebase
- **08-MIGRATIONS-BOAS-PRATICAS.md** - Database

### Intervalo 09-12: Desenvolvimento e Deploy
- **09-CHECKLIST-DESENVOLVIMENTO.md** - Tarefas
- **10-COMO-APLICAR-MIGRATION.md** - Guias práticos
- **11-CREDENCIAIS.md** - Configuração (não commitado)
- **12-DEPLOY-SERVIDOR.md** - Produção

### Intervalo 13-16: Status e Progresso
- **13-HYBRID-AUTH-STATUS.md** - Status específico
- **14-IMPLEMENTACAO-COMPLETA.md** - Resumo geral
- **15-PLANO-MIGRACAO.md** - Planejamento (referência)
- **16-PROGRESSO.md** - Tracking

### Intervalo 99: Auxiliares
- **99-STATUS.md** - Status rápido

---

## 🎯 Benefícios da Reorganização

### ✅ Para Desenvolvedores

1. **Navegação Clara**
   - Ordem lógica de leitura (01 → 02 → 03...)
   - Agrupamento por categoria
   - Fácil encontrar documentos

2. **Raiz Limpa**
   - Apenas `README.md` na raiz
   - Foco no essencial
   - Menos confusão visual

3. **Manutenção Fácil**
   - Documentos relacionados próximos
   - Numeração permite inserções futuras
   - Índice centralizado (00-INDEX.md)

### ✅ Para o Projeto

1. **Profissionalismo**
   - Estrutura padrão da indústria
   - Fácil onboarding de novos devs
   - Documentação escalável

2. **Versionamento**
   - Git rastreia movimentações
   - Histórico preservado
   - Links atualizados automaticamente

3. **Consistência**
   - Padrão único de nomenclatura
   - Convenção clara para novos docs
   - Fácil manutenção do índice

---

## 📖 Como Usar a Documentação

### Para Novo Desenvolvedor

```
1. README.md (raiz)                    ← Visão geral
2. docs/00-INDEX.md                    ← Navegação completa
3. docs/01-INICIO-RAPIDO.md           ← Setup ambiente
4. docs/02-ARQUITETURA.md             ← Entender estrutura
5. docs/09-CHECKLIST-DESENVOLVIMENTO.md ← Ver tarefas
```

### Para Desenvolvimento Ativo

```
1. docs/09-CHECKLIST-DESENVOLVIMENTO.md ← O que fazer
2. docs/99-STATUS.md                    ← Onde estamos
3. docs/[específico].md                 ← Consulta pontual
```

### Para Deploy

```
1. docs/12-DEPLOY-SERVIDOR.md          ← Guia completo
2. docs/11-CREDENCIAIS.md              ← Valores produção
3. scripts/README.md                    ← Scripts disponíveis
```

---

## 🔄 Migração Realizada

### Arquivos Movidos (8 arquivos)

| Antes (raiz) | Depois (docs/) | Motivo |
|--------------|----------------|--------|
| `CHECKLIST_DESENVOLVIMENTO.md` | `09-CHECKLIST-DESENVOLVIMENTO.md` | Desenvolvimento |
| `COMO_APLICAR_MIGRATION.md` | `10-COMO-APLICAR-MIGRATION.md` | Guia prático |
| `CREDENCIAIS.md` | `11-CREDENCIAIS.md` | Configuração |
| `DEPLOY_SERVIDOR.md` | `12-DEPLOY-SERVIDOR.md` | Deploy |
| `HYBRID_AUTH_STATUS.md` | `13-HYBRID-AUTH-STATUS.md` | Status |
| `IMPLEMENTACAO_COMPLETA.md` | `14-IMPLEMENTACAO-COMPLETA.md` | Resumo |
| `PLANO_MIGRACAO.md` | `15-PLANO-MIGRACAO.md` | Planejamento |
| `PROGRESSO.md` | `16-PROGRESSO.md` | Tracking |

### Arquivos Deletados

| Arquivo | Motivo |
|---------|--------|
| `DECISAO_FIREBASE.md` | Conteúdo redundante/desatualizado |

### Arquivos Atualizados

| Arquivo | Mudança |
|---------|---------|
| `docs/00-INDEX.md` | Adicionados 8 novos documentos |
| `.gitignore` | Atualizado path de CREDENCIAIS.md |

---

## 📝 Convenções para Novos Documentos

### Adicionar Novo Documento

1. **Escolha número apropriado:**
   - 01-09: Setup/Arquitetura
   - 10-19: Desenvolvimento
   - 20-29: Features (reservado)
   - 90-99: Auxiliares

2. **Nomeie com padrão:**
   ```
   NN-NOME-DESCRITIVO.md
   ```

3. **Adicione ao INDEX.md:**
   ```markdown
   #### 📄 [NN-NOME-DESCRITIVO.md](./NN-NOME-DESCRITIVO.md)
   **Descrição:** ...
   **Quando usar:** ...
   ```

4. **Commit com mensagem clara:**
   ```bash
   git add docs/NN-NOME-DESCRITIVO.md docs/00-INDEX.md
   git commit -m "docs: add NN-NOME-DESCRITIVO guide"
   ```

### Atualizar Documento Existente

1. Edite o arquivo
2. Atualize "Última Atualização" no INDEX.md
3. Commit descritivo

### Não Fazer

❌ Criar `.md` na raiz (apenas README.md)  
❌ Duplicar numeração  
❌ Esquecer de atualizar INDEX.md  
❌ Usar nomes genéricos (doc.md, info.md)

---

## 🎓 Melhores Práticas

### ✅ DO (Faça)

- Mantenha README.md como único `.md` na raiz
- Use numeração sequencial em docs/
- Atualize INDEX.md quando adicionar/mover docs
- Agrupe documentos relacionados por intervalo
- Use nomes descritivos e claros
- Adicione emoji para identificação visual (opcional)

### ❌ DON'T (Não Faça)

- Não crie `.md` soltos na raiz
- Não use numeração aleatória
- Não esqueça de atualizar INDEX.md
- Não use nomes genéricos (doc1.md, teste.md)
- Não commite CREDENCIAIS.md

---

## 📊 Estatísticas

**Antes da Reorganização:**
- ✅ 1 `.md` na raiz (ideal)
- ❌ 8 `.md` na raiz (bagunça)
- ✅ 8 `.md` em docs/

**Depois da Reorganização:**
- ✅ 1 `.md` na raiz (README.md)
- ✅ 18 `.md` em docs/ (todos organizados)
- ✅ 100% documentação centralizada

**Melhoria:**
- 📉 -89% arquivos na raiz
- 📈 +125% docs em docs/
- 🎯 100% organização

---

## 🔗 Links Úteis

- **Índice Completo:** [docs/00-INDEX.md](docs/00-INDEX.md)
- **Setup Inicial:** [docs/01-INICIO-RAPIDO.md](docs/01-INICIO-RAPIDO.md)
- **Checklist Completo:** [docs/09-CHECKLIST-DESENVOLVIMENTO.md](docs/09-CHECKLIST-DESENVOLVIMENTO.md)
- **Scripts:** [scripts/README.md](scripts/README.md)

---

**Reorganização realizada em:** 2025-10-05  
**Commit:** `refactor(docs): reorganize documentation files into docs/ folder`  
**Arquivos movidos:** 8  
**Arquivos deletados:** 1  
**Benefício:** Estrutura profissional e escalável 🚀
