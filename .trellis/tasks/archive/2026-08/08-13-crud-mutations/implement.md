# 写操作增强 + Provider/Route 架构重构：执行计划

## 有序检查清单

1. provider 模块：
   - `KeyProvider` 增加 `keyResolver: ((RoutingCall) -> Any?)?`；
   - `CreateProvider`/`EditProvider` 增加 `saveMode`/`associatedSaveMode`/`fetcher`（响应投影）属性；
   - `CreateScope`/`EditScope` 补齐默认值（INSERT_ONLY/MERGE、UPDATE_ONLY/UPDATE）。
2. route 模块：
   - 新增 `ApiConfig`/`CreateConfig`/`EditConfig`（或复用现有类型改造）；
   - `Api.kt` 改为注册期求值 + 配置直连；新增 `patch` 按需注册；
   - 新增 `Patch.kt`（`Route.patch`）；
   - `Create.kt`/`Edit.kt` 使用配置的 SaveMode 并支持响应投影；
   - id/remove 使用 `keyResolver`。
3. 测试：新增 `MutationConfigTest`（注册期求值计数、upsert、响应投影、PATCH 按需、key resolver、顶层 input 兼容）。
4. `./gradlew test` 全绿。
5. sample `:book-service:compileKotlin`（预期仅既有 HttpClient.kt 错误）。
6. 提交（子模块 + 父仓库指针）。

## 验证命令

```bash
cd ktor-jimmer-rest/ktor-jimmer-rest && bash gradlew test
cd sample && bash gradlew :book-service:compileKotlin
```

## 风险点 / 回滚点

- 回滚点：重构提交前可整体还原（改动集中在 provider 与 route 模块 + 测试）。
- 若外部用法存在顶层 `call`（非本仓库），破坏点已在 PRD/设计中标明，实施时以 sample + README 为准。
- Ktor 3.5 若 `patch(path)` 方法不存在，改用 `route(path, HttpMethod.Patch)`。
- 注册期求值后 filter 内的 `call` 仍可用（FilterScope.call），需回归锁定。

## 完成后的检查

- 45 个既有测试 + 新增用例全绿。
- `api` 用法（无顶层 call）与重构前行为一致（测试覆盖）。
- sample 编译无新增错误。
