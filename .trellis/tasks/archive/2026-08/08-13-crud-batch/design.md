# 批量操作：技术设计

## 配置与注册

```kotlin
// ApiConfig
var batchEnabled = false

class BatchConfig {
    var path: String = "batch"
    var createEnabled: Boolean = true
    var updateEnabled: Boolean = true
    var deleteEnabled: Boolean = true
    var deleteIdsParameterName: String = "ids"
}

fun <T : Any> ApiConfig<T>.batch(block: BatchConfig.() -> Unit = {}) {
    batchEnabled = true
    this.batch.apply(block)
}
```

`batch {}` 启用并可配置路径/操作开关/删除参数名；批量端点复用 `config.create`（校验/转换/SaveMode）与 `config.edit`。Api.kt 中按 `batchConfig.createEnabled/updateEnabled/deleteEnabled` 条件注册，路径用 `batchConfig.path`，删除参数名用 `batchConfig.deleteIdsParameterName`：

```kotlin
if (config.batchEnabled) {
    createBatch<TEntity>("batch") {
        input = config.create.input
        validator = config.create.validator
        transformer = config.create.transformer
        saveMode = config.create.saveMode
        associatedSaveMode = config.create.associatedSaveMode
    }
    updateBatch<TEntity>("batch") { /* 用 config.edit */ }
    deleteBatch<TEntity>("batch")
}
```

## 路由实现（Batch.kt）

```kotlin
inline fun <reified TEntity : Any> Route.createBatch(
    path: String = "batch",
    crossinline block: suspend CreateProvider<TEntity>.() -> Unit,
) = post(path) {
    val provider = CreateScope<TEntity>(call).apply { block() }
    val bodies = call.receive<List<TEntity>>()
    val entities = bodies.map { provider.prepare(it) }
    val result = sqlClient.saveEntitiesCommand(entities) {
        setMode(provider.saveMode)
        setAssociatedModeAll(provider.associatedSaveMode)
    }.execute()
    call.respond(result.entities)
}
```

`updateBatch` 同构（EditProvider）；`deleteBatch`：

```kotlin
inline fun <reified TEntity : Any> Route.deleteBatch(path: String = "batch") = delete(path) {
    @Suppress("UNCHECKED_CAST")
    val idType = entityIdType<TEntity>() as KClass<Any>
    val ids = call.queryParameterValues(idType, "ids")
    sqlClient.deleteByIds(TEntity::class, ids)
    call.response.status(HttpStatusCode.OK)
}
```

`queryParameterValues` 复用现有工具（逗号分隔/重复参数，解析失败 400）。

## 公共校验/转换提取（SaveSupport.kt）

```kotlin
@PublishedApi
internal fun <T : Any> SaveProvider<T>.prepare(body: T): T {
    validator?.validate(body)
    return transformer.transform(body)
}
```

`performSave` 改为调用 `prepare`，避免逻辑重复。

## 测试设计

| 场景 | 断言 |
|------|------|
| POST 数组创建 2 条 | 200，`findByIds` 均可查到 |
| POST 数组含非法条目（空 name） | 400，无条目落库 |
| PUT 数组更新价格 | 200，价格更新 |
| DELETE `?ids=1,2` | 200，记录删除 |
| 未启用 `batch {}` | 405 |
| create 配置 UPSERT + 批量重复键 | 更新而非报错（可选） |

## 兼容性与风险

- 批量端点仅在 `batch {}` 时注册，默认零路由变化。
- `DELETE /book/batch` 与 `GET /book/{id}` 同路径不同方法，无冲突；`GET /book/batch` 会被 id 路由按参数解析失败处理（400），文档说明。
- 全批失败语义：单条校验失败 → 整批 400（Jimmer save 命令整体执行）。
