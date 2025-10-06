# 📚 Índice da Documentação

> **Última atualização:** 2025-10-06 16:45  
> **Total de arquivos:** 13 (organização alfabética, sem numeração)

Este diretório contém toda a documentação técnica do projeto **Gestão de Mensalidades API**.

---

## 🚀 Para Começar (Quick Start)

1. **[INICIO-RAPIDO.md](./INICIO-RAPIDO.md)** - Setup inicial e primeiros passos
2. **[DOCKER.md](./DOCKER.md)** - Ambiente de desenvolvimento com containers
3. **[ARQUITETURA.md](./ARQUITETURA.md)** - Visão geral da arquitetura

---

## 📖 Documentação por Categoria

### Arquitetura & Design
- **[ARQUITETURA.md](./ARQUITETURA.md)** - Clean Architecture (domain/application/infrastructure/presentation)
- **[ARQUITETURA-HIBRIDA.md](./ARQUITETURA-HIBRIDA.md)** - Modelo híbrido Stripe + Compras diretas

### Autenticação & Segurança
- **[AUTENTICACAO-HIBRIDA.md](./AUTENTICACAO-HIBRIDA.md)** - Firebase + JWT (custom claims)

### Integrações
- **[STRIPE.md](./STRIPE.md)** - Integração completa Stripe (Subscriptions + Payment mode)

### Database & Migrations
- **[MIGRATIONS-BOAS-PRATICAS.md](./MIGRATIONS-BOAS-PRATICAS.md)** - Boas práticas para criar migrations
- **[COMO-APLICAR-MIGRATION.md](./COMO-APLICAR-MIGRATION.md)** - Passo a passo aplicação manual

### Validações
- **[JAKARTA-VALIDATIONS.md](./JAKARTA-VALIDATIONS.md)** - Guia completo Jakarta Bean Validation

### Deploy & Infraestrutura
- **[DOCKER.md](./DOCKER.md)** - Docker compose, containers, pgAdmin
- **[DEPLOY-SERVIDOR.md](./DEPLOY-SERVIDOR.md)** - Deploy em servidor VPS

### Desenvolvimento
- **[CHECKLIST-DESENVOLVIMENTO.md](./CHECKLIST-DESENVOLVIMENTO.md)** - Checklist ao criar features
- **[CREDENCIAIS.md](./CREDENCIAIS.md)** - Credenciais locais (**não commitado no Git**)

### Status do Projeto
- **[PROJECT-STATUS.md](./PROJECT-STATUS.md)** - Status detalhado, progresso 80% backend

---

## 📂 Estrutura de Arquivos (Alfabética)

```
docs/
├── INDEX.md                         ← Você está aqui
├── ARQUITETURA-HIBRIDA.md
├── ARQUITETURA.md
├── AUTENTICACAO-HIBRIDA.md
├── CHECKLIST-DESENVOLVIMENTO.md
├── COMO-APLICAR-MIGRATION.md
├── CREDENCIAIS.md                   (local only, .gitignore)
├── DEPLOY-SERVIDOR.md
├── DOCKER.md
├── INICIO-RAPIDO.md
├── JAKARTA-VALIDATIONS.md
├── MIGRATIONS-BOAS-PRATICAS.md
├── PROJECT-STATUS.md
└── STRIPE.md
```

**Total:** 14 arquivos (incluindo este INDEX.md)

---

## 🔍 Referência Rápida (Quick Access)

| Cenário | Documento |
|---------|-----------|
| Primeiro dia no projeto | `INICIO-RAPIDO.md` |
| Levantar ambiente local | `DOCKER.md` |
| Criar nova feature | `CHECKLIST-DESENVOLVIMENTO.md` |
| Integrar Stripe | `STRIPE.md` |
| Criar migration | `MIGRATIONS-BOAS-PRATICAS.md` |
| Aplicar migration | `COMO-APLICAR-MIGRATION.md` |
| Validar DTOs | `JAKARTA-VALIDATIONS.md` |
| Ver progresso do projeto | `PROJECT-STATUS.md` |

---

## 📝 Histórico de Mudanças (2025-10-06)

### Reorganização da Documentação

**Antes:**
- 25+ arquivos com numeração inconsistente (01-04, skip, 07-12, skip, 17)
- Arquivos temporários de implementação (reviews, análises)
- `STATUS-GERAL.md` redundante com `PROJECT-STATUS.md`

**Depois (Atual):**
- ✅ 14 arquivos organizados alfabeticamente (incluindo INDEX.md)
- ✅ Sem numeração (nomes autoexplicativos)
- ✅ Apenas documentação permanente
- ✅ Um único arquivo de status (`PROJECT-STATUS.md`)

**Benefícios:**
- 🎯 Fácil encontrar documentos pelo nome
- 🎯 Não precisa renumerar ao adicionar novos
- 🎯 Menos confusão com gaps de numeração
- 🎯 Estrutura mais limpa e mantível

---

**Mantido por:** GitHub Copilot AI  
**Contato:** Ver `PROJECT-STATUS.md` para atualizações
