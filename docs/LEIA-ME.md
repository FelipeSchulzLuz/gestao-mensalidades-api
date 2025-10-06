# 📖 Documentação do Projeto - Índice e Guia de Navegação

> **🎯 Porta de entrada única para toda a documentação**  
> **📚 Índice completo do projeto**  
> Escolha seu perfil abaixo e siga o fluxo recomendado

---

## 🎯 Escolha Seu ### 🏭️ Arquitetura (Referência constante)

| Documento | Foco | Status |
|-----------|------|--------|
| [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md) | Products + Prices | ✅ ATUAL |
| [02-ARQUITETURA.md](./02-ARQUITETURA.md) | Clean Architecture | ✅ BASE |
<details open>
<summary><strong>👶 Sou Novo no Projeto</strong></summary>

### Seu fluxo: Entender → Configurar → Explorar

1. **📄 [README.md](../README.md)** (5 min)
   - Visão geral: O que é o projeto?
   - Stack tecnológica
   - Status atual

2. **🚨 [18-MUDANCA-ARQUITETURAL-RESUMO.md](./18-MUDANCA-ARQUITETURAL-RESUMO.md)** (10 min)
   - ⚠️ **LEIA ANTES DE CODIFICAR!**
   - Entenda a mudança Plan → Product + Price
   - Checklist rápido
   - Armadilhas comuns

3. **🚀 [01-INICIO-RAPIDO.md](./01-INICIO-RAPIDO.md)** (15 min)
   - Instale dependências
   - Rode o projeto localmente
   - Primeiro "Hello World"

4. **🏗️ [02-ARQUITETURA.md](./02-ARQUITETURA.md)** (20 min)
   - Clean Architecture
   - Estrutura de pastas
   - Padrões de código

5. **💡 [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md)** (30 min)
   - Arquitetura completa Products + Prices
   - Schemas SQL
   - Fluxos de checkout

**Tempo total:** ~1h30  
**Próximo passo:** Ver [09-CHECKLIST-DESENVOLVIMENTO.md](./09-CHECKLIST-DESENVOLVIMENTO.md)

</details>

<details>
<summary><strong>💻 Vou Desenvolver Agora</strong></summary>

### Seu fluxo: Verificar → Implementar → Testar

#### Antes de Codificar (OBRIGATÓRIO)

1. **🚨 [18-MUDANCA-ARQUITETURAL-RESUMO.md](./18-MUDANCA-ARQUITETURAL-RESUMO.md)** (10 min)
   - Confirme que está usando terminologia correta
   - Product/Price, NÃO Plan
   - Checklist de 5 perguntas

2. **📋 [09-CHECKLIST-DESENVOLVIMENTO.md](./09-CHECKLIST-DESENVOLVIMENTO.md)** (5 min)
   - Veja quais fases estão completas
   - Identifique sua tarefa atual

#### Durante o Desenvolvimento

3. **🎯 [GUIA-RAPIDO.md](./GUIA-RAPIDO.md)** (referência)
   - Comandos mais usados
   - Decisões arquiteturais rápidas
   - Links para docs completos

4. **💳 [04-STRIPE.md](./04-STRIPE.md)** (consulta)
   - Webhooks do Stripe
   - Fluxos de pagamento
   - Troubleshooting

5. **💡 [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md)** (referência)
   - Schemas detalhados
   - Exemplos de código
   - Lógica de webhooks

#### Infraestrutura

- **🐳 [03-DOCKER.md](./03-DOCKER.md)** - Problemas com containers
- **🗄️ [10-COMO-APLICAR-MIGRATION.md](./10-COMO-APLICAR-MIGRATION.md)** - Rodar migrations

**Próximo passo:** Commit e push (ver [12-DEPLOY-SERVIDOR.md](./12-DEPLOY-SERVIDOR.md) para deploy)

</details>

<details>
<summary><strong>👀 Vou Revisar Código (Code Review)</strong></summary>

### Seu fluxo: Verificar Conformidade → Arquitetura → Qualidade

#### Checklist de Code Review

1. **🚨 Terminologia Correta?**
   - ❌ Código usa "Plan"?
   - ✅ Código usa "Product" e "Price"?
   - Ver: [18-MUDANCA-ARQUITETURAL-RESUMO.md](./18-MUDANCA-ARQUITETURAL-RESUMO.md)

2. **🏗️ Segue Clean Architecture?**
   - Camadas respeitadas (presentation → domain → data)?
   - DTOs no lugar certo?
   - Ver: [02-ARQUITETURA.md](./02-ARQUITETURA.md)

3. **💳 Integração Stripe Correta?**
   - Checkout mode correto (subscription vs payment)?
   - Webhooks distinguem fluxos?
   - Ver: [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md) seção "Webhooks"

4. **🗄️ Migrations Bem Escritas?**
   - Idempotente?
   - Rollback possível?
   - Ver: [08-MIGRATIONS-BOAS-PRATICAS.md](./08-MIGRATIONS-BOAS-PRATICAS.md)

5. **✅ Fase Completa?**
   - Atualizar: [09-CHECKLIST-DESENVOLVIMENTO.md](./09-CHECKLIST-DESENVOLVIMENTO.md)

</details>

<details>
<summary><strong>🚀 Vou Fazer Deploy</strong></summary>

### Seu fluxo: Preparar → Testar → Deploy → Monitorar

1. **📋 Pré-Deploy**
   - [ ] Todas migrations aplicadas?
   - [ ] Variáveis de ambiente configuradas?
   - [ ] Stripe webhooks configurados?
   - Ver: [11-CREDENCIAIS.md](./11-CREDENCIAIS.md)

2. **🚀 [12-DEPLOY-SERVIDOR.md](./12-DEPLOY-SERVIDOR.md)** (30 min)
   - Passo a passo completo
   - Configuração servidor
   - NGINX, SSL, etc.

3. **🔐 Configuração Stripe Produção**
   - Ver: [04-STRIPE.md](./04-STRIPE.md) seção "Produção"

4. **📊 Pós-Deploy**
   - [ ] Testar checkout subscription
   - [ ] Testar checkout payment
   - [ ] Verificar webhooks funcionando

</details>

<details>
<summary><strong>🐛 Estou com Problemas</strong></summary>

### Troubleshooting por Área

#### 🐳 Docker não sobe
→ [03-DOCKER.md](./03-DOCKER.md) - Seção "Troubleshooting"

#### 🗄️ Erro de database/migrations
→ [10-COMO-APLICAR-MIGRATION.md](./10-COMO-APLICAR-MIGRATION.md)  
→ [08-MIGRATIONS-BOAS-PRATICAS.md](./08-MIGRATIONS-BOAS-PRATICAS.md)

#### 💳 Stripe não funciona
→ [04-STRIPE.md](./04-STRIPE.md) - Seção "Troubleshooting Stripe"

#### 🔐 Problemas de autenticação
→ [07-AUTENTICACAO-HIBRIDA.md](./07-AUTENTICACAO-HIBRIDA.md)

#### 🏗️ Arquitetura/Design
→ [02-ARQUITETURA.md](./02-ARQUITETURA.md)  
→ [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md)

#### ❓ Não sei por onde começar
→ Você está no lugar certo! Escolha um perfil acima ↑

</details>

---

## 📂 Mapa Completo da Documentação

### 🎯 Essenciais (Leia primeiro)

| Documento | Tempo | Quando Ler |
|-----------|-------|------------|
| [README.md](../README.md) | 5 min | Primeiro contato |
| [18-MUDANCA-ARQUITETURAL-RESUMO.md](./18-MUDANCA-ARQUITETURAL-RESUMO.md) | 10 min | **ANTES DE CODIFICAR** |
| [01-INICIO-RAPIDO.md](./01-INICIO-RAPIDO.md) | 15 min | Configurar ambiente |
| [02-ARQUITETURA.md](./02-ARQUITETURA.md) | 20 min | Entender estrutura |

### 🏗️ Arquitetura (Referência constante)

| Documento | Foco | Status |
|-----------|------|--------|
| [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md) | Products + Prices | ✅ ATUAL |
| [02-ARQUITETURA.md](./02-ARQUITETURA.md) | Clean Architecture | ✅ BASE |

### 🛠️ Ferramentas & Integração

| Documento | Conteúdo |
|-----------|----------|
| [03-DOCKER.md](./03-DOCKER.md) | Docker Compose, troubleshooting |
| [04-STRIPE.md](./04-STRIPE.md) | Webhooks, CLI, checkout |
| [07-AUTENTICACAO-HIBRIDA.md](./07-AUTENTICACAO-HIBRIDA.md) | JWT + Firebase Auth |

### 🗄️ Database

| Documento | Conteúdo |
|-----------|----------|
| [08-MIGRATIONS-BOAS-PRATICAS.md](./08-MIGRATIONS-BOAS-PRATICAS.md) | Filosofia e padrões |
| [10-COMO-APLICAR-MIGRATION.md](./10-COMO-APLICAR-MIGRATION.md) | Guia visual DBeaver |

### 📋 Desenvolvimento

| Documento | Conteúdo |
|-----------|----------|
| [09-CHECKLIST-DESENVOLVIMENTO.md](./09-CHECKLIST-DESENVOLVIMENTO.md) | 14 fases do projeto |
| [GUIA-RAPIDO.md](./GUIA-RAPIDO.md) | Comandos e decisões rápidas |
| [STATUS-GERAL.md](./STATUS-GERAL.md) | Status consolidado |

### 🚀 Deploy & Configuração

| Documento | Conteúdo |
|-----------|----------|
| [11-CREDENCIAIS.md](./11-CREDENCIAIS.md) | Senhas/chaves (privado) |
| [12-DEPLOY-SERVIDOR.md](./12-DEPLOY-SERVIDOR.md) | Guia completo deploy |

### 📚 Referência & Contexto

| Documento | Status |
|-----------|--------|
| [STATUS-GERAL.md](./STATUS-GERAL.md) | Status consolidado do projeto |
| [09-CHECKLIST-DESENVOLVIMENTO.md](./09-CHECKLIST-DESENVOLVIMENTO.md) | Checklist completo de todas as fases |

---

## 🎓 Convenções Importantes

### ✅ USE (Nova Arquitetura)
```java
Product product = new Product();
Price price = new Price();
ProductType.SUBSCRIPTION ou ProductType.ONE_TIME
```

### ❌ NÃO USE (Arquitetura Antiga)
```java
Plan plan = new Plan();  // ❌ OBSOLETO!
```

### 📝 Commits
```bash
git commit -m "feat: add product creation endpoint"
git commit -m "fix: correct subscription webhook handler"
git commit -m "docs: update architecture diagram"
```

---

## 🆘 Precisa de Ajuda?

1. **Não encontrei o que procuro**
   - Ver [STATUS-GERAL.md](./STATUS-GERAL.md) (status e documentação consolidada)
   - Ver [09-CHECKLIST-DESENVOLVIMENTO.md](./09-CHECKLIST-DESENVOLVIMENTO.md) (checklist completo)

2. **Documento está desatualizado**
   - Abra uma issue ou atualize e faça PR

3. **Dúvida sobre arquitetura**
   - Primeiro: [18-MUDANCA-ARQUITETURAL-RESUMO.md](./18-MUDANCA-ARQUITETURAL-RESUMO.md)
   - Depois: [17-ARQUITETURA-HIBRIDA.md](./17-ARQUITETURA-HIBRIDA.md)

4. **Erro no código**
   - Ver seção "Estou com Problemas" acima ↑

---

## 🔄 Última Atualização

**Data:** 2025-10-06  
**Mudanças recentes:**
- ✅ Removido 00-INDEX.md duplicado (consolidado neste arquivo)
- ✅ Migration 003 aplicada com sucesso
- ✅ Entidades e Repositories implementados (Organization, Product, Price, Order)
- ✅ Progresso atualizado para 50%
- ✅ Documentação limpa e consistente

**Próxima revisão:** Após implementação dos Services

---

**💡 Dica:** Marque esta página para acesso rápido!
