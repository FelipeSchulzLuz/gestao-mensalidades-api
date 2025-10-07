# Configuração de CI/CD e Análise de Qualidade

Este documento descreve a configuração completa de CI/CD, análise de qualidade e segurança do projeto.

## 📋 Índice

- [Workflows GitHub Actions](#workflows-github-actions)
- [SonarCloud](#sonarcloud)
- [Security Scanning](#security-scanning)
- [Docker](#docker)
- [Dependabot](#dependabot)

---

## 🔄 Workflows GitHub Actions

### 1. **CI Pipeline** (`.github/workflows/ci.yml`)

**Execução:** Push e Pull Requests para `master` e `develop`

**Jobs:**
- ✅ **Build & Test**: Compilação e testes com PostgreSQL e Redis
- 📊 **Code Quality**: OWASP Dependency Check, Trivy, SonarCloud (opcional)
- 🐳 **Docker Build**: Build e push da imagem Docker (apenas em push)
- 📢 **Notification**: Notificação do status do pipeline

**Serviços:**
- PostgreSQL 15-alpine (porta 5432)
- Redis 7-alpine (porta 6379)

**Artefatos gerados:**
- Resultados dos testes
- Relatório de cobertura (Jacoco)
- Imagem Docker (DockerHub)

---

### 2. **Security Scanning** (`.github/workflows/security-scan.yml`)

**Execução:** 
- Push e Pull Requests
- Agendado: Toda segunda-feira às 3h AM
- Manualmente via `workflow_dispatch`

**Jobs:**
1. **Dependency Scan**: OWASP Dependency Check + GitHub Dependency Graph
2. **Trivy Scan**: Análise de vulnerabilidades no código
3. **Docker Scan**: Scan da imagem Docker com Trivy e Docker Scout
4. **Secret Scan**: TruffleHog para detectar secrets expostos
5. **CodeQL**: Análise estática de segurança do GitHub
6. **Security Summary**: Resumo consolidado de todos os scans

**Relatórios:**
- Resultados enviados para GitHub Security tab
- Artefatos: Relatórios OWASP e Trivy
- Summary com status de todos os scans

---

### 3. **SonarCloud Analysis** (`.github/workflows/sonarcloud.yml`)

**Execução:** Push e Pull Requests para `master` e `develop`

**Métricas analisadas:**
- 🐛 Bugs
- 🔒 Vulnerabilidades
- 🧪 Code Smells
- 📊 Cobertura de testes
- 🔁 Duplicação de código
- 📏 Complexidade ciclomática

---

### 4. **CD Pipeline** (`.github/workflows/cd.yml`)

**Execução:** 
- Push para `master`: Deploy para Staging
- Tags `v*.*.*`: Deploy para Production

**Ambientes:**
- 🧪 **Staging**: https://staging.gestao-mensalidades.com
- 🚀 **Production**: https://api.gestao-mensalidades.com

---

### 5. **PR Checks** (`.github/workflows/pr-checks.yml`)

**Validações:**
- ✅ Conventional Commits no título
- 📏 Tamanho do PR (alerta se > 1000 linhas)
- 🔍 Detecção de conflitos de merge
- 🏗️ Build do projeto
- 🔍 Checkstyle
- 📊 Relatório de cobertura comentado no PR

---

## ☁️ SonarCloud

### Configuração Inicial

1. **Criar conta no SonarCloud:**
   - Acesse: https://sonarcloud.io
   - Faça login com sua conta GitHub
   - Autorize o acesso aos repositórios

2. **Importar o projeto:**
   - Clique em "+" → "Analyze new project"
   - Selecione `FelipeSchulzLuz/gestao-mensalidades-api`
   - Escolha "With GitHub Actions"

3. **Configurar token:**
   - Copie o `SONAR_TOKEN` gerado
   - Vá em Settings → Secrets and variables → Actions
   - Adicione o secret `SONAR_TOKEN`

4. **Atualizar organization (se necessário):**
   - Edite `.github/workflows/sonarcloud.yml`
   - Atualize `sonar.organization` com sua org do SonarCloud

### Executar localmente

```bash
./gradlew sonar \
  -Dsonar.projectKey=FelipeSchulzLuz_gestao-mensalidades-api \
  -Dsonar.organization=felipeschulzluz \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=SEU_TOKEN_AQUI
```

### Quality Gates

**Critérios padrão:**
- Coverage > 80%
- Duplicação < 3%
- Maintainability Rating: A
- Reliability Rating: A
- Security Rating: A

---

## 🔒 Security Scanning

### Trivy

**Análise de:**
- Vulnerabilidades em dependências
- Vulnerabilidades na imagem Docker
- Misconfigurations
- Secrets expostos

**Severidades verificadas:**
- 🔴 CRITICAL
- 🟠 HIGH
- 🟡 MEDIUM

### OWASP Dependency Check

**Verifica:**
- CVEs conhecidos nas dependências
- Fail build se CVSS ≥ 7.0

**Executar localmente:**

```bash
./gradlew dependencyCheckAnalyze
# Relatório em: build/reports/dependency-check-report.html
```

### CodeQL

Análise estática automática do GitHub com:
- Security queries
- Quality queries
- Extended ruleset

### TruffleHog

Detecta secrets expostos:
- API keys
- Tokens
- Senhas
- Chaves privadas

---

## 🐳 Docker

### Build Automático

O workflow CI builda e publica a imagem Docker automaticamente no push para `master` ou `develop`.

**Tags geradas:**
- `master` / `develop` (branch name)
- `master-abc1234` / `develop-abc1234` (branch-sha)
- `v1.0.0` (se tag semver)
- `buildcache` (layer caching)

### Build Manual

```bash
# Build local
docker build -t gestao-mensalidades-api:local .

# Run local
docker-compose up -d
```

### Scan de Segurança

```bash
# Scan com Trivy
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image gestao-mensalidades-api:latest

# Scan com Docker Scout (se disponível)
docker scout cves gestao-mensalidades-api:latest
```

---

## 🤖 Dependabot

O Dependabot está configurado para atualizar automaticamente:

### GitHub Actions
- **Frequência:** Semanal (segunda-feira, 9h)
- **Limite:** 5 PRs abertos simultaneamente

### Gradle Dependencies
- **Frequência:** Semanal (segunda-feira, 9h)
- **Limite:** 10 PRs abertos simultaneamente
- **Ignora:** Major updates do Spring Boot (requer revisão manual)

### Docker
- **Frequência:** Semanal (segunda-feira, 9h)
- **Limite:** 5 PRs abertos simultaneamente

**Labels automáticos:**
- `dependencies`
- `github-actions` / `java` / `docker`

**Commits:**
- Formato: `chore(deps): update dependency-name to v1.2.3`

---

## 📊 Badges

Adicione ao README.md:

```markdown
[![CI Pipeline](https://github.com/FelipeSchulzLuz/gestao-mensalidades-api/workflows/CI%20Pipeline/badge.svg)](https://github.com/FelipeSchulzLuz/gestao-mensalidades-api/actions/workflows/ci.yml)
[![Security Scanning](https://github.com/FelipeSchulzLuz/gestao-mensalidades-api/workflows/Security%20Scanning/badge.svg)](https://github.com/FelipeSchulzLuz/gestao-mensalidades-api/actions/workflows/security-scan.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=FelipeSchulzLuz_gestao-mensalidades-api&metric=alert_status)](https://sonarcloud.io/dashboard?id=FelipeSchulzLuz_gestao-mensalidades-api)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=FelipeSchulzLuz_gestao-mensalidades-api&metric=coverage)](https://sonarcloud.io/dashboard?id=FelipeSchulzLuz_gestao-mensalidades-api)
```

---

## 🚀 Próximos Passos

1. **Configurar SonarCloud**
   - Criar conta e importar projeto
   - Adicionar `SONAR_TOKEN` nos secrets

2. **Configurar Docker Hub**
   - Adicionar `DOCKER_USERNAME` nos secrets
   - Adicionar `DOCKER_PASSWORD` nos secrets

3. **Revisar Quality Gates**
   - Ajustar limites de cobertura se necessário
   - Customizar regras do SonarCloud

4. **Testar workflows**
   - Criar PR de teste
   - Verificar se todos os checks passam
   - Revisar relatórios de segurança

5. **Documentar no README principal**
   - Adicionar badges
   - Link para dashboards
   - Instruções de contribuição

---

## 📚 Referências

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [SonarCloud Documentation](https://docs.sonarcloud.io/)
- [Trivy Documentation](https://aquasecurity.github.io/trivy/)
- [OWASP Dependency Check](https://owasp.org/www-project-dependency-check/)
- [Docker Security Best Practices](https://docs.docker.com/develop/security-best-practices/)

---

**Última atualização:** 2025-10-07
**Versão:** 1.0
