# Script para aplicar migration 002 (Firebase Authentication Support)
# Data: 2025-10-05

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  APLICAR MIGRATION 002 - Firebase Auth" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configurações do banco (do .env ou padrão Docker)
$DB_HOST = "192.168.1.12"
$DB_PORT = "54320"
$DB_NAME = "gestao_mensalidades"
$DB_USER = "admin"
$DB_PASSWORD = "admin123"  # Ajuste se necessário

Write-Host "Configurações:" -ForegroundColor Yellow
Write-Host "  Host: $DB_HOST"
Write-Host "  Port: $DB_PORT"
Write-Host "  Database: $DB_NAME"
Write-Host "  User: $DB_USER"
Write-Host ""

# Verificar se o arquivo de migration existe
$MIGRATION_FILE = "database\migrations\002_add_firebase_auth_support.sql"

if (-Not (Test-Path $MIGRATION_FILE)) {
    Write-Host "❌ ERRO: Arquivo de migration não encontrado!" -ForegroundColor Red
    Write-Host "   Esperado em: $MIGRATION_FILE" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Arquivo de migration encontrado" -ForegroundColor Green
Write-Host ""

# Ler conteúdo do arquivo SQL
$SQL_CONTENT = Get-Content $MIGRATION_FILE -Raw

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  OPÇÕES DE APLICAÇÃO" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Escolha como deseja aplicar a migration:" -ForegroundColor Yellow
Write-Host ""
Write-Host "  1. Copiar SQL para clipboard (colar no DBeaver/pgAdmin)" -ForegroundColor White
Write-Host "  2. Usar psql (se instalado no PATH)" -ForegroundColor White
Write-Host "  3. Usar Docker exec (se PostgreSQL está no container)" -ForegroundColor White
Write-Host "  4. Tentar conectar via .NET (experimental)" -ForegroundColor White
Write-Host "  5. Cancelar" -ForegroundColor White
Write-Host ""

$choice = Read-Host "Digite sua escolha (1-5)"

switch ($choice) {
    "1" {
        # Copiar para clipboard
        Write-Host ""
        Write-Host "📋 Copiando SQL para clipboard..." -ForegroundColor Cyan
        
        $SQL_CONTENT | Set-Clipboard
        
        Write-Host "✅ SQL copiado para clipboard!" -ForegroundColor Green
        Write-Host ""
        Write-Host "PRÓXIMOS PASSOS:" -ForegroundColor Yellow
        Write-Host "  1. Abra DBeaver ou pgAdmin" -ForegroundColor White
        Write-Host "  2. Conecte ao banco: ${DB_HOST}:${DB_PORT}/${DB_NAME}" -ForegroundColor White
        Write-Host "  3. Abra um SQL Editor" -ForegroundColor White
        Write-Host "  4. Cole o SQL (Ctrl+V)" -ForegroundColor White
        Write-Host "  5. Execute (Ctrl+Enter ou F5)" -ForegroundColor White
        Write-Host ""
        Write-Host "Aguardando você aplicar a migration..." -ForegroundColor Yellow
        Write-Host "Pressione qualquer tecla quando terminar..."
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    }
    
    "2" {
        # Usar psql
        Write-Host ""
        Write-Host "🔧 Tentando usar psql..." -ForegroundColor Cyan
        
        # Verificar se psql está disponível
        $psqlPath = Get-Command psql -ErrorAction SilentlyContinue
        
        if ($null -eq $psqlPath) {
            Write-Host "❌ ERRO: psql não encontrado no PATH" -ForegroundColor Red
            Write-Host ""
            Write-Host "OPÇÕES:" -ForegroundColor Yellow
            Write-Host "  1. Instalar PostgreSQL Client Tools" -ForegroundColor White
            Write-Host "  2. Usar opção 1 (DBeaver) ou 3 (Docker)" -ForegroundColor White
            exit 1
        }
        
        Write-Host "✅ psql encontrado: $($psqlPath.Source)" -ForegroundColor Green
        Write-Host ""
        Write-Host "Executando migration..." -ForegroundColor Cyan
        
        $env:PGPASSWORD = $DB_PASSWORD
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f $MIGRATION_FILE
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✅ Migration aplicada com sucesso!" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "❌ Erro ao aplicar migration" -ForegroundColor Red
            exit 1
        }
    }
    
    "3" {
        # Usar Docker
        Write-Host ""
        Write-Host "🐳 Tentando usar Docker..." -ForegroundColor Cyan
        
        # Verificar se Docker está disponível
        $dockerPath = Get-Command docker -ErrorAction SilentlyContinue
        
        if ($null -eq $dockerPath) {
            Write-Host "❌ ERRO: Docker não encontrado" -ForegroundColor Red
            exit 1
        }
        
        Write-Host "✅ Docker encontrado" -ForegroundColor Green
        Write-Host ""
        Write-Host "Procurando container PostgreSQL..." -ForegroundColor Cyan
        
        # Tentar encontrar container PostgreSQL
        $containers = docker ps --filter "ancestor=postgres" --format "{{.Names}}" 2>$null
        
        if ($null -eq $containers -or $containers.Count -eq 0) {
            Write-Host "❌ Nenhum container PostgreSQL encontrado rodando" -ForegroundColor Red
            Write-Host ""
            Write-Host "Containers disponíveis:" -ForegroundColor Yellow
            docker ps --format "table {{.Names}}\t{{.Image}}"
            Write-Host ""
            $containerName = Read-Host "Digite o nome do container PostgreSQL (ou Enter para cancelar)"
            
            if ([string]::IsNullOrWhiteSpace($containerName)) {
                Write-Host "Cancelado" -ForegroundColor Yellow
                exit 0
            }
        } else {
            $containerName = $containers[0]
            Write-Host "✅ Container encontrado: $containerName" -ForegroundColor Green
        }
        
        Write-Host ""
        Write-Host "Copiando arquivo SQL para container..." -ForegroundColor Cyan
        docker cp $MIGRATION_FILE "${containerName}:/tmp/migration.sql"
        
        Write-Host "Executando migration..." -ForegroundColor Cyan
        docker exec -i $containerName psql -U $DB_USER -d $DB_NAME -f /tmp/migration.sql
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✅ Migration aplicada com sucesso!" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "❌ Erro ao aplicar migration" -ForegroundColor Red
            exit 1
        }
    }
    
    "4" {
        # Tentar via .NET
        Write-Host ""
        Write-Host "⚠️  EXPERIMENTAL: Aplicando via .NET (Npgsql)" -ForegroundColor Yellow
        Write-Host ""
        
        # Script C# inline
        $csharpCode = @"
using System;
using Npgsql;

public class MigrationApplier {
    public static void Main() {
        string connectionString = "Host=$DB_HOST;Port=$DB_PORT;Database=$DB_NAME;Username=$DB_USER;Password=$DB_PASSWORD";
        string sql = @"$SQL_CONTENT";
        
        try {
            using (var conn = new NpgsqlConnection(connectionString)) {
                conn.Open();
                Console.WriteLine("✅ Conectado ao banco");
                
                using (var cmd = new NpgsqlCommand(sql, conn)) {
                    cmd.ExecuteNonQuery();
                    Console.WriteLine("✅ Migration aplicada com sucesso!");
                }
            }
        } catch (Exception ex) {
            Console.WriteLine("❌ Erro: " + ex.Message);
            Environment.Exit(1);
        }
    }
}
"@
        
        Write-Host "Esta opção requer Npgsql instalado." -ForegroundColor Yellow
        Write-Host "Recomendamos usar opção 1 (DBeaver) ou 3 (Docker)" -ForegroundColor Yellow
    }
    
    "5" {
        Write-Host ""
        Write-Host "Operação cancelada" -ForegroundColor Yellow
        exit 0
    }
    
    default {
        Write-Host ""
        Write-Host "❌ Opção inválida" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  VERIFICAÇÃO PÓS-MIGRATION" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Execute esta query para verificar:" -ForegroundColor Yellow
Write-Host ""
Write-Host "SELECT " -ForegroundColor White
Write-Host "    column_name, " -ForegroundColor White
Write-Host "    data_type, " -ForegroundColor White
Write-Host "    is_nullable " -ForegroundColor White
Write-Host "FROM information_schema.columns " -ForegroundColor White
Write-Host "WHERE table_name = 'users' " -ForegroundColor White
Write-Host "AND column_name IN ('password', 'firebase_uid', 'organization_id');" -ForegroundColor White
Write-Host ""
Write-Host "Esperado:" -ForegroundColor Yellow
Write-Host "  - password: VARCHAR, YES (nullable)" -ForegroundColor White
Write-Host "  - firebase_uid: VARCHAR(128), YES (nullable)" -ForegroundColor White
Write-Host "  - organization_id: BIGINT, NO (not null)" -ForegroundColor White
Write-Host ""
Write-Host "✅ Script finalizado!" -ForegroundColor Green
