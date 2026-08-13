# 写操作增强 + Provider/Route 架构重构

## Goal

重构 `api<T>` 为"注册期求值"架构（block 只执行一次，构建纯配置对象），在此基础上实现 C1（SaveMode 可配置）、C4（create/edit 响应投影）、C5（PATCH 部分更新）。保持对外 DSL 用法兼容（`api`/`filter`/`fetcher`/`input` 不变）。

## Background

- 现 `api<T>` 的 block 在每次请求时重新执行（`Api.kt` 每个路由处理器内 `ApiScope(call).apply { block() }`），配置每次请求重建；Api.kt 依赖"跑完 block 再拷字段"的复制模式，脆弱且阻碍注册期特性（按需注册 PATCH、自定义动作）。
- `Create.kt:24` 固定 `INSERT_ONLY`/`MERGE`；`Edit.kt:24` 固定 `UPDATE_ONLY`/`UPDATE`，无法 upsert。
- create/edit 响应固定返回完整 `modifiedEntity`，无法投影；无 PATCH 路由。
- 依赖 test-foundation（45 测试）与 mapping-hardening。

## Requirements

R0. 架构重构（注册期求值）：
   - `ApiScope` → `ApiConfig`（纯配置，无 `call`），`api` block 在注册时执行一次；
   - 依赖 `call` 的部分改为请求期 lambda：filter 已是 `(query, call) -> Unit`（不变），key 增加 `keyResolver: (RoutingCall) -> Any?`；
   - `Api.kt` 用配置直连各路由，删除"跑 block + 拷字段"模式；PATCH 可按配置条件注册；
   - 顶层 `input {}` 继续同时配置 create/edit（兼容）。
R1. C1：create/edit 独立配置（`create {}`/`edit {}` 块），`saveMode`/`associatedSaveMode` 可配置（默认值不变），支持 upsert（`SaveMode.MERGE`）。
R2. C4：create/edit 支持 `fetcher {}` 作为响应投影；保存后按 id + fetcher 重新查询返回；未配置返回 `modifiedEntity`。
R3. C5：新增 `Route.patch`（PATCH 集合路径，`UPDATE_ONLY` 部分更新）；`api` 中 `patch {}` 块按需启用并复用 edit 配置。
R4. 测试覆盖：注册期求值回归（现有 45 测试）、SaveMode upsert、响应投影、PATCH 部分更新、`key` resolver、顶层 input 兼容。

## Acceptance Criteria

- [ ] A1. 现有 45 个测试全部通过（含 `api` 用法兼容性回归）。
- [ ] A2. `api` block 注册期求值验证：block 内副作用只发生一次（测试/审查确认）。
- [ ] A3. `create { saveMode = SaveMode.MERGE }` upsert 生效；`create { fetcher {} }`/`edit { fetcher {} }` 响应按投影返回。
- [ ] A4. `patch {}` 启用后 PATCH 路由可用（部分字段更新不覆盖其他字段）；未配置不注册 PATCH。
- [ ] A5. `key { call -> ... }` resolver 生效（覆盖路径变量语义保持）。
- [ ] A6. sample 编译无新增错误。

## Out of Scope

- 批量操作（crud-batch）、count/exists + 错误响应（crud-queries-errors）、自定义动作 + 复合主键（crud-edge）。
- 不改独立路由函数（`Route.id/list/create/edit/remove`）的按请求 block 语义（低层 API，api 重构不破坏）。
- 不改变 PUT 现有语义（PATCH 与 PUT 共享 edit 配置，KDoc 说明）。
