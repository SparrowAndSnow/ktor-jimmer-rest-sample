# 查询与错误：执行计划

## 有序检查清单

1. util 模块：新增 `ApiError.kt`（含 `jimmerRestErrors()`）；`gradle/libs.versions.toml` 增加 `ktor-server-status-pages` compileOnly。
2. route 模块：新增 `Count.kt`（`Route.count`）与 `Exists.kt`（`Route.exists`）。
3. `Api.kt`：始终注册 count / exists。
4. `Query.kt`：404 返回 `ApiError.notFound()`。
5. 测试基建：`TestApplication.kt` 的 StatusPages 改用 `jimmerRestErrors()`。
6. 测试：`CountExistsTest`（count 过滤/全量、exists true/false、404 envelope）；IdTest/CreateTest/ErrorTest 补充 body 断言。
7. sample `Frameworks.kt`：改用 `jimmerRestErrors()`。
8. `./gradlew test` 全绿；sample `:book-service:compileKotlin`。
9. 提交（子模块 + sample + 指针），归档 + 日志。

## 验证命令

```bash
cd ktor-jimmer-rest/ktor-jimmer-rest && bash gradlew test
cd sample && bash gradlew :book-service:compileKotlin
```

## 风险点 / 回滚点

- 回滚点：ApiError / Count / Exists / Query.kt 404 改动可整体还原。
- `fetchUnlimitedCount` 若与 Jimmer 0.11.5 签名不符，改用 `select(rowCount()).fetchOne()`。
- `jimmerRestErrors()` 的 Throwable 兜底与用户自定义 handler 的顺序——文档说明"先注册自定义 handler"。

## 完成后的检查

- 61 个既有测试 + 新增用例全绿。
- 404/400/500 均输出 envelope（有 body 断言）。
- sample 编译无新增错误。
