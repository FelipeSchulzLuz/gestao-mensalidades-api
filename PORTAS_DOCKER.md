# 🔌 Mapeamento de Portas - Docker Compose

## 📊 Portas Configuradas (Não Convencionais)

Este projeto usa **portas não convencionais** para evitar conflitos com outros serviços que você possa ter rodando.

### 🗄️ Serviços e Portas

| Serviço       | Porta Padrão | Porta Usada | Motivo                                    |
|---------------|--------------|-------------|-------------------------------------------|
| **PostgreSQL**| 5432         | **54320**   | Evitar conflito com PostgreSQL local      |
| **pgAdmin**   | 5050         | **8800**    | Evitar conflito com outros serviços web   |
| **Redis**     | 6379         | **63790**   | Evitar conflito com Redis local           |
| **API**       | 8080         | **8800**    | Evitar conflito com outras APIs           |

---

## 🌐 URLs de Acesso (Rede Local)

### Acesso via Localhost (mesma máquina)
```
PostgreSQL:  localhost:54320
pgAdmin:     http://localhost:8800
Redis:       localhost:63790
API:         http://localhost:8800/api
```

### Acesso via Rede (192.168.1.12)
```
PostgreSQL:  SEU_IP:54320
pgAdmin:     http://SEU_IP:8800
Redis:       SEU_IP:63790
API:         http://SEU_IP:8800/api
```

---

## 🔧 Configuração no Código

### application.yml (Spring Boot)
```yaml
# Desenvolvimento (localhost)
DB_HOST: localhost
DB_PORT: 54320  # ⚠️ Porta não convencional

# Docker Compose (containers se comunicam)
DB_HOST: postgres
DB_PORT: 5432   # ⚠️ Porta interna do container
```

### .env
```bash
# Para aplicação rodando FORA do Docker (localhost)
DB_HOST=localhost
DB_PORT=54320

# Para aplicação rodando DENTRO do Docker
DB_HOST=postgres
DB_PORT=5432
```

---

## 🔄 Como Mudar as Portas

Se precisar usar portas diferentes, edite o arquivo `docker-compose.yml`:

```yaml
services:
  postgres:
    ports:
      - "SUA_PORTA_EXTERNA:5432"  # Exemplo: "5433:5432"
  
  pgadmin:
    ports:
      - "SUA_PORTA_EXTERNA:80"    # Exemplo: "8800:80"
  
  redis:
    ports:
      - "SUA_PORTA_EXTERNA:6379"  # Exemplo: "6380:6379"
  
  app:
    ports:
      - "SUA_PORTA_EXTERNA:8080"  # Exemplo: "8800:8080"
```

**Importante:** 
- A porta ANTES dos `:` é a porta **externa** (host)
- A porta DEPOIS dos `:` é a porta **interna** (container)
- Containers se comunicam pelas portas internas

---

## 🔍 Verificar Portas em Uso

### Windows (PowerShell)
```powershell
# Ver todas as portas em uso
netstat -an | Select-String "LISTENING"

# Verificar porta específica
netstat -an | Select-String "54320"

# Ver processo usando a porta
Get-NetTCPConnection -LocalPort 54320 | Select-Object -Property LocalPort, OwningProcess, State
```

### Verificar se o Docker está usando as portas
```powershell
docker compose ps
```

---

## 🛡️ Firewall (Windows)

Para permitir acesso externo, execute como **Administrador**:

```powershell
# PostgreSQL
New-NetFirewallRule -DisplayName "Gestão - PostgreSQL" -Direction Inbound -LocalPort 54320 -Protocol TCP -Action Allow

# pgAdmin
New-NetFirewallRule -DisplayName "Gestão - pgAdmin" -Direction Inbound -LocalPort 8800 -Protocol TCP -Action Allow

# Redis
New-NetFirewallRule -DisplayName "Gestão - Redis" -Direction Inbound -LocalPort 63790 -Protocol TCP -Action Allow

# API
New-NetFirewallRule -DisplayName "Gestão - API" -Direction Inbound -LocalPort 8800 -Protocol TCP -Action Allow
```

### Remover regras (se necessário)
```powershell
Remove-NetFirewallRule -DisplayName "Gestão - PostgreSQL"
Remove-NetFirewallRule -DisplayName "Gestão - pgAdmin"
Remove-NetFirewallRule -DisplayName "Gestão - Redis"
Remove-NetFirewallRule -DisplayName "Gestão - API"
```

---

## 🧪 Testar Conectividade

### PostgreSQL
```powershell
# De dentro do container
docker compose exec postgres psql -U gestao_app_user -d gestao_mensalidades_dev

# De fora (localhost)
psql -h localhost -p 54320 -U gestao_app_user -d gestao_mensalidades_dev

# De outra máquina na rede (substitua SEU_IP)
psql -h SEU_IP -p 54320 -U gestao_app_user -d gestao_mensalidades_dev

# Testar conectividade TCP
Test-NetConnection -ComputerName SEU_IP -Port 54320
```

### pgAdmin
```powershell
# Abrir no navegador
Start-Process "http://localhost:8800"
# ou (substitua SEU_IP)
Start-Process "http://SEU_IP:8800"
```

### Redis
```powershell
# De dentro do container
docker compose exec redis redis-cli -a SUA_SENHA ping

# De fora (precisa ter redis-cli instalado)
redis-cli -h localhost -p 63790 -a SUA_SENHA ping
```

### API (quando estiver rodando)
```powershell
# Health check
curl http://localhost:8800/api/actuator/health
# ou
Invoke-RestMethod -Uri "http://localhost:8800/api/actuator/health"
```

---

## 📝 Notas Importantes

1. **Containers se comunicam entre si usando portas internas (5432, 6379, 8080)**
   - A aplicação Spring Boot dentro do Docker usa `postgres:5432`
   - NÃO use `localhost:54320` quando rodando dentro do Docker

2. **Acesso externo usa portas mapeadas (54320, 63790, 8085)**
   - Sua máquina de desenvolvimento usa `localhost:54320`
   - Outras máquinas na rede usam `192.168.1.12:54320`

3. **Conflitos de porta**
   - Se ainda tiver conflito, verifique com `netstat -an | findstr "54320"`
   - Mude no `docker-compose.yml` e atualize `.env` e `application.yml`

4. **Segurança**
   - Em produção, use firewall para limitar acesso
   - Nunca exponha PostgreSQL/Redis diretamente na internet
   - Use SSL/TLS em produção
   - Use senhas fortes no arquivo `.env`

---

## 🎯 Checklist de Configuração

- [ ] Copiar `.env.docker` para `.env`
- [ ] Configurar senhas fortes no `.env`
- [ ] Iniciar containers: `docker compose up -d`
- [ ] Verificar status: `docker compose ps`
- [ ] Testar PostgreSQL: `Test-NetConnection -ComputerName localhost -Port 54320`
- [ ] Acessar pgAdmin: http://localhost:8090
- [ ] Configurar firewall (se acesso externo necessário)
- [ ] Atualizar `application.yml` se mudar portas

---

## 🆘 Ajuda Rápida

**Container não inicia:**
```powershell
docker compose logs nome_do_servico
```

**Porta em uso:**
```powershell
netstat -an | findstr "PORTA"
# Pare o serviço que está usando ou mude a porta
```

**Resetar tudo:**
```powershell
docker compose down -v  # ⚠️ APAGA DADOS!
docker compose up -d
```

**Ver uso de recursos:**
```powershell
docker stats
```
