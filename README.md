# Species Atlas 物种图谱

一个用于展示和管理水生生物数据的可视化平台。

## 功能特性

- **物种分类浏览** - 支持 5 类水生生物：底栖动物、水生植物、浮游植物、鱼类、浮游动物
- **物种详情展示** - 查看物种的中文名、拉丁名、形态描述、地理分布、DNA 序列等信息
- **跨类别搜索** - 支持按名称搜索所有类别的物种
- **数据统计** - 可视化展示各类别物种数量统计
- **关系图谱** - Three.js 实现的物种关系可视化
- **动画效果** - GSAP 驱动的滚动动画和视差效果

## 技术栈

- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite 8
- **状态管理**: Pinia 4
- **样式**: Tailwind CSS 4
- **HTTP 请求**: Axios
- **3D 渲染**: Three.js
- **动画**: GSAP

## 项目结构

```
src/
├── api/           # API 请求封装
├── assets/        # 静态资源
├── components/    # Vue 组件
│   ├── Navbar.vue          # 导航栏
│   ├── HeroSection.vue     # 首屏展示
│   ├── CategoryCards.vue   # 分类卡片
│   ├── SpeciesGrid.vue     # 物种列表
│   ├── SpeciesCard.vue     # 物种卡片
│   ├── SpeciesDetail.vue   # 物种详情
│   ├── StatsSection.vue    # 统计展示
│   ├── RelationGraph.vue   # 关系图谱
│   ├── SearchModal.vue     # 搜索弹窗
│   └── ...
├── composables/   # 组合式函数
│   ├── useScrollReveal.ts  # 滚动显示动画
│   ├── useParallax.ts      # 视差效果
│   └── useCountUp.ts       # 数字递增动画
├── stores/        # Pinia 状态管理
├── styles/        # 全局样式
└── types/         # TypeScript 类型定义
```

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
```

## 环境要求

- Node.js >= 18
- npm >= 9

## License

MIT
