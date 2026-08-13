# 测试地基：route 级测试与参数映射测试

## Goal

为框架建立可重复运行的测试基线：5 条 CRUD 路由的行为测试 + filter 参数映射测试，覆盖当前功能，为后续重构（F2 映射加固、B 系列 filter 扩展、C 系列 CRUD 功能）提供安全网。

## Background

- 框架（route/provider/util/validator/config 5 个模块）目前零测试；根聚合模块已配置 `tasks.test { useJUnitPlatform() }`，但只有 `kotlin-test` 依赖，无测试引擎。
- sample 使用 Kotlin 2.4.10 + KSP + jimmer-ksp（`jimmer.dto.mutable=true`）生成实体代码，KSP 插件版本 2.3.11。
- 路由行为依赖注入的 `KSqlClient`（`jimmerSqlClientFactory`），filter 参数映射依赖 `RoutingCall`，适合用 Ktor test host + H2 内存库做集成式测试。
- 任务来自父任务 framework-issues 的 F1；mapping-hardening（F2）会在此之后重构映射实现，因此本任务只加测试、不改生产源码。

## Requirements

R1. 在框架根模块（`ktor-jimmer-rest/ktor-jimmer-rest`）建立测试工程：JUnit5 引擎、Ktor test host、H2 驱动、jimmer-ksp 测试代码生成。
R2. 提供最小测试实体（Book/BookStore/Author，含 `@Key`、`@ManyToOne`、`@ManyToMany`）及测试用 Jimmer `KSqlClient`（H2 内存库 + 自动/脚本建表）。
R3. route 测试覆盖：`GET /{id}`（命中与 404）、`GET` list（`ilike?`/`between?` 参数映射、`pageIndex`/`pageSize` 分页、`pager.enabled=false`）、`POST`（校验失败 400 / transformer 生效）、`PUT`、`DELETE /{id}`。
R4. 参数映射测试覆盖：`table::name`→`name`、`table.store::name`→`store_name`、`price__ge`/`price__le`、`name__start`/`name__exact` 等 ext 解析、缺参返回 null。
R5. 异常路径测试：`ValidationException`→400、参数解析失败→400（`ParseException` 处理，若 StatusPages 配置存在）。
R6. 记录统一测试运行命令，供后续子任务与 CI 复用。

## Acceptance Criteria

- [ ] A1. `./gradlew test`（框架根模块）全部通过。
- [ ] A2. 5 条路由均有正向用例 + 至少一条边界用例（404 / 400 / 空列表）。
- [ ] A3. 映射测试覆盖根表、嵌套表、ext 后缀、缺参四类场景。
- [ ] A4. 测试不依赖外部服务（H2 内存库，无 Docker/PostgreSQL）。
- [ ] A5. 不修改任何 `src/main` 生产源码（构建脚本 `build.gradle.kts`/`libs.versions.toml` 除外）。

## Out of Scope

- 不改生产代码逻辑（本任务发现的行为问题只记录，留给对应功能子任务）。
- 不做覆盖率指标化；聚焦 CRUD 路由与参数映射两条主线。
