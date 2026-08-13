# filter DSL 补齐：操作符、ext 与动态排序

## Goal

补齐 filter DSL 的自动参数映射能力：`eq?` 支持 `__exact`（B1）、新增常用操作符 `in?`/`notIn?`/`lt?`/`gt?`/`le?`/`ge?`/`notEq?`/`isNull`（B2）、支持 `sort=字段,asc|desc` 动态排序（B3）。保持与现有 DSL 一致的命名与行为风格。

## Background

- 现有 filter 自动映射操作符只有 `eq?`/`ilike?`/`between?`/`noNull`（`Condition.kt`）。
- `eq?` 目前取"第一个匹配参数"（`default()`），`?name=x&name__exact=y` 时结果依赖参数顺序，有歧义；`ilike?`/`between?` 已显式处理 ext。
- `queryParameterExt` 已支持按名 + ext 解析（`RoutingCallExtension.kt`）；ParameterNames 组件（F2）已就绪，可复用 resolve + D3 检测。
- Jimmer Kotlin 已提供全部所需谓词：`eq?`/`ne?`/`lt?`/`le?`/`gt?`/`ge?`/`between?`/`valueIn`/`valueNotIn`/`isNull`/`asc`/`desc`，无需新造。
- 依赖 mapping-hardening 的 ParameterNames 组件与测试基线。

## Requirements

R1. B1：`eq?` 参数解析改为确定性规则——优先无后缀参数，其次 `__exact`，忽略其他 ext；不依赖参数顺序。
R2. B2：新增自动映射扩展（命名与现有 `?` 风格一致）：
   - `in?`：`?field=1,2,3`（逗号分隔）或重复参数 `?field=1&field=2`，空值时跳过谓词；
   - `notIn?`：同 `in?` 取值，`valueNotIn`；
   - `lt?`/`gt?`/`le?`/`ge?`：分别读 `__lt`/`__gt`/`__le`/`__ge`；
   - `notEq?`：读无后缀参数（与 `eq?` 同参数名，文档注明不要与 `eq?` 同属性混用）；
   - `isNull`：静态谓词（与 `noNull` 一致，不读参数）。
R3. B3：新增 `sort(parameterName: String = "sort")`：支持 `?sort=price,desc&sort=id,asc`，字段名经 Jimmer 元数据校验，非法字段/方向抛 `ParseException`（映射 400）。
R4. 新增 `RoutingCall.queryParameterValues`（逗号分隔 + 重复参数 → 类型化列表），解析失败沿用 `ParseException`。
R5. 测试覆盖：B1 四种场景、B2 每个操作符至少一条、B3 升/降序/多字段/非法字段。

## Acceptance Criteria

- [ ] A1. 现有 28 个测试全部通过（无行为回退）。
- [ ] A2. `eq?` 对 `?name=...&name__exact=...` 结果确定且与参数顺序无关；`__start` 等被忽略。
- [ ] A3. `in?`/`notIn?` 支持逗号与重复参数两种形式；空值不产生谓词。
- [ ] A4. 单边比较操作符各自读对应 ext；`notEq?` 读无后缀参数。
- [ ] A5. `sort` 支持多字段多方向；非法字段/方向返回 400（ParseException）。
- [ ] A6. 新增测试全绿，sample 编译无新增错误。

## Out of Scope

- 不改 `ilike?`/`between?` 现有行为。
- 不做 `or` 分组、注解式参数名（进阶项）。
- 文档更新归 docs-sync 任务。
