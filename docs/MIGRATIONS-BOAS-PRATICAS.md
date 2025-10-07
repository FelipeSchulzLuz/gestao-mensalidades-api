# 🎓 Boas Práticas: Database Migrations

> **"Migrations são o Git do seu banco de dados"**

---

## 🤔 A Grande Questão

### ❓ "Devo usar migrations mesmo sem dados importantes no banco?"

**RESPOSTA: SIM! Absolutamente!**

---

## ✅ Por que usar Migrations SEMPRE

### 1. **Histórico de Mudanças**

```
📚 Cada migration = commit do banco
✅ Sabe QUANDO mudou
✅ Sabe POR QUÊ mudou  
✅ Pode REVERTER se necessário
```

**Exemplo Real:**
```bash
# Daqui 2 meses:
$ git log --oneline database/migrations/

002_add_firebase_auth_support.sql  "feat: add Firebase authentication"
003_add_organizations.sql          "feat: add multi-tenancy support"
004_add_payment_history.sql        "feat: add payment audit trail"

# Você NUNCA vai esquecer o que mudou!
```

---

### 2. **Time Sincronizado**

**❌ SEM Migration:**
```
Dev 1: ALTER TABLE users ADD COLUMN firebase_uid ...
Dev 2: git pull
Dev 2: ./gradlew bootRun
💥 ERRO: column "firebase_uid" does not exist
Dev 2: "Oi, o que você mudou no banco?"
```

**✅ COM Migration:**
```
Dev 1: git add src/main/resources/db/migration/V002__*.sql
Dev 1: git push
Dev 2: git pull
Dev 2: make start  # equivalent to ./scripts/toolbox.sh start-with-flyway
✅ FUNCIONA!
```

---

### 3. **Deploy Seguro**

```
🖥️  Dev       → ✅ Migration 002 aplicada
🧪 Staging   → ✅ Migration 002 aplicada
🚀 Produção  → ✅ Migration 002 aplicada

Resultado: ZERO surpresas em produção! 🎯
```

---

### 4. **Documentação Automática**

```sql
-- database/migrations/002_add_firebase_auth_support.sql

-- POR QUÊ: Suportar login via Google/Email
-- QUANDO: 2024-10-05
-- AUTOR: Felipe
-- TICKET: FEAT-123

ALTER TABLE users ADD COLUMN firebase_uid VARCHAR(128);
```

**Benefício:** Você NÃO precisa lembrar depois!

---

## 🚫 O que NÃO fazer (Anti-patterns)

### ❌ Mudar schema direto no banco

```bash
# NUNCA FAÇA ISSO:
docker exec -it postgres psql ...
ALTER TABLE users ADD COLUMN firebase_uid ...
```

**Resultado:**
- ❌ Outro dev não tem a coluna → quebra
- ❌ Staging não tem a coluna → quebra
- ❌ Produção não tem a coluna → quebra
- ❌ Você esquece o que mudou → 💣

---

### ❌ "Ah, é só dev, não precisa migration"

```
Semana 1: "Não tem nada, posso dropar"
Semana 2: "Só dados de teste, tranquilo"
Semana 3: "Tem uns dados importantes mas..."
Semana 4: "CADÊ MEUS DADOS?!" 💥
```

**Lição:** Acostume com o workflow correto DESDE O INÍCIO!

---

## 📋 Workflow Profissional

### ✅ O que fazer sempre

```bash
# 1. Mudança no schema? Criar migration!
# database/migrations/003_add_user_phone.sql

# 2. Commitar ANTES do código que usa
git add database/migrations/003_add_user_phone.sql
git commit -m "feat: add phone column to users"
git push

# 3. Aplicar localmente
make start

# 4. Atualizar código Java
# User.java → private String phone;

# 5. Testar
./gradlew bootRun

# 6. Commitar código
git add src/
git commit -m "feat: implement phone field in User entity"
git push
```

---

## 🎯 Seu Caso: Firebase Auth Migration

### ✅ O que você FEZ CERTO:

1. ✅ Criou `002_add_firebase_auth_support.sql`
2. ✅ Adicionou no `docker-compose.yml`
3. ✅ Pronto para commitar

### 🎯 O que fazer AGORA:

```bash
# 1. Commitar migration
git add database/migrations/002_add_firebase_auth_support.sql
git add docker-compose.yml
git commit -m "feat: add Firebase authentication support - migration 002"

# 2. Aplicar localmente (recriar DB + migrations)
docker compose down -v
make start
# ✅ Rápido (cria DB limpo e aplica todas migrations)
# ✅ Estado limpo garantido

# 3. Testar aplicação (já rodando via wrapper)
docker compose logs -f app

# 4. Commitar código que USA a migration
git add src/
git commit -m "feat: implement hybrid JWT/Firebase authentication"

# 5. Push tudo
git push
```

---

## 🏆 Vantagens a Longo Prazo

### 📊 Comparação

**SEM Migrations:**
```
❌ Novo dev: "Como configuro o banco?"
❌ Staging quebrado: "Falta uma coluna"
❌ Produção: "Erro desconhecido"
❌ Você em 2 meses: "Por que essa coluna existe?"
```

**COM Migrations:**
```
✅ Novo dev: git clone → make start (or ./scripts/toolbox.sh start-with-flyway) → pronto!
✅ Staging: Sempre sincronizado
✅ Produção: Deploy previsível
✅ Documentação: Cada migration explica
```

---

## 🎓 Casos Extremos

### "E se eu quiser dropar tudo e recomeçar?"

**✅ COM migrations:**
```bash
docker compose down -v
make start
# ✅ Dropa tudo
# ✅ Recria com TODAS migrations via Flyway
# ✅ Estado consistente garantido
```

**❌ SEM migrations:**
```bash
# Você precisa LEMBRAR:
# - Quais tabelas existem?
# - Quais colunas cada uma tem?
# - Quais índices existem?
# - Quais constraints existem?
# 
# BOA SORTE! 💀
```

---

## 🚀 Ferramentas Pro (Futuro)

Quando o projeto crescer:

### Flyway (Recomendado para Spring Boot)

```gradle
// build.gradle
dependencies {
    implementation 'org.flywaydb:flyway-core'
}
```

**Vantagens:**
- ✅ Aplica migrations AUTOMATICAMENTE no `bootRun`
- ✅ Versionamento automático
- ✅ Rollback controlado
- ✅ Zero scripts manuais

**Como funciona:**
```
./gradlew bootRun
↓
Spring Boot inicia
↓
Flyway verifica database/migrations/
↓
Aplica apenas as NOVAS migrations
↓
Aplicação inicia
```

---

## 📝 Estrutura Recomendada

```
database/
├── init.sql                    ← Schema inicial (primeira vez)
└── migrations/
    ├── 001_initial_schema.sql
    ├── 002_add_firebase_auth_support.sql  ← Você está aqui
    ├── 003_add_organizations.sql         ← Próxima feature
    ├── 004_add_audit_logs.sql
    └── 005_add_payment_history.sql
```

### 🔢 Nomenclatura:

```
[NÚMERO]_[DESCRIÇÃO_CURTA].sql

✅ Bom:
- 002_add_firebase_auth_support.sql
- 003_create_organizations_table.sql
- 004_add_user_email_index.sql

❌ Ruim:
- migration.sql
- fix.sql
- update_2024.sql
- teste123.sql
```

---

## 🎯 Checklist: Para CADA Mudança no Banco

```
[ ] 1. Criar migration numerada sequencialmente
[ ] 2. Escrever SQL com BEGIN/COMMIT
[ ] 3. Adicionar comentários explicativos
[ ] 4. git add + commit migration
[ ] 5. git push
[ ] 6. Aplicar localmente: ./scripts/run-make.sh start
[ ] 7. Verificar: SELECT * FROM tabela LIMIT 1;
[ ] 8. Atualizar código Java
[ ] 9. Testar: ./gradlew bootRun
[ ] 10. git add + commit código
[ ] 11. git push
```

---

## 💡 Resposta Final

### ❓ "Não seria melhor não fazer migrations em dev?"

**NÃO! Porque:**

1. ✅ **Profissionalismo** - É assim que empresas sérias trabalham
2. ✅ **Zero surpresas** - Dev/Staging/Prod sempre iguais
3. ✅ **Onboarding rápido** - Novo dev pronto em 5 minutos
4. ✅ **Menos bugs** - Schema sempre consistente
5. ✅ **Histórico claro** - Nunca esquece mudanças
6. ✅ **Rollback fácil** - Reverter é simples

### ❓ "Mas não tem dados importantes..."

**Exatamente! Por isso:**
- ✅ Use `./scripts/run-make.sh start` ou `docker compose down -v && ./scripts/run-make.sh start` sem medo
- ✅ 15 segundos para banco limpo
- ✅ Todas migrations aplicadas
- ✅ Acostuma com workflow correto

---

## 🎯 Conclusão

### Use migrations em DEV? **SIM!**

### Quando NÃO usar?
- ❌ PoC descartável (1 dia)
- ❌ Script de uso único
- ❌ Nunca vai para produção
- ❌ Você é único dev para sempre

### Para SEU projeto? **USE MIGRATIONS! 🚀**

**Benefício:** Projeto profissional, escalável, sem dívida técnica!

---

## 📚 Recursos

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Liquibase vs Flyway](https://www.baeldung.com/liquibase-vs-flyway)
- [Database Migrations Best Practices](https://martinfowler.com/articles/evodb.html)

---

**✅ PRÓXIMO PASSO:**

```bash
# Commita e aplica! 🎯
git add src/main/resources/db/migration/V002__*.sql
git add docker-compose.yml
git commit -m "feat: add Firebase authentication support - migration 002"
git push

./scripts/run-make.sh start
# App já sobe automaticamente!
```

🎉 **Parabéns! Você está fazendo do jeito certo!**
