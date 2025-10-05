# 🐘 Guia de Configuração do PostgreSQL

Este documento fornece instruções detalhadas para configurar o PostgreSQL para o projeto.

---

## 📋 Índice

1. [Instalação](#instalação)
2. [Configuração com Docker](#configuração-com-docker-recomendado)
3. [Configuração Local](#configuração-local)
4. [Executar Script de Inicialização](#executar-script-de-inicialização)
5. [Verificação](#verificação)
6. [Troubleshooting](#troubleshooting)

---

## 🚀 Instalação

### Opção 1: Docker (Recomendado para Desenvolvimento)

Mais rápido e isolado do sistema operacional.

### Opção 2: Instalação Local

Para uso em produção ou preferência pessoal.

---

## 🐳 Configuração com Docker (Recomendado)

### 1. Iniciar PostgreSQL com Docker

```bash
docker run --name postgres-dev \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=gestao_mensalidades_dev \
  -p 5432:5432 \
  -v postgres-data:/var/lib/postgresql/data \
  -d postgres:15
```

**Explicação dos parâmetros:**
- `--name postgres-dev`: Nome do container
- `-e POSTGRES_PASSWORD`: Senha do usuário postgres
- `-e POSTGRES_DB`: Nome do banco inicial
- `-p 5432:5432`: Mapeia porta do container para host
- `-v postgres-data:/var/lib/postgresql/data`: Volume persistente
- `-d`: Executa em background
- `postgres:15`: Imagem PostgreSQL versão 15

### 2. Verificar se está rodando

```bash
docker ps
```

### 3. Parar o Container

```bash
docker stop postgres-dev
```

### 4. Iniciar Container Existente

```bash
docker start postgres-dev
```

### 5. Remover Container (dados serão perdidos se não usar volume)

```bash
docker stop postgres-dev
docker rm postgres-dev
```

### 6. Acessar o PostgreSQL no Container

```bash
docker exec -it postgres-dev psql -U postgres -d gestao_mensalidades_dev
```

---

## 💻 Configuração Local

### Windows

1. **Baixar Instalador**
   - Acesse: https://www.postgresql.org/download/windows/
   - Baixe a versão 15 ou superior
   - Execute o instalador

2. **Durante a Instalação**
   - Defina senha para usuário `postgres`
   - Porta: `5432` (padrão)
   - Locale: `Portuguese, Brazil` ou `Default`

3. **Adicionar ao PATH** (se não foi feito automaticamente)
   ```powershell
   # Adicionar ao PATH do sistema
   $env:Path += ";C:\Program Files\PostgreSQL\15\bin"
   ```

4. **Verificar Instalação**
   ```powershell
   psql --version
   ```

### Linux (Ubuntu/Debian)

```bash
# Atualizar repositórios
sudo apt update

# Instalar PostgreSQL
sudo apt install postgresql postgresql-contrib

# Verificar status
sudo systemctl status postgresql

# Iniciar serviço
sudo systemctl start postgresql

# Habilitar na inicialização
sudo systemctl enable postgresql
```

### macOS

```bash
# Usando Homebrew
brew install postgresql@15

# Iniciar serviço
brew services start postgresql@15

# Verificar
psql --version
```

---

## 📝 Executar Script de Inicialização

### 1. Localizar o Script

O script está em: `database/init.sql`

### 2. Executar via psql

#### Com Docker:

```bash
# Copiar script para o container
docker cp database/init.sql postgres-dev:/tmp/init.sql

# Executar script
docker exec -it postgres-dev psql -U postgres -f /tmp/init.sql
```

#### Local (Windows):

```powershell
psql -U postgres -f database/init.sql
```

#### Local (Linux/Mac):

```bash
sudo -u postgres psql -f database/init.sql
```

### 3. Conectar ao Banco Criado

```bash
# Docker
docker exec -it postgres-dev psql -U postgres -d gestao_mensalidades_dev

# Local
psql -U postgres -d gestao_mensalidades_dev
```

### 4. Comandos Úteis no psql

```sql
-- Listar bancos de dados
\l

-- Conectar a um banco
\c gestao_mensalidades_dev

-- Listar tabelas
\dt

-- Descrever tabela
\d users

-- Listar extensões
\dx

-- Sair
\q
```

---

## ✅ Verificação

### 1. Verificar Conexão

Teste a conexão usando as credenciais do `.env`:

```bash
# Variáveis do .env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=gestao_mensalidades_dev
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

### 2. Testar Conexão via psql

```bash
psql -h localhost -p 5432 -U postgres -d gestao_mensalidades_dev
```

### 3. Verificar Tabelas Criadas

Após rodar a aplicação pela primeira vez, o Hibernate criará as tabelas automaticamente (com `ddl-auto: update` em dev).

```sql
-- Conectar ao banco
\c gestao_mensalidades_dev

-- Listar tabelas
\dt

-- Deverá exibir:
-- users
-- plans
-- subscriptions
```

### 4. Testar Aplicação

```bash
# Rodar aplicação
./gradlew bootRun

# Verificar logs para confirmar conexão com banco
```

---

## 🐛 Troubleshooting

### Erro: "connection refused"

**Causa**: PostgreSQL não está rodando ou porta incorreta.

**Solução**:

```bash
# Docker: Verificar se container está rodando
docker ps

# Local (Linux): Verificar serviço
sudo systemctl status postgresql

# Local (Windows): Verificar em Services
```

### Erro: "password authentication failed"

**Causa**: Senha incorreta no `.env`.

**Solução**:
1. Verificar senha em `.env`
2. Resetar senha se necessário:

```bash
# Docker
docker exec -it postgres-dev psql -U postgres
ALTER USER postgres PASSWORD 'nova_senha';

# Local
sudo -u postgres psql
ALTER USER postgres PASSWORD 'nova_senha';
```

### Erro: "database does not exist"

**Causa**: Banco não foi criado.

**Solução**:

```bash
# Criar banco manualmente
psql -U postgres
CREATE DATABASE gestao_mensalidades_dev;
\q
```

### Erro: "FATAL: role does not exist"

**Causa**: Usuário especificado não existe.

**Solução**:
- Use o usuário `postgres` (padrão)
- Ou crie o usuário necessário

### Porta 5432 já em uso

**Causa**: Outra instância do PostgreSQL rodando.

**Solução Docker**:

```bash
# Usar porta diferente
docker run --name postgres-dev \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=gestao_mensalidades_dev \
  -p 5433:5432 \
  -d postgres:15

# Atualizar .env
DB_PORT=5433
```

### Performance lenta

**Causas possíveis**:
- Muitas conexões abertas
- Falta de índices
- Queries não otimizadas

**Soluções**:

```sql
-- Ver conexões ativas
SELECT count(*) FROM pg_stat_activity;

-- Ver queries lentas
SELECT query, calls, mean_exec_time 
FROM pg_stat_statements 
ORDER BY mean_exec_time DESC 
LIMIT 10;

-- Vacuum
VACUUM ANALYZE;
```

---

## 🔧 Configurações Avançadas

### Connection Pooling (HikariCP)

Já configurado em `application.yml`:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10      # Dev
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
```

Para produção, ajuste:

```yaml
hikari:
  maximum-pool-size: 20
  minimum-idle: 10
```

### Configurar PostgreSQL para Performance

Editar `postgresql.conf`:

```ini
# Memória
shared_buffers = 256MB          # 25% da RAM
work_mem = 16MB
maintenance_work_mem = 64MB

# Checkpoints
checkpoint_completion_target = 0.9
wal_buffers = 16MB

# Logging (desenvolvimento)
log_statement = 'all'
log_duration = on
```

---

## 📚 Recursos Adicionais

- [Documentação PostgreSQL](https://www.postgresql.org/docs/)
- [PostgreSQL Docker Hub](https://hub.docker.com/_/postgres)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)

---

## 🎯 Próximos Passos

Após configurar o PostgreSQL:

1. ✅ Configurar variáveis em `.env`
2. ✅ Executar `./gradlew bootRun`
3. ✅ Verificar logs de conexão
4. ✅ Prosseguir para Fase 3 (Autenticação)

---

**Última atualização**: 2025-10-05  
**Versão**: 1.0
