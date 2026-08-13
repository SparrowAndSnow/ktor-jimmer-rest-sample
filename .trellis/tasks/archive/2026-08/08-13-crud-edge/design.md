# 扩展与边界：技术设计

## C7：自定义动作扩展点

### ApiConfig 扩展

```kotlin
// ApiConfig
var customRoutes: (Route.() -> Unit)? = null

fun <T : Any> ApiConfig<T>.action(block: Route.() -> Unit) {
    customRoutes = block
}
```

### Api.kt 接线（内置路由之后）

```kotlin
route(path) {
    // ... 内置路由（id/list/count/exists/create/edit/patch/batch/remove）...
    config.customRoutes?.invoke(this)
}
```

`action` 的 block 在注册期执行一次，handler 内可访问 `call`、`sqlClient` 等（与普通 Ktor 路由一致）。

### 使用示例

```kotlin
api<Book>("/book") {
    action {
        get("stats") {
            val count = sqlClient.createQuery(Book::class) {
                select(rowCount())
            }.fetchUnlimitedCount()
            call.respond(mapOf("count" to count))
        }
        post("{id}/publish") {
            val id = call.pathParameters["id"]?.toLong()
            call.respond(mapOf("id" to id, "published" to true))
        }
    }
}
```

## C8：复合主键边界

### 实证结论

Jimmer 0.11.5 KSP 对双 `@Id` 实体报 `too many properties are decorated by @Id`——复合主键在 ORM 层不支持，框架无法也不应"实现"。

### entityIdType 防御性校验（JimmerReflect.kt）

```kotlin
inline fun <reified T : Any> entityIdType(): KClass<*> {
    val ids = T::class.memberProperties
        .filter { it.annotations.any { a -> a.annotationClass == Id::class } }
    require(ids.size == 1) {
        "实体 ${T::class.simpleName} 必须且只能有一个 @Id 属性（Jimmer 不支持复合主键），实际 ${ids.size} 个"
    }
    return ids.single().returnType.classifier as KClass<*>
}
```

### 文档要点（docs-sync 落）

- 复合主键不支持（Jimmer 限制）；推荐代理主键（`@Id` + IDENTITY）+ `@Key` 业务键。
- `@Key` 复合业务键（如 Book 的 name+edition）在 create/upsert 已可用（有测试）。

## 测试设计

| 场景 | 断言 |
|------|------|
| `GET /book-custom/stats` | 返回 `{"count":N}`（与库中条数一致） |
| `POST /book-custom/{id}/publish` | 返回 `{"id":X,"published":true}` |
| 自定义动作仅注册一次 | 注册期求值（现有计数测试模式可复用，或代码审查） |
| C8 防御性 | 不可测（KSP 编译期拒绝双 @Id），代码审查 + KDoc 为准 |

## 兼容性与风险

- `action` 注册在内置路由之后：同路径同方法的内置路由优先（Ktor 先注册先匹配）。
- 自定义动作可访问 `com.eimsound.jimmer.sqlClient` 全局客户端（公开 API）。
- 风险：`action` block 若捕获外部可变状态并在 handler 中使用，需自行注意并发（注册期捕获的闭包共享）——文档提示。
