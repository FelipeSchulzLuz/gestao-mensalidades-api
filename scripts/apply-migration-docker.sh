#!/bin/bash

# ==========================================
# Script: Aplicar Migration via Docker
# ==========================================
# Aplica a migration 002 diretamente no container Docker
# sem necessidade de psql instalado no host.
# ==========================================

set -e  # Exit on error

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║   🐳 APLICAR MIGRATION VIA DOCKER                          ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Find postgres container
CONTAINER=$(docker ps --format '{{.Names}}' | grep -i postgres | head -n 1)

if [ -z "$CONTAINER" ]; then
    echo "❌ Erro: Nenhum container PostgreSQL encontrado rodando!"
    echo ""
    echo "Containers disponíveis:"
    docker ps -a --format "  - {{.Names}} ({{.Status}})"
    echo ""
    echo "Dica: Execute 'docker-compose up -d postgres' primeiro"
    exit 1
fi

echo "📦 Container encontrado: $CONTAINER"
echo ""

# Read migration SQL
MIGRATION_SQL=$(cat database/migrations/002_add_firebase_auth_support.sql)

echo "🔄 Aplicando migration..."
echo ""

# Apply migration
echo "$MIGRATION_SQL" | docker exec -i "$CONTAINER" psql \
    -U gestao_app_user \
    -d gestao_mensalidades_dev

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Migration aplicada com sucesso!"
    echo ""
    
    # Verify
    echo "🔍 Verificando colunas..."
    docker exec -i "$CONTAINER" psql -U gestao_app_user -d gestao_mensalidades_dev -c "
    SELECT column_name, data_type, is_nullable 
    FROM information_schema.columns 
    WHERE table_name = 'users' 
    AND column_name IN ('password', 'firebase_uid', 'organization_id');"
    
    echo ""
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║   ✅ PROCESSO CONCLUÍDO!                                   ║"
    echo "╚════════════════════════════════════════════════════════════╝"
else
    echo ""
    echo "❌ Erro ao aplicar migration!"
    echo "   (Pode já estar aplicada)"
fi

echo ""
