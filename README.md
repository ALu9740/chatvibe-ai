<div align="center">

# ChatVibe-AI

**个人 AI 应用 · 多模态聊天 · PDF 知识库问答 · 情感沟通教练**

Vue 3 + Spring Boot 3 + Spring AI 全栈项目

</div>

---

## 项目简介

ChatVibe-AI 是一个面向个人使用的 AI 应用，集成了三大核心功能模块：

- **AI 聊天** — 支持多模态输入（文本 + 图片附件）、SSE 流式响应、会话历史管理
- **ChatPDF** — 上传 PDF 文档，基于 RAG（检索增强生成）进行知识库问答
- **学会哄人** — 情感沟通教练，内置 5 种关系场景，AI 扮演当事人并给出共情评分与技巧点评

项目采用前后端分离架构，后端通过 Spring AI 接入阿里通义千问（OpenAI 兼容协议），前端使用 Vue 3 + TypeScript 构建。

## 功能特性

### AI 聊天

- 多模态对话：支持文本输入 + 图片附件
- SSE 流式输出：实时逐字返回 AI 回复
- 会话管理：创建、切换、删除会话，自动持久化历史消息
- Markdown 渲染：支持代码高亮、表格、公式等富文本展示

### ChatPDF

- PDF 上传与解析：基于 Spring AI PdfDocumentReader
- 向量检索：使用 SimpleVectorStore 进行文档向量化与语义检索
- RAG 问答：结合检索到的文档片段生成回答
- 原文查看：支持在线预览和下载原始 PDF

### 学会哄人

- 5 种内置场景：伴侣、朋友、父母、同事、孩子
- 双角色扮演：AI 同时扮演「当事人」和「教练」
- 实时评分：每次对话后给出共情评分（0-10）与技巧点评
- 流式输出：当事人反应与教练点评通过 SSE 实时推送

### 安全与合规

- JWT 无状态鉴权：登出后 Token 加入 Redis 黑名单
- AES-256-GCM 加密：前后端共享密钥，敏感数据加密传输
- 接口限流：基于 Bucket4j，登录 5 次/分钟，AI 调用 30 次/分钟
- 内容过滤：敏感词拦截（输入校验 + 输出净化）
- CORS 白名单：仅允许配置的前端域名
- HTTPS：开发环境使用内置证书，生产环境由 Nginx 终结 SSL

## 技术栈

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | ^3.5 | 前端框架（`<script setup>`） |
| TypeScript | ~6.0 | 类型安全 |
| Vite | ^8.1 | 构建工具 |
| Pinia | ^3.0 | 状态管理 |
| Vue Router | ^4.6 | 路由 |
| Tailwind CSS | ^3.4 | 原子化 CSS |
| Axios | ^1.18 | HTTP 客户端 |
| marked + DOMPurify | - | Markdown 渲染与 XSS 防护 |
| highlight.js | ^11.11 | 代码高亮 |
| Lucide Icons | ^0.577 | 图标库 |

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.4.3 | 应用框架 |
| Java | 17 | 运行时 |
| Spring AI | 1.0.0-M6 | AI 模型接入（通义千问） |
| Spring Security | - | 认证鉴权 |
| MyBatis-Plus | 3.5.10 | ORM |
| MySQL | - | 主数据库 |
| Redis | - | JWT 黑名单 / 限流 |
| JJWT | 0.12.6 | JWT 签发与校验 |
| Bucket4j | 8.10.1 | 接口限流 |
| SpringDoc OpenAPI | 2.6.0 | API 文档（Swagger UI） |
| Lombok | - | 简化样板代码 |

### 基础设施

| 组件 | 用途 |
|------|------|
| MySQL 8.x | 主数据库（用户、会话、消息、PDF 元数据、场景配置） |
| Redis | JWT 黑名单、限流计数 |
| Nginx | 生产环境反向代理 + SSL 终结 |
| 通义千问 API | AI 模型服务（OpenAI 兼容协议） |

## 项目结构

```
chatvibe-ai-full/
├── backend/                        # 后端 — Spring Boot
│   ├── src/main/java/icu/chatvibe_ai/
│   │   ├── common/                 # 通用：统一响应、全局异常
│   │   ├── config/                 # 配置：Security、Redis、AI、SpringDoc
│   │   ├── controller/             # 控制器：Auth、Chat、Pdf、Comfort
│   │   ├── dto/                    # 数据传输对象
│   │   ├── entity/                 # 数据库实体
│   │   ├── mapper/                 # MyBatis-Plus Mapper
│   │   ├── security/               # 安全：JWT 过滤器、工具类
│   │   ├── service/                # 业务逻辑：聊天、PDF、情感教练、限流、过滤
│   │   └── util/                   # 工具：AES 加密
│   ├── src/main/resources/
│   │   ├── application.yml         # 主配置
│   │   ├── application-local.yml   # 本地配置（已 gitignore）
│   │   ├── application-prod.yml    # 生产配置
│   │   ├── schema.sql              # 建表脚本
│   │   ├── data.sql                # 初始化数据
│   │   ├── blocklist.txt           # 敏感词表
│   │   └── keystore/               # SSL 证书
│   └── pom.xml
│
├── frontend/                       # 前端 — Vue 3
│   ├── src/
│   │   ├── api/                    # API 封装：auth、chat、pdf、comfort、sse
│   │   ├── components/             # 组件：聊天消息、侧边栏、PDF 查看器等
│   │   ├── composables/            # 组合式函数：useAuth、useChat、useStreaming
│   │   ├── layouts/                # 布局：认证页、默认页
│   │   ├── router/                 # 路由配置
│   │   ├── stores/                 # Pinia 状态：auth、chat
│   │   ├── views/                  # 页面：首页、聊天、PDF、情感教练、登录等
│   │   └── utils/                  # 工具：AES 加密
│   ├── .env.example                # 环境变量模板
│   └── package.json
│
└── .gitignore
```

## 快速开始

### 环境要求

- **JDK** 17+
- **Node.js** 18+（推荐 20+）
- **MySQL** 8.x
- **Redis** 6.x+
- **Maven** 3.8+（或使用 IDE 内置）
- 通义千问 API Key（[获取地址](https://dashscope.console.aliyun.com/)）

### 1. 克隆仓库

```bash
git clone https://github.com/你的用户名/chatvibe-ai-full.git
cd chatvibe-ai-full
```

### 2. 配置后端

```bash
cd backend
```

创建本地配置文件 `src/main/resources/application-local.yml`（已被 gitignore，不会提交）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chatvibe?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: 你的MySQL密码
  data:
    redis:
      host: localhost
      port: 6379
      password: 你的Redis密码（无密码则留空）
  ai:
    openai:
      api-key: 你的通义千问API_KEY

chatvibe:
  security:
    jwt:
      secret: 你的JWT密钥_至少32个字符
    aes:
      key: 你的AES密钥_正好32字节
    hardcoded-user:
      password: '{bcrypt}通过PasswordHashGenerator生成的哈希'
```

> **生成密码哈希**：运行 `backend` 中的 `PasswordHashGenerator` 测试类，将输出的 BCrypt 哈希填入配置。

数据库会在首次启动时自动建表并初始化数据（`schema.sql` + `data.sql`），无需手动执行。

### 3. 启动后端

```bash
# 在 backend 目录下
./mvnw spring-boot:run
# 或使用 IDE 运行 ChatVibeAiApplication.java
```

后端默认运行在 `https://localhost:8080`，Swagger UI 位于 `https://localhost:8080/swagger-ui.html`。

### 4. 配置前端

```bash
cd frontend
npm install
```

复制环境变量模板并填入 AES 密钥（需与后端一致）：

```bash
cp .env.example .env
```

```env
# .env
VITE_AES_KEY=与后端chatvibe.security.aes.key相同的值
```

### 5. 启动前端

```bash
npm run dev
```

前端开发服务器运行在 `https://localhost:5173`，自动代理 `/api` 请求到后端。

## API 概览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 鉴权 | POST | `/api/auth/login` | 登录，返回 JWT |
| 鉴权 | GET | `/api/auth/me` | 获取当前用户信息 |
| 鉴权 | POST | `/api/auth/logout` | 登出（JWT 加入黑名单） |
| 聊天 | POST | `/api/chat` | 多模态聊天（SSE 流） |
| 聊天 | GET | `/api/chat/sessions` | 会话列表 |
| 聊天 | GET | `/api/chat/sessions/{id}/messages` | 会话消息历史 |
| 聊天 | DELETE | `/api/chat/sessions/{id}` | 删除会话 |
| PDF | POST | `/api/pdf/upload/{chatId}` | 上传 PDF |
| PDF | GET | `/api/pdf/chat` | PDF 问答（SSE 流） |
| PDF | GET | `/api/pdf/file/{chatId}` | 下载原始 PDF |
| PDF | GET | `/api/pdf/sessions` | PDF 会话列表 |
| 情感教练 | GET | `/api/comfort/scenarios` | 场景列表 |
| 情感教练 | POST | `/api/comfort/chat` | 教练对话（SSE 流，含评分） |

> 所有接口（除登录外）均需在请求头携带 `Authorization: Bearer <token>`。

## 数据库设计

| 表名 | 说明 |
|------|------|
| `users` | 用户表（个人版仅硬编码单用户） |
| `chat_session` | 聊天会话表（统一承载 chat / pdf / comfort 三种类型） |
| `chat_message` | 消息表（含角色、内容、元数据） |
| `pdf_file` | PDF 文件表（文件名、大小等元信息） |
| `comfort_scenario` | 情感教练场景表（5 种内置关系场景） |

向量数据使用 `SimpleVectorStore` 持久化到本地文件，无需额外的向量数据库。

## 生产部署

生产环境采用 Nginx 反向代理架构，参考 `application-prod.yml`：

```
客户端 → Nginx (HTTPS/443) → Spring Boot (127.0.0.1:8080 HTTP)
```

部署步骤概述：

1. **构建前端**：`npm run build`，将 `dist/` 部署到 Nginx 静态目录
2. **构建后端**：`mvn package`，生成 `chatvibe-ai-backend.jar`
3. **配置环境变量**：通过环境变量注入 `MYSQL_PASSWORD`、`REDIS_PASSWORD`、`AI_API_KEY`、`JWT_SECRET`、`AES_KEY`、`HASHED_PASSWORD`
4. **启动后端**：`SPRING_PROFILES_ACTIVE=prod java -jar chatvibe-ai-backend.jar`
5. **配置 Nginx**：反向代理 `/api` 到后端，静态资源指向前端 `dist/`
6. 首次部署手动执行 `schema.sql` + `data.sql` 初始化数据库

## 配置说明

项目通过环境变量 + Profile 配置文件管理不同环境的参数：

| 参数 | 环境变量 | 说明 |
|------|----------|------|
| MySQL 地址 | `MYSQL_HOST` / `MYSQL_PORT` | 默认 localhost:3306 |
| MySQL 凭证 | `MYSQL_USER` / `MYSQL_PASSWORD` | - |
| Redis 地址 | `REDIS_HOST` / `REDIS_PORT` | 默认 localhost:6379 |
| Redis 密码 | `REDIS_PASSWORD` | - |
| AI API Key | `AI_API_KEY` | 通义千问 DashScope |
| JWT 密钥 | `JWT_SECRET` | 至少 32 字符 |
| AES 密钥 | `AES_KEY` | 正好 32 字节 |
| 密码哈希 | `HASHED_PASSWORD` | BCrypt 格式 |
| 激活 Profile | `SPRING_PROFILES_ACTIVE` | `local` / `prod` |

## License

Copyright © 2026 Alu. All rights reserved.

本项目为个人项目，已备案上线，备案号：**湘ICP备2026027106号**。

未经作者书面授权，禁止任何形式的商业使用、二次分发或转载。仅供学习参考。
