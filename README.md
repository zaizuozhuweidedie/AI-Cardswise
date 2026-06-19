# CardWise

基于间隔重复（Spaced Repetition）的智能卡片学习应用，采用 **Spring Boot 3 + Vue 3** 全栈架构，集成 **SM-2 算法** 与 **AI 生成** 能力，帮助用户高效记忆与复习。

## 技术栈

### 后端 — `cardwise-server/`

- **Spring Boot 3.2.5** (Java 17)
- **Spring Security** — JWT 认证
- **Spring Data JPA** — 持久层
- **PostgreSQL** — 数据库
- **SM-2 算法** — 间隔重复核心
- **DeepSeek API** — AI 生成卡片内容
- **Maven** — 构建管理

### 前端 — `cardwise-vue/`

- **Vue 3** (Composition API)
- **Vue Router 4** — 路由管理
- **Pinia** — 状态管理
- **Axios** — HTTP 请求
- **Tailwind CSS** — 样式框架
- **Vite** — 构建工具

## 功能特性

- 用户注册与 JWT 登录认证
- 创建与管理知识卡片组（Deck）
- 支持 Markdown 格式的卡片正反面（Question/Answer）
- **SM-2 间隔重复算法** — 根据记忆质量自动规划复习时间
- **AI 自动生成卡片** — 输入主题与数量，调用 DeepSeek API 智能生成
- 学习仪表盘 — 总卡片数、待复习数、今日学习数、掌握数、每日活动趋势
- 响应式 UI 设计

## 快速开始

### 前置要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- PostgreSQL 14+

### 1. 配置数据库

创建 PostgreSQL 数据库，并设置环境变量：

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/cardwise
export DATABASE_USERNAME=your_username
export DATABASE_PASSWORD=your_password
export JWT_SECRET=your_jwt_secret_key
export AI_API_KEY=your_deepseek_api_key   # AI 卡片生成（可选）
```

### 2. 启动后端

```bash
cd cardwise-server
mvn spring-boot:run
```

服务启动在 `http://localhost:8080`

### 3. 启动前端

```bash
cd cardwise-vue
npm install
npm run dev
```

应用访问在 `http://localhost:5173`

> 开发环境下前端通过 Vite proxy 将 `/api` 请求转发至后端 `8080` 端口，无需额外配置跨域。

## API 概览

| 端点 | 方法 | 说明 |
| --- | --- | --- |
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/login` | POST | 用户登录 |
| `/api/decks` | GET/POST | 卡片组列表 / 新建 |
| `/api/decks/{id}` | GET/PUT/DELETE | 卡片组详情 / 更新 / 删除 |
| `/api/decks/{id}/cards` | GET/POST | 卡片列表 / 新建 |
| `/api/cards/{id}/review` | POST | 提交复习评分（SM-2） |
| `/api/ai/generate` | POST | AI 生成卡片 |
| `/api/stats` | GET | 学习统计 |

## 项目结构

```
cardwise/
├── cardwise-server/            # 后端 Spring Boot 应用
│   └── src/main/java/com/cardwise/
│       ├── ai/                 # AI 集成（DeepSeek）
│       ├── config/             # 安全配置、JWT 工具
│       ├── controller/         # REST 控制器
│       ├── dto/                # 请求/响应 DTO
│       ├── exception/          # 全局异常处理
│       ├── model/              # JPA 实体（User, Deck, Card, ReviewLog）
│       ├── repository/         # 数据仓库
│       └── service/            # 业务逻辑（SM-2 算法、认证、卡片、统计）
├── cardwise-vue/               # 前端 Vue 3 应用
│   └── src/
│       ├── api/                # Axios 接口封装
│       ├── router/             # 路由配置
│       ├── stores/             # Pinia 状态管理
│       └── views/              # 页面组件
│           ├── auth/           # 登录/注册
│           ├── dashboard/      # 仪表盘
│           ├── decks/          # 卡片组管理
│           └── study/          # 学习模式
└── docs/                       # 文档
```

## 许可证

MIT
