# CardWise — AI 学习助手 设计文档

> 日期：2026-06-19
> 状态：批准待实施

---

## 一、项目概述

CardWise 是一个 AI 驱动的 Flashcards（闪卡）学习应用。用户粘贴学习材料，AI 自动生成 Flashcards，配合 SM-2 间隔重复算法科学安排复习。

### 核心价值

- **AI 加速**：从学习材料一键生成复习卡片，节省手动制作时间
- **科学记忆**：SM-2 算法根据每张卡片的掌握程度动态安排复习时间
- **多供应商 AI**：通过策略模式支持 DeepSeek、OpenAI 等多供应商，配置即可切换

---

## 二、技术栈

### 后端
- Java 17+、Spring Boot 3.2.x
- Spring Data JPA + PostgreSQL（Neon 云数据库）
- Spring Security + JWT（jjwt 0.12.x）认证
- Maven 构建

### 前端
- Vue 3 + Vite 5
- Vue Router 4 + Pinia 状态管理
- Tailwind CSS 3 样式
- Axios HTTP 请求

---

## 三、项目结构

```
D:\JavaCode\cardwise/
├── cardwise-server/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/cardwise/
│       │   ├── CardwiseApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   └── JwtUtil.java
│       │   ├── model/
│       │   │   ├── User.java
│       │   │   ├── Deck.java
│       │   │   ├── Card.java
│       │   │   └── ReviewLog.java
│       │   ├── repository/
│       │   │   ├── UserRepository.java
│       │   │   ├── DeckRepository.java
│       │   │   ├── CardRepository.java
│       │   │   └── ReviewLogRepository.java
│       │   ├── service/
│       │   │   ├── AuthService.java
│       │   │   ├── CardService.java
│       │   │   ├── StatsService.java
│       │   │   └── SM2Algorithm.java
│       │   ├── ai/
│       │   │   ├── AiProvider.java              # 接口
│       │   │   ├── AiProviderFactory.java       # 工厂类
│       │   │   ├── DeepSeekAiProvider.java      # DeepSeek 实现
│       │   │   ├── OpenAiProvider.java          # OpenAI 实现（预留）
│       │   │   └── AiProperties.java            # 配置绑定
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── DeckController.java
│       │   │   ├── CardController.java
│       │   │   ├── AiController.java
│       │   │   └── StatsController.java
│       │   ├── dto/
│       │   │   ├── LoginRequest.java
│       │   │   ├── RegisterRequest.java
│       │   │   ├── DeckRequest.java
│       │   │   ├── CardRequest.java
│       │   │   ├── ReviewRequest.java
│       │   │   └── StatsResponse.java
│       │   └── exception/
│       │       ├── GlobalExceptionHandler.java
│       │       ├── ResourceNotFoundException.java
│       │       └── AiGenerationException.java
│       └── resources/
│           └── application.yml
│
├── cardwise-vue/
│   ├── package.json
│   ├── vite.config.js
│   ├── tailwind.config.js
│   ├── index.html
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── api/
│       │   └── index.js
│       ├── router/
│       │   └── index.js
│       ├── stores/
│       │   ├── auth.js
│       │   ├── decks.js
│       │   ├── cards.js
│       │   └── stats.js
│       ├── assets/
│       │   └── main.css
│       └── views/
│           ├── HomePage.vue
│           ├── auth/
│           │   ├── LoginPage.vue
│           │   └── RegisterPage.vue
│           ├── dashboard/
│           │   ├── DashboardLayout.vue
│           │   ├── IndexPage.vue
│           │   └── components/
│           │       ├── StatCard.vue
│           │       └── HeatmapChart.vue
│           ├── decks/
│           │   ├── DecksPage.vue
│           │   ├── NewDeckPage.vue
│           │   ├── DeckDetailPage.vue
│           │   └── components/
│           │       └── AiGeneratePanel.vue
│           └── study/
│               ├── StudyPage.vue
│               └── components/
│                   └── FlashCard.vue
│
└── docs/superpowers/specs/
    └── 2026-06-19-cardwise-design.md       ← 本文档
```

---

## 四、数据库设计

### 表结构

**users 表**
- id: UUID PK
- email: VARCHAR 唯一
- password_hash: VARCHAR（BCrypt 加密）
- name: VARCHAR
- created_at: TIMESTAMP
- updated_at: TIMESTAMP

**decks 表**
- id: UUID PK
- name: VARCHAR
- description: TEXT 可空
- color: VARCHAR default '#6366f1'
- user_id: UUID FK → users.id
- created_at: TIMESTAMP
- updated_at: TIMESTAMP

**cards 表**
- id: UUID PK
- front: TEXT
- back: TEXT
- tags: TEXT（逗号分隔）
- deck_id: UUID FK → decks.id
- user_id: UUID FK → users.id
- ease_factor: DOUBLE default 2.5
- interval_days: INT default 0
- repetitions: INT default 0
- next_review_at: TIMESTAMP
- last_review_at: TIMESTAMP 可空
- created_at: TIMESTAMP
- updated_at: TIMESTAMP

**review_logs 表**
- id: UUID PK
- card_id: UUID FK → cards.id
- user_id: UUID FK → users.id
- quality: INT（1-4）
- ease_factor: DOUBLE
- interval_days: INT
- repetitions: INT
- reviewed_at: TIMESTAMP

### 策略
- ddl-auto: update（JPA 自动建表）
- 索引：cards 表 (user_id + next_review_at)、(deck_id)、review_logs (user_id + reviewed_at)

---

## 五、后端 API 设计

### 认证（无需 JWT）

```
POST /api/auth/register   { email, password, name } → { token, userId, email, name }
POST /api/auth/login      { email, password } → { token, userId, email, name }
```

### 卡组（需 JWT）

```
GET    /api/decks                     → Deck[]
POST   /api/decks                     { name, description, color } → Deck
PUT    /api/decks/{id}                { name, description, color } → Deck
DELETE /api/decks/{id}                → 204
```

### 卡片（需 JWT）

```
GET    /api/decks/{deckId}/cards      → Card[]
POST   /api/decks/{deckId}/cards      { front, back, tags } → Card
PUT    /api/cards/{id}                { front, back, tags } → Card
DELETE /api/cards/{id}                → 204
GET    /api/cards/due?deckId=xxx      → Card[]（deckId 可选）
POST   /api/cards/{id}/review         { quality: 1-4 } → SM2Result
```

### 统计（需 JWT）

```
GET /api/stats → { totalCards, dueCards, studiedToday, masteredCards, dailyActivity[] }
```

### AI 生成（需 JWT）

```
POST /api/ai/generate  { source, sourceType } → { cards: [{front, back}] }
```

---

## 六、SM-2 间隔重复算法

```java
public SM2Result calculate(int quality, double easeFactor, int intervalDays, int repetitions) {
    if (quality < 3) {
        repetitions = 0;
        intervalDays = 1;
    } else {
        repetitions++;
        if (repetitions == 1) intervalDays = 1;
        else if (repetitions == 2) intervalDays = 6;
        else intervalDays = Math.round(intervalDays * easeFactor);
    }
    double newEase = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
    if (newEase < 1.3) newEase = 1.3;
    LocalDateTime nextReviewAt = LocalDateTime.now().plusDays(intervalDays);
}
```

评分标准：
- 1 = Again：完全忘了，重置间隔
- 2 = Hard：想起来了但很困难
- 3 = Good：正常记住
- 4 = Easy：非常轻松

---

## 七、AI Provider 策略层

### 架构

```
AiController
    ↓
AiProviderFactory.getProvider()
    ↓
AiProvider 接口
 ├── DeepSeekAiProvider    ← 首次实现
 ├── OpenAiProvider        ← 预留扩展
 └── ...（灵活扩展）
```

### AiProvider 接口

```java
public interface AiProvider {
    String getProviderName();
    List<Card> generateCards(String source, String sourceType);
}
```

- `source`: 用户粘贴的文本或 URL 内容
- `sourceType`: `"text"` / `"url"`
- 返回: 从 AI 响应解析出的 Card 列表（仅含 front/back）
- 解析失败时: 抛出 `AiGenerationException`

### 配置驱动（application.yml）

```yaml
cardwise:
  ai:
    provider: deepseek
    providers:
      deepseek:
        api-url: ${AI_API_URL:https://api.deepseek.com}
        api-key: ${AI_API_KEY}
        model: deepseek-chat
      openai:
        api-url: ${OPENAI_API_URL:https://api.openai.com}
        api-key: ${OPENAI_API_KEY}
        model: gpt-4o-mini
```

### 扩展方式

1. 新建类实现 `AiProvider`
2. 在 `AiProperties` 配置中新增供应商连接信息
3. 实现类上标注 `@Component` + `@ConditionalOnProperty(name = "cardwise.ai.provider", havingValue = "xxx")`
4. 工厂类 `AiProviderFactory` 自动注入所有 `AiProvider` 实现并按配置返回活跃实例

### AI Prompt 设计

系统消息：

```
你是一个专业的闪卡(Flashcard)生成助手。你的任务是从用户提供的学习材料中提取关键概念，生成问答对形式的闪卡。

每张闪卡包括：
- front：一个清晰的问题或术语
- back：对应的答案或解释，简明扼要

要求：
1. 提取材料中最重要的概念、定义、公式、流程
2. 每张卡片聚焦一个知识点
3. 问题要具体，答案要准确
4. 中文材料用中文，英文材料用英文
5. 返回 JSON 数组格式：[{"front": "问题", "back": "答案"}]
6. 生成 5-15 张卡片，视材料长度而定
```

---

## 八、前端页面设计

### 路由

| 路径 | 页面 | 说明 |
|---|---|---|
| `/` | HomePage | 引导页，Logo + 功能介绍 + 注册/登录入口 |
| `/auth/login` | LoginPage | 卡片式登录表单 |
| `/auth/register` | RegisterPage | 卡片式注册表单 |
| `/dashboard` | DashboardLayout | 骨架布局（顶栏+侧边栏+内容区） |
| `/dashboard/` | IndexPage | 统计仪表盘 |
| `/dashboard/decks` | DecksPage | 卡组网格列表 |
| `/dashboard/decks/new` | NewDeckPage | 新建卡组表单 |
| `/dashboard/decks/:id` | DeckDetailPage | 卡组详情 + AI生成 + 卡片管理 |
| `/dashboard/decks/:id/study` | StudyPage | 按卡组学习 |
| `/dashboard/study` | StudyPage | 全局学习（所有到期卡片） |

### 颜色方案

| 元素 | 样式 |
|---|---|
| 整体背景 | `bg-white` |
| 边框 / 分割线 | `border-gray-200` |
| 主按钮 | `bg-gray-900 hover:bg-gray-800 text-white` |
| 次要按钮 | `bg-white border border-gray-300` |
| 卡组首字母方块 | 使用 `deck.color` 字段作背景色 |
| 学习卡片背景 | 使用 `deck.color`，文字白色 |
| Again 按钮 | 红色系 |
| Hard 按钮 | 橙色系 |
| Good 按钮 | 绿色系 |
| Easy 按钮 | 蓝色系 |

### 关键交互 — 学习模式

1. 进入学习页面，加载待复习卡片列表（`GET /api/cards/due`）
2. 显示当前卡片正面（front），背景色为 deck.color
3. 点击卡片 → CSS 翻转动画（`transform: rotateY(180deg)`）→ 显示背面（back）
4. 翻转后显示四个评分按钮（Again / Hard / Good / Easy）
5. 点击评分按钮 → `POST /api/cards/{id}/review { quality }` → 更新本地状态 → 切换到下一张
6. 全部完成后显示复习小结（本次复习卡片数、各评级分布、正确率）

### 关键交互 — AI 生成

1. 用户在 DeckDetailPage 的 AI 区域粘贴学习文本
2. 点击"生成卡片" → `POST /api/ai/generate` → loading 状态
3. 生成的卡片以可编辑预览列表展示（每张显示 front + back，可修改）
4. 用户点击"保存全部" → 逐个 `POST /api/decks/{deckId}/cards`
5. 保存后刷新卡片列表

### 状态管理（Pinia）

- **auth store**: token 存储与管理、用户信息、登录/注册/退出操作
  - 初始化时从 localStorage 读取 token
  - 登录成功后保存 token 到 localStorage
  - logout 时清除 token 并跳转登录页

- **decks store**: 卡组列表、当前卡组、CRUD 操作
  - fetchDecks(): 加载用户所有卡组
  - createDeck(data): 新建卡组
  - updateDeck(id, data): 更新卡组
  - deleteDeck(id): 删除卡组

- **cards store**: 当前卡片列表、待复习卡片、复习操作
  - fetchCards(deckId): 加载指定卡组的卡片
  - fetchDueCards(deckId?): 加载待复习卡片
  - createCard(deckId, data): 添加卡片
  - reviewCard(cardId, quality): 提交复习评分
  - generateCards(deckId, source): 调用 AI 生成

- **stats store**: 统计面板数据
  - fetchStats(): 加载仪表盘统计数据

### API 请求配置

```js
// api/index.js
const api = axios.create({ baseURL: '/api' });

api.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

api.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token');
            router.push('/auth/login');
        }
        return Promise.reject(error);
    }
);
```

---

## 九、错误处理

### 统一响应格式

```json
{ "error": "错误描述" }
```

### 全局异常处理器（@ControllerAdvice）

| 异常 | HTTP | 说明 |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | 请求体验证失败 |
| `BadCredentialsException` | 401 | 用户名或密码错误 |
| `ResourceNotFoundException` | 404 | 资源不存在 |
| `AiGenerationException` | 502 | AI 调用失败 |
| `Exception`（兜底） | 500 | 未知错误 |

---

## 十、环境变量

| 变量名 | 用途 | 必填 |
|---|---|---|
| `DATABASE_URL` | PostgreSQL JDBC 连接串 | 是 |
| `DATABASE_USERNAME` | 数据库用户名 | 是 |
| `DATABASE_PASSWORD` | 数据库密码 | 是 |
| `JWT_SECRET` | JWT 签名密钥（≥32字符） | 是 |
| `AI_API_KEY` | AI 供应商 API Key | 否（AI 功能按需） |
| `AI_API_URL` | AI 供应商基础 URL | 否（默认 DeepSeek） |

---

## 十一、前端 Vite 代理

```js
// vite.config.js
export default defineConfig({
    plugins: [vue()],
    server: {
        proxy: { '/api': 'http://localhost:8080' }
    }
});
```

---

## 十二、安全配置

- Spring Security 放行 `/api/auth/**`（注册、登录）
- 其余接口均需 JWT 认证
- JWT 过期时间：7 天
- CORS 允许前端开发服务器 `http://localhost:5173`
- 密码使用 BCrypt 加密存储
