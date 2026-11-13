# Species Atlas Server

物种图谱后端服务 - 基于 Spring Boot 3.x

## 技术栈

- Java 17
- Spring Boot 3.2.x
- Spring Security 6.x
- Spring Data JPA
- MySQL 8.0
- JWT (jjwt)
- Maven
- Swagger/OpenAPI 3

## 项目结构

```
species-atlas-server/
├── pom.xml                                     # Maven 依赖配置
├── README.md                                   # 项目说明
├── .gitignore                                  # Git 忽略配置
│
├── src/
│   ├── main/
│   │   ├── java/com/species/atlas/
│   │   │   ├── SpeciesAtlasApplication.java   # 启动类
│   │   │   │
│   │   │   ├── config/                        # 配置类
│   │   │   │   ├── SecurityConfig.java        # Spring Security 配置
│   │   │   │   ├── JwtConfig.java             # JWT 配置属性
│   │   │   │   ├── CorsConfig.java            # 跨域配置
│   │   │   │   ├── SwaggerConfig.java         # API 文档配置
│   │   │   │   ├── WebMvcConfig.java          # Web MVC 配置
│   │   │   │   └── JacksonConfig.java         # JSON 序列化配置
│   │   │   │
│   │   │   ├── security/                      # 安全模块
│   │   │   │   ├── JwtTokenProvider.java      # JWT Token 生成/验证
│   │   │   │   ├── JwtAuthenticationFilter.java   # JWT 认证过滤器
│   │   │   │   ├── JwtAuthenticationEntryPoint.java # 未认证处理
│   │   │   │   ├── UserDetailsServiceImpl.java    # 用户详情服务
│   │   │   │   └── SecurityUser.java          # 安全用户封装
│   │   │   │
│   │   │   ├── controller/                    # 控制器层
│   │   │   │   ├── AuthController.java        # 认证接口
│   │   │   │   ├── UserController.java        # 用户管理接口
│   │   │   │   ├── BenthicAnimalController.java   # 底栖动物接口
│   │   │   │   ├── AquaticPlantController.java    # 水生植物接口
│   │   │   │   ├── PhytoplanktonController.java   # 浮游植物接口
│   │   │   │   ├── FishController.java            # 鱼类接口
│   │   │   │   ├── ZooplanktonController.java     # 浮游动物接口
│   │   │   │   ├── StatisticsController.java      # 统计接口
│   │   │   │   ├── SearchController.java          # 搜索接口
│   │   │   │   └── FileController.java            # 文件上传接口
│   │   │   │
│   │   │   ├── service/                       # 服务层
│   │   │   │   ├── AuthService.java           # 认证服务接口
│   │   │   │   ├── UserService.java           # 用户服务接口
│   │   │   │   ├── BaseSpeciesService.java    # 物种基础服务接口
│   │   │   │   ├── BenthicAnimalService.java  # 底栖动物服务接口
│   │   │   │   ├── AquaticPlantService.java   # 水生植物服务接口
│   │   │   │   ├── PhytoplanktonService.java  # 浮游植物服务接口
│   │   │   │   ├── FishService.java           # 鱼类服务接口
│   │   │   │   ├── ZooplanktonService.java    # 浮游动物服务接口
│   │   │   │   ├── StatisticsService.java     # 统计服务接口
│   │   │   │   ├── SearchService.java         # 搜索服务接口
│   │   │   │   ├── FileService.java           # 文件服务接口
│   │   │   │   │
│   │   │   │   └── impl/                      # 服务实现
│   │   │   │       ├── AuthServiceImpl.java
│   │   │   │       ├── UserServiceImpl.java
│   │   │   │       ├── BenthicAnimalServiceImpl.java
│   │   │   │       ├── AquaticPlantServiceImpl.java
│   │   │   │       ├── PhytoplanktonServiceImpl.java
│   │   │   │       ├── FishServiceImpl.java
│   │   │   │       ├── ZooplanktonServiceImpl.java
│   │   │   │       ├── StatisticsServiceImpl.java
│   │   │   │       ├── SearchServiceImpl.java
│   │   │   │       └── FileServiceImpl.java
│   │   │   │
│   │   │   ├── repository/                    # 数据访问层
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── BenthicAnimalRepository.java
│   │   │   │   ├── AquaticPlantRepository.java
│   │   │   │   ├── PhytoplanktonRepository.java
│   │   │   │   ├── FishRepository.java
│   │   │   │   ├── ZooplanktonRepository.java
│   │   │   │   └── OperationLogRepository.java
│   │   │   │
│   │   │   ├── entity/                        # 实体类
│   │   │   │   ├── BaseEntity.java            # 基础实体（公共字段）
│   │   │   │   ├── BaseSpeciesEntity.java     # 物种基础实体
│   │   │   │   ├── User.java                  # 用户实体
│   │   │   │   ├── BenthicAnimal.java         # 底栖动物实体
│   │   │   │   ├── AquaticPlant.java          # 水生植物实体
│   │   │   │   ├── Phytoplankton.java         # 浮游植物实体
│   │   │   │   ├── Fish.java                  # 鱼类实体
│   │   │   │   ├── Zooplankton.java           # 浮游动物实体
│   │   │   │   └── OperationLog.java          # 操作日志实体
│   │   │   │
│   │   │   ├── dto/                           # 数据传输对象
│   │   │   │   ├── request/                   # 请求 DTO
│   │   │   │   │   ├── LoginRequest.java      # 登录请求
│   │   │   │   │   ├── RegisterRequest.java   # 注册请求
│   │   │   │   │   ├── RefreshTokenRequest.java   # 刷新Token请求
│   │   │   │   │   ├── ChangePasswordRequest.java # 修改密码请求
│   │   │   │   │   ├── UserCreateRequest.java     # 创建用户请求
│   │   │   │   │   ├── UserUpdateRequest.java     # 更新用户请求
│   │   │   │   │   ├── SpeciesCreateRequest.java  # 创建物种请求
│   │   │   │   │   ├── SpeciesUpdateRequest.java  # 更新物种请求
│   │   │   │   │   ├── SpeciesQueryRequest.java   # 物种查询请求
│   │   │   │   │   ├── BatchDeleteRequest.java    # 批量删除请求
│   │   │   │   │   └── SearchRequest.java         # 搜索请求
│   │   │   │   │
│   │   │   │   └── response/                  # 响应 DTO
│   │   │   │       ├── ApiResponse.java       # 统一响应包装
│   │   │   │       ├── PageResponse.java      # 分页响应
│   │   │   │       ├── LoginResponse.java     # 登录响应
│   │   │   │       ├── UserResponse.java      # 用户信息响应
│   │   │   │       ├── SpeciesResponse.java   # 物种信息响应
│   │   │   │       ├── SpeciesDetailResponse.java # 物种详情响应
│   │   │   │       ├── StatisticsResponse.java    # 统计响应
│   │   │   │       └── SearchResultResponse.java  # 搜索结果响应
│   │   │   │
│   │   │   ├── converter/                     # 对象转换器
│   │   │   │   ├── UserConverter.java
│   │   │   │   ├── SpeciesConverter.java
│   │   │   │   └── BaseConverter.java
│   │   │   │
│   │   │   ├── exception/                     # 异常处理
│   │   │   │   ├── GlobalExceptionHandler.java    # 全局异常处理器
│   │   │   │   ├── BusinessException.java         # 业务异常
│   │   │   │   ├── UnauthorizedException.java     # 未授权异常
│   │   │   │   ├── NotFoundException.java         # 资源不存在异常
│   │   │   │   ├── BadRequestException.java       # 请求参数异常
│   │   │   │   └── ErrorCode.java                 # 错误码枚举
│   │   │   │
│   │   │   ├── enums/                         # 枚举类
│   │   │   │   ├── UserRole.java              # 用户角色
│   │   │   │   ├── UserStatus.java            # 用户状态
│   │   │   │   ├── SpeciesType.java           # 物种类型
│   │   │   │   └── OperationType.java         # 操作类型
│   │   │   │
│   │   │   ├── annotation/                    # 自定义注解
│   │   │   │   ├── CurrentUser.java           # 当前用户注解
│   │   │   │   ├── RequireAdmin.java          # 管理员权限注解
│   │   │   │   └── OperationLog.java          # 操作日志注解
│   │   │   │
│   │   │   ├── aspect/                        # 切面
│   │   │   │   └── OperationLogAspect.java    # 操作日志切面
│   │   │   │
│   │   │   ├── util/                          # 工具类
│   │   │   │   ├── PageUtil.java              # 分页工具
│   │   │   │   ├── JwtUtil.java               # JWT 工具
│   │   │   │   ├── SecurityUtil.java          # 安全工具
│   │   │   │   ├── IpUtil.java                # IP 工具
│   │   │   │   ├── FileUtil.java              # 文件工具
│   │   │   │   └── ExcelUtil.java             # Excel 导入导出工具
│   │   │   │
│   │   │   └── constant/                      # 常量
│   │   │       ├── SecurityConstants.java     # 安全相关常量
│   │   │       ├── ApiConstants.java          # API 相关常量
│   │   │       └── FileConstants.java         # 文件相关常量
│   │   │
│   │   └── resources/
│   │       ├── application.yml                # 主配置文件
│   │       ├── application-dev.yml            # 开发环境配置
│   │       ├── application-prod.yml           # 生产环境配置
│   │       ├── application-test.yml           # 测试环境配置
│   │       │
│   │       ├── db/
│   │       │   └── migration/                 # 数据库迁移脚本 (Flyway)
│   │       │       ├── V1.0.0__create_user_table.sql
│   │       │       ├── V1.0.1__create_benthic_animal_table.sql
│   │       │       ├── V1.0.2__create_aquatic_plant_table.sql
│   │       │       ├── V1.0.3__create_phytoplankton_table.sql
│   │       │       ├── V1.0.4__create_fish_table.sql
│   │       │       ├── V1.0.5__create_zooplankton_table.sql
│   │       │       ├── V1.0.6__create_operation_log_table.sql
│   │       │       └── V1.1.0__init_admin_user.sql
│   │       │
│   │       ├── static/                        # 静态资源
│   │       │   └── .gitkeep
│   │       │
│   │       └── templates/                     # 模板文件
│   │           └── .gitkeep
│   │
│   └── test/
│       └── java/com/species/atlas/
│           ├── SpeciesAtlasApplicationTests.java
│           │
│           ├── controller/                    # 控制器测试
│           │   ├── AuthControllerTest.java
│           │   ├── UserControllerTest.java
│           │   └── BenthicAnimalControllerTest.java
│           │
│           ├── service/                       # 服务测试
│           │   ├── AuthServiceTest.java
│           │   ├── UserServiceTest.java
│           │   └── BenthicAnimalServiceTest.java
│           │
│           └── repository/                    # 仓库测试
│               ├── UserRepositoryTest.java
│               └── BenthicAnimalRepositoryTest.java
│
├── docker/
│   ├── Dockerfile                             # Docker 镜像配置
│   ├── docker-compose.yml                     # Docker Compose 配置
│   ├── docker-compose.dev.yml                 # 开发环境 Docker Compose
│   └── nginx/
│       └── nginx.conf                         # Nginx 配置（反向代理）
│
├── docs/
│   ├── api/                                   # API 文档
│   │   └── openapi.yaml                       # OpenAPI 规范文档
│   ├── database/                              # 数据库文档
│   │   ├── schema.sql                         # 完整建表脚本
│   │   └── ER-diagram.png                     # ER 图
│   └── deployment/                            # 部署文档
│       ├── deployment-guide.md                # 部署指南
│       └── environment-setup.md               # 环境搭建指南
│
├── scripts/
│   ├── build.sh                               # 构建脚本
│   ├── deploy.sh                              # 部署脚本
│   ├── db-backup.sh                           # 数据库备份脚本
│   └── db-restore.sh                          # 数据库恢复脚本
│
└── logs/                                      # 日志目录
    └── .gitkeep
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### 本地开发

```bash
# 1. 克隆项目
git clone <repository-url>
cd species-atlas-server

# 2. 配置数据库
# 修改 application-dev.yml 中的数据库连接信息

# 3. 运行项目
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 4. 访问 API 文档
# http://localhost:8080/swagger-ui.html
```

### 构建部署

```bash
# 打包
mvn clean package -DskipTests

# 运行
java -jar target/species-atlas-server-1.0.0.jar --spring.profiles.active=prod
```

### Docker 部署

```bash
# 构建镜像
docker build -t species-atlas-server:latest .

# 使用 Docker Compose 启动
docker-compose up -d
```

## API 接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/refresh` - 刷新 Token
- `POST /api/auth/logout` - 退出登录
- `GET /api/auth/profile` - 获取当前用户信息

### 物种接口（以底栖动物为例）
- `GET /api/species/benthic-animal/list` - 分页查询
- `GET /api/species/benthic-animal/{id}` - 获取详情
- `POST /api/species/benthic-animal` - 新增
- `PUT /api/species/benthic-animal/{id}` - 更新
- `DELETE /api/species/benthic-animal/{id}` - 删除
- `DELETE /api/species/benthic-animal/batch` - 批量删除
- `POST /api/species/benthic-animal/import` - 导入
- `GET /api/species/benthic-animal/export` - 导出

### 统计接口
- `GET /api/statistics/overview` - 总览统计

### 搜索接口
- `GET /api/species/search` - 跨类别搜索

## License

MIT
