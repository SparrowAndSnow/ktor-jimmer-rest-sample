# 测试地基：执行计划

## 有序检查清单

1. `gradle/libs.versions.toml`：新增 ksp、junit-jupiter、h2、ktor-server-test-host、ktor-client-content-negotiation、ktor-serialization-jackson 依赖项与 ksp 插件。
2. 根模块 `build.gradle.kts`：应用 ksp 插件，添加测试依赖与 `kspTest(libs.jimmer.ksp)`、`jimmer.dto.mutable=true`。
3. 新建测试实体（Book / BookStore / Author）与 `.dto`（BookView / BookSpec / BookInput）。
4. 新建 `infra/TestJimmer.kt`：H2 数据源 + `KSqlClient`（含建表初始化）。
5. 新建 `infra/TestApplication.kt`：`testApplication` 帮助函数（ContentNegotiation + JimmerRest + api<Book> 路由，DSL 与 sample 对齐）。
6. 编写 route 测试：
   - IdTest：命中返回实体 / 不存在返回 404
   - ListTest：`ilike?`（`name__start`）、`between?`（`price__ge/__le`）、分页、`pager.enabled=false`
   - CreateTest：成功创建、校验失败 400、transformer 生效（name 大写）
   - EditTest：成功更新
   - RemoveTest：删除成功、再次删除幂等
   - ErrorTest：参数解析失败 400（ParseException 路径）
7. 编写映射测试 ParameterMappingTest：根表/嵌套表/ext/缺参四类场景。
8. 运行 `./gradlew test`（在 `ktor-jimmer-rest/ktor-jimmer-rest`），修复测试自身问题。
9. 记录行为发现（若有与预期不符的框架行为），报告给父任务，不改生产代码。
10. 提交本任务改动（仅测试 + 构建配置）。

## 验证命令

```bash
cd ktor-jimmer-rest/ktor-jimmer-rest
./gradlew test
```

可选（确认不影响 sample）：

```bash
cd sample
./gradlew :book-service:compileKotlin
```

## 风险点 / 回滚点

- `build.gradle.kts` 与 `libs.versions.toml` 的改动是回滚点 1（git 提交前可整体还原）。
- 若 KSP 代码生成失败，优先检查插件版本对齐 sample。
- 若 Jimmer H2 自动建表失败，切换为手写 schema.sql（design.md 备选方案）。

## 完成后的检查

- `./gradlew test` 通过，且测试数量记录在提交说明中。
- 确认 `src/main` 无任何改动。
