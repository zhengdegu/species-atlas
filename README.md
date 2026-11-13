# Species Atlas 物种图谱

一个用于展示和管理水生生物数据的可视化平台。

## 项目结构

```
species-atlas/
├── species-atlas-web/          # 前端项目 (Vue 3 + TypeScript)
│   ├── src/                    # 源代码
│   │   ├── api/                # API 请求
│   │   ├── components/         # Vue 组件
│   │   ├── composables/        # 组合式函数
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── styles/             # 全局样式
│   │   └── types/              # TypeScript 类型
│   ├── public/                 # 静态资源
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
│
├── species-atlas-server/       # 后端项目 (Spring Boot 3.x)
│   ├── src/main/java/          # Java 源代码
│   │   └── com/species/atlas/
│   │       ├── config/         # 配置类
│   │       ├── controller/     # 控制器
│   │       ├── service/        # 服务层
│   │       ├── repository/     # 数据访问层
│   │       ├── entity/         # 实体类
│   │       ├── dto/            # 数据传输对象
│   │       ├── security/       # 安全模块
│   │       ├── exception/      # 异常处理
│   │       ├── enums/          # 枚举
│   │       └── util/           # 工具类
│   ├── src/main/resources/     # 配置文件
│   │   ├── db/migration/       # 数据库迁移脚本
│   │   └── application*.yml    # 应用配置
│   └── pom.xml
│
├── MIGRATION_PLAN.md           # 迁移实施方案
└── README.md
```

## 功能特性

- **用户认证** - 用户名密码登录，JWT Token 认证
- **物种分类浏览** - 支持 5 类水生生物：底栖动物、水生植物、浮游植物、鱼类、浮游动物
- **物种管理** - 完整的增删改查功能
- **物种详情展示** - 查看物种的中文名、拉丁名、形态描述、地理分布、DNA 序列等信息
- **跨类别搜索** - 支持按名称搜索所有类别的物种
- **数据统计** - 可视化展示各类别物种数量统计
- **关系图谱** - Three.js 实现的物种关系可视化
- **动画效果** - GSAP 驱动的滚动动画和视差效果

## 技术栈

### 前端
- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite 8
- **状态管理**: Pinia 4
- **样式**: Tailwind CSS 4
- **HTTP 请求**: Axios
- **3D 渲染**: Three.js
- **动画**: GSAP

### 后端
- **框架**: Spring Boot 3.2.x
- **安全**: Spring Security 6.x + JWT
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA
- **数据库迁移**: Flyway
- **API 文档**: SpringDoc OpenAPI 3

## 快速开始

### 前端

```bash
cd species-atlas-web
npm install
npm run dev
```

访问 http://localhost:3000

### 后端

```bash
cd species-atlas-server

# 配置数据库连接（修改 application-dev.yml）

# 运行
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

API 文档: http://localhost:8080/api/swagger-ui.html

### 默认管理员账号
- 用户名: `admin`
- 密码: `admin123`

## 环境要求

- Node.js >= 18
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Docker & Docker Compose (可选)

## Docker 部署

### 快速启动

```bash
# 1. 复制环境变量配置
cp .env.example .env

# 2. 编辑 .env 文件，修改数据库密码和 JWT 密钥

# 3. 启动所有服务
docker-compose up -d

# 4. 查看服务状态
docker-compose ps
```

### 开发环境（只启动 MySQL）

```bash
# 启动 MySQL 和 phpMyAdmin
docker-compose -f docker-compose.dev.yml up -d

# 本地运行后端
cd species-atlas-server && mvn spring-boot:run

# 本地运行前端
cd species-atlas-web && npm run dev
```

### 部署脚本

```bash
# Windows PowerShell
.\scripts\docker-deploy.ps1 -Action up -Env prod
.\scripts\docker-deploy.ps1 -Action logs -Service server
.\scripts\docker-deploy.ps1 -Action down

# Linux/Mac
./scripts/docker-deploy.sh up prod
./scripts/docker-deploy.sh logs prod server
./scripts/docker-deploy.sh down
```

### 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| Web | 80 | 前端 (Nginx) |
| Server | 8080 | 后端 API |
| MySQL | 3306 | 数据库 |
| phpMyAdmin | 8888 | 数据库管理 (开发环境) |

## API 接口

### 认证接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/refresh` | 刷新 Token |
| GET | `/api/auth/profile` | 获取当前用户 |

### 物种接口（以底栖动物为例）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/species/benthic-animal/list` | 分页查询 |
| GET | `/api/species/benthic-animal/{id}` | 获取详情 |
| POST | `/api/species/benthic-animal` | 新增 |
| PUT | `/api/species/benthic-animal/{id}` | 更新 |
| DELETE | `/api/species/benthic-animal/{id}` | 删除 |

其他物种类型路径：
- 水生植物: `/api/species/aquatic-plant/*`
- 浮游植物: `/api/species/phytoplankton/*`
- 鱼类: `/api/species/fish/*`
- 浮游动物: `/api/species/zooplankton/*`

### 统计接口
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/statistics/overview` | 总览统计 |

## License

MIT
