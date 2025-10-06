# Script para aplicar migration no banco EXISTENTE via Docker
# NÃO apaga dados existentes

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  APLICAR MIGRATION VIA DOCKER" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "📋 Copiando arquivo SQL para o container..." -ForegroundColor Cyan
docker cp database\migrations\002_add_firebase_auth_support.sql gestao-postgres:/tmp/migration.sql

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "❌ Erro ao copiar arquivo. Container PostgreSQL está rodando?" -ForegroundColor Red
    Write-Host ""
    Write-Host "Verifique:" -ForegroundColor Yellow
    Write-Host "  docker ps | Select-String postgres" -ForegroundColor White
    exit 1
}

Write-Host "✅ Arquivo copiado!" -ForegroundColor Green
Write-Host ""

Write-Host "🚀 Executando migration..." -ForegroundColor Cyan
Write-Host ""

docker exec -i gestao-postgres psql -U gestao_app_user -d gestao_mensalidades_dev -f /tmp/migration.sql

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  ✅ MIGRATION APLICADA COM SUCESSO!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    
    Write-Host "Verificando colunas criadas..." -ForegroundColor Cyan
    Write-Host ""
    
    docker exec -i gestao-postgres psql -U gestao_app_user -d gestao_mensalidades_dev -c "
    SELECT 
        column_name, 
        data_type, 
        is_nullable,
        character_maximum_length
    FROM information_schema.columns 
    WHERE table_name = 'users' 
    AND column_name IN ('password', 'firebase_uid', 'organization_id')
    ORDER BY column_name;
    "
    
    Write-Host ""
    Write-Host "✅ Pronto! Agora você pode rodar: ./gradlew bootRun" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "❌ Erro ao aplicar migration" -ForegroundColor Red
    Write-Host ""
    Write-Host "Possíveis causas:" -ForegroundColor Yellow
    Write-Host "  - Migration já foi aplicada antes" -ForegroundColor White
    Write-Host "  - Coluna já existe" -ForegroundColor White
    Write-Host ""
    Write-Host "Para ver detalhes do erro, verifique os logs acima" -ForegroundColor Yellow
    exit 1
}
