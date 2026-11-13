# Species Atlas 快速启动指南

## 方式一：Docker 一键部署（推荐）

### 1. 准备环境
确保已安装 Docker 和 Docker Compose

### 2. 配置环境变量
```powershell
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，修改数据库密码和 JWT 密钥
```

### 3. 启动服务
```powershell
# Windows PowerShell
.\scripts\docker-deploy.ps1 -Action up -Env prod

# 或者使用 docker-compose
docker-compose up -d
```

### 4. 访问
- 前端: http://localhost
- API 文档: http://localhost:8080/api/swagger-ui.html
- 默认账号: `admin` / `admin123`

---

## 方式二：本地开发

### 1. 启动数据库（使用 Docker）
```powershell
# 只启动 MySQL 和 phpMyAdmin
docker-compose -f docker-compose.dev.yml up -d

# phpMyAdmin: http://localhost:8888 (root/root)
```

### 2. 启动后端
```powershell
cd species-atlas-server

# 首次运行：Flyway 会自动创建数据库表并初始化管理员账号
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端启动后:
- API: http://localhost:8080/api
- Swagger 文档: http://localhost:8080/api/swagger-ui.html

### 3. 启动前端
```powershell
cd species-atlas-web
npm install
npm run dev
```

前端启动后: http://localhost:3000

---

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |

---

## 目录结构

```
species-atlas/
├── species-atlas-web/          # 前端 (Vue 3 + Vite)
├── species-atlas-server/       # 后端 (Spring Boot 3)
├── docker/                     # Docker 配置
├── scripts/                    # 部署脚本
├── docker-compose.yml          # 生产环境
├── docker-compose.dev.yml      # 开发环境
└── .env.example                # 环境变量模板
```

---

## API 接口概览

### 认证
- `POST /api/auth/login` - 登录
- `POST /api/auth/register` - 注册
- `POST /api/auth/refresh` - 刷新 Token
- `GET /api/auth/profile` - 获取当前用户

### 物种数据（5 类通用）
- `GET /api/species/{type}/list` - 分页列表
- `GET /api/species/{type}/{id}` - 详情
- `POST /api/species/{type}` - 新增
- `PUT /api/species/{type}/{id}` - 更新
- `DELETE /api/species/{type}/{id}` - 删除

type 可选值: `benthic-animal`, `aquatic-plant`, `phytoplankton`, `fish`, `zooplankton`

### 搜索和统计
- `GET /api/species/search?keyword=xxx` - 跨类别搜索
- `GET /api/statistics/overview` - 数据统计

---

## 常见问题

### 1. 数据库连接失败
确保 MySQL 服务已启动，检查 `application-dev.yml` 中的连接配置

### 2. 端口被占用
- 3306: MySQL
- 8080: 后端
- 3000: 前端开发服务器
- 80: 生产环境前端

### 3. 登录失败
检查后端日志，确认 Flyway 已正确执行初始化脚本

---

*更多详情请查看 [README.md](./README.md) 和 [MIGRATION_PLAN.md](./MIGRATION_PLAN.md)*
