# 写操作增强 + Provider/Route 架构重构：技术设计

## 一、注册期求值架构

### 配置对象（provider 或 route 模块）

```kotlin
// ApiConfig：无 call，注册期构建
class ApiConfig<T : Any> : FilterProvider<T>, FetcherProvider<T>, KeyProvider<T>, PageProvider {
    override var fetcher: Fetchers<T>? = null
    override var filter: Filters<T>? = null
    override var key: Any? = null
    override var keyResolver: ((RoutingCall) -> Any?)? = null
    override var pager: Pager = Pager()

    val create = CreateConfig<T>()
    val edit = EditConfig<T>()
    var patchEnabled = false
}

class CreateConfig<T : Any> : InputProvider<T>, FetcherProvider<T> {
    override var input: Inputs<T> = Inputs.Entity()
    override var validator: Validators<T>? = null
    override var transformer: Transformers<T>? = null
    override var fetcher: Fetchers<T>? = null        // 响应投影（C4）
    var saveMode: SaveMode = SaveMode.INSERT_ONLY    // C1
    var associatedSaveMode: AssociatedSaveMode = AssociatedSaveMode.MERGE
}
// EditConfig 同构，默认 UPDATE_ONLY / UPDATE
```

### DSL 扩展

```kotlin
fun <T : Any> ApiConfig<T>.input(block: EntityScope<T>.() -> Unit) { create.input(block); edit.input(block) }
fun <T : Any, TInput : Input<T>> ApiConfig<T>.input(type: KClass<TInput>, block: InputScope<T, TInput>.() -> Unit) {
    create.input(type, block); edit.input(type, block)
}
fun <T : Any> ApiConfig<T>.create(block: CreateConfig<T>.() -> Unit) { create.apply(block) }
fun <T : Any> ApiConfig<T>.edit(block: EditConfig<T>.() -> Unit) { edit.apply(block) }
fun <T : Any> ApiConfig<T>.patch(block: EditConfig<T>.() -> Unit = {}) { patchEnabled = true; edit.apply(block) }
fun <T : Any> ApiConfig<T>.key(block: (RoutingCall) -> Any?) { keyResolver = block }
```

`filter {}`/`fetcher {}`/`pager {}` 复用现有 provider 扩展（存储纯配置或请求期 lambda）。

### KeyProvider 扩展

```kotlin
interface KeyProvider<T : Any> {
    var key: Any?
    var keyResolver: ((RoutingCall) -> Any?)?   // 新增，默认 null
}
```

id/remove 解析顺序：`key` → `keyResolver?.invoke(call)` → 路径参数。

### Api.kt 重构

```kotlin
inline fun <reified TEntity : Any> Route.api(
    path: String,
    pathVariable: String = Configuration.router.defaultPathVariable,
    crossinline block: ApiConfig<TEntity>.() -> Unit,
) {
    val config = ApiConfig<TEntity>().apply { block() }   // 注册期执行一次
    route(path) {
        id<TEntity>(pathVariable) {
            fetcher = config.fetcher
            key = config.key
            keyResolver = config.keyResolver
        }
        list<TEntity> {
            fetcher = config.fetcher
            filter = config.filter
            pager = config.pager
        }
        create<TEntity> {
            input = config.create.input
            validator = config.create.validator
            transformer = config.create.transformer
            saveMode = config.create.saveMode
            associatedSaveMode = config.create.associatedSaveMode
            fetcher = config.create.fetcher
        }
        edit<TEntity> { /* 同 create，用 config.edit */ }
        if (config.patchEnabled) {
            patch<TEntity> { /* 同 edit */ }
        }
        remove<TEntity>(pathVariable) {
            key = config.key
            keyResolver = config.keyResolver
        }
    }
}
```

独立路由函数（`Route.id/list/create/edit/remove/patch`）签名不变；`api` 内部以配置直连，不再重复执行用户 block。

## 二、C1/C4/C5 实现

### 保存与响应（Create.kt / Edit.kt / Patch.kt）

```kotlin
val entity = transformer.transform(body)
val result = sqlClient.save(entity, provider.saveMode, provider.associatedSaveMode)
val response = provider.fetcher?.let { f ->
    when (f) {
        is Fetchers.Fetch<T> -> sqlClient.findById(f.fetcher, result.modifiedEntity.id)
        is Fetchers.ViewType<T> -> sqlClient.findById(f.viewType, result.modifiedEntity.id)
    }
} ?: result.modifiedEntity
call.respond(response)
```

`Route.patch` 与 Edit 同构（`patch(path)` 方法路由，`UPDATE_ONLY`）。

## 三、测试设计

| 场景 | 断言 |
|------|------|
| 现有 45 测试 | 全绿（`api` 用法兼容回归） |
| block 副作用计数 | 注册后请求多次，计数不再增长 |
| `create { saveMode = MERGE }` 同 key 两次 POST | 第二次为更新（upsert） |
| `create { fetcher { name() } }` | 响应仅含投影字段 |
| `patch {}` 启用后 PATCH 部分字段 | 其他字段不覆盖；未启用则无 PATCH 路由（404） |
| `key { call -> ... }` | id/remove 使用 resolver 结果 |
| 顶层 `input {}` + `create {}` 混用 | 校验/转换与保存配置同时生效 |

## 四、兼容性与风险

- 破坏点：`api` block 不再提供 `call`（需用 `filter {}` 内或 `key { call -> }`）；block 不再 suspend。仓库内 sample/README 无顶层 `call` 用法（已核查），外部用户需文档说明。
- `ApiScope` 重命名 `ApiConfig`（内部类，非公开 API 表面）。
- PATCH 按需注册，默认不新增路由（零意外变化）。
- 风险：`keyResolver` 每请求调用一次；`findById` 投影多一次查询（正确性优先）。
