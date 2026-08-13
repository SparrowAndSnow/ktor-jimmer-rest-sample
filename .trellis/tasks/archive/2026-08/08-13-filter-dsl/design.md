# filter DSL 补齐：技术设计

## 公共模式

所有自动映射扩展沿用统一样板（复用 ParameterNames，含 D3 检测）：

```kotlin
val resolved = ParameterNames.resolveWithPath(param)
ParameterNames.ensureNoRootCollision(table, resolved)
```

## B1：eq? 确定性解析

```kotlin
val parameters = call.queryParameterExt<P>(P::class, resolved.value)
val value = parameters[null]?.value ?: parameters["exact"]?.value
return param.call().`eq?`(value)
```

- `parameters[null]` = 无后缀参数；`parameters["exact"]` = `__exact`。
- 优先级：无后缀 > `__exact`；其他 ext（`__start`/`__ge` 等）直接忽略。
- 与参数顺序无关（不再用 `default()`）。

## B2：新操作符

```kotlin
// in? / notIn? —— 逗号分隔或重复参数
val values = call.queryParameterValues<P>(P::class, resolved.value)
if (values.isEmpty()) null else param.call().valueIn(values)      // valueNotIn 同理

// lt?/gt?/le?/ge? —— 单边 ext
val value = call.queryParameter<P>(P::class, resolved.value, "lt")
param.call().`lt?`(value)                                        // gt/le/ge 同理

// notEq? —— 无后缀参数
val value = call.queryParameter<P>(P::class, resolved.value)
param.call().`ne?`(value)

// isNull —— 静态谓词，不读参数
param.call().isNull()
```

### queryParameterValues 帮助函数（RoutingCallExtension）

```kotlin
fun <T : Any> RoutingCall.queryParameterValues(type: KClass<T>, name: String): List<T> =
    queryParameters.getAll(name).orEmpty()
        .flatMap { it.split(",") }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { it.parse(type) }
```

解析失败抛 `ParseException`（与现有行为一致，sample 映射 400）。

## B3：动态排序

```kotlin
inline fun <reified T : Any> FilterScope<T>.sort(parameterName: String = "sort") {
    call.queryParameters.getAll(parameterName).orEmpty().forEach { item ->
        val parts = item.split(",")
        val propertyName = parts[0].trim()
        val direction = parts.getOrNull(1)?.trim()?.lowercase() ?: "asc"
        if (propertyName.isEmpty()) throw ParseException("排序参数格式错误: '$item'，应为 '字段,asc|desc'")
        val expression = try {
            table.get<Comparable<*>>(propertyName)
        } catch (e: Exception) {
            throw ParseException("排序字段不存在: '$propertyName'")
        }
        val order = when (direction) {
            "asc" -> expression.asc()
            "desc" -> expression.desc()
            else -> throw ParseException("不支持的排序方向: '$direction'，仅支持 asc/desc")
        }
        orderBy(order)
    }
}
```

- 字段名经 `KProps.get(String)` 由 Jimmer 元数据校验，天然防注入。
- 用户输入错误统一抛 `ParseException` → 与框架现有 400 语义一致。
- `asc()`/`desc()` 为 Jimmer Kotlin 扩展（`org.babyfish.jimmer.sql.kt.ast.expression`）。

## 测试设计

### 新增路由帮助（TestApplication.kt）

| 帮助函数 | filter 内容 |
|----------|-------------|
| `eqRoutes()` | `where(eq?(table::name))` |
| `inRoutes()` | `where(in?(table::id))` |
| `notInRoutes()` | `where(notIn?(table::id))` |
| `comparisonRoutes()` | `where(lt?(table::price), gt?(table::price))` / `le?`/`ge?` 各一 |
| `notEqRoutes()` | `where(notEq?(table::name))` |
| `isNullRoutes()` | `where(isNull(table::store_name))`（OrderItem 的蛇形可空字段） |
| `sortRoutes()` | `filter { sort() }` |

### 用例（FilterDslTest / SortTest）

| 场景 | 断言 |
|------|------|
| `?name=x` | eq? 命中 |
| `?name__exact=x` | eq? 命中（B1） |
| `?name=a&name__exact=b` 与反序 | 结果一致（无后缀优先） |
| `?name__start=x` | eq? 忽略 → 全量 |
| `?id=1,2` / `?id=1&id=3` | in? 命中对应行 |
| `?id=1` | notIn? 排除该行 |
| `?price__lt=60&price__gt=40` | 命中 40<price<60 |
| `?price__le=60&price__ge=40` | 命中 40≤price≤60 |
| `?name=x` | notEq? 排除该行 |
| isNull 无参数 | 返回 store_name 为空的行 |
| `?sort=price,desc` | 降序 |
| `?sort=price,asc` | 升序 |
| `?sort=price,desc&sort=id,asc` | 多字段生效 |
| `?sort=unknown` / `?sort=price,sideways` | 400 |

## 兼容性与风险

- 不改变 `ilike?`/`between?`；`eq?` 在仅有无后缀参数时行为不变。
- `notEq?` 与 `eq?` 同属性混用时参数名冲突——文档（KDoc）说明，不主动检测（可由 D3 机制延伸，后续可选）。
- `in?` 空值语义：无参数/空值 → 谓词为 null → 全量（与现有 `eq?` 缺参语义一致）。
