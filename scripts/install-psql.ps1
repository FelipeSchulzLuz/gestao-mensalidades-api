# 🚀 Instalar PostgreSQL Client Tools (psql) no Windows

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  INSTALAR PostgreSQL Client (psql)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Existem 3 opções para instalar psql no Windows:" -ForegroundColor Yellow
Write-Host ""
Write-Host "  1. Via Chocolatey (RECOMENDADO - mais rápido)" -ForegroundColor Green
Write-Host "  2. Via Scoop (alternativa)" -ForegroundColor White
Write-Host "  3. Instalador oficial PostgreSQL (mais pesado)" -ForegroundColor White
Write-Host "  4. Cancelar" -ForegroundColor White
Write-Host ""

$choice = Read-Host "Digite sua escolha (1-4)"

switch ($choice) {
    "1" {
        # Chocolatey
        Write-Host ""
        Write-Host "📦 Instalando via Chocolatey..." -ForegroundColor Cyan
        Write-Host ""
        
        # Verificar se Chocolatey está instalado
        $chocoPath = Get-Command choco -ErrorAction SilentlyContinue
        
        if ($null -eq $chocoPath) {
            Write-Host "⚠️  Chocolatey não está instalado" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "Deseja instalar Chocolatey agora? (s/n)" -ForegroundColor Yellow
            $installChoco = Read-Host
            
            if ($installChoco -eq "s" -or $installChoco -eq "S") {
                Write-Host ""
                Write-Host "Instalando Chocolatey..." -ForegroundColor Cyan
                Write-Host "NOTA: Pode solicitar permissões de administrador" -ForegroundColor Yellow
                Write-Host ""
                
                # Instalar Chocolatey
                Set-ExecutionPolicy Bypass -Scope Process -Force
                [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
                Invoke-Expression ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
                
                Write-Host ""
                Write-Host "✅ Chocolatey instalado!" -ForegroundColor Green
                Write-Host ""
                
                # Recarregar PATH
                $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
            } else {
                Write-Host "Instalação cancelada. Escolha opção 2 ou 3." -ForegroundColor Yellow
                exit 0
            }
        } else {
            Write-Host "✅ Chocolatey já está instalado: $($chocoPath.Source)" -ForegroundColor Green
        }
        
        Write-Host ""
        Write-Host "Instalando PostgreSQL Client Tools..." -ForegroundColor Cyan
        Write-Host "NOTA: Pode solicitar permissões de administrador" -ForegroundColor Yellow
        Write-Host ""
        
        choco install postgresql --params '/Password:postgres' -y
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✅ PostgreSQL Client instalado com sucesso!" -ForegroundColor Green
            Write-Host ""
            Write-Host "IMPORTANTE: Feche e reabra o PowerShell para usar 'psql'" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "Depois execute:" -ForegroundColor Cyan
            Write-Host "  psql --version" -ForegroundColor White
            Write-Host ""
            Write-Host "E para aplicar a migration:" -ForegroundColor Cyan
            Write-Host "  `$env:PGPASSWORD = 'admin123'" -ForegroundColor White
            Write-Host "  psql -h 192.168.1.12 -p 54320 -U admin -d gestao_mensalidades -f database\migrations\002_add_firebase_auth_support.sql" -ForegroundColor White
        } else {
            Write-Host ""
            Write-Host "❌ Erro na instalação" -ForegroundColor Red
            exit 1
        }
    }
    
    "2" {
        # Scoop
        Write-Host ""
        Write-Host "📦 Instalando via Scoop..." -ForegroundColor Cyan
        Write-Host ""
        
        # Verificar se Scoop está instalado
        $scoopPath = Get-Command scoop -ErrorAction SilentlyContinue
        
        if ($null -eq $scoopPath) {
            Write-Host "⚠️  Scoop não está instalado" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "Deseja instalar Scoop agora? (s/n)" -ForegroundColor Yellow
            $installScoop = Read-Host
            
            if ($installScoop -eq "s" -or $installScoop -eq "S") {
                Write-Host ""
                Write-Host "Instalando Scoop..." -ForegroundColor Cyan
                Write-Host ""
                
                Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
                Invoke-RestMethod -Uri https://get.scoop.sh | Invoke-Expression
                
                Write-Host ""
                Write-Host "✅ Scoop instalado!" -ForegroundColor Green
                Write-Host ""
                
                # Recarregar PATH
                $env:Path = [System.Environment]::GetEnvironmentVariable("Path","User")
            } else {
                Write-Host "Instalação cancelada. Escolha opção 1 ou 3." -ForegroundColor Yellow
                exit 0
            }
        } else {
            Write-Host "✅ Scoop já está instalado: $($scoopPath.Source)" -ForegroundColor Green
        }
        
        Write-Host ""
        Write-Host "Instalando PostgreSQL Client..." -ForegroundColor Cyan
        Write-Host ""
        
        scoop install postgresql
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "✅ PostgreSQL Client instalado com sucesso!" -ForegroundColor Green
            Write-Host ""
            Write-Host "Testando instalação..." -ForegroundColor Cyan
            psql --version
            Write-Host ""
            Write-Host "Para aplicar a migration:" -ForegroundColor Cyan
            Write-Host "  `$env:PGPASSWORD = 'admin123'" -ForegroundColor White
            Write-Host "  psql -h 192.168.1.12 -p 54320 -U admin -d gestao_mensalidades -f database\migrations\002_add_firebase_auth_support.sql" -ForegroundColor White
        } else {
            Write-Host ""
            Write-Host "❌ Erro na instalação" -ForegroundColor Red
            exit 1
        }
    }
    
    "3" {
        # Instalador oficial
        Write-Host ""
        Write-Host "🌐 Baixando instalador oficial..." -ForegroundColor Cyan
        Write-Host ""
        Write-Host "PASSOS:" -ForegroundColor Yellow
        Write-Host "  1. Abrindo página de download do PostgreSQL..." -ForegroundColor White
        Write-Host "  2. Baixe a versão mais recente (recomendado: PostgreSQL 15)" -ForegroundColor White
        Write-Host "  3. Execute o instalador" -ForegroundColor White
        Write-Host "  4. Na tela de componentes, marque apenas:" -ForegroundColor White
        Write-Host "     - Command Line Tools (psql)" -ForegroundColor White
        Write-Host "  5. Desmarque:" -ForegroundColor White
        Write-Host "     - PostgreSQL Server (não precisa)" -ForegroundColor White
        Write-Host "     - pgAdmin (já tem DBeaver)" -ForegroundColor White
        Write-Host "  6. Conclua a instalação" -ForegroundColor White
        Write-Host ""
        Write-Host "Pressione Enter para abrir o navegador..."
        Read-Host
        
        Start-Process "https://www.postgresql.org/download/windows/"
        
        Write-Host ""
        Write-Host "Após instalar, feche e reabra o PowerShell" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Depois execute:" -ForegroundColor Cyan
        Write-Host "  psql --version" -ForegroundColor White
    }
    
    "4" {
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
Write-Host "  PRÓXIMOS PASSOS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Feche e reabra o PowerShell" -ForegroundColor Yellow
Write-Host ""
Write-Host "2. Teste se psql está funcionando:" -ForegroundColor Yellow
Write-Host "   psql --version" -ForegroundColor White
Write-Host ""
Write-Host "3. Aplique a migration:" -ForegroundColor Yellow
Write-Host "   `$env:PGPASSWORD = 'admin123'" -ForegroundColor White
Write-Host "   psql -h 192.168.1.12 -p 54320 -U admin -d gestao_mensalidades -f database\migrations\002_add_firebase_auth_support.sql" -ForegroundColor White
Write-Host ""
Write-Host "4. Execute a aplicação:" -ForegroundColor Yellow
Write-Host "   ./gradlew bootRun" -ForegroundColor White
Write-Host ""
Write-Host "✅ Script finalizado!" -ForegroundColor Green
