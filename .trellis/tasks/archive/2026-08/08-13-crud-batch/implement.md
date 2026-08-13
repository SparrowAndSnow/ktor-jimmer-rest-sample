# 批量操作：执行计划

## 有序检查清单

1. `SaveSupport.kt`：提取 `SaveProvider.prepare`，`performSave` 复用之。
2. 新增 `Batch.kt`（route 模块）：`Route.createBatch`/`updateBatch`/`deleteBatch`。
3. `ApiConfig.kt`：`batchEnabled` + `batch {}` 扩展。
4. `Api.kt`：`if (config.batchEnabled)` 注册三个批量端点。
5. 测试：新增 `BatchTest` + 测试路由帮助（`batchRoutes`）；扩展 `TestApplication.kt`。
6. `./gradlew test` 全绿。
7. sample `:book-service:compileKotlin`（预期仅既有 HttpClient.kt 错误）。
8. 提交（子模块 + 父仓库指针），归档 + 日志。

## 验证命令

```bash
cd ktor-jimmer-rest/ktor-jimmer-rest && bash gradlew test
cd sample && bash gradlew :book-service:compileKotlin
```

## 风险点 / 回滚点

- 回滚点：Batch.kt / ApiConfig / Api.kt / SaveSupport 改动可整体还原。
- `receive<List<TEntity>>()` 需要 ContentNegotiation 反序列化数组（测试基建已配置）。
- `result.entities` 若与 Jimmer 0.11.5 签名不符，以 `KBatchSaveResult` 实际 API 调整。

## 完成后的检查

- 52 个既有测试 + 新增用例全绿。
- 批量端点按需注册（未启用 405 有测试）。
- sample 编译无新增错误。
