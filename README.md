# spring-ai-interview

基于 Spring AI 2 的 AI 面试辅助平台，集成简历分析、知识库 RAG、智能面试（文字 / 语音）与异步任务管理。

> 学习项目，使用 Java 21 + Spring Boot 4 + Spring AI 2 + Gradle (Kotlin DSL)。

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言与构建 | Java 21（虚拟线程）、Gradle Kotlin DSL |
| Web / 框架 | Spring Boot 4.0.1、Spring MVC、Spring Data JPA |
| AI | Spring AI 2.0.0-M1（OpenAI 兼容模式）、pgvector Vector Store |
| 数据存储 | PostgreSQL + pgvector、Redis（缓存 / 限流） |
| 对象存储 | AWS SDK v2 S3（适配 RustFS / MinIO 等 S3 兼容服务） |
| 文档处理 | Apache Tika（解析）、iText（PDF 导出） |
| 语音 | 第三方 STT API + tts-edge-java（Edge TTS） |
| 接口文档 | springdoc-openapi（Swagger UI） |
| 其他 | Hutool、Lombok |

## 核心模块

### resume — 简历管理与分析
- 上传简历（PDF / Word / 文本），通过 Tika 解析为统一文本
- 异步触发 AI 分析（结构化输出：基本信息、技能、项目亮点、改进建议等）
- JD 匹配：给定职位描述，输出匹配度评估
- 导出 PDF 分析报告（iText 中文字体支持）
- 入口：`/resume/**`、`/jd-match/**`

### knowledge — 知识库与 RAG
- 文档上传 → Tika 解析 → 文本分块 → embedding → pgvector 入库
- 增量向量化：基于 `chunk_hash` 计算差量，只新增 / 删除变化的 chunk
- RAG 检索链路：向量召回（topK + minScore）→ Rerank 重排（finalTopK）→ 拼接上下文
- 多轮 RAG 对话（SSE 流式输出，支持会话切换知识库范围）
- 入口：`/knowledge/**`、`/rag/**`

### interview — 智能面试
- 创建面试会话：绑定简历 + JD，由 LLM **基于 skill 描述自选**面试官风格（`InterviewSkillRouter`）
- 内置 skill 包：`java`、`llm`、`frontend`、`python`、`system_design`、`testing`、`behavioral`
- 文字面试：SSE 流式对话
- 语音面试：音频上传 → STT 转写 → LLM 回答 → Edge TTS 合成音频返回
- 入口：`/interview/**`

> Skill 派发采用 **LLM 描述驱动自选**：每个 skill 通过 `resources/skills/<name>/SKILL.md` 描述触发条件与提问风格，由 router 根据简历 + JD 选择最合适的一种。新增风格只需新建一份 SKILL.md，无需改 Java 枚举。

### task — 异步任务中心
- 简历分析、知识库向量化等耗时操作均以任务方式提交，返回 `taskId`
- `/task/status/{resourceType}/{resourceId}` 查询任务进度（PROCESSING / COMPLETED / FAILED）

## 架构亮点

- **统一 AI 客户端**：`AiClient` 封装同步 / 流式 / 结构化输出三种调用范式，统一异常与耗时日志
- **Prompt 模板管理**：`AiPromptManager` 加载 `resources/prompt/**/*.st`，运行时按 name 缓存渲染
- **限流**：Redis Lua 实现令牌桶（`RateLimitService` + `@Limit` AOP）
- **虚拟线程**：开启 `spring.threads.virtual.enabled=true`，提升 IO 密集型任务吞吐
- **全局异常 + 统一返回**：`GlobalExceptionHandler` + `Result<T>`
- **OpenAPI 文档**：启动后访问 `/swagger-ui.html`

## 项目结构

```
app/
├── build.gradle.kts
└── src/main/
    ├── java/org/nembx/app/
    │   ├── AppApplication.java
    │   ├── common/                 # 基础设施（AI、AOP、配置、限流、文件、异常）
    │   │   ├── ai/                 # AiClient / AiPromptManager / SkillRegistry
    │   │   ├── aop/                # @Limit 限流注解
    │   │   ├── service/            # 文件存储、文档解析、PDF 导出、限流等
    │   │   └── ...
    │   └── module/
    │       ├── resume/             # 简历模块
    │       ├── knowledge/          # 知识库 + RAG
    │       ├── interview/          # 面试（文字 / 语音 / skill 路由）
    │       └── task/               # 任务中心
    └── resources/
        ├── application.yml
        ├── application-dev.yml
        ├── prompt/                 # Spring AI PromptTemplate (.st)
        │   ├── interview/
        │   ├── knowledge/
        │   └── resume/
        ├── skills/                 # 面试 skill 描述（LLM 自选）
        │   ├── java/SKILL.md
        │   ├── llm/SKILL.md
        │   └── ...
        └── fonts/                  # iText 中文字体
```

## 快速开始

### 前置依赖

- JDK 21
- PostgreSQL 15+（启用 pgvector 扩展：`CREATE EXTENSION vector;`）
- Redis 6+
- S3 兼容对象存储（RustFS / MinIO / AWS S3 均可）
- OpenAI 兼容的 LLM、Embedding、Rerank、STT 服务

### 配置环境变量

在 `app/.env.properties` 中提供：

```properties
# PostgreSQL
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=interview
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# Chat 模型
AI_URL=https://api.openai.com
AI_KEY=sk-xxx
AI_MODEL=gpt-4o-mini

# Embedding 模型（独立 provider）
EMBEDDING_URL=https://api.example.com
EMBEDDING_KEY=sk-xxx
EMBEDDING_MODEL=text-embedding-3-small

# Rerank
RERANK_URL=https://api.example.com
RERANK_KEY=sk-xxx
RERANK_MODEL=bge-reranker-v2-m3

# STT（语音识别）
AUDIO_URL=https://api.example.com
AUDIO_KEY=sk-xxx
STT_MODEL=whisper-1

# 对象存储
APP_STORAGE_ENDPOINT=http://localhost:9000
APP_STORAGE_ACCESS_KEY=xxx
APP_STORAGE_SECRET_KEY=xxx
APP_STORAGE_BUCKET=interview
APP_STORAGE_REGION=us-east-1
```

### 启动

```bash
cd app
./gradlew bootRun
```

启动后：

- API 服务：http://localhost:8080
- Swagger UI：http://localhost:8080/swagger-ui.html
- 首次启动建议先把 `application-dev.yml` 中的 `spring.jpa.hibernate.ddl-auto` 改为 `create` 建表，建好后改回 `update`

## 主要接口

| 模块 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 简历 | POST | `/resume/upload` | 上传简历并触发 AI 分析 |
| 简历 | GET | `/resume/detail/{resumeId}` | 查询简历分析详情 |
| 简历 | GET | `/resume/export/{resumeId}` | 导出分析报告 PDF |
| JD 匹配 | POST | `/jd-match/match/{resumeId}` | 提交 JD 进行匹配分析 |
| 知识库 | POST | `/knowledge/upload` | 上传并向量化文档 |
| 知识库 | POST | `/knowledge/reVector/{id}` | 重新向量化 |
| RAG | POST | `/rag/create` | 创建 RAG 会话 |
| RAG | POST | `/rag/chat` | SSE 流式对话 |
| 面试 | POST | `/interview/create` | 创建面试会话（自动路由 skill） |
| 面试 | POST | `/interview/chat` | 文字面试（SSE） |
| 面试 | POST | `/interview/voice-chat/{sessionId}` | 语音面试（语音进语音出） |
| 任务 | GET | `/task/status/{type}/{id}` | 查询异步任务状态 |
