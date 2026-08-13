# 批量操作：批量创建/删除/更新

## Goal

为框架增加批量 CRUD（C2）：`batch {}` 配置块按需启用三个批量端点（数组创建、数组更新、按 id 批量删除），复用 create/edit 配置（校验、转换、SaveMode）。

## Background

- 现有 create/edit/remove 均为单实体操作；Jimmer 提供 `saveEntitiesCommand`（批量保存，支持 `setMode`/`setAssociatedModeAll`）与 `deleteByIds`。
- `api<T>` 已重构为注册期求值（ApiConfig），可按配置条件注册路由（与 `patch {}` 同模式）。
- `SaveSupport.performSave` 中的"校验 + 转换"逻辑可提取复用（`SaveProvider.prepare`）。
- 依赖 crud-mutations 的架构重构（C1/C4/C5 已归档）。

## Requirements

R1. `batch {}` 配置块启用批量端点（复用 create/edit 配置）；未启用不注册（405）。
R2. `POST {path}/batch`：JSON 数组批量创建；逐条校验 + 转换，`saveEntitiesCommand` 使用 create 配置的 SaveMode/AssociatedSaveMode。
R3. `PUT {path}/batch`：JSON 数组批量更新；使用 edit 配置。
R4. `DELETE {path}/batch?ids=1,2,3`：按 id 批量删除；id 类型取自实体 `@Id` 属性。
R5. 提供独立路由函数 `Route.createBatch`/`updateBatch`/`deleteBatch`（与 create/edit 风格一致）。
R6. 测试覆盖：批量创建成功、含非法条目 400（全批失败）、批量更新、批量删除、未启用 405、批量 upsert（可选）。

## Acceptance Criteria

- [ ] A1. 现有 52 个测试全部通过。
- [ ] A2. 三个批量端点各有一条正向用例；校验失败全批 400。
- [ ] A3. 未启用 `batch {}` 时批量路径返回 405。
- [ ] A4. 批量创建/更新逐条应用 validator 与 transformer（有测试）。
- [ ] A5. sample 编译无新增错误。

## Out of Scope

- 批量响应投影（fetcher）——后续任务可选。
- 自定义批量路径/分页批量——非必需。
- 事务边界说明：单次请求一个 save 命令（Jimmer 语义），不额外包事务。
