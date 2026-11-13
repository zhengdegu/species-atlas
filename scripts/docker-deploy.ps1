# ============================================
# Docker 部署脚本 (PowerShell)
# ============================================

param(
    [ValidateSet("up", "down", "restart", "logs", "ps", "clean")]
    [string]$Action = "up",
    
    [ValidateSet("dev", "prod")]
    [string]$Env = "prod",
    
    [string]$Service = ""
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Species Atlas Docker Deployment" -ForegroundColor Cyan
Write-Host "Action: $Action" -ForegroundColor Cyan
Write-Host "Environment: $Env" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 检查 .env 文件
if (-not (Test-Path ".env") -and $Env -eq "prod") {
    Write-Host "Warning: .env file not found!" -ForegroundColor Yellow
    Write-Host "Copying from .env.example..." -ForegroundColor Yellow
    Copy-Item ".env.example" ".env"
    Write-Host "Please edit .env file and run again." -ForegroundColor Red
    exit 1
}

$composeFile = if ($Env -eq "dev") { "docker-compose.dev.yml" } else { "docker-compose.yml" }

switch ($Action) {
    "up" {
        Write-Host "Starting services..." -ForegroundColor Yellow
        docker-compose -f $composeFile up -d
        Write-Host ""
        Write-Host "Services started!" -ForegroundColor Green
        docker-compose -f $composeFile ps
    }
    
    "down" {
        Write-Host "Stopping services..." -ForegroundColor Yellow
        docker-compose -f $composeFile down
        Write-Host "Services stopped!" -ForegroundColor Green
    }
    
    "restart" {
        Write-Host "Restarting services..." -ForegroundColor Yellow
        docker-compose -f $composeFile restart
        Write-Host "Services restarted!" -ForegroundColor Green
    }
    
    "logs" {
        if ($Service) {
            docker-compose -f $composeFile logs -f $Service
        } else {
            docker-compose -f $composeFile logs -f
        }
    }
    
    "ps" {
        docker-compose -f $composeFile ps
    }
    
    "clean" {
        Write-Host "Cleaning up..." -ForegroundColor Yellow
        docker-compose -f $composeFile down -v --remove-orphans
        docker system prune -f
        Write-Host "Cleanup completed!" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Green
