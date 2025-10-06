# 🚀 CI/CD - GitHub Actions

> **Pipelines automatizados de integração e deploy contínuo**

Este projeto utiliza **GitHub Actions** para automação completa de CI/CD, garantindo qualidade de código, segurança e deploys confiáveis.

---

## 📋 Índice

- [Workflows Disponíveis](#workflows-disponíveis)
- [CI Pipeline](#ci-pipeline)
- [CD Pipeline](#cd-pipeline)
- [PR Checks](#pr-checks)
- [Configuração de Secrets](#configuração-de-secrets)
- [Como Usar](#como-usar)
- [Badges](#badges)

---

## 🔄 Workflows Disponíveis

### 1. CI Pipeline (`ci.yml`)
**Trigger:** Push em `master` ou `develop`, Pull Requests

**Jobs:**
- ✅ **Build & Test** - Compila e testa com Java 21
- ✅ **Code Quality** - OWASP, Trivy, SonarCloud (opcional)
- ✅ **Docker Build** - Build e push de imagens Docker
- ✅ **Notification** - Notificações de sucesso/falha

### 2. CD Pipeline (`cd.yml`)
**Trigger:** Push em `master`, Tags `v*.*.*`, Manual dispatch

**Jobs:**
- ✅ **Deploy Staging** - Deploy automático em staging
- ✅ **Deploy Production** - Deploy em produção (requer aprovação)
- ✅ **Health Check** - Validação pós-deploy
- ✅ **Rollback** - Rollback automático em caso de falha

### 3. PR Checks (`pr-checks.yml`)
**Trigger:** Abertura/atualização de Pull Requests

**Jobs:**
- ✅ **PR Validation** - Valida título (Conventional Commits), tamanho, conflitos
- ✅ **Auto Review** - Checkstyle, SpotBugs, Coverage Report
- ✅ **Documentation Check** - Verifica atualizações de docs
- ✅ **Security Scan** - OWASP Dependency Check
- ✅ **Docker Test Build** - Testa build Docker

---

## 🏗️ CI Pipeline

### Fluxo de Execução

```mermaid
graph LR
    A[Push/PR] --> B[Build & Test]
    B --> C[Code Quality]
    B --> D[Docker Build]
    C --> E[Notification]
    D --> E
```

### Services Utilizados

**PostgreSQL** (Testes)
```yaml
postgres:15-alpine
  - POSTGRES_DB: gestao_mensalidades_test
  - POSTGRES_USER: test_user
  - POSTGRES_PASSWORD: test_password
```

**Redis** (Cache)
```yaml
redis:7-alpine
```

### Build & Test

```bash
# Build
./gradlew clean build -x test --no-daemon

# Tests
./gradlew test --no-daemon

# Coverage
./gradlew jacocoTestReport --no-daemon
```

### Code Quality & Security

**OWASP Dependency Check**
```bash
./gradlew dependencyCheckAnalyze --no-daemon
```

**Trivy Security Scan**
- Scan de filesystem
- Upload de resultados para GitHub Security

**SonarCloud** (Opcional)
- Requer `SONAR_TOKEN` secret
- Análise completa de qualidade

### Docker Build & Push

**Tags geradas:**
- `master` - Build da branch master
- `master-sha-abc123` - Build com SHA do commit
- `v1.0.0` - Versão semântica (tags)

---

## 🚀 CD Pipeline

### Deploy Staging

**Ambiente:** `staging`  
**URL:** https://staging.gestao-mensalidades.com  
**Trigger:** Push em `master`

**Processo:**
1. 📥 Pull código + Docker image
2. ⏸️ Stop container atual
3. 🚀 Start novo container
4. ⏳ Wait + Health check
5. 🧹 Cleanup imagens antigas

### Deploy Production

**Ambiente:** `production`  
**URL:** https://api.gestao-mensalidades.com  
**Trigger:** Tags `v*.*.*` ou manual dispatch  
**Requer:** Aprovação manual (GitHub Environment)

**Processo:**
1. 📸 Backup database
2. 📥 Pull código + Docker image
3. ⏸️ Stop container atual
4. 🚀 Start novo container
5. ⏳ Wait + Health check
6. 🏷️ Create GitHub Release
7. 🔄 Rollback automático se falhar

### Rollback Automático

Se o health check falhar após deploy em produção:
```bash
docker-compose down
docker-compose up -d  # Volta para versão anterior
```

---

## ✅ PR Checks

### Validações Automáticas

#### 1. **PR Title** (Conventional Commits)
Formatos aceitos:
```
feat: add new feature
fix: resolve bug
docs: update documentation
refactor: improve code structure
test: add unit tests
chore: update dependencies
```

#### 2. **PR Size Check**
- ⚠️ Warning se PR > 1000 linhas
- Recomenda quebrar em PRs menores

#### 3. **Merge Conflicts**
- ❌ Bloqueia se houver conflitos
- ✅ Passa se não houver conflitos

### Code Review Automation

**Coverage Report**
- Mínimo 70% coverage geral
- Mínimo 80% coverage em arquivos alterados
- Comentário automático no PR

**Checkstyle + SpotBugs**
- Análise estática de código
- Relatórios em artifacts

### Documentation Check

- ⚠️ Alerta se código mudou mas docs não
- 🔗 Verifica links quebrados em `docs/`

---

## 🔐 Configuração de Secrets

### GitHub Secrets Necessários

#### **Docker Hub**
```
DOCKER_USERNAME=your-dockerhub-username
DOCKER_PASSWORD=your-dockerhub-token
```

#### **SSH Keys (Staging)**
```
STAGING_SSH_KEY=-----BEGIN OPENSSH PRIVATE KEY-----...
STAGING_USER=ubuntu
STAGING_HOST=staging.example.com
```

#### **SSH Keys (Production)**
```
PRODUCTION_SSH_KEY=-----BEGIN OPENSSH PRIVATE KEY-----...
PRODUCTION_USER=ubuntu
PRODUCTION_HOST=api.example.com
```

#### **SonarCloud** (Opcional)
```
SONAR_TOKEN=your-sonarcloud-token
```

### Como Adicionar Secrets

1. Acesse: `Settings` → `Secrets and variables` → `Actions`
2. Clique em `New repository secret`
3. Adicione nome e valor
4. Salve

### Gerar SSH Key

```bash
# Gerar nova key
ssh-keygen -t ed25519 -C "github-actions@gestao-mensalidades"

# Adicionar public key no servidor
ssh-copy-id -i ~/.ssh/id_ed25519.pub user@server

# Copiar private key para GitHub Secret
cat ~/.ssh/id_ed25519
```

---

## 📖 Como Usar

### 1. Desenvolvimento Local

```bash
# Push para develop (CI apenas)
git checkout develop
git add .
git commit -m "feat: add new feature"
git push origin develop
```

### 2. Pull Request

```bash
# Criar PR para master
git checkout -b feature/my-feature
git add .
git commit -m "feat: implement amazing feature"
git push origin feature/my-feature
# Abrir PR no GitHub
```

**Checks executados:**
- ✅ PR title validation
- ✅ Build & test
- ✅ Code quality
- ✅ Security scan
- ✅ Coverage report
- ✅ Documentation check

### 3. Deploy Staging

```bash
# Merge PR para master (automático)
git checkout master
git merge feature/my-feature
git push origin master
```

**Resultado:** Deploy automático em staging

### 4. Deploy Production

```bash
# Criar tag de versão
git tag -a v1.2.3 -m "Release v1.2.3"
git push origin v1.2.3
```

**Resultado:**
1. Deploy em staging (automático)
2. Aguarda aprovação manual
3. Deploy em production
4. Cria GitHub Release

### 5. Manual Deploy

1. Acesse: `Actions` → `CD Pipeline - Deploy`
2. Clique em `Run workflow`
3. Escolha ambiente: `staging` ou `production`
4. Clique em `Run workflow`

---

## 🎯 Badges

Adicione ao README.md:

```markdown
![CI](https://github.com/FelipeSchulzLuz/gestao-mensalidades-api/workflows/CI%20Pipeline/badge.svg)
![CD](https://github.com/FelipeSchulzLuz/gestao-mensalidades-api/workflows/CD%20Pipeline%20-%20Deploy/badge.svg)
![Coverage](https://img.shields.io/badge/coverage-80%25-green)
![Java](https://img.shields.io/badge/java-21-blue)
![Spring Boot](https://img.shields.io/badge/spring--boot-3.5.6-green)
```

**Resultado:**

![CI](https://github.com/FelipeSchulzLuz/gestao-mensalidades-api/workflows/CI%20Pipeline/badge.svg)
![CD](https://github.com/FelipeSchulzLuz/gestao-mensalidades-api/workflows/CD%20Pipeline%20-%20Deploy/badge.svg)
![Coverage](https://img.shields.io/badge/coverage-80%25-green)
![Java](https://img.shields.io/badge/java-21-blue)
![Spring Boot](https://img.shields.io/badge/spring--boot-3.5.6-green)

---

## 🐛 Troubleshooting

### Build falha com "Permission denied"

```bash
# Adicione permissão ao gradlew
chmod +x gradlew
git add gradlew
git commit -m "fix: add execute permission to gradlew"
```

### Docker push falha

- Verifique `DOCKER_USERNAME` e `DOCKER_PASSWORD`
- Confirme que token tem permissões de write

### SSH connection falha

- Verifique formato da key (deve incluir `-----BEGIN ... -----END`)
- Confirme que public key está no servidor (`~/.ssh/authorized_keys`)
- Teste conexão: `ssh -o StrictHostKeyChecking=no user@host`

### Health check falha

- Verifique se Actuator está habilitado
- Confirme porta 8800 exposta
- Aumente timeout em `cd.yml` se necessário

---

## 📚 Referências

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

---

**Mantido por:** GitHub Copilot AI  
**Última atualização:** 2025-10-06  
**Status:** ✅ Operacional
