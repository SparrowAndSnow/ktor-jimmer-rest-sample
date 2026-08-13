# 参数映射加固与路由小坑修正：执行计划

## 有序检查清单

1. 新增 `ParameterNames.kt`（util 模块）：resolve / resolveWithPath / ensureNoRootCollision / boundReceiverOf / tableAliasOf（getter 按类缓存）。
2. 重构 `RoutingCallExtension.kt`：属性版 `queryParameterExt` 委托 ParameterNames；新增 `pathParameter(name)`；修正 `defaultPathVariable` KDoc；删除被收敛的旧 helper。
3. 删除 `Reflect.getPropertyReceiver`、`JimmerReflect.tableName`/`tableType`（确认无其他使用方后）。
4. 重构 `Condition.kt`：`eq?`/`ilike?`/`between?` 用 `ParameterNames.resolveWithPath` + `ensureNoRootCollision(table, ...)`。
5. D2：`Query.kt`/`Remove.kt` 改用 `pathParameter(pathVariable.removeSurrounding("{", "}"))`。
6. D1：`KeyProvider.key` 与 `key(...)` 补 KDoc（覆盖语义、作用范围、典型场景）。
7. 测试：
   - 新增 Order 测试实体 + H2 schema + cleanDatabase；
   - ParameterNamesTest（根表/嵌套/非 bound 错误/一致性）；
   - NestedRouteTest（D2）、KeyBehaviorTest（D1）、CollisionTest（D3）。
8. 运行 `./gradlew test`（框架根模块），修复失败直至全绿。
9. 运行 sample `./gradlew :book-service:compileKotlin`（预期仅既有 HttpClient.kt 错误）。
10. 提交（子模块 + 父仓库指针）。

## 验证命令

```bash
cd ktor-jimmer-rest/ktor-jimmer-rest && bash gradlew test
cd sample && bash gradlew :book-service:compileKotlin
```

## 风险点 / 回滚点

- 回滚点：ParameterNames 提交前可整体还原（改动集中在 util 模块与两个 route 文件）。
- D3 检测放请求期（api 块请求时执行的设计），首次请求才报错；如需要注册期检测，留给后续任务评估。
- 若 `javaTable` getter 反射在 Jimmer 0.11.5 行为与预期不符，以现有 ParameterMappingTest 为基准修正。

## 完成后的检查

- 现有 19 个测试 + 新增用例全绿。
- 无 `src/main` 中未使用的旧反射 helper 残留。
- sample 编译除既有 HttpClient.kt 问题外无新增错误。
