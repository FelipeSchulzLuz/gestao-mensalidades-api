# Script para recriar o banco de dados com as migrations
# ATENÇÃO: Isso vai APAGAR todos os dados!

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RECRIAR BANCO COM MIGRATIONS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "⚠️  ATENÇÃO: Esta operação irá:" -ForegroundColor Yellow
Write-Host "   - Parar o container PostgreSQL" -ForegroundColor White
Write-Host "   - APAGAR todos os dados do banco" -ForegroundColor Red
Write-Host "   - Recriar o banco com todas as migrations" -ForegroundColor White
Write-Host ""

$confirm = Read-Host "Tem certeza? Digite 'SIM' para confirmar"

if ($confirm -ne "SIM") {
    Write-Host ""
    Write-Host "Operação cancelada" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "🛑 Parando container PostgreSQL..." -ForegroundColor Cyan
docker-compose stop postgres

Write-Host ""
Write-Host "🗑️  Removendo container e volume..." -ForegroundColor Cyan
docker-compose rm -f postgres
docker volume rm gestao-mensalidades-api_postgres_data

Write-Host ""
Write-Host "🚀 Recriando container com migrations..." -ForegroundColor Cyan
docker-compose up -d postgres

Write-Host ""
Write-Host "⏳ Aguardando PostgreSQL ficar pronto..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "🔍 Verificando se migrations foram aplicadas..." -ForegroundColor Cyan
Write-Host ""

# Aguardar postgres ficar healthy
$maxAttempts = 30
$attempt = 0
$isHealthy = $false

while ($attempt -lt $maxAttempts -and -not $isHealthy) {
    $attempt++
    $status = docker inspect --format='{{.State.Health.Status}}' gestao-postgres 2>$null
    
    if ($status -eq "healthy") {
        $isHealthy = $true
        Write-Host "✅ PostgreSQL está pronto!" -ForegroundColor Green
    } else {
        Write-Host "Aguardando... ($attempt/$maxAttempts)" -ForegroundColor Yellow
        Start-Sleep -Seconds 2
    }
}

if (-not $isHealthy) {
    Write-Host ""
    Write-Host "❌ PostgreSQL não ficou pronto. Verifique os logs:" -ForegroundColor Red
    Write-Host "   docker-compose logs postgres" -ForegroundColor White
    exit 1
}

Write-Host ""
Write-Host "Verificando colunas criadas..." -ForegroundColor Cyan

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
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  ✅ BANCO RECRIADO COM SUCESSO!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Próximos passos:" -ForegroundColor Yellow
Write-Host "  1. Configurar Firebase (se ainda não configurou)" -ForegroundColor White
Write-Host "  2. Rodar aplicação: ./gradlew bootRun" -ForegroundColor White
Write-Host ""
