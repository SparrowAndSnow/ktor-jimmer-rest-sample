# 查询与错误：count/exists 与统一错误响应

## Goal

增加 count/exists 端点（C6），并统一错误响应为 error envelope（C3）：框架自产错误（404）与用户侧 StatusPages 处理均输出统一结构。

## Background

- 列表接口支持 filter + 分页，但无 count；`GET /book/{id}` 不存在时返回 404 空 body。
- 错误响应现状：校验 400 `{errors:[...]}`、参数解析 400 纯文本、500 纯文本，由用户手工配置 StatusPages，格式不统一。
- `fetchUnlimitedCount()`（Jimmer）可做"忽略分页/排序的总数查询"；`findById` 可用于 exists 判断。
- 依赖 test-foundation（61 测试）、mapping-hardening、crud-mutations 架构。

## Requirements

R1. C6-count：`GET {path}/count` 返回过滤后的总数（应用 filter，忽略分页/排序）；api 中始终注册。
R2. C6-exists：`GET {path}/exists/{id}` 返回是否存在（支持 key/keyResolver 解析）；api 中始终注册。
R3. C3：新增 `ApiError`（status/code/message/errors）响应类型（util 模块）。
R4. C3：id 路由 404 改为返回 `ApiError` envelope（带 body）。
R5. C3：提供 `StatusPagesConfig.jimmerRestErrors()` 帮助函数，统一处理 ValidationException/ParseException/Throwable 为 envelope；sample 使用之。
R6. 测试：count 过滤计数、exists true/false、404 body 为 envelope、校验/解析错误 body 为 envelope。

## Acceptance Criteria

- [ ] A1. 现有 61 个测试全部通过（状态码不变，body 结构按预期变化）。
- [ ] A2. `GET /book/count` 返回过滤后的数量（与列表一致）；与 `{id}` 路由无冲突。
- [ ] A3. `GET /book/exists/{id}` 返回布尔。
- [ ] A4. 404/400/500 响应均为 `ApiError` 结构（code/message/errors 字段齐全）。
- [ ] A5. sample 使用 `jimmerRestErrors()`，编译无新增错误。

## Out of Scope

- 批量端点错误响应（沿用统一 envelope 即可，无需专项）。
- 国际化/自定义 code 字典——envelope 保留扩展空间，不实现。
- 错误日志/追踪 ID 等可观测性增强。
