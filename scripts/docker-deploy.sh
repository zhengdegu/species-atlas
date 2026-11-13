#!/bin/bash
# ============================================
# Docker 部署脚本
# ============================================

set -e

ACTION=${1:-up}
ENV=${2:-prod}

echo "=========================================="
echo "Species Atlas Docker Deployment"
echo "Action: $ACTION"
echo "Environment: $ENV"
echo "=========================================="

# 检查 .env 文件
if [ ! -f .env ] && [ "$ENV" = "prod" ]; then
    echo "Warning: .env file not found!"
    echo "Copying from .env.example..."
    cp .env.example .env
    echo "Please edit .env file and run again."
    exit 1
fi

case $ACTION in
    up)
        echo "Starting services..."
        if [ "$ENV" = "dev" ]; then
            docker-compose -f docker-compose.dev.yml up -d
        else
            docker-compose up -d
        fi
        echo ""
        echo "Services started!"
        docker-compose ps
        ;;
        
    down)
        echo "Stopping services..."
        if [ "$ENV" = "dev" ]; then
            docker-compose -f docker-compose.dev.yml down
        else
            docker-compose down
        fi
        echo "Services stopped!"
        ;;
        
    restart)
        echo "Restarting services..."
        if [ "$ENV" = "dev" ]; then
            docker-compose -f docker-compose.dev.yml restart
        else
            docker-compose restart
        fi
        echo "Services restarted!"
        ;;
        
    logs)
        SERVICE=${3:-""}
        if [ "$ENV" = "dev" ]; then
            docker-compose -f docker-compose.dev.yml logs -f $SERVICE
        else
            docker-compose logs -f $SERVICE
        fi
        ;;
        
    ps)
        if [ "$ENV" = "dev" ]; then
            docker-compose -f docker-compose.dev.yml ps
        else
            docker-compose ps
        fi
        ;;
        
    clean)
        echo "Cleaning up..."
        docker-compose down -v --remove-orphans
        docker system prune -f
        echo "Cleanup completed!"
        ;;
        
    *)
        echo "Usage: $0 {up|down|restart|logs|ps|clean} [dev|prod] [service]"
        exit 1
        ;;
esac

echo ""
echo "Done!"
