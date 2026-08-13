# 参数映射加固与路由小坑修正：技术设计

## F2：ParameterNames 组件

位置：`ktor-jimmer-rest-util/src/main/kotlin/com/eimsound/util/ktor/ParameterNames.kt`

```kotlin
internal data class ResolvedName(val value: String, val segments: List<String>)

object ParameterNames {
    fun resolve(property: KProperty<*>): String = resolveWithPath(property).value
    internal fun resolveWithPath(property: KProperty<*>): ResolvedName
    internal fun ensureNoRootCollision(rootTable: Any, resolved: ResolvedName)
}
```

### resolve 流程

1. **boundReceiverOf(property)**：优先 `(property as? KProperty0<*>)` 取 bound receiver（现有 `PropertyReference0Impl` 路径）；失败时抛
   `IllegalArgumentException("filter 参数必须是绑定的属性引用（如 table::name），实际是：${property}")`。
2. **tableAliasOf(receiver)**：按 receiver 类缓存反射到的 `javaTable` getter（`ConcurrentHashMap<Class<*>, KCallable<*>>`），每请求只做 `getter.call(receiver).toString()`（别名值不缓存——同一类不同实例别名不同，如根表 `BOOK_STORE` vs 嵌套 `BOOK.store`）。
   - getter 缺失或调用失败 → `IllegalStateException`（提示 Jimmer 版本兼容问题）。
3. **path** = alias `.split(".").drop(1)`（根表为空列表）。
4. **name** = `(path + property.name).joinToString(Configuration.router.subParameterSeparator)`。

### D3：冲突检测

`ensureNoRootCollision(rootTable, resolved)`：仅当 `resolved.segments` 非空（嵌套路径）时检查：

- rootTable 需实现 `TableTypeProvider`（Jimmer 表对象均实现）→ `immutableType.props.keys`；
- 若根类型存在名为 `resolved.value` 的属性（如 `store_name`），抛
  `IllegalStateException("查询参数 'store_name' 与根实体 <类型> 的标量属性同名冲突（嵌套路径 store.name）。请调整 router.subParameterSeparator 或重命名冲突字段。")`。

注意：根属性参数名 = 属性原名（camelCase），嵌套参数名用分隔符连接（snake），仅当根属性本身含 `_`（如 `store_name`）才会真正冲突，检测逻辑与之对应。

## 接线改动

### RoutingCallExtension.kt

- `queryParameterExt(property)` 内部改为 `ParameterNames.resolve(property)` + 既有按名 API（无 D3 检查，保持公共 API 兼容）。
- `defaultPathVariable` 保留，KDoc 说明"仅适用于单路径参数路由"，并提示优先使用 `pathParameter(name)`。
- 新增：
  ```kotlin
  fun RoutingCall.pathParameter(name: String): String =
      pathParameters[name] ?: throw IllegalStateException("路径参数 $name 不存在")
  ```
- 删除无使用方的 `getPropertyReceiver`（Reflect.kt）、`tableName`/`tableType`（JimmerReflect.kt），逻辑并入 ParameterNames。

### Condition.kt（filter 扩展）

`eq?`/`ilike?`/`between?` 改用：

```kotlin
val resolved = ParameterNames.resolveWithPath(param)
ParameterNames.ensureNoRootCollision(table, resolved)   // table 来自 FilterScope 委托
val parameter = call.queryParameterExt<P>(P::class, resolved.value).default()
```

`noNull` 不读参数，跳过检测。

### Query.kt / Remove.kt（D2）

```kotlin
val key = provider.key ?: call.pathParameter(pathVariable.removeSurrounding("{", "}"))
    .parse(entityIdType<TEntity>())
```

`pathVariable` 是路由注册时传入的模板（默认 `{id}`）。

### KeyProvider（D1）

`key(...)` / `KeyProvider.key` 补充 KDoc：固定 key 会覆盖路径变量，同时作用于 id 与 remove；典型场景是 id 来自上下文（登录态/租户）。行为不变。

## 测试设计

### 新增测试实体 Order（仅用于 D3）

```kotlin
@Entity
interface Order : BaseEntity {
    @Key val code: String
    @ManyToOne val store: BookStore?
    val store_name: String?   // 蛇形属性 → 参数名 store_name，与 store.name 冲突
}
```

H2 schema 增加 `order` 表（id、code、store_id、store_name）；cleanDatabase 增加删除。

### 用例

| 测试 | 断言 |
|------|------|
| ParameterNamesTest.resolve 根表 | `table::name` → `"name"` |
| ParameterNamesTest.resolve 嵌套 | `table.store::name` → `"store_name"` |
| ParameterNamesTest 非 bound 引用 | 抛 IllegalArgumentException，消息含"绑定的属性引用" |
| ParameterNamesTest 缓存 | 同参数解析两次，getter 反射只发生一次（可注入计数器或仅验证结果一致） |
| NestedRouteTest（D2） | `route("/{tenant}") { bookRoutes() }` 下 `GET /t1/book/{id}` 与 `DELETE` 正常 |
| KeyBehaviorTest（D1） | `key(b2.id)` 时 `GET /book/{b1.id}` 返回 b2；`DELETE /book/{b1.id}` 删除 b2 |
| CollisionTest（D3） | `/order?store_name=x` 请求抛异常（500），消息含"subParameterSeparator" |

## 兼容性与风险

- 行为不变的保证：现有 19 个测试（含 ParameterMappingTest 四场景）回归通过。
- `defaultPathVariable` 保留但文档标注限制；`queryParameterExt(property)` 公共 API 签名不变。
- 风险：`javaTable` 内部属性在 Jimmer 升级后可能改名 → getter 缓存失败路径给出明确错误，并把该发现回写 spec（后续任务）。
