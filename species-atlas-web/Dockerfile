# ============================================
# Species Atlas Web - Dockerfile
# 多阶段构建
# ============================================

# 第一阶段：构建
FROM node:20-alpine AS builder

WORKDIR /build

# 先复制 package.json 并安装依赖（利用 Docker 缓存）
COPY package*.json ./
RUN npm ci --registry=https://registry.npmmirror.com

# 复制源代码并构建
COPY . .
RUN npm run build

# ============================================
# 第二阶段：运行（Nginx）
FROM nginx:alpine

LABEL maintainer="Species Atlas Team"
LABEL description="Species Atlas Frontend"
LABEL version="1.0.0"

# 删除默认配置
RUN rm -rf /usr/share/nginx/html/*

# 复制构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD wget -q --spider http://localhost/health || exit 1

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
