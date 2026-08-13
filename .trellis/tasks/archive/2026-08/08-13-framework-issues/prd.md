# 框架问题修复：测试、filter DSL、CRUD 功能与文档

## Goal

按分析清单逐项修复框架，覆盖四类问题：测试与稳定性、filter DSL 能力、CRUD 功能缺口、文档同步。以"先地基后功能"的顺序分批交付，每个子任务独立可验收。

## Background（分析结论摘要）

- 框架模块（route/provider/util/validator/config）目前零测试，参数映射依赖 `PropertyReference0Impl` 强转和 Jimmer 内部 `javaTable` 反射（`Reflect.kt:29`、`RoutingCallExtension.kt:73`）。
- `filter` 自动映射只有 `eq?`/`ilike?`/`between?`/`noNull`，且 `eq?` 不感知 ext 后缀（`Condition.kt:12` 取 default 第一个参数）。
- create/edit 的 SaveMode 写死 `INSERT_ONLY`/`UPDATE_ONLY`（`Create.kt:24`、`Edit.kt:24`），无批量、无 PATCH、无 count/exists、错误响应不统一、create/edit 响应不支持 fetcher 投影。
- 小坑：`key(...)` 覆盖路径变量且同时作用于 id/remove（`Query.kt:21`）；`defaultPathVariable` 取第一个路径参数（`RoutingCallExtension.kt:181`）；嵌套字段 `store_name` 与真实字段名冲突；`entityIdType` 只取第一个 `@Id`。

## 需求清单（映射到子任务）

| ID | 需求 | 子任务 |
|----|------|--------|
| F1 | route 级测试与参数映射测试（测试地基） | test-foundation |
| F2 | 参数映射加固：拆分反射映射为独立可测组件 | mapping-hardening |
| D1 | `key()` 语义修正或文档化 | mapping-hardening |
| D2 | `defaultPathVariable` 按注册名取参 | mapping-hardening |
| D3 | 嵌套字段名冲突处理 | mapping-hardening |
| B1 | `eq?` 支持 `__exact` 等 ext，消除歧义 | filter-dsl |
| B2 | 补齐 `in?`/`notIn?`/`lt?`/`gt?`/`le?`/`ge?`/`notEq?`/`isNull?` | filter-dsl |
| B3 | 动态排序 `sort=price,desc` | filter-dsl |
| C1 | create/edit 的 SaveMode/AssociatedSaveMode 可配置（含 upsert） | crud-features |
| C2 | 批量创建/删除/更新 | crud-features |
| C3 | 统一错误响应（error envelope，404 带 body） | crud-features |
| C4 | create/edit 响应支持 fetcher 投影 | crud-features |
| C5 | PATCH/部分更新语义 | crud-features |
| C6 | count/exists 端点 | crud-features |
| C7 | `api<T>` 自定义动作扩展点 | crud-features |
| C8 | 复合主键支持 | crud-features |
| E1 | 文档同步（README/quick-start/文档站）+ 回归 | docs-sync |

## 子任务映射

- parent: framework-issues（本任务，仅协调与最终集成验收，不直接实现）
- child: test-foundation（F1）
- child: mapping-hardening（F2、D1、D2、D3，依赖 test-foundation）
- child: filter-dsl（B1、B2、B3，依赖 mapping-hardening 的映射组件）
- child: crud-features（C1-C8，依赖 test-foundation）
- child: docs-sync（E1，依赖全部功能子任务）

## 跨子任务验收标准

- [ ] A1. 每个功能子任务完成时，框架测试全部通过（`./gradlew test`）。
- [ ] A2. 对外 DSL 语法保持兼容（新增为扩展，不破坏现有 `api<T>`/`filter`/`fetcher`/`input` 用法）。
- [ ] A3. 行为变化（如 `eq?` ext、`key()`、SaveMode 配置）有文档说明并同步到 docs-sync。
- [ ] A4. 所有子任务完成后，sample（book-service）可编译运行，功能不回退。

## Out of Scope

- 不重构 sample 业务代码（仅为验证可调整）。
- 不引入 OpenAPI、鉴权、软删除策略等未列入清单的新特性（可留待后续任务）。
