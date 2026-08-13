# 扩展与边界：执行计划

## 有序检查清单

1. `ApiConfig.kt`：新增 `customRoutes` 属性与 `action {}` 扩展。
2. `Api.kt`：内置路由后 `config.customRoutes?.invoke(this)`。
3. `JimmerReflect.kt`：`entityIdType` 防御性校验（@Id 数量必须为 1）。
4. 测试：新增 `CustomActionTest` + 测试路由帮助（`customActionRoutes`）。
5. `./gradlew test` 全绿。
6. sample `:book-service:compileKotlin`（预期仅既有 HttpClient.kt 错误）。
7. 提交（子模块 + 指针），归档 + 日志。

## 验证命令

```bash
cd ktor-jimmer-rest/ktor-jimmer-rest && bash gradlew test
cd sample && bash gradlew :book-service:compileKotlin
```

## 风险点 / 回滚点

- 回滚点：ApiConfig/Api.kt/JimmerReflect 改动可整体还原。
- C8 防御性代码在 Jimmer 约束下不可达——保持 3 行以内，避免过度设计。

## 完成后的检查

- 65 个既有测试 + 新增用例全绿。
- `action` 路由可用且位于 `{path}` 下。
- sample 编译无新增错误。
