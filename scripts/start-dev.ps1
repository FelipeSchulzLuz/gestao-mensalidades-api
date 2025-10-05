# =============================================================================
# Script de Inicialização do Ambiente de Desenvolvimento
# =============================================================================
# Este script automatiza o início de todos os serviços necessários para desenvolvimento

param(
    [switch]$Help
)

# Cores para output
$ErrorColor = "Red"
$SuccessColor = "Green"
$InfoColor = "Cyan"
$WarningColor = "Yellow"

function Show-Help {
    Write-Host "==============================================================================" -ForegroundColor $InfoColor
    Write-Host "  Script de Inicialização - Gestão de Mensalidades API" -ForegroundColor $InfoColor
    Write-Host "==============================================================================" -ForegroundColor $InfoColor
    Write-Host ""
    Write-Host "Uso: .\start-dev.ps1 [opções]" -ForegroundColor White
    Write-Host ""
    Write-Host "Opções:" -ForegroundColor White
    Write-Host "  -Help         Exibe esta mensagem de ajuda" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Descrição:" -ForegroundColor White
    Write-Host "  Este script inicia os containers Docker no servidor (192.168.1.12)" -ForegroundColor Gray
    Write-Host "  PostgreSQL, pgAdmin e Redis serão iniciados." -ForegroundColor Gray
    Write-Host ""
    Write-Host "  O Stripe CLI deve ser rodado SEPARADAMENTE na sua máquina local." -ForegroundColor Gray
    Write-Host "  Veja instruções abaixo ou em STRIPE_DESENVOLVIMENTO.md" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Exemplo:" -ForegroundColor White
    Write-Host "  .\start-dev.ps1                 # Inicia containers Docker" -ForegroundColor Gray
    Write-Host ""
    exit 0
}

function Write-Step {
    param([string]$Message)
    Write-Host "`n==> $Message" -ForegroundColor $InfoColor
}

function Write-Success {
    param([string]$Message)
    Write-Host "✓ $Message" -ForegroundColor $SuccessColor
}

function Write-Error {
    param([string]$Message)
    Write-Host "✗ $Message" -ForegroundColor $ErrorColor
}

function Write-Warning {
    param([string]$Message)
    Write-Host "⚠ $Message" -ForegroundColor $WarningColor
}

function Test-Command {
    param([string]$Command)
    $exists = $null -ne (Get-Command $Command -ErrorAction SilentlyContinue)
    return $exists
}

function Start-DockerServices {
    Write-Step "Iniciando serviços Docker..."
    
    # Verificar se Docker está instalado
    if (-not (Test-Command "docker")) {
        Write-Error "Docker não está instalado!"
        Write-Host "Instale o Docker Desktop: https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
        return $false
    }
    
    # Verificar se Docker está rodando
    try {
        docker ps | Out-Null
    } catch {
        Write-Error "Docker não está rodando!"
        Write-Host "Inicie o Docker Desktop e tente novamente." -ForegroundColor Yellow
        return $false
    }
    
    # Verificar se arquivo .env existe
    if (-not (Test-Path ".env")) {
        Write-Warning "Arquivo .env não encontrado!"
        if (Test-Path ".env.docker") {
            Write-Host "Copiando .env.docker para .env..." -ForegroundColor Yellow
            Copy-Item ".env.docker" ".env"
            Write-Success "Arquivo .env criado"
        } else {
            Write-Error "Arquivo .env.docker também não encontrado!"
            return $false
        }
    }
    
    # Iniciar containers
    Write-Host "Iniciando containers (PostgreSQL, pgAdmin, Redis)..." -ForegroundColor White
    docker compose up -d
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Containers iniciados com sucesso!"
        
        # Aguardar alguns segundos para os serviços subirem
        Write-Host "Aguardando serviços iniciarem..." -ForegroundColor Gray
        Start-Sleep -Seconds 5
        
        # Verificar status dos containers
        Write-Host "`nStatus dos containers:" -ForegroundColor White
        docker compose ps
        
        return $true
    } else {
        Write-Error "Erro ao iniciar containers!"
        return $false
    }
}

function Test-DockerHealth {
    Write-Step "Verificando saúde dos serviços..."
    
    # PostgreSQL
    Write-Host "PostgreSQL (54320)..." -ForegroundColor White -NoNewline
    $pgTest = Test-NetConnection -ComputerName "192.168.1.12" -Port 54320 -WarningAction SilentlyContinue -ErrorAction SilentlyContinue
    if ($pgTest.TcpTestSucceeded) {
        Write-Host " ✓" -ForegroundColor $SuccessColor
    } else {
        Write-Host " ✗" -ForegroundColor $ErrorColor
    }
    
    # pgAdmin
    Write-Host "pgAdmin (8800)..." -ForegroundColor White -NoNewline
    $pgadminTest = Test-NetConnection -ComputerName "192.168.1.12" -Port 8800 -WarningAction SilentlyContinue -ErrorAction SilentlyContinue
    if ($pgadminTest.TcpTestSucceeded) {
        Write-Host " ✓" -ForegroundColor $SuccessColor
    } else {
        Write-Host " ✗" -ForegroundColor $ErrorColor
    }
    
    # Redis
    Write-Host "Redis (63790)..." -ForegroundColor White -NoNewline
    $redisTest = Test-NetConnection -ComputerName "192.168.1.12" -Port 63790 -WarningAction SilentlyContinue -ErrorAction SilentlyContinue
    if ($redisTest.TcpTestSucceeded) {
        Write-Host " ✓" -ForegroundColor $SuccessColor
    } else {
        Write-Host " ✗" -ForegroundColor $ErrorColor
    }
}

function Show-StripeInstructions {
    Write-Step "Instruções do Stripe CLI (sua máquina local)"
    
    Write-Host "O Stripe CLI deve ser executado em um terminal SEPARADO na sua máquina local." -ForegroundColor Yellow
    Write-Host ""
    
    # Verificar se Stripe CLI está instalado
    if (-not (Test-Command "stripe")) {
        Write-Warning "Stripe CLI não está instalada na máquina local!"
        Write-Host ""
        Write-Host "Para instalar (Windows):" -ForegroundColor Cyan
        Write-Host "  1. Instale Scoop: iwr -useb get.scoop.sh | iex" -ForegroundColor Gray
        Write-Host "  2. Adicione bucket: scoop bucket add stripe https://github.com/stripe/scoop-stripe-cli.git" -ForegroundColor Gray
        Write-Host "  3. Instale: scoop install stripe" -ForegroundColor Gray
        Write-Host ""
        Write-Host "Ou baixe de: https://github.com/stripe/stripe-cli/releases/latest" -ForegroundColor Gray
    } else {
        Write-Success "Stripe CLI detectada na máquina local!"
        
        # Verificar autenticação
        $stripeConfig = stripe config --list 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Warning "Não autenticado no Stripe!"
            Write-Host "Execute primeiro: stripe login" -ForegroundColor Yellow
        } else {
            Write-Success "Autenticado no Stripe!"
        }
    }
    
    Write-Host ""
    Write-Host "Passos para iniciar o Stripe CLI:" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  1. Abra um novo terminal PowerShell" -ForegroundColor White
    Write-Host "  2. Execute o comando:" -ForegroundColor White
    Write-Host ""
    Write-Host "     stripe listen --thin-events '*' --forward-thin-to localhost:8800/api/webhooks/stripe" -ForegroundColor Green
    Write-Host ""
    Write-Host "  3. Copie o webhook secret (whsec_xxxxx) que aparece" -ForegroundColor White
    Write-Host "  4. Adicione ao arquivo .env:" -ForegroundColor White
    Write-Host "     STRIPE_WEBHOOK_SECRET=whsec_xxxxx" -ForegroundColor Gray
    Write-Host ""
    Write-Host "⚠ IMPORTANTE: Mantenha o terminal do Stripe aberto durante o desenvolvimento!" -ForegroundColor $WarningColor
    Write-Host ""
    Write-Host "📖 Documentação completa: STRIPE_DESENVOLVIMENTO.md" -ForegroundColor Cyan
    Write-Host ""
}

function Show-AccessInfo {
    Write-Host ""
    Write-Host "==============================================================================" -ForegroundColor $SuccessColor
    Write-Host "  Ambiente de Desenvolvimento Iniciado!" -ForegroundColor $SuccessColor
    Write-Host "==============================================================================" -ForegroundColor $SuccessColor
    Write-Host ""
    Write-Host "Acesse os serviços:" -ForegroundColor White
    Write-Host ""
    Write-Host "  PostgreSQL:" -ForegroundColor Cyan
    Write-Host "    Host:     192.168.1.12:54320" -ForegroundColor Gray
    Write-Host "    Database: gestao_mensalidades_dev" -ForegroundColor Gray
    Write-Host "    User:     gestao_app_user" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  pgAdmin (Interface Visual):" -ForegroundColor Cyan
    Write-Host "    URL:      http://192.168.1.12:8800" -ForegroundColor Gray
    Write-Host "    Email:    admin@gestao.local" -ForegroundColor Gray
    Write-Host "    Senha:    (veja CREDENCIAIS.md)" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  Redis:" -ForegroundColor Cyan
    Write-Host "    Host:     192.168.1.12:63790" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  API Spring Boot (quando rodando):" -ForegroundColor Cyan
    Write-Host "    URL:      http://192.168.1.12:8800/api" -ForegroundColor Gray
    Write-Host "    Health:   http://192.168.1.12:8800/api/actuator/health" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Documentação:" -ForegroundColor White
    Write-Host "  • DOCKER_GUIA.md         - Guia completo do Docker" -ForegroundColor Gray
    Write-Host "  • STRIPE_DESENVOLVIMENTO.md - Guia do Stripe CLI" -ForegroundColor Gray
    Write-Host "  • CREDENCIAIS.md         - Credenciais e senhas" -ForegroundColor Gray
    Write-Host "  • PORTAS_DOCKER.md       - Referência de portas" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Comandos úteis:" -ForegroundColor White
    Write-Host "  docker compose logs -f       # Ver logs em tempo real" -ForegroundColor Gray
    Write-Host "  docker compose ps            # Status dos containers" -ForegroundColor Gray
    Write-Host "  docker compose stop          # Parar containers" -ForegroundColor Gray
    Write-Host "  docker compose restart       # Reiniciar containers" -ForegroundColor Gray
    Write-Host ""
}

# =============================================================================
# MAIN
# =============================================================================

if ($Help) {
    Show-Help
}

Clear-Host

Write-Host ""
Write-Host "==============================================================================" -ForegroundColor $InfoColor
Write-Host "  Gestão de Mensalidades - Ambiente de Desenvolvimento" -ForegroundColor $InfoColor
Write-Host "==============================================================================" -ForegroundColor $InfoColor
Write-Host ""

# Verificar se está no diretório correto
if (-not (Test-Path "docker-compose.yml")) {
    Write-Error "Arquivo docker-compose.yml não encontrado!"
    Write-Host "Execute este script da raiz do projeto." -ForegroundColor Yellow
    exit 1
}

# Iniciar Docker no servidor
$dockerSuccess = Start-DockerServices

if ($dockerSuccess) {
    Test-DockerHealth
    Show-AccessInfo
    Show-StripeInstructions
}

Write-Host "Pronto para desenvolvimento! 🚀" -ForegroundColor $SuccessColor
Write-Host ""
