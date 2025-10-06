# Script simplificado para aplicar migration via Docker
# Usa o container PostgreSQL que já está rodando

Write-Host "🐳 Aplicando Migration 002 via Docker..." -ForegroundColor Cyan
Write-Host ""

# Encontrar container PostgreSQL
Write-Host "Procurando containers PostgreSQL rodando..." -ForegroundColor Yellow
$containers = docker ps --format "{{.Names}}" | Select-String -Pattern "postgres|pg|db"

if ($containers.Count -eq 0) {
    Write-Host "❌ Nenhum container PostgreSQL encontrado" -ForegroundColor Red
    Write-Host ""
    Write-Host "Containers rodando:" -ForegroundColor Yellow
    docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Ports}}"
    Write-Host ""
    Write-Host "Use o script apply-migration-002.ps1 e escolha opção 1 (DBeaver)" -ForegroundColor Yellow
    exit 1
}

$containerName = $containers[0].ToString()
Write-Host "✅ Container encontrado: $containerName" -ForegroundColor Green
Write-Host ""

# Configurações
$DB_NAME = "gestao_mensalidades"
$DB_USER = "admin"

# Ler SQL
$SQL_FILE = "database\migrations\002_add_firebase_auth_support.sql"
$SQL_CONTENT = Get-Content $SQL_FILE -Raw

# Executar no container
Write-Host "Executando migration..." -ForegroundColor Cyan
Write-Host ""

$SQL_CONTENT | docker exec -i $containerName psql -U $DB_USER -d $DB_NAME

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Migration aplicada com sucesso!" -ForegroundColor Green
    Write-Host ""
    
    # Verificar colunas
    Write-Host "Verificando colunas criadas..." -ForegroundColor Cyan
    $verifySQL = @"
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'users' 
AND column_name IN ('password', 'firebase_uid', 'organization_id')
ORDER BY ordinal_position;
"@
    
    $verifySQL | docker exec -i $containerName psql -U $DB_USER -d $DB_NAME -t
    
    Write-Host ""
    Write-Host "✅ Pronto! Agora você pode rodar: ./gradlew bootRun" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "❌ Erro ao aplicar migration" -ForegroundColor Red
    Write-Host ""
    Write-Host "Tente usar o script apply-migration-002.ps1 e escolher opção 1 (DBeaver)" -ForegroundColor Yellow
    exit 1
}
