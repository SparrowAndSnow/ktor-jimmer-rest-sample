# 更新项目文档：README、文档站（中英）与示例说明

## Goal

让 ktor-jimmer-rest 框架的使用文档完整、准确、可复现：框架 README 可读、文档站（中英）无 TODO 且能跑通快速开始、sample 项目有可执行的运行说明。

## Background（仓库证据）

- 框架本体位于 `ktor-jimmer-rest/`（git submodule，独立仓库 eimsound/ktor-jimmer-rest，version 0.0.4，JitPack 发布），共 5 个 Gradle 模块：`route`、`provider`、`util`、`validator`、`config`。
- 核心 DSL：`api<TEntity>(path) {}` 一次生成 5 条路由 —— `POST` create、`GET /{id}` id、`PUT /{id}` edit、`DELETE /{id}` remove、`GET` list（源码：`ktor-jimmer-rest/ktor-jimmer-rest/ktor-jimmer-rest-route/.../Api.kt`）。
- 子 DSL：`filter`（`eq?` / `ilike?` / `between?` / `noNull` 扩展，支持 `filter(Spec::class)` Specification 模式）、`fetcher`（fetcher DSL 或 View DTO）、`input`（实体或 Jimmer Input DTO，含 `validator` + `transformer`）。
- 校验 DSL：`notNull` / `notBlank` / `notEmpty` / `length` / `isUrl` / `isUUID` / `regex` / `range` / `max` / `min` / `before` / `after` / `between`；校验失败抛 `ValidationException`，可注册自定义 `ValidationExceptionCatcher`（默认捕获 Jimmer `UnloadedException`）。
- 参数解析：内置 String / Int / Long / Float / Double / Boolean / BigDecimal / Char / Short / ByteArray；`parser { register<...> }` 支持自定义类型。
- 分页：默认 `pageIndex=0` / `pageSize=10`，参数名可配置，`pageFactory` 可自定义返回分页对象。
- 路由配置：`extParameterSeparator`（默认 `__`，如 `price__ge`）、`subParameterSeparator`（默认 `_`，用于嵌套表字段）、`defaultPathVariable`（默认 `{id}`）。
- 技术栈版本：Kotlin 2.4.10、Ktor 3.5.2、Jimmer 0.11.5（`ktor-jimmer-rest/ktor-jimmer-rest/gradle/libs.versions.toml`）。
- sample：`book-service`（端口 8081，Book / BookStore / Author 实体 + filter/fetcher/input 完整示例）、`order-service`（脚手架，端口 8080）；数据库 PostgreSQL `ktor_jimmer`（`.devcontainer/docker-compose.yml` + `.devcontainer/init-postgres.sql`，用户/密码 postgres/postgres；另有 consul、redis 容器）。
- 文档现状：
  - `ktor-jimmer-rest/README.md`：英文，结构完整但缺少特性/模块/文档站索引。
  - `ktor-jimmer-rest/docs/`（mkdocs，zh 默认 + en）：`zh/index.md` 有 TODO 且「快速开始」链接指向不存在的 `./start.md`（实际文件为 `quick-start.md`）；`zh/quick-start.md` 全文 TODO；`zh/example/data-preparation.md` 已完整（PostgreSQL / MySQL 两版 SQL）；`en/index.md` 全文 TODO，en 无 quick-start。
  - `sample/README.md`：仅一行 `# ktor-jimmer-test`，无任何说明。

## Requirements

R1. 补全 `ktor-jimmer-rest/README.md`：特性列表、模块结构、JitPack 安装方式、快速示例、文档站链接。
R2. 补全文档站中文页：`zh/index.md`（修复 start.md 死链、补「使用前/使用后」对比示例）、`zh/quick-start.md`（从零到跑通：依赖引入 → 插件配置 → 实体定义 → `api<T>` DSL 示例 → 运行 sample）。
R3. 编写文档站英文页：`en/index.md`、`en/quick-start.md`，内容与中文页对齐。
R4. 重写 `sample/README.md`：项目简介、服务清单与端口、技术栈、运行步骤（docker compose 起库 + gradlew 启动）、目录结构。
R5. 所有文档中的代码示例与当前框架源码保持一致（DSL 签名、配置项、参数命名、Jimmer/Ktor 版本）。

## Acceptance Criteria

- [ ] A1. 中英文文档站页面无 TODO 占位；文档站内不存在指向缺失文件的链接（`./start.md` 死链已修复）。
- [ ] A2. README 包含：特性、模块结构、安装、快速示例、文档站链接，且示例可编译对应（可对照源码验证）。
- [ ] A3. `zh/quick-start.md` 步骤可从零复现（依赖引入、插件安装、示例路由），并指向 sample 的运行方式（docker compose + gradlew）。
- [ ] A4. `sample/README.md` 含服务清单、端口、技术栈、运行步骤，命令可复现。
- [ ] A5. 代码示例与框架源码一致（filter/fetcher/input DSL、parser/pager/router 配置项、查询参数命名规则）。

## Notes

## Out of Scope

- 不修改框架与 sample 的任何源代码。
- 不新增文档站功能（nav / i18n 等 mkdocs 结构调整仅在必要的最小范围内）。
- 不补充 `example/data-preparation` 之外的新示例页面（英文版 data-preparation 不在本次范围）。

## Key Decisions

- 框架 README 保持英文（GitHub 对外仓库面向英文读者），中文内容放在文档站（mkdocs 已配置 zh 为默认语言）。
- 文档站英文页仅补 `index` + `quick-start`，与中文页保持结构对齐。
