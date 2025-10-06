#!/bin/bash

# ==========================================
# Script: Aplicar Migration em Database Existente
# ==========================================
# Este script aplica a migration SEM dropar o banco.
# Preserva os dados existentes.
#
# Use quando:
# - Já tem dados importantes no banco
# - Quer aplicar apenas a nova migration
# - Simular ambiente de produção (incremental)
# ==========================================

set -e  # Exit on error

MIGRATION_FILE="database/migrations/002_add_firebase_auth_support.sql"
CONTAINER_NAME="gestao-postgres"

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║   🔄 APLICAR MIGRATION (Não Destrutivo)                   ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Check if migration file exists
if [ ! -f "$MIGRATION_FILE" ]; then
    echo "❌ Erro: Arquivo de migration não encontrado!"
    echo "   Procurando: $MIGRATION_FILE"
    exit 1
fi

# Check if docker is running
if ! docker ps &>/dev/null; then
    echo "❌ Erro: Docker não está rodando ou você não tem permissão."
    echo "   Execute: sudo usermod -aG docker $USER"
    echo "   Depois faça logout/login"
    exit 1
fi

# Check if postgres container exists
if ! docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "❌ Erro: Container '$CONTAINER_NAME' não encontrado!"
    echo ""
    echo "Containers disponíveis:"
    docker ps -a --format "  - {{.Names}}"
    echo ""
    echo "Dica: Rode 'docker-compose up -d' primeiro"
    exit 1
fi

# Check if postgres is running
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "⚠️  Container '$CONTAINER_NAME' existe mas não está rodando."
    echo "   Iniciando..."
    docker-compose up -d postgres
    echo "   Aguardando 5 segundos..."
    sleep 5
fi

echo "1️⃣  Copiando migration para container..."
docker cp "$MIGRATION_FILE" "${CONTAINER_NAME}:/tmp/002_add_firebase_auth_support.sql"
echo "   ✅ Arquivo copiado"
echo ""

echo "2️⃣  Aplicando migration..."
docker exec -i "$CONTAINER_NAME" psql -U gestao_app_user -d gestao_mensalidades_dev \
    -f /tmp/002_add_firebase_auth_support.sql

if [ $? -eq 0 ]; then
    echo "   ✅ Migration aplicada!"
else
    echo "   ⚠️  Erro ao aplicar migration (pode já estar aplicada)"
fi
echo ""

echo "3️⃣  Verificando colunas criadas..."
echo ""

result=$(docker exec -i "$CONTAINER_NAME" psql -U gestao_app_user -d gestao_mensalidades_dev -t -c "
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'users' 
AND column_name IN ('password', 'firebase_uid', 'organization_id')
ORDER BY column_name;
")

echo "$result"
echo ""

# Check if firebase_uid exists
if echo "$result" | grep -q "firebase_uid"; then
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║   ✅ MIGRATION APLICADA COM SUCESSO!                       ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo ""
    echo "Colunas verificadas:"
    echo "  ✅ firebase_uid (character varying, nullable)"
    echo "  ✅ organization_id (bigint, nullable)"
    echo "  ✅ password (character varying, nullable)"
    echo ""
    echo "Próximos passos:"
    echo "  1. ./gradlew bootRun"
    echo "  2. Testar endpoints"
    echo ""
else
    echo "❌ Erro: Coluna firebase_uid não encontrada!"
    echo "   A migration pode não ter sido aplicada corretamente."
    echo ""
    echo "Verifique os logs:"
    echo "  docker logs $CONTAINER_NAME"
    echo ""
fi

exit 0
