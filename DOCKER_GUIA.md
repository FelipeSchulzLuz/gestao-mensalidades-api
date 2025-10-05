# 🐳 Guia Docker - Gestão de Mensalidades API

## 📋 Índice

1. [Pré-requisitos](#pré-requisitos)
2. [Configuração Inicial](#configuração-inicial)
3. [Comandos Úteis](#comandos-úteis)
4. [Acesso aos Serviços](#acesso-aos-serviços)
5. [Configuração para Rede 192.168.1.12](#configuração-para-rede-1921681012)
6. [Troubleshooting](#troubleshooting)
7. [Backup e Restauração](#backup-e-restauração)
8. [Monitoramento](#monitoramento)

---

## 🔧 Pré-requisitos

- Docker instalado (versão 20.10+)
- Docker Compose instalado (versão 2.0+)
- 4GB RAM disponível (mínimo)
- 10GB espaço em disco

**Verificar instalação:**
```bash
docker --version
docker compose version
```

---

## ⚙️ Configuração Inicial

### 1. Criar arquivo de variáveis de ambiente

```bash
# Copiar template
cp .env.docker .env

# Editar com seus valores (IMPORTANTE!)
notepad .env  # Windows
# ou
nano .env     # Linux/Mac
```

### 2. Gerar senhas seguras

```powershell
# PowerShell - Gerar senha aleatória
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 32 | ForEach-Object {[char]$_})

# Ou online (use apenas em desenvolvimento)
# https://passwordsgenerator.net/
```

### 3. Configurar JWT Secret

```bash
# Linux/Mac
openssl rand -base64 64

# PowerShell
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))
```

---

## 🚀 Comandos Úteis

### Iniciar todos os serviços

```bash
# Primeira vez (com build)
docker compose up -d --build

# Iniciar normalmente
docker compose up -d

# Ver logs em tempo real
docker compose logs -f

# Ver logs de um serviço específico
docker compose logs -f postgres
docker compose logs -f pgadmin
```

### Parar serviços

```bash
# Parar todos (mantém volumes)
docker compose stop

# Parar e remover containers (mantém volumes)
docker compose down

# Parar, remover containers E volumes (CUIDADO!)
docker compose down -v
```

### Status dos serviços

```bash
# Ver status
docker compose ps

# Ver recursos utilizados
docker stats

# Ver redes
docker network ls

# Ver volumes
docker volume ls
```

### Executar comandos nos containers

```bash
# PostgreSQL - Acessar psql
docker compose exec postgres psql -U gestao_app_user -d gestao_mensalidades_dev

# PostgreSQL - Backup manual
docker compose exec postgres pg_dump -U gestao_app_user gestao_mensalidades_dev > backup_$(date +%Y%m%d).sql

# Redis - Acessar CLI
docker compose exec redis redis-cli -a redis_password_seguro

# Ver logs de erro do PostgreSQL
docker compose exec postgres tail -f /var/log/postgresql/postgresql.log
```

### Reiniciar serviço específico

```bash
# Reiniciar PostgreSQL
docker compose restart postgres

# Reiniciar tudo
docker compose restart
```

---

## 🌐 Acesso aos Serviços

### PostgreSQL

```yaml
Host: localhost
Port: 54320  # ⚠️ Porta não convencional para evitar conflitos
Database: gestao_mensalidades_dev
Username: gestao_app_user
Password: (definido no .env)
```

**Conexão via psql:**
```bash
psql -h localhost -p 54320 -U gestao_app_user -d gestao_mensalidades_dev
```

**Connection String:**
```
postgresql://gestao_app_user:PASSWORD@localhost:54320/gestao_mensalidades_dev
```

### pgAdmin (Interface Visual)

```
URL: http://localhost:8800
Email: admin@gestao.local
Password: (definido no .env como PGADMIN_PASSWORD)
```

**Primeira conexão:**
1. Acesse http://localhost:8800
2. Login com credenciais acima
3. Clique em "Add New Server"
4. **General Tab:**
   - Name: `Gestão Mensalidades`
5. **Connection Tab:**
   - Host: `postgres` (nome do serviço Docker)
   - Port: `5432` (⚠️ porta interna, não externa)
   - Database: `gestao_mensalidades_dev`
   - Username: `gestao_app_user`
   - Password: (senha do .env)
   - Save password: ✓

### Redis

```yaml
Host: localhost
Port: 63790  # ⚠️ Porta não convencional para evitar conflitos
Password: (definido no .env como REDIS_PASSWORD)
```

**Teste de conexão:**
```bash
docker compose exec redis redis-cli -a redis_password_seguro ping
# Deve retornar: PONG
```

### API Spring Boot (quando descomentado)

```
URL: http://localhost:8800/api
Health Check: http://localhost:8800/api/actuator/health
Swagger: http://localhost:8800/api/swagger-ui.html
```

---

## 🏠 Configuração para Rede 192.168.1.12

### Opção 1: Bind em todas as interfaces (mais simples)

Já está configurado! O `docker-compose.yml` usa `"5432:5432"` que faz bind em `0.0.0.0`, permitindo acesso de qualquer IP.

**Acesso:**
```
PostgreSQL: localhost:54320
pgAdmin:    http://localhost:8800
Redis:      localhost:63790
API:        http://localhost:8800/api (quando descomentar)
```

### Opção 2: Bind específico em 192.168.1.12

Edite `docker-compose.yml` para cada serviço:

```yaml
services:
  postgres:
    ports:
      - "SEU_IP:54320:5432"  # Exemplo: "192.168.1.10:54320:5432"
  
  pgadmin:
    ports:
      - "SEU_IP:8800:80"
  
  redis:
    ports:
      - "SEU_IP:63790:6379"
```
```
PostgreSQL: 192.168.1.12:5432
pgAdmin:    http://192.168.1.12:5050
Redis:      192.168.1.12:6379
API:        http://192.168.1.12:8080/api
```

### Opção 2: Bind específico em 192.168.1.12

Edite `docker-compose.yml` para cada serviço:

```yaml
services:
  postgres:
    ports:
      - "192.168.1.12:5432:5432"  # Em vez de "5432:5432"
  
  pgadmin:
    ports:
      - "192.168.1.12:5050:80"
  
  redis:
    ports:
      - "192.168.1.12:6379:6379"
```

### Firewall Windows

```powershell
# Permitir PostgreSQL (porta não convencional)
New-NetFirewallRule -DisplayName "PostgreSQL Docker" -Direction Inbound -LocalPort 54320 -Protocol TCP -Action Allow

# Permitir pgAdmin
New-NetFirewallRule -DisplayName "pgAdmin Docker" -Direction Inbound -LocalPort 8800 -Protocol TCP -Action Allow

# Permitir Redis (porta não convencional)
New-NetFirewallRule -DisplayName "Redis Docker" -Direction Inbound -LocalPort 63790 -Protocol TCP -Action Allow

# Permitir API
New-NetFirewallRule -DisplayName "Gestão API Docker" -Direction Inbound -LocalPort 8800 -Protocol TCP -Action Allow
```

### Testar acesso remoto

```bash
# De outra máquina na rede (substitua SEU_IP pelo IP do servidor)
telnet SEU_IP 54320
# ou
Test-NetConnection -ComputerName SEU_IP -Port 54320
```

---

## 🔍 Troubleshooting

### PostgreSQL não inicia

```bash
# Ver logs
docker compose logs postgres

# Erro comum: porta em uso
netstat -an | findstr 54320
# Solução: parar serviço que usa a porta ou mudar porta no docker-compose.yml
# O projeto já usa portas não convencionais para evitar conflitos:
# - PostgreSQL: 54320 (padrão seria 5432)
# - pgAdmin: 8800 (padrão seria 5050 ou 80)
# - Redis: 63790 (padrão seria 6379)
# - API: 8800 (padrão seria 8080)

# Erro: permissão no volume
docker compose down -v
docker volume rm gestao-mensalidades-api_postgres_data
docker compose up -d
```

### pgAdmin não conecta ao PostgreSQL

1. **Verificar se o PostgreSQL está rodando:**
   ```bash
   docker compose ps postgres
   ```

2. **Usar nome do serviço como host:**
   - ✅ Host: `postgres` (nome do serviço)
   - ❌ Host: `localhost` ou `127.0.0.1`

3. **Verificar rede Docker:**
   ```bash
   docker network inspect gestao-mensalidades-api_gestao-network
   ```

### Redis não responde

```bash
# Verificar se está rodando
docker compose ps redis

# Testar conexão
docker compose exec redis redis-cli -a redis_password_seguro ping

# Ver logs
docker compose logs redis
```

### Aplicação não conecta ao banco

1. **Verificar variáveis de ambiente:**
   ```bash
   docker compose config
   ```

2. **Verificar se usou nome do serviço:**
   - ✅ `DB_HOST=postgres`
   - ❌ `DB_HOST=localhost`

3. **Ver logs da aplicação:**
   ```bash
   docker compose logs app
   ```

### Desempenho lento

```bash
# Ver recursos utilizados
docker stats

# Aumentar recursos no Docker Desktop:
# Settings > Resources > aumentar CPU e RAM

# Limpar containers não usados
docker system prune -a --volumes
```

---

## 💾 Backup e Restauração

### Backup automático do PostgreSQL

**Criar script de backup:**

```bash
# backup.sh (Linux/Mac)
#!/bin/bash
BACKUP_DIR="./backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

docker compose exec -T postgres pg_dump -U gestao_app_user gestao_mensalidades_dev | gzip > "$BACKUP_DIR/backup_$TIMESTAMP.sql.gz"

echo "Backup criado: backup_$TIMESTAMP.sql.gz"

# Manter apenas últimos 7 dias
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +7 -delete
```

```powershell
# backup.ps1 (Windows)
$BackupDir = ".\backups"
$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
New-Item -ItemType Directory -Force -Path $BackupDir

docker compose exec -T postgres pg_dump -U gestao_app_user gestao_mensalidades_dev | Out-File "$BackupDir\backup_$Timestamp.sql"

Write-Host "Backup criado: backup_$Timestamp.sql"

# Manter apenas últimos 7 dias
Get-ChildItem $BackupDir -Filter "backup_*.sql" | Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-7) } | Remove-Item
```

**Agendar no cron/Task Scheduler:**

```bash
# Linux - crontab -e
0 2 * * * cd /caminho/para/projeto && ./backup.sh
```

### Restaurar backup

```bash
# Descompactar e restaurar
gunzip < backups/backup_20250105_020000.sql.gz | docker compose exec -T postgres psql -U gestao_app_user gestao_mensalidades_dev

# Ou Windows (sem compressão)
Get-Content backups\backup_20250105_020000.sql | docker compose exec -T postgres psql -U gestao_app_user gestao_mensalidades_dev
```

### Backup completo do volume Docker

```bash
# Backup
docker run --rm -v gestao-mensalidades-api_postgres_data:/data -v $(pwd)/backups:/backup ubuntu tar czf /backup/postgres_volume_backup.tar.gz -C /data .

# Restaurar
docker run --rm -v gestao-mensalidades-api_postgres_data:/data -v $(pwd)/backups:/backup ubuntu tar xzf /backup/postgres_volume_backup.tar.gz -C /data
```

---

## 📊 Monitoramento

### Health Checks

```bash
# PostgreSQL
docker compose exec postgres pg_isready -U gestao_app_user

# Redis
docker compose exec redis redis-cli -a redis_password_seguro ping

# API (quando estiver rodando)
curl http://localhost:8080/api/actuator/health
```

### Métricas de recursos

```bash
# Ver uso de CPU/RAM/Rede
docker stats

# Ver tamanho dos volumes
docker system df -v

# Ver logs de erro
docker compose logs --tail=100 | grep -i error
```

### Logs estruturados

```bash
# Últimas 100 linhas
docker compose logs --tail=100

# Desde uma data
docker compose logs --since="2025-01-05T10:00:00"

# Seguir logs de produção
docker compose logs -f --no-color | tee logs/prod_$(date +%Y%m%d).log
```

---

## 🎯 Próximos Passos

1. ✅ Configurar `.env` com valores reais
2. ✅ Iniciar serviços: `docker compose up -d`
3. ✅ Verificar saúde: `docker compose ps`
4. ✅ Acessar pgAdmin e configurar conexão
5. ⏳ Descomentar serviço `app` quando aplicação estiver pronta
6. ⏳ Configurar CI/CD para build automático
7. ⏳ Configurar nginx como reverse proxy (produção)
8. ⏳ Configurar SSL/TLS com Let's Encrypt

---

## 📞 Suporte

**Comandos de diagnóstico completo:**

```bash
# Gerar relatório completo
echo "=== Docker Info ===" > diagnostico.txt
docker compose ps >> diagnostico.txt
echo "\n=== Logs PostgreSQL ===" >> diagnostico.txt
docker compose logs --tail=50 postgres >> diagnostico.txt
echo "\n=== Logs pgAdmin ===" >> diagnostico.txt
docker compose logs --tail=50 pgadmin >> diagnostico.txt
echo "\n=== Container Stats ===" >> diagnostico.txt
docker stats --no-stream >> diagnostico.txt
echo "\n=== Volumes ===" >> diagnostico.txt
docker volume ls >> diagnostico.txt
```

---

**Documentação oficial:**
- Docker: https://docs.docker.com/
- Docker Compose: https://docs.docker.com/compose/
- PostgreSQL Docker: https://hub.docker.com/_/postgres
- pgAdmin Docker: https://www.pgadmin.org/docs/pgadmin4/latest/container_deployment.html
