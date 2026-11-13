#!/bin/bash
# ============================================
# Docker 镜像构建脚本
# ============================================

set -e

VERSION=${1:-latest}
REGISTRY=${2:-""}

echo "=========================================="
echo "Building Species Atlas Docker Images"
echo "Version: $VERSION"
echo "=========================================="

# 构建后端镜像
echo ""
echo "Building backend image..."
docker build -t species-atlas-server:$VERSION ./species-atlas-server

# 构建前端镜像
echo ""
echo "Building frontend image..."
docker build -t species-atlas-web:$VERSION ./species-atlas-web

echo ""
echo "=========================================="
echo "Build completed!"
echo "=========================================="

# 如果指定了镜像仓库，则推送
if [ -n "$REGISTRY" ]; then
    echo ""
    echo "Pushing images to registry: $REGISTRY"
    
    docker tag species-atlas-server:$VERSION $REGISTRY/species-atlas-server:$VERSION
    docker tag species-atlas-web:$VERSION $REGISTRY/species-atlas-web:$VERSION
    
    docker push $REGISTRY/species-atlas-server:$VERSION
    docker push $REGISTRY/species-atlas-web:$VERSION
    
    echo "Push completed!"
fi

echo ""
echo "Images:"
docker images | grep species-atlas
