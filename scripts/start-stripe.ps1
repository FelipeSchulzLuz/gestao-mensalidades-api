# =============================================================================
# Script para Iniciar o Stripe CLI Listener (Máquina Local)
# =============================================================================
# Este script deve ser executado na sua máquina local, não no servidor Docker

param(
    [switch]$Help,
    [string]$ForwardTo = "localhost:8800/api/webhooks/stripe"
)

# Cores para output
$ErrorColor = "Red"
$SuccessColor = "Green"
$InfoColor = "Cyan"
$WarningColor = "Yellow"

function Show-Help {
    Write-Host "==============================================================================" -ForegroundColor $InfoColor
    Write-Host "  Stripe CLI Listener - Gestão de Mensalidades" -ForegroundColor $InfoColor
    Write-Host "==============================================================================" -ForegroundColor $InfoColor
    Write-Host ""
    Write-Host "Uso: .\start-stripe.ps1 [opções]" -ForegroundColor White
    Write-Host ""
    Write-Host "Opções:" -ForegroundColor White
    Write-Host "  -Help              Exibe esta mensagem de ajuda" -ForegroundColor Gray
    Write-Host "  -ForwardTo <url>   URL para onde encaminhar webhooks" -ForegroundColor Gray
    Write-Host "                     Padrão: localhost:8800/api/webhooks/stripe" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Exemplos:" -ForegroundColor White
    Write-Host "  .\start-stripe.ps1                                        # Usa padrão" -ForegroundColor Gray
    Write-Host "  .\start-stripe.ps1 -ForwardTo localhost:8080/api/webhooks/stripe   # Porta customizada" -ForegroundColor Gray
    Write-Host ""
    Write-Host "📖 Documentação completa: STRIPE_DESENVOLVIMENTO.md" -ForegroundColor Cyan
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

# =============================================================================
# MAIN
# =============================================================================

if ($Help) {
    Show-Help
}

Clear-Host

Write-Host ""
Write-Host "==============================================================================" -ForegroundColor $InfoColor
Write-Host "  Stripe CLI Listener - Webhooks para Desenvolvimento Local" -ForegroundColor $InfoColor
Write-Host "==============================================================================" -ForegroundColor $InfoColor
Write-Host ""

# Verificar se Stripe CLI está instalado
Write-Step "Verificando instalação do Stripe CLI..."

if (-not (Test-Command "stripe")) {
    Write-Error "Stripe CLI não está instalada!"
    Write-Host ""
    Write-Host "Para instalar no Windows:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  Opção 1: Usando Scoop (recomendado)" -ForegroundColor Cyan
    Write-Host "    1. Instale Scoop:" -ForegroundColor White
    Write-Host "       iwr -useb get.scoop.sh | iex" -ForegroundColor Gray
    Write-Host ""
    Write-Host "    2. Adicione o bucket do Stripe:" -ForegroundColor White
    Write-Host "       scoop bucket add stripe https://github.com/stripe/scoop-stripe-cli.git" -ForegroundColor Gray
    Write-Host ""
    Write-Host "    3. Instale o Stripe CLI:" -ForegroundColor White
    Write-Host "       scoop install stripe" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  Opção 2: Download direto" -ForegroundColor Cyan
    Write-Host "    https://github.com/stripe/stripe-cli/releases/latest" -ForegroundColor Gray
    Write-Host ""
    Write-Host "📖 Veja documentação completa em: STRIPE_DESENVOLVIMENTO.md" -ForegroundColor Cyan
    Write-Host ""
    exit 1
}

Write-Success "Stripe CLI instalado!"

# Verificar autenticação
Write-Step "Verificando autenticação..."

$stripeConfig = stripe config --list 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Error "Não autenticado no Stripe!"
    Write-Host ""
    Write-Host "Execute o comando:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  stripe login" -ForegroundColor White
    Write-Host ""
    Write-Host "Isso abrirá seu navegador para autenticação." -ForegroundColor Gray
    Write-Host ""
    exit 1
}

Write-Success "Autenticado no Stripe!"

# Exibir informações
Write-Host ""
Write-Host "==============================================================================" -ForegroundColor $SuccessColor
Write-Host "  Iniciando Stripe Listener" -ForegroundColor $SuccessColor
Write-Host "==============================================================================" -ForegroundColor $SuccessColor
Write-Host ""
Write-Host "Configurações:" -ForegroundColor White
Write-Host "  Encaminhar para: $ForwardTo" -ForegroundColor Gray
Write-Host "  Eventos:         Todos (*)" -ForegroundColor Gray
Write-Host "  Modo:            Thin Events (otimizado)" -ForegroundColor Gray
Write-Host ""
Write-Host "⚠ IMPORTANTE:" -ForegroundColor $WarningColor
Write-Host "  1. O webhook secret (whsec_xxxxx) será exibido abaixo" -ForegroundColor Yellow
Write-Host "  2. Copie e adicione ao arquivo .env:" -ForegroundColor Yellow
Write-Host "     STRIPE_WEBHOOK_SECRET=whsec_xxxxx" -ForegroundColor Gray
Write-Host "  3. Mantenha este terminal aberto durante o desenvolvimento" -ForegroundColor Yellow
Write-Host ""
Write-Host "Comandos úteis (em outro terminal):" -ForegroundColor White
Write-Host "  stripe trigger checkout.session.completed   # Simular checkout" -ForegroundColor Gray
Write-Host "  stripe trigger invoice.payment_succeeded    # Simular pagamento" -ForegroundColor Gray
Write-Host "  stripe trigger customer.subscription.deleted # Simular cancelamento" -ForegroundColor Gray
Write-Host ""
Write-Host "📖 Lista completa de eventos: STRIPE_DESENVOLVIMENTO.md" -ForegroundColor Cyan
Write-Host ""
Write-Host "==============================================================================" -ForegroundColor $InfoColor
Write-Host ""

# Iniciar o listener
Write-Host "Iniciando listener... (Pressione Ctrl+C para parar)" -ForegroundColor Green
Write-Host ""

# Executar stripe listen
stripe listen --thin-events '*' --forward-thin-to $ForwardTo

# Se chegou aqui, o listener foi interrompido
Write-Host ""
Write-Host "Stripe listener encerrado." -ForegroundColor $WarningColor
Write-Host ""
