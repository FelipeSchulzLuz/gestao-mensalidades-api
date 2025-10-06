#!/bin/bash

# ==========================================
# Script: Recriar Database com Todas Migrations
# ==========================================
# ATENÇÃO: Este script é DESTRUTIVO!
# Ele irá DROPAR o banco de dados atual e recriar do zero.
#
# Use quando:
# - Não tem dados importantes
# - Quer garantir estado limpo do banco
# - Quer aplicar todas migrations desde o início
# ==========================================

set -e  # Exit on error

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║   🗑️  RECRIAR DATABASE (DESTRUTIVO)                        ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo "⚠️  ATENÇÃO: Este script irá:"
echo "   1. Parar o container PostgreSQL"
echo "   2. Remover o volume do banco (APAGA TODOS OS DADOS!)"
echo "   3. Recriar do zero com todas migrations aplicadas"
echo ""

# Confirmation
read -p "Tem certeza? Digite 'SIM' para continuar: " confirmation

if [ "$confirmation" != "SIM" ]; then
    echo ""
    echo "❌ Operação cancelada."
    exit 0
fi

echo ""
echo "🔄 Iniciando processo de recriação..."
echo ""

# 1. Stop postgres container
echo "1️⃣  Parando container PostgreSQL..."
docker-compose stop postgres || true
echo "   ✅ Container parado"
echo ""

# 2. Remove container and volume
echo "2️⃣  Removendo container e volume..."
docker-compose rm -f postgres || true
docker volume rm gestao-mensalidades-api_postgres_data 2>/dev/null || true
echo "   ✅ Volume removido"
echo ""

# 3. Recreate with docker-compose
echo "3️⃣  Recriando PostgreSQL com migrations..."
docker-compose up -d postgres
echo "   ✅ Container criado"
echo ""

# 4. Wait for PostgreSQL to be ready
echo "4️⃣  Aguardando PostgreSQL ficar pronto..."
attempt=0
max_attempts=30

while [ $attempt -lt $max_attempts ]; do
    if docker-compose exec -T postgres pg_isready -U gestao_app_user -d gestao_mensalidades_dev &>/dev/null; then
        echo "   ✅ PostgreSQL está pronto!"
        break
    fi
    
    attempt=$((attempt + 1))
    echo "   ⏳ Tentativa $attempt/$max_attempts..."
    sleep 2
done

if [ $attempt -eq $max_attempts ]; then
    echo ""
    echo "❌ Timeout esperando PostgreSQL ficar pronto."
    echo "   Verifique os logs: docker-compose logs postgres"
    exit 1
fi

echo ""

# 5. Verify migrations applied
echo "5️⃣  Verificando se migrations foram aplicadas..."
echo ""

result=$(docker-compose exec -T postgres psql -U gestao_app_user -d gestao_mensalidades_dev -t -c "
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
    echo "✅ Migration aplicada com sucesso!"
    echo ""
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║   ✅ DATABASE RECRIADO COM SUCESSO!                        ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo ""
    echo "Próximos passos:"
    echo "  1. ./gradlew bootRun"
    echo "  2. Testar endpoints"
    echo ""
else
    echo "⚠️  AVISO: Coluna firebase_uid não encontrada!"
    echo "   Verifique se a migration foi aplicada corretamente."
    echo ""
fi

exit 0
