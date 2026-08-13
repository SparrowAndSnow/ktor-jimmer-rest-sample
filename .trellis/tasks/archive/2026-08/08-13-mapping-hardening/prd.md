# 参数映射加固与路由小坑修正

## Goal

把"属性引用 → 查询参数名"的反射映射封装为独立、可测、带清晰错误的组件（F2），并修复三个小坑：`key()` 语义文档化（D1）、`defaultPathVariable` 按注册名取参（D2）、嵌套字段名冲突检测（D3）。现有 DSL 行为保持兼容。

## Background

- 现映射逻辑在 `RoutingCallExtension.queryParameterExt(property)`（`RoutingCallExtension.kt:70-79`）：强转 `PropertyReference0Impl` 取 bound receiver（`Reflect.kt:29`），再反射读 Jimmer 内部 `javaTable` 属性并 toString 取别名（`JimmerReflect.kt`），路径 = 别名 `.split(".").drop(1)` + 属性名，用 `subParameterSeparator` 连接。
- `defaultPathVariable` 取 `pathParameters.names().first()`（`RoutingCallExtension.kt:181`）——外层 `route("/{tenant}")` 嵌套 `api` 时取错参数。
- `key()` 同时作用于 id/remove 且覆盖路径变量（`Query.kt:21`），无任何文档，容易误用。
- 嵌套字段 `store.name` 映射为 `store_name`，与根实体上名为 `store_name` 的标量属性冲突（参数名相同）。
- 反射工具（`getPropertyReceiver`/`tableName`/`tableType`）只被 `RoutingCallExtension` 使用，可安全收敛（已确认无其他使用方）。
- 父任务 framework-issues 的 F2、D1、D2、D3；依赖 test-foundation 的测试基线。

## Requirements

R1. 新增 `ParameterNames` 组件（util 模块）：
   - `resolve(property)` → 查询参数名（根表/嵌套表），行为与现状一致；
   - 非 bound 引用给出清晰错误（不再裸 `ClassCastException`）；
   - `javaTable` 的 getter 反射按类缓存，减少每请求反射；别名值不缓存（同一类不同实例别名不同）。
R2. `RoutingCallExtension.queryParameterExt(property)` 改用组件；无其他使用方的旧 helper 收敛或删除。
R3. D2：id/remove 路由按注册的 `pathVariable` 名取路径参数（`{id}` → `"id"`），嵌套 route 不再取错；`defaultPathVariable` 保留兼容并修正文档。
R4. D1：`key()` 语义文档化（覆盖路径变量、作用于 id/remove、典型场景为上下文 id），行为兼容。
R5. D3：嵌套参数名与根实体标量属性（同名字段）冲突时，请求期 fail-fast 抛清晰错误，提示调整 `router.subParameterSeparator` 或字段名；根表信息来自 FilterScope 的 `table`。
R6. 测试覆盖：现有 19 个测试保持通过；新增 resolve 根表/嵌套/非 bound 错误信息、缓存行为、D2 嵌套 route、D1 key 覆盖、D3 冲突各至少一条用例。

## Acceptance Criteria

- [ ] A1. 现有测试全部通过（无行为回退）。
- [ ] A2. 新测试覆盖 F2 / D1 / D2 / D3 各至少一条用例。
- [ ] A3. 非 bound 引用报错信息包含明确原因（提示使用 `table::name` 形式）。
- [ ] A4. 外层带路径参数时，id/remove 仍按 `{id}` 正确取参。
- [ ] A5. D3 冲突场景抛异常且错误信息提示可配置 separator。
- [ ] A6. sample 编译不受影响（已知 `HttpClient.kt` 既有问题除外）。

## Out of Scope

- 不改 filter 操作符集合（`eq?` ext、`in?` 等属于 filter-dsl 子任务 B1/B2/B3）。
- 不引入代码生成/注解式参数名方案（进阶项，另行评估）。
- 不修 sample 的 `HttpClient.kt`（既有问题，另行处理）。
