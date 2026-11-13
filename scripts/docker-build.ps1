# ============================================
# Docker 镜像构建脚本 (PowerShell)
# ============================================

param(
    [string]$Version = "latest",
    [string]$Registry = ""
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Building Species Atlas Docker Images" -ForegroundColor Cyan
Write-Host "Version: $Version" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 构建后端镜像
Write-Host ""
Write-Host "Building backend image..." -ForegroundColor Yellow
docker build -t "species-atlas-server:$Version" ./species-atlas-server
if ($LASTEXITCODE -ne 0) { exit 1 }

# 构建前端镜像
Write-Host ""
Write-Host "Building frontend image..." -ForegroundColor Yellow
docker build -t "species-atlas-web:$Version" ./species-atlas-web
if ($LASTEXITCODE -ne 0) { exit 1 }

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "Build completed!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green

# 如果指定了镜像仓库，则推送
if ($Registry) {
    Write-Host ""
    Write-Host "Pushing images to registry: $Registry" -ForegroundColor Yellow
    
    docker tag "species-atlas-server:$Version" "$Registry/species-atlas-server:$Version"
    docker tag "species-atlas-web:$Version" "$Registry/species-atlas-web:$Version"
    
    docker push "$Registry/species-atlas-server:$Version"
    docker push "$Registry/species-atlas-web:$Version"
    
    Write-Host "Push completed!" -ForegroundColor Green
}

Write-Host ""
Write-Host "Images:" -ForegroundColor Cyan
docker images | Select-String "species-atlas"
