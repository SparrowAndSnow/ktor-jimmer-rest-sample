# 查询与错误：技术设计

## C6：count / exists

### count 路由（List.kt 或新 Count.kt）

```kotlin
inline fun <reified TEntity : Any> Route.count(
    path: String = "count",
    crossinline block: suspend ListProvider<TEntity>.() -> Unit,
) = get(path) {
    val provider = ListScope<TEntity>(call).apply { block() }
    val filter = provider.filter
    val count = sqlClient.createQuery(TEntity::class) {
        filter.invoke(this, call)
    }.fetchUnlimitedCount()
    call.respond(count)
}
```

`fetchUnlimitedCount()` 忽略排序与分页，只应用 where——与列表过滤语义一致。Ktor 路由中常量段 `count` 优先于参数段 `{id}`，无冲突。

### exists 路由（Query.kt 或新 Exists.kt）

```kotlin
inline fun <reified TEntity : Any> Route.exists(
    path: String = "exists/{id}",
    crossinline block: suspend QueryProvider<TEntity>.() -> Unit,
) = get(path) {
    val provider = QueryScope<TEntity>(call).apply { block() }
    val key = call.resolveKey(provider, "{id}")
    val exists = sqlClient.findById(TEntity::class, key) != null
    call.respond(exists)
}
```

复用 `resolveKey`（key → keyResolver → 路径参数）。

### Api.kt 接线（始终注册）

```kotlin
count<TEntity>("count") {
    filter = config.filter
}
exists<TEntity>("exists/{id}") {
    key = config.key
    keyResolver = config.keyResolver
}
```

## C3：统一错误 envelope

### ApiError（util 模块，`com.eimsound.util.ktor`）

```kotlin
data class ApiError(
    val status: Int,
    val code: String,
    val message: String,
    val errors: List<String> = emptyList(),
) {
    companion object {
        fun notFound(message: String = "Not Found") = ApiError(404, "NOT_FOUND", message)
        fun badRequest(message: String, errors: List<String> = emptyList()) =
            ApiError(400, "BAD_REQUEST", message, errors)
        fun internal(message: String? = null) =
            ApiError(500, "INTERNAL_ERROR", message ?: "Internal Server Error")
    }
}
```

### 404（Query.kt）

```kotlin
if (result != null) call.respond(result) else call.respond(HttpStatusCode.NotFound, ApiError.notFound())
```

### jimmerRestErrors() 帮助函数（util 模块）

```kotlin
fun StatusPagesConfig.jimmerRestErrors() {
    exception<ValidationException> { call, cause ->
        call.respond(cause.httpStatusCode, ApiError.badRequest(cause.errors.joinToString(), cause.errors))
    }
    exception<ParseException> { call, cause ->
        call.respond(HttpStatusCode.BadRequest, ApiError.badRequest(cause.message ?: "Bad Request"))
    }
    exception<Throwable> { call, cause ->
        call.respond(HttpStatusCode.InternalServerError, ApiError.internal(cause.message))
    }
}
```

util 模块需增加 `compileOnly(ktor-server-status-pages)`。

## 测试设计

| 场景 | 断言 |
|------|------|
| `GET /book/count?name__start=...` | 与过滤后列表条数一致 |
| `GET /book/count`（无过滤） | 全量条数 |
| `GET /book/exists/{id}` 存在/不存在 | true / false |
| `GET /book/{不存在}` | 404 + body 为 ApiError（code=NOT_FOUND） |
| 校验失败 400 | body 为 ApiError（errors 含消息） |
| 参数解析失败 400 | body 为 ApiError（code=BAD_REQUEST） |
| 自定义 Throwable | 500 envelope（可选用例） |

测试基建（jimmerRestTestModule）改用 `jimmerRestErrors()`；IdTest/CreateTest/ErrorTest 补充 body 断言。

## 兼容性与风险

- 状态码不变；响应 body 从"纯文本/裸数组"变为 envelope——这是 C3 的目的，需在文档（docs-sync）说明。
- `count`/`exists` 始终注册：`GET /book/count` 由"参数解析 400"变为"正常计数"，属预期行为变化。
- 风险：`jimmerRestErrors()` 中 Throwable 兜底会吞掉用户自定义异常处理——用户应在调用前注册自己的 handler（StatusPages 按异常类型匹配，先注册的优先）。
