-- =====================================================
-- Script de Inicialização do Banco de Dados
-- Sistema de Gestão de Mensalidades
-- =====================================================

-- Este script cria o banco de dados e usuário para a aplicação
-- Execute como superuser (postgres)

-- =====================================================
-- 1. CRIAR BANCO DE DADOS DE DESENVOLVIMENTO
-- =====================================================

-- Criar banco de desenvolvimento
CREATE DATABASE gestao_mensalidades_dev
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'pt_BR.UTF-8'
    LC_CTYPE = 'pt_BR.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    TEMPLATE = template0;

COMMENT ON DATABASE gestao_mensalidades_dev 
    IS 'Banco de dados de desenvolvimento do Sistema de Gestão de Mensalidades';

-- =====================================================
-- 2. CRIAR USUÁRIO DA APLICAÇÃO (Opcional - Produção)
-- =====================================================

-- Para produção, é recomendado criar um usuário específico
-- Em desenvolvimento, pode usar o usuário postgres padrão

-- Descomentar para criar usuário específico:
/*
CREATE USER gestao_app_user WITH
    LOGIN
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    NOINHERIT
    NOREPLICATION
    CONNECTION LIMIT -1
    PASSWORD 'sua_senha_forte_aqui';

-- Conceder privilégios ao usuário
GRANT CONNECT ON DATABASE gestao_mensalidades_dev TO gestao_app_user;
GRANT USAGE ON SCHEMA public TO gestao_app_user;
GRANT CREATE ON SCHEMA public TO gestao_app_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO gestao_app_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO gestao_app_user;

-- Garantir privilégios em objetos futuros
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL PRIVILEGES ON TABLES TO gestao_app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT ALL PRIVILEGES ON SEQUENCES TO gestao_app_user;
*/

-- =====================================================
-- 3. CONECTAR AO BANCO E CRIAR EXTENSÕES
-- =====================================================

-- Conectar ao banco
\c gestao_mensalidades_dev

-- Criar extensão para UUID (caso necessário no futuro)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Criar extensão para full-text search em português
CREATE EXTENSION IF NOT EXISTS "unaccent";

-- =====================================================
-- 4. CONFIGURAÇÕES DO BANCO
-- =====================================================

-- Configurar timezone
ALTER DATABASE gestao_mensalidades_dev SET timezone TO 'America/Sao_Paulo';

-- =====================================================
-- 5. CRIAR BANCO DE TESTE (Opcional)
-- =====================================================

-- Para testes automatizados
CREATE DATABASE gestao_mensalidades_test
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'pt_BR.UTF-8'
    LC_CTYPE = 'pt_BR.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    TEMPLATE = template0;

COMMENT ON DATABASE gestao_mensalidades_test 
    IS 'Banco de dados de testes do Sistema de Gestão de Mensalidades';

-- =====================================================
-- 6. CRIAR BANCO DE PRODUÇÃO
-- =====================================================

-- Para produção (executar apenas em ambiente produtivo)
/*
CREATE DATABASE gestao_mensalidades_prod
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'pt_BR.UTF-8'
    LC_CTYPE = 'pt_BR.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = 100
    TEMPLATE = template0;

COMMENT ON DATABASE gestao_mensalidades_prod 
    IS 'Banco de dados de produção do Sistema de Gestão de Mensalidades';

-- Configurar timezone
ALTER DATABASE gestao_mensalidades_prod SET timezone TO 'America/Sao_Paulo';
*/

-- =====================================================
-- 7. VERIFICAÇÃO
-- =====================================================

-- Listar bancos de dados criados
SELECT datname, encoding, datcollate, datctype 
FROM pg_database 
WHERE datname LIKE 'gestao_mensalidades%';

-- Verificar extensões instaladas
\dx

-- =====================================================
-- NOTAS IMPORTANTES
-- =====================================================

/*
1. BACKUP E RESTORE:
   
   Backup:
   pg_dump -U postgres -d gestao_mensalidades_prod > backup.sql
   
   Restore:
   psql -U postgres -d gestao_mensalidades_prod < backup.sql

2. MONITORAMENTO:
   
   -- Ver conexões ativas
   SELECT * FROM pg_stat_activity WHERE datname = 'gestao_mensalidades_dev';
   
   -- Ver tamanho do banco
   SELECT pg_size_pretty(pg_database_size('gestao_mensalidades_dev'));
   
   -- Ver tabelas e tamanhos
   SELECT schemaname, tablename, 
          pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
   FROM pg_tables 
   WHERE schemaname = 'public'
   ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

3. MANUTENÇÃO:
   
   -- Vacuum (limpeza)
   VACUUM ANALYZE;
   
   -- Reindexação
   REINDEX DATABASE gestao_mensalidades_dev;

4. SEGURANÇA:
   
   - Use senhas fortes para usuários
   - Limite conexões por IP no pg_hba.conf
   - Use SSL em produção
   - Faça backup regularmente
   - Mantenha o PostgreSQL atualizado

5. PERFORMANCE:
   
   - Configure shared_buffers (25% da RAM)
   - Configure work_mem adequadamente
   - Use connection pooling (HikariCP já configurado)
   - Monitore queries lentas com pg_stat_statements
*/

-- =====================================================
-- FIM DO SCRIPT
-- =====================================================
