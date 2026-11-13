# ============================================
# Species Atlas Server - Dockerfile
# 多阶段构建，优化镜像大小
# ============================================

# 第一阶段：构建
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 先复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================
# 第二阶段：运行
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="Species Atlas Team"
LABEL description="Species Atlas Backend Service"
LABEL version="1.0.0"

# 创建非 root 用户
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -D appuser

WORKDIR /app

# 从构建阶段复制 jar 包
COPY --from=builder /build/target/*.jar app.jar

# 创建日志和上传目录
RUN mkdir -p /app/logs /app/uploads && \
    chown -R appuser:appgroup /app

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8080

# JVM 参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/api/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
