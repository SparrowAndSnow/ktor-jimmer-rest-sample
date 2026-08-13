# CRUD 功能补齐：SaveMode、批量、错误响应等

## Goal

补齐 CRUD 功能缺口（父任务 framework-issues 的 C1-C8），按可独立验收的子任务分批交付：写操作增强、批量操作、查询与错误、扩展与边界。

## Background

- create/edit 的 SaveMode 写死 `INSERT_ONLY`/`UPDATE_ONLY`（`Create.kt:24`、`Edit.kt:24`），无 upsert。
- 无批量创建/删除/更新；无 PATCH；create/edit 响应不支持 fetcher 投影（返回完整 `modifiedEntity`）。
- 无 count/exists；错误响应不统一（404 空 body、校验 400 `{errors}`、解析错误纯文本、500 纯文本）。
- `api<T>` 没有自定义动作扩展点；`entityIdType` 只取第一个 `@Id` 属性（`JimmerReflect.kt`）。
- 依赖 test-foundation 的测试基线（45 个测试）与 mapping-hardening 的 ParameterNames 组件。

## 需求清单（映射到子任务）

| ID | 需求 | 子任务 |
|----|------|--------|
| C1 | create/edit 的 SaveMode/AssociatedSaveMode 可配置（含 upsert） | crud-mutations |
| C4 | create/edit 响应支持 fetcher 投影 | crud-mutations |
| C5 | PATCH/部分更新语义 | crud-mutations |
| C2 | 批量创建/删除/更新（saveAll/deleteAll） | crud-batch |
| C6 | count/exists 端点 | crud-queries-errors |
| C3 | 统一错误响应（error envelope，404 带 body） | crud-queries-errors |
| C7 | `api<T>` 自定义动作扩展点 | crud-edge |
| C8 | 复合主键支持 | crud-edge |

## 子任务映射

- parent: crud-features（本任务，仅协调与最终集成验收，不直接实现）
- child: crud-mutations（C1、C4、C5，含 ApiScope create/edit 独立配置块重构）
- child: crud-batch（C2，依赖 crud-mutations 的 SaveMode 配置）
- child: crud-queries-errors（C6、C3）
- child: crud-edge（C7、C8，依赖 crud-mutations 的 DSL 结构）

## 跨子任务验收标准

- [ ] A1. 每个子任务完成时框架测试全部通过（`./gradlew test`）。
- [ ] A2. 对外 DSL 兼容：现有 `api<T>`/`filter`/`fetcher`/`input` 用法不破坏（`input {}` 顶层块继续同时作用于 create/edit）。
- [ ] A3. 新增端点/配置有测试覆盖。
- [ ] A4. 全部完成后 sample（book-service）编译不受影响（除既有 HttpClient.kt 问题）。

## Out of Scope

- OpenAPI、鉴权、软删除策略等未列入清单的新特性。
- sample 业务代码改动（仅验证需要时可调整）。
