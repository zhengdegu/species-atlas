# Species Atlas 后端迁移实施方案

## 一、项目概述

### 1.1 迁移目标
将水生态项目的图谱数据和登录功能后端迁移到独立服务，实现：
- 独立的用户认证系统（用户名/密码登录）
- 完整的图谱数据增删改查 API
- 前端适配新的后端服务

### 1.2 现状分析

**当前前端架构：**
- 直接使用 Token（手动输入）通过代理访问 `http://116.131.53.50:9521/prod-api`
- 5 类物种数据：底栖动物、水生植物、浮游植物、鱼类、浮游动物
- Token 存储在 localStorage

**需要迁移的功能：**
- 用户登录认证
- 物种图谱 CRUD 接口（5 类）
- 数据统计接口

---

## 二、后端技术选型

| 技术栈 | 选型 | 说明 |
|--------|------|------|
| 语言 | Java 17 / Node.js 18+ | 根据团队技术栈选择 |
| 框架 | Spring Boot 3.x / NestJS | 主流企业级框架 |
| 数据库 | MySQL 8.0 / PostgreSQL 15 | 关系型数据库 |
| 认证 | JWT (JSON Web Token) | 无状态认证 |
| API 规范 | RESTful | 统一接口风格 |

---

## 三、后端目录结构

### 3.1 Java Spring Boot 版本

```
species-atlas-server/
├── pom.xml                              # Maven 依赖配置
├── src/
│   ├── main/
│   │   ├── java/com/species/atlas/
│   │   │   ├── SpeciesAtlasApplication.java    # 启动类
│   │   │   │
│   │   │   ├── config/                         # 配置类
│   │   │   │   ├── SecurityConfig.java         # Spring Security 配置
│   │   │   │   ├── JwtConfig.java              # JWT 配置
│   │   │   │   ├── CorsConfig.java             # 跨域配置
│   │   │   │   └── SwaggerConfig.java          # API 文档配置
│   │   │   │
│   │   │   ├── controller/                     # 控制器层
│   │   │   │   ├── AuthController.java         # 认证接口
│   │   │   │   ├── UserController.java         # 用户管理
│   │   │   │   ├── BenthicAnimalController.java    # 底栖动物
│   │   │   │   ├── AquaticPlantController.java     # 水生植物
│   │   │   │   ├── PhytoplanktonController.java    # 浮游植物
│   │   │   │   ├── FishController.java             # 鱼类
│   │   │   │   ├── ZooplanktonController.java      # 浮游动物
│   │   │   │   └── StatisticsController.java       # 统计接口
│   │   │   │
│   │   │   ├── service/                        # 服务层
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── BenthicAnimalService.java
│   │   │   │   ├── AquaticPlantService.java
│   │   │   │   ├── PhytoplanktonService.java
│   │   │   │   ├── FishService.java
│   │   │   │   ├── ZooplanktonService.java
│   │   │   │   └── impl/                       # 服务实现
│   │   │   │
│   │   │   ├── repository/                     # 数据访问层
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── BenthicAnimalRepository.java
│   │   │   │   ├── AquaticPlantRepository.java
│   │   │   │   ├── PhytoplanktonRepository.java
│   │   │   │   ├── FishRepository.java
│   │   │   │   └── ZooplanktonRepository.java
│   │   │   │
│   │   │   ├── entity/                         # 实体类
│   │   │   │   ├── User.java
│   │   │   │   ├── BenthicAnimal.java
│   │   │   │   ├── AquaticPlant.java
│   │   │   │   ├── Phytoplankton.java
│   │   │   │   ├── Fish.java
│   │   │   │   └── Zooplankton.java
│   │   │   │
│   │   │   ├── dto/                            # 数据传输对象
│   │   │   │   ├── request/
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   ├── SpeciesCreateRequest.java
│   │   │   │   │   └── SpeciesUpdateRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── LoginResponse.java
│   │   │   │       ├── UserResponse.java
│   │   │   │       ├── SpeciesResponse.java
│   │   │   │       └── PageResponse.java
│   │   │   │
│   │   │   ├── security/                       # 安全相关
│   │   │   │   ├── JwtTokenProvider.java       # JWT 生成/验证
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   │
│   │   │   ├── exception/                      # 异常处理
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── BusinessException.java
│   │   │   │   └── UnauthorizedException.java
│   │   │   │
│   │   │   └── util/                           # 工具类
│   │   │       ├── PageUtil.java
│   │   │       └── ResponseUtil.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml                 # 主配置
│   │       ├── application-dev.yml             # 开发环境
│   │       ├── application-prod.yml            # 生产环境
│   │       └── db/
│   │           └── migration/                  # 数据库迁移脚本
│   │               ├── V1__init_schema.sql
│   │               └── V2__init_data.sql
│   │
│   └── test/                                   # 测试
│       └── java/com/species/atlas/
│           ├── controller/
│           └── service/
│
└── docker/
    ├── Dockerfile
    └── docker-compose.yml
```

### 3.2 Node.js NestJS 版本

```
species-atlas-server/
├── package.json
├── tsconfig.json
├── nest-cli.json
├── .env                                    # 环境变量
├── .env.example
│
├── src/
│   ├── main.ts                             # 入口文件
│   ├── app.module.ts                       # 根模块
│   │
│   ├── config/                             # 配置
│   │   ├── database.config.ts
│   │   ├── jwt.config.ts
│   │   └── app.config.ts
│   │
│   ├── common/                             # 公共模块
│   │   ├── decorators/
│   │   │   ├── current-user.decorator.ts
│   │   │   └── public.decorator.ts
│   │   ├── filters/
│   │   │   └── http-exception.filter.ts
│   │   ├── guards/
│   │   │   └── jwt-auth.guard.ts
│   │   ├── interceptors/
│   │   │   └── transform.interceptor.ts
│   │   └── dto/
│   │       └── page.dto.ts
│   │
│   ├── modules/
│   │   ├── auth/                           # 认证模块
│   │   │   ├── auth.module.ts
│   │   │   ├── auth.controller.ts
│   │   │   ├── auth.service.ts
│   │   │   ├── strategies/
│   │   │   │   ├── jwt.strategy.ts
│   │   │   │   └── local.strategy.ts
│   │   │   └── dto/
│   │   │       ├── login.dto.ts
│   │   │       └── register.dto.ts
│   │   │
│   │   ├── user/                           # 用户模块
│   │   │   ├── user.module.ts
│   │   │   ├── user.controller.ts
│   │   │   ├── user.service.ts
│   │   │   ├── user.entity.ts
│   │   │   └── dto/
│   │   │
│   │   ├── species/                        # 物种模块（基类）
│   │   │   ├── species.module.ts
│   │   │   ├── base-species.controller.ts
│   │   │   ├── base-species.service.ts
│   │   │   └── dto/
│   │   │       ├── create-species.dto.ts
│   │   │       ├── update-species.dto.ts
│   │   │       └── query-species.dto.ts
│   │   │
│   │   ├── benthic-animal/                 # 底栖动物
│   │   │   ├── benthic-animal.module.ts
│   │   │   ├── benthic-animal.controller.ts
│   │   │   ├── benthic-animal.service.ts
│   │   │   └── benthic-animal.entity.ts
│   │   │
│   │   ├── aquatic-plant/                  # 水生植物
│   │   │   └── ... (同上结构)
│   │   │
│   │   ├── phytoplankton/                  # 浮游植物
│   │   │   └── ...
│   │   │
│   │   ├── fish/                           # 鱼类
│   │   │   └── ...
│   │   │
│   │   ├── zooplankton/                    # 浮游动物
│   │   │   └── ...
│   │   │
│   │   └── statistics/                     # 统计模块
│   │       ├── statistics.module.ts
│   │       ├── statistics.controller.ts
│   │       └── statistics.service.ts
│   │
│   └── database/
│       ├── migrations/
│       └── seeds/
│
├── test/
│   ├── app.e2e-spec.ts
│   └── jest-e2e.json
│
└── docker/
    ├── Dockerfile
    └── docker-compose.yml
```

---

## 四、数据库设计

### 4.1 用户表 (sys_user)

```sql
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(500) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    role VARCHAR(20) DEFAULT 'user' COMMENT '角色：admin/user',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '软删除标记'
) COMMENT '用户表';
```

### 4.2 物种基础表结构（以底栖动物为例）

```sql
CREATE TABLE benthic_animal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    serial_number VARCHAR(50) COMMENT '序号',
    species_code VARCHAR(100) NOT NULL COMMENT '物种编码',
    chinese_name VARCHAR(200) NOT NULL COMMENT '中文名',
    latin_name VARCHAR(200) COMMENT '拉丁名',
    photo_code VARCHAR(100) COMMENT '照片编码',
    photo_paths JSON COMMENT '照片路径列表',
    taxonomic_status VARCHAR(500) COMMENT '分类地位',
    gene_name VARCHAR(200) COMMENT '基因名称',
    dna_sequence_code VARCHAR(100) COMMENT 'DNA序列编码',
    dna_coding_property TEXT COMMENT 'DNA编码属性',
    morphological_description TEXT COMMENT '形态描述',
    reference TEXT COMMENT '参考文献',
    geographical_distribution TEXT COMMENT '地理分布',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT COMMENT '更新人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '软删除标记',
    INDEX idx_chinese_name (chinese_name),
    INDEX idx_species_code (species_code),
    INDEX idx_latin_name (latin_name)
) COMMENT '底栖动物表';

-- 其他 4 张表结构相同
-- aquatic_plant (水生植物)
-- phytoplankton (浮游植物)
-- fish (鱼类)
-- zooplankton (浮游动物)
```

### 4.3 操作日志表（可选）

```sql
CREATE TABLE sys_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '操作用户',
    operation_type VARCHAR(20) COMMENT '操作类型：CREATE/UPDATE/DELETE',
    target_table VARCHAR(50) COMMENT '目标表',
    target_id BIGINT COMMENT '目标ID',
    old_data JSON COMMENT '原数据',
    new_data JSON COMMENT '新数据',
    ip VARCHAR(50) COMMENT '操作IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '操作日志表';
```

---

## 五、API 接口设计

### 5.1 认证接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/login` | 用户登录 | 否 |
| POST | `/api/auth/register` | 用户注册 | 否 |
| POST | `/api/auth/refresh` | 刷新 Token | 是 |
| POST | `/api/auth/logout` | 退出登录 | 是 |
| GET | `/api/auth/profile` | 获取当前用户信息 | 是 |

**登录请求：**
```json
POST /api/auth/login
{
    "username": "admin",
    "password": "123456"
}
```

**登录响应：**
```json
{
    "code": 200,
    "msg": "登录成功",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiIs...",
        "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
        "expiresIn": 7200,
        "user": {
            "id": 1,
            "username": "admin",
            "nickname": "管理员",
            "role": "admin"
        }
    }
}
```

### 5.2 物种图谱接口（以底栖动物为例，其他 4 类相同）

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/species/benthic-animal/list` | 分页查询列表 | 是 |
| GET | `/api/species/benthic-animal/:id` | 获取详情 | 是 |
| POST | `/api/species/benthic-animal` | 新增 | 是 |
| PUT | `/api/species/benthic-animal/:id` | 更新 | 是 |
| DELETE | `/api/species/benthic-animal/:id` | 删除 | 是 |
| DELETE | `/api/species/benthic-animal/batch` | 批量删除 | 是 |
| POST | `/api/species/benthic-animal/import` | 导入 | 是 |
| GET | `/api/species/benthic-animal/export` | 导出 | 是 |

**其他物种接口路径：**
- 水生植物：`/api/species/aquatic-plant/*`
- 浮游植物：`/api/species/phytoplankton/*`
- 鱼类：`/api/species/fish/*`
- 浮游动物：`/api/species/zooplankton/*`

**列表查询参数：**
```
GET /api/species/benthic-animal/list?pageNum=1&pageSize=20&chineseName=蜻蜓
```

**列表响应：**
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "rows": [
            {
                "id": 1,
                "serialNumber": "BA001",
                "speciesCode": "BENTHOS_001",
                "chineseName": "摇蚊幼虫",
                "latinName": "Chironomidae",
                "taxonomicStatus": "节肢动物门 > 昆虫纲 > 双翅目",
                "morphologicalDescription": "体长5-15mm...",
                "geographicalDistribution": "全国各地淡水水体",
                "photoPaths": ["/uploads/benthic/001.jpg"],
                "createTime": "2024-01-15T10:30:00"
            }
        ],
        "total": 156,
        "pageNum": 1,
        "pageSize": 20
    }
}
```

**新增/更新请求：**
```json
POST /api/species/benthic-animal
{
    "speciesCode": "BENTHOS_002",
    "chineseName": "蜉蝣幼虫",
    "latinName": "Ephemeroptera",
    "taxonomicStatus": "节肢动物门 > 昆虫纲 > 蜉蝣目",
    "morphologicalDescription": "体长10-20mm，细长形...",
    "geographicalDistribution": "长江流域",
    "geneName": "COI",
    "dnaSequenceCode": "SEQ_002"
}
```

### 5.3 统计接口

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/api/statistics/overview` | 总览统计 | 是 |
| GET | `/api/statistics/by-type` | 按类型统计 | 是 |
| GET | `/api/statistics/trend` | 趋势统计 | 是 |

**统计响应：**
```json
{
    "code": 200,
    "data": {
        "total": 1580,
        "byType": {
            "benthicAnimal": 320,
            "aquaticPlant": 280,
            "phytoplankton": 450,
            "fish": 230,
            "zooplankton": 300
        }
    }
}
```

### 5.4 跨类别搜索接口

```
GET /api/species/search?keyword=蜻蜓&pageSize=20
```

---

## 六、前端适配改造

### 6.1 需要修改的文件清单

```
src/
├── api/
│   ├── request.ts          # 【修改】请求拦截器、Token 处理
│   ├── species.ts          # 【修改】API 路径映射
│   └── auth.ts             # 【新增】认证 API
│
├── stores/
│   ├── species.ts          # 【修改】适配新接口
│   └── user.ts             # 【新增】用户状态管理
│
├── types/
│   ├── species.ts          # 【修改】可能微调字段
│   └── user.ts             # 【新增】用户类型定义
│
├── components/
│   ├── TokenInput.vue      # 【重写】改为登录表单
│   ├── Navbar.vue          # 【修改】用户信息展示
│   └── UserMenu.vue        # 【新增】用户下拉菜单
│
└── App.vue                 # 【修改】认证流程
```

### 6.2 新增前端目录

```
src/
├── api/
│   └── auth.ts             # 认证 API
│
├── stores/
│   └── user.ts             # 用户状态
│
├── types/
│   └── user.ts             # 用户类型
│
├── components/
│   ├── LoginModal.vue      # 登录弹窗
│   ├── RegisterModal.vue   # 注册弹窗（可选）
│   └── UserMenu.vue        # 用户菜单
│
└── views/                  # 【可选】如需后台管理
    └── admin/
        ├── SpeciesManage.vue   # 物种管理
        └── UserManage.vue      # 用户管理
```

### 6.3 关键文件改造示例

#### 6.3.1 `src/api/auth.ts`（新增）

```typescript
import { request } from './request'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: {
    id: number
    username: string
    nickname: string
    role: string
  }
}

// 登录
export function login(params: LoginParams): Promise<{ data: LoginResult }> {
  return request({
    url: '/auth/login',
    method: 'post',
    data: params
  })
}

// 刷新 Token
export function refreshToken(): Promise<{ data: { accessToken: string } }> {
  return request({
    url: '/auth/refresh',
    method: 'post'
  })
}

// 获取用户信息
export function getUserProfile(): Promise<{ data: LoginResult['user'] }> {
  return request({
    url: '/auth/profile',
    method: 'get'
  })
}

// 退出登录
export function logout(): Promise<void> {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}
```

#### 6.3.2 `src/api/request.ts`（修改）

```typescript
import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`  // 添加 Bearer 前缀
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code && res.code !== 200) {
      // Token 过期处理
      if (res.code === 401) {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('user')
        window.location.reload()
      }
      return Promise.reject(new Error(res.msg || 'Error'))
    }
    return res
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken')
      window.location.reload()
    }
    return Promise.reject(error)
  }
)

export function request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
  return service(config) as Promise<T>
}

export default service
```

#### 6.3.3 `src/api/species.ts`（修改）

```typescript
import { request } from './request'
import type { Species, PageParams, PageResponse, SpeciesType } from '@/types/species'

// 新的 API 路径映射
const API_PATHS: Record<SpeciesType, string> = {
  '底栖动物': '/species/benthic-animal',
  '水生植物': '/species/aquatic-plant',
  '浮游植物': '/species/phytoplankton',
  '鱼类': '/species/fish',
  '浮游动物': '/species/zooplankton'
}

// 获取列表
export function getSpeciesList(type: SpeciesType, params: PageParams): Promise<PageResponse<Species>> {
  const basePath = API_PATHS[type]
  return request({
    url: `${basePath}/list`,
    method: 'get',
    params
  })
}

// 获取详情
export function getSpeciesDetail(type: SpeciesType, id: string | number): Promise<{ data: Species }> {
  const basePath = API_PATHS[type]
  return request({
    url: `${basePath}/${id}`,
    method: 'get'
  })
}

// 【新增】创建
export function createSpecies(type: SpeciesType, data: Partial<Species>): Promise<{ data: Species }> {
  const basePath = API_PATHS[type]
  return request({
    url: basePath,
    method: 'post',
    data
  })
}

// 【新增】更新
export function updateSpecies(type: SpeciesType, id: string | number, data: Partial<Species>): Promise<{ data: Species }> {
  const basePath = API_PATHS[type]
  return request({
    url: `${basePath}/${id}`,
    method: 'put',
    data
  })
}

// 【新增】删除
export function deleteSpecies(type: SpeciesType, id: string | number): Promise<void> {
  const basePath = API_PATHS[type]
  return request({
    url: `${basePath}/${id}`,
    method: 'delete'
  })
}

// 【新增】批量删除
export function batchDeleteSpecies(type: SpeciesType, ids: (string | number)[]): Promise<void> {
  const basePath = API_PATHS[type]
  return request({
    url: `${basePath}/batch`,
    method: 'delete',
    data: { ids }
  })
}

// 统计数据
export function getSpeciesStats(): Promise<{
  data: { total: number; byType: Record<string, number> }
}> {
  return request({
    url: '/statistics/overview',
    method: 'get'
  })
}

// 搜索
export function searchSpecies(keyword: string, pageSize = 20): Promise<{
  data: { type: SpeciesType; species: Species }[]
}> {
  return request({
    url: '/species/search',
    method: 'get',
    params: { keyword, pageSize }
  })
}
```

#### 6.3.4 `src/stores/user.ts`（新增）

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserProfile, type LoginParams, type LoginResult } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const user = ref<LoginResult['user'] | null>(null)
  const token = ref<string | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'admin')

  // 初始化：从 localStorage 恢复状态
  function init() {
    const savedToken = localStorage.getItem('accessToken')
    const savedUser = localStorage.getItem('user')
    if (savedToken) {
      token.value = savedToken
      if (savedUser) {
        user.value = JSON.parse(savedUser)
      }
    }
  }

  // 登录
  async function doLogin(params: LoginParams) {
    const res = await login(params)
    const { accessToken, refreshToken, user: userData } = res.data
    
    token.value = accessToken
    user.value = userData
    
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    localStorage.setItem('user', JSON.stringify(userData))
    
    return res.data
  }

  // 登出
  async function doLogout() {
    try {
      await logout()
    } finally {
      token.value = null
      user.value = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
    }
  }

  // 获取用户信息
  async function fetchProfile() {
    const res = await getUserProfile()
    user.value = res.data
    localStorage.setItem('user', JSON.stringify(res.data))
  }

  return {
    user,
    token,
    isLoggedIn,
    isAdmin,
    init,
    doLogin,
    doLogout,
    fetchProfile
  }
})
```

#### 6.3.5 `vite.config.ts`（修改代理）

```typescript
export default defineConfig({
  // ...
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // 新后端地址
        changeOrigin: true,
        // 如果后端路径不带 /api 前缀，则需要 rewrite
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
```

---

## 七、实施步骤

### 阶段一：后端开发（2-3 周）

| 周次 | 任务 | 交付物 |
|------|------|--------|
| 第 1 周 | 项目初始化、数据库设计、基础框架搭建 | 项目骨架、数据库脚本 |
| 第 2 周 | 认证模块、用户管理、JWT 集成 | 登录/注册 API |
| 第 3 周 | 5 类物种 CRUD、统计接口、搜索接口 | 全部 API |

### 阶段二：数据迁移（1 周）

| 任务 | 说明 |
|------|------|
| 数据导出 | 从原系统导出 5 类物种数据 |
| 数据清洗 | 字段映射、格式转换 |
| 数据导入 | 导入新数据库 |
| 数据验证 | 比对数据完整性 |

### 阶段三：前端适配（1 周）

| 任务 | 说明 |
|------|------|
| API 层改造 | 更新请求拦截器、API 路径 |
| 认证流程 | TokenInput → LoginModal |
| 状态管理 | 新增 user store |
| 联调测试 | 前后端联调 |

### 阶段四：测试与上线（1 周）

| 任务 | 说明 |
|------|------|
| 功能测试 | 全流程测试 |
| 性能测试 | 接口压测 |
| 部署上线 | 生产环境部署 |
| 监控配置 | 日志、告警配置 |

---

## 八、注意事项

1. **数据迁移**：确保原系统数据完整迁移，建议保留原系统只读访问作为回滚方案
2. **Token 兼容**：迁移期间可能需要同时支持新旧两种认证方式
3. **API 版本**：建议使用 `/api/v1/` 前缀，为未来版本升级预留空间
4. **安全考虑**：
   - 密码必须加密存储（bcrypt）
   - JWT 设置合理过期时间
   - 敏感操作需要权限校验
   - 添加请求频率限制
5. **文件上传**：物种图片需要独立的文件存储服务

---

## 九、技术选型建议

如果团队熟悉 Java，推荐 **Spring Boot** 方案：
- 生态成熟，社区活跃
- Spring Security 认证方案完善
- 适合企业级应用

如果团队熟悉 JavaScript/TypeScript，推荐 **NestJS** 方案：
- 与前端技术栈统一
- TypeScript 全栈
- 开发效率高

---

*文档版本：v1.0*
*创建时间：2026-11-11*
