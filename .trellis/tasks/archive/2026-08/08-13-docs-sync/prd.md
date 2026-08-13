# 文档同步与回归收尾

## Goal

把本阶段全部框架改动同步到 README 与文档站（中英），使文档与实际 API 一致；随后完成父任务 framework-issues 的跨任务验收与归档。

## Background

- 本阶段新增能力（均已测试锁定）：filter 操作符（`eq?`/`notEq?`/`in?`/`notIn?`/`lt?`/`gt?`/`le?`/`ge?`/`isNull`）、`sort()` 动态排序、注册期求值架构、`create {}`/`edit {}`/`patch {}`/`batch {}` 配置块、SaveMode（UPSERT）、响应投影、count/exists 端点、`ApiError` envelope（`jimmerRestErrors()`）、`endpoint {}` 配置（batchPath/countPath/existsPath/sortParameterName 等）、`action {}` 自定义路由。
- 文档现状：`ktor-jimmer-rest/README.md`（英文）、`docs/zh/quick-start.md`、`docs/en/quick-start.md`、`zh/en index.md`、`sample/README.md`——均为早期内容，未覆盖上述新特性。

## Requirements

R1. 框架 README：更新 Usage（新操作符表、配置块示例）、端点清单（含 count/exists/batch/patch/action）、endpoint 配置说明、错误响应（ApiError/jimmerRestErrors）。
R2. 文档站中文 quick-start：新增"过滤与排序""写操作配置（create/edit/patch/batch）""count/exists""统一错误响应""endpoint 配置""自定义动作"小节，示例与源码一致。
R3. 文档站英文 quick-start：与中文对齐。
R4. 首页（zh/en index）：特性列表同步（如批量、错误 envelope、自定义动作）。
R5. sample/README.md：curl 示例更新（PATCH、batch、count、exists、新 filter 参数）。
R6. mkdocs build 通过；无 TODO/死链。

## Acceptance Criteria

- [ ] A1. mkdocs 中英文站点 build 通过，无死链、无 TODO 残留。
- [ ] A2. README/quick-start 覆盖全部新特性，代码示例与源码一致（对照实现）。
- [ ] A3. 端点清单准确（含按需注册的 PATCH/batch、始终注册的 count/exists）。
- [ ] A4. 错误响应文档说明 envelope 结构。
- [ ] A5. sample/README 的 curl 示例可复现。

## Out of Scope

- 不新增独立功能页面（示例页保持现有结构）。
- 不修改框架/sample 代码（仅文档）。
