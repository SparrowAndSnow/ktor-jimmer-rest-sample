# 测试地基：技术设计

## 测试位置与结构

- 测试放在根聚合模块 `ktor-jimmer-rest/ktor-jimmer-rest` 的 `src/test/kotlin`：根模块 `api(project(...))` 依赖全部子模块，适合承载集成式 route 测试。
- 目录：
  ```text
  src/test/kotlin/com/eimsound/rest/test/
  ├── entity/          # Book / BookStore / Author 测试实体
  ├── dto/             # BookView / BookSpec / BookInput（.dto 文件）
  ├── infra/           # TestJimmer.kt（KSqlClient）、TestApplication.kt（testApplication 帮助函数）
  └── route/           # IdTest / ListTest / CreateTest / EditTest / RemoveTest / ErrorTest
  ```

## 依赖与构建

`gradle/libs.versions.toml` 新增：

```toml
ksp = "2.3.11"                       # 与 sample 对齐
junit-jupiter = "5.10.2"
h2 = "2.2.224"
ktor-server-test-host = { module = "io.ktor:ktor-server-test-host-jvm", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation-jvm", version.ref = "ktor" }
ktor-serialization-jackson = { module = "io.ktor:ktor-serialization-jackson", version.ref = "ktor" }
```

根模块 `build.gradle.kts` 增加：

```kotlin
alias(libs.plugins.ksp)                       // plugins
testImplementation(libs.ktor.server.test.host)
testImplementation(libs.ktor.client.content.negotiation)
testImplementation(libs.ktor.serialization.jackson)
testImplementation(libs.junit.jupiter)
testImplementation(libs.h2)
kspTest(libs.jimmer.ksp)
ksp { arg("jimmer.dto.mutable", "true") }      // 生成 Draft/Input 需要
```

注意：root 模块的 `subprojects` 块只作用于子模块；root 自身的测试依赖需要在 root 的 `dependencies` 块声明。

## 测试基础设施

### TestJimmer：H2 内存库 + KSqlClient

```kotlin
val dataSource = JdbcDataSource().apply {
    setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
    user = "sa"
}

val sqlClient = JSqlClient.newKsqlClient {
    setDialect(H2Dialect())
    setConnectionManager(dataSource)
    setDatabaseInitializer(InitializerType.SCRIPT) // Jimmer 按实体自动建表
}
```

备选：若 `InitializerType.SCRIPT` 对 H2 不生效，改为在测试启动时执行手写 `schema.sql`。

### TestApplication：可复用的 testApplication

```kotlin
fun Application.jimmerRestTestModule() {
    install(ContentNegotiation) { jackson { } }
    install(JimmerRest) {
        jimmerSqlClientFactory { lazy { testSqlClient } }
    }
    routing {
        api<Book>("/book") {
            filter { /* 与 sample 对齐的 ilike?/between? */ }
            fetcher { fetch.by { allScalarFields(); store { name() } } }
            input { validator { ... }; transformer { ... } }
        }
    }
}
```

每个测试类用 `testApplication { application { jimmerRestTestModule() } }` 启动，`client` 发请求断言。

## 关键风险与对策

| 风险 | 对策 |
|------|------|
| KSP 版本与 Kotlin 2.4.10 不兼容 | 直接对齐 sample 已使用的 2.3.11 |
| Jimmer 自动建表在 H2 上不可用 | 备选手写 schema.sql 初始化 |
| `Page` 响应序列化形状不稳定 | 测试只断言可稳定部分（rows 长度/字段）；分页形状变更留给 C 系列任务 |
| root 模块加入测试依赖影响发布 | 只加 testImplementation/kspTest，不影响 main 产物 |

## 兼容性说明

- 本任务不引入任何生产 API 变化；测试仅依赖当前公开 DSL（`api`/`filter`/`fetcher`/`input`）。
- 测试 DSL 配置与 sample book-service 保持一致，保证 sample 也是测试覆盖对象。
