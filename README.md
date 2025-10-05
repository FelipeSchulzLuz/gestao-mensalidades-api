# 🎯 Sistema de Gestão de Mensalidades - API

API REST robusta para gestão de mensalidades e assinaturas desenvolvida com Spring Boot e integração com Stripe.

## 📋 Sobre o Projeto

Sistema genérico para gerenciamento de assinaturas recorrentes, desenvolvido para atender organizações como ONGs e pequenos negócios (estúdios de Pilates, academias, escolas, etc). Substitui processos manuais por uma plataforma automatizada, segura e eficiente.

### ✨ Funcionalidades Principais

- ✅ Autenticação JWT segura
- ✅ Gerenciamento de usuários e perfis
- ✅ Catálogo de planos de assinatura
- ✅ Checkout integrado com Stripe
- ✅ Portal do cliente para autogerenciamento
- ✅ Webhooks para sincronização automática
- ✅ Suporte a múltiplos métodos de pagamento (Cartão, Pix)
- ✅ Notificações automáticas de pagamento

## 🛠️ Stack Tecnológica

### Backend
- **Java 21** (LTS)
- **Spring Boot 3.5.6**
  - Spring Web (REST API)
  - Spring Data JPA (Persistência)
  - Spring Security (Autenticação/Autorização)
  - Spring Validation (Validações)
- **PostgreSQL** (Banco de dados)
- **Lombok** (Redução de boilerplate)

### Integrações
- **Stripe Java SDK 30.0.0** (Pagamentos e assinaturas)
- **JWT (jjwt 0.12.5)** (Autenticação stateless)

### Build Tool
- **Gradle** com Groovy DSL

## 🏗️ Arquitetura

O projeto segue os princípios da **Clean Architecture**, garantindo:
- ✅ Separação de responsabilidades
- ✅ Baixo acoplamento
- ✅ Alta testabilidade
- ✅ Facilidade de manutenção

```
src/main/java/br/com/gestaomensalidades/gestao_mensalidades_api/
├── domain/              # Entidades e regras de domínio
│   ├── entities/        # User, Plan, Subscription
│   └── enums/           # BillingInterval, SubscriptionStatus
├── application/         # Casos de uso e lógica de negócio
│   └── services/        # AuthService, SubscriptionService, etc
├── infrastructure/      # Implementações técnicas
│   ├── persistence/     # Repositórios JPA
│   ├── security/        # JWT, Spring Security
│   └── stripe/          # Integrações Stripe
└── presentation/        # API REST
    ├── controllers/     # Endpoints REST
    ├── dto/             # Request/Response DTOs
    └── mappers/         # Conversores Entity ↔ DTO
```

## 🚀 Como Executar

### Pré-requisitos

- Java 21+ instalado
- PostgreSQL 15+ instalado e rodando
- Conta no Stripe (modo test)
- Stripe CLI (para testar webhooks localmente)

### 1. Configurar Banco de Dados

```bash
# Criar banco de dados PostgreSQL
psql -U postgres
CREATE DATABASE gestao_mensalidades_dev;
\q
```

Ou usar Docker:
```bash
docker run --name postgres-dev \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=gestao_mensalidades_dev \
  -p 5432:5432 -d postgres:15
```

### 2. Configurar Variáveis de Ambiente

```bash
# Copiar template de variáveis
cp .env.example .env

# Editar .env com suas credenciais
# - Configurar credenciais do PostgreSQL
# - Adicionar chaves do Stripe (https://dashboard.stripe.com/test/apikeys)
# - Gerar JWT_SECRET forte (openssl rand -base64 64)
```

### 3. Executar a Aplicação

```bash
# Linux/Mac
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

A API estará disponível em: `http://localhost:8080/api`

### 4. Configurar Webhooks (Desenvolvimento)

```bash
# Instalar Stripe CLI
# https://stripe.com/docs/stripe-cli

# Login no Stripe
stripe login

# Forward webhooks para aplicação local
stripe listen --forward-to localhost:8080/api/webhooks/stripe

# Copiar o webhook secret (whsec_...) e adicionar no .env
```

## 📚 Documentação da API

### Endpoints Principais

#### Autenticação
- `POST /api/auth/register` - Registrar novo usuário
- `POST /api/auth/login` - Fazer login

#### Usuários
- `GET /api/users/me` - Obter dados do usuário autenticado 🔒

#### Planos
- `GET /api/plans` - Listar planos disponíveis
- `POST /api/plans` - Criar novo plano 🔒👤

#### Assinaturas
- `POST /api/subscriptions/create-checkout-session` - Iniciar processo de assinatura 🔒
- `GET /api/subscriptions/me` - Obter assinatura ativa 🔒
- `POST /api/subscriptions/manage-portal-session` - Acessar portal do cliente 🔒

#### Webhooks
- `POST /api/webhooks/stripe` - Receber eventos do Stripe

🔒 = Requer autenticação JWT  
👤 = Requer permissão de administrador

### Documentação Interativa (Swagger)

Após iniciar a aplicação, acesse:
- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api/v3/api-docs`

## 🔐 Segurança

### Implementações de Segurança

- ✅ Senhas hasheadas com BCrypt
- ✅ Autenticação JWT stateless
- ✅ Validação de assinatura de webhooks Stripe
- ✅ CORS configurável
- ✅ Proteção CSRF (exceto webhooks)
- ✅ Validação de entrada em todos endpoints
- ✅ Secrets via variáveis de ambiente

### Boas Práticas Aplicadas

1. **Secrets nunca commitados** no Git
2. **HTTPS obrigatório** em produção
3. **Webhook signatures validadas** antes de processar
4. **Princípio do menor privilégio** em permissões
5. **Logs estruturados** sem informações sensíveis

## 🧪 Testes

```bash
# Executar todos os testes
./gradlew test

# Executar com coverage
./gradlew test jacocoTestReport

# Ver relatório de coverage
open build/reports/jacoco/test/html/index.html
```

### Tipos de Testes

- ✅ Testes Unitários (JUnit 5 + Mockito)
- ✅ Testes de Integração (TestContainers)
- ✅ Testes de API (MockMvc)

## 📊 Monitoramento

### Logs

Os logs são configurados de acordo com o profile:

**Development (`dev`)**
- Nível: DEBUG
- Destino: Console
- SQL Queries: Visíveis

**Production (`prod`)**
- Nível: INFO/WARN
- Destino: Arquivo (`logs/gestao-mensalidades-api.log`)
- Rotação: 10MB por arquivo, 30 dias de histórico

## 🌍 Ambientes

### Development (dev)

```bash
# Ativar profile dev
export SPRING_PROFILE=dev
./gradlew bootRun
```

- DDL auto: `update` (facilita desenvolvimento)
- Logs verbosos
- Chaves Stripe de teste
- HTTPS não obrigatório

### Production (prod)

```bash
# Ativar profile prod
export SPRING_PROFILE=prod
java -jar build/libs/gestao-mensalidades-api-0.0.1-SNAPSHOT.jar
```

- DDL auto: `validate` (segurança)
- Logs otimizados
- Chaves Stripe live
- HTTPS obrigatório
- Todas variáveis obrigatórias

## 🔧 Configuração do Stripe

### 1. Criar Produtos e Prices

```bash
# Via Dashboard
https://dashboard.stripe.com/test/products

# Ou via API/CLI
stripe products create --name="Plano Mensal" --description="Assinatura mensal"
stripe prices create --product=prod_xxx --unit-amount=9990 --currency=brl --recurring[interval]=month
```

### 2. Configurar Customer Portal

1. Acesse: https://dashboard.stripe.com/test/settings/billing/portal
2. Habilite:
   - ✅ Update payment methods
   - ✅ Cancel subscriptions
   - ✅ View invoices
3. Configure branding (logo, cores)
4. Defina URL de retorno

### 3. Configurar Webhooks (Produção)

1. Acesse: https://dashboard.stripe.com/test/webhooks
2. Adicione endpoint: `https://seu-dominio.com/api/webhooks/stripe`
3. Selecione eventos:
   - `checkout.session.completed`
   - `invoice.payment_succeeded`
   - `invoice.payment_failed`
   - `customer.subscription.deleted`
   - `customer.subscription.updated`
4. Copie o webhook secret e configure no `.env`

## 📦 Deploy

### Preparação

```bash
# Build da aplicação
./gradlew clean build

# JAR gerado em:
build/libs/gestao-mensalidades-api-0.0.1-SNAPSHOT.jar
```

### Opções de Deploy

1. **Servidor VPS** (AWS EC2, DigitalOcean, etc)
2. **Container** (Docker + Kubernetes)
3. **PaaS** (Heroku, Railway, Render)
4. **Cloud Native** (AWS Elastic Beanstalk, Google App Engine)

### Checklist de Deploy

- [ ] Banco de dados em produção configurado
- [ ] Todas variáveis de ambiente definidas
- [ ] SSL/TLS configurado
- [ ] Webhook endpoint registrado no Stripe (live mode)
- [ ] Chaves Stripe live configuradas
- [ ] Backup automático do banco configurado
- [ ] Monitoramento configurado
- [ ] Logs persistentes configurados

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👥 Autores

- **Equipe de Desenvolvimento** - *Trabalho Inicial*

## 📞 Suporte

Para dúvidas e suporte:
- 📧 Email: suporte@gestaomensalidades.com.br
- 📚 Documentação: [CHECKLIST_DESENVOLVIMENTO.md](CHECKLIST_DESENVOLVIMENTO.md)
- 🐛 Issues: Use o sistema de issues do GitHub

---

**Status do Projeto**: 🚧 Em Desenvolvimento

**Última atualização**: 2025-10-05
