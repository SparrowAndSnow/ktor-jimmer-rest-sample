# filter DSL 补齐：执行计划

## 有序检查清单

1. `RoutingCallExtension.kt`：新增 `queryParameterValues(type, name)` 与 reified 重载。
2. `Condition.kt`：
   - B1：重构 `eq?`（无后缀 > `__exact`，确定性）；
   - B2：新增 `in?`/`notIn?`/`lt?`/`gt?`/`le?`/`ge?`/`notEq?`/`isNull`；
   - B3：新增 `sort(parameterName = "sort")`；
   - 统一使用 `ParameterNames.resolveWithPath` + `ensureNoRootCollision`。
3. `TestApplication.kt`：新增 `eqRoutes`/`inRoutes`/`notInRoutes`/`comparisonRoutes`/`notEqRoutes`/`isNullRoutes`/`sortRoutes`。
4. 新增 `FilterDslTest`（B1/B2）与 `SortTest`（B3）。
5. 运行 `./gradlew test`，修复失败直至全绿。
6. 运行 sample `./gradlew :book-service:compileKotlin`（预期仅既有 HttpClient.kt 错误）。
7. 提交（子模块 + 父仓库指针）。

## 验证命令

```bash
cd ktor-jimmer-rest/ktor-jimmer-rest && bash gradlew test
cd sample && bash gradlew :book-service:compileKotlin
```

## 风险点 / 回滚点

- 回滚点：改动集中在 Condition.kt / RoutingCallExtension.kt 与测试，可整体还原。
- `isNull` 参数类型若与 KNullablePropExpression 不匹配，放宽签名（`KProperty<KExpression<P>>`）或改测试实体字段。
- `valueIn`/`asc` 等若与 Jimmer 0.11.5 签名有出入，以编译错误为准调整（已核对源码）。

## 完成后的检查

- 现有 28 个测试 + 新增用例全绿。
- B1 确定性规则有测试锁定（两种参数顺序结果一致）。
- sample 编译除既有 HttpClient.kt 问题外无新增错误。
