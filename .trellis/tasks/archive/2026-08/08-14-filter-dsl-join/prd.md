# filter 关联过滤 DSL：join 子表作用域重构

## 背景

当前对关联表做过滤需要：

```kotlin
where += table.authors {
    firstName `ilike?` this@filter.call.query("firstName")
}
```

问题：
1. `this@filter.call` 标签：`table.authors {}` 块内 receiver 是子表，FilterScope 上下文（`call`、参数解析）全部失联。
2. 手拼字符串参数名 `"firstName"`，无类型安全。
3. 块内无法使用 `ilike?` 等操作符的自动参数解析。

## 目标

```kotlin
filter {
    where(Book::authors) {
        `ilike?`(table::firstName)   // 参数名自动解析为 authors_firstName
    }
}
```

底层使用 Jimmer 隐式子查询（EXISTS 语义），与 `table.authors {}` 一致，
避免显式 JOIN（joinList）导致的数据重复与分页失效。

## 非目标

- 不引入自定义 KSP 处理器（不生成 `FilterScope.authors {}`）。
- 不改动现有 `eq?`/`in?`/`between?` 等操作符的参数语义。
- 不修改路由/保存/校验 DSL。

## 验收标准

1. `FilterScope.where(prop, block)` 可用（与现有 `where(...)`/`where {}` 重载并存），块内 receiver 提供子表 `table` 和 `call` 上下文。
2. 子表块内 `ilike?`/`eq?` 等操作符可自动解析参数名（`Book::authors` + `firstName` → `authors_firstName`）。
3. 现有 FilterDsl 全部操作符对外用法不变（编译期兼容）。
4. 新增测试覆盖：关联过滤按 `authors_firstName` 参数过滤、无参数时不产生谓词。
5. sample book-service 的关联过滤改为新写法。

## 设计决策

- 提取 `FilterQueryScope` 接口（`call` + `table` + `resolved`），`FilterScope` 与新增 `AssociationFilterScope` 均实现之。
- FilterDsl 操作符 receiver 从 `FilterScope<T>` 泛化为 `FilterQueryScope<T>`（内部实现复用现有逻辑）。
- `where(prop, block)` 通过 `KProps.exists(prop, block)`（EXISTS 隐式子查询）获取子表，包装为 `AssociationFilterScope`。
- 参数名解析使用 `join` 的关联属性名（复数）+ 属性名（如 `authors_firstName`），由 `AssociationFilterScope.resolved` 构造，不依赖运行时 receiver 绑定。

## 过程中验证的结论

- Jimmer 的 `table.authors {}` 底层是 `KProps.exists`（EXISTS 隐式子查询），不是 JOIN。
- `joinList` 是显式 JOIN，多对多场景会产生数据重复并破坏分页，不可用于关联过滤。
- `AssociationFilterScope` 不能委托 `KNonNullProps`（会导致属性引用 receiver 解析到外层 FilterScope）。
