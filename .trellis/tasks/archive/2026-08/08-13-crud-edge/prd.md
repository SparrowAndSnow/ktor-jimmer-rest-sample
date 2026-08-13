# 扩展与边界：自定义动作与复合主键

## Goal

为 `api<T>` 提供自定义动作扩展点（C7），并明确复合主键边界（C8）：Jimmer 不支持复合 `@Id`（已实证 KSP 报错），框架据此做防御性检测 + 文档化，不虚假承诺"支持复合主键"。

## Background

- `api<T>` 已重构为注册期求值（ApiConfig），具备在注册期注册额外路由的条件。
- 实证：Jimmer 0.11.5 KSP 对双 `@Id` 实体报错 `too many properties are decorated by @Id`——复合主键在 ORM 层不支持。
- `entityIdType<T>()` 目前取第一个 `@Id` 属性（`JimmerReflect.kt`），在 Jimmer 约束下恒为唯一，但缺防御性校验。
- 依赖 crud-mutations 的注册期架构。

## Requirements

R1. C7：新增 `action { }` 配置块，在 api 注册期注册自定义路由（block 接收者为 `Route`，可注册任意 Ktor 路由到 `{path}` 下）。
R2. C7：自定义动作在**内置路由之后**注册（内置优先，自定义不遮蔽内置，除显式同路径覆盖）。
R3. C8：`entityIdType` 增加防御性校验——`@Id` 属性数 ≠ 1 时抛清晰错误（含"Jimmer 不支持复合主键"提示）。
R4. C8：KDoc/文档说明复合主键不支持（Jimmer 限制）与推荐模式（代理主键 + `@Key` 业务键；`@Key` 复合业务键在 save/upsert 已可用）。
R5. 测试：自定义动作注册与请求（stats 计数、路径参数动作）；C8 防御性不可测（Jimmer KSP 编译期拒绝），以代码审查为准。

## Acceptance Criteria

- [ ] A1. 现有 65 个测试全部通过。
- [ ] A2. `action { }` 注册的自定义路由可访问（有 GET/POST 用例），且仅注册一次（注册期求值）。
- [ ] A3. 自定义动作路径位于 `{path}` 下，与内置路由不冲突。
- [ ] A4. `entityIdType` 含多/零 `@Id` 的防御性报错（代码审查 + KDoc）。
- [ ] A5. sample 编译无新增错误。

## Out of Scope

- 不实现复合主键（Jimmer 不支持，属 ORM 边界）。
- 不提供业务键（`@Key`）查询路由——现有 save/upsert 已覆盖复合业务键；查询类接口如有需要另行评估。
- 不改造独立路由函数（`Route.id` 等）的 action 能力（api 专属）。
