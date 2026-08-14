# Quality Guidelines

> Code quality standards for backend development.

---

## Overview

<!--
Document your project's quality standards here.

Questions to answer:
- What patterns are forbidden?
- What linting rules do you enforce?
- What are your testing requirements?
- What code review standards apply?
-->

(To be filled by the team)

---

## Forbidden Patterns

<!-- Patterns that should never be used and why -->

(To be filled by the team)

---

## Required Patterns

<!-- Patterns that must always be used -->

(To be filled by the team)

### 端点路径与查询参数名字面量必须进配置（已确认约定）

框架新增任何端点路径、查询参数名或其他 URL 字面量时，必须收进
`com.eimsound.ktor.config.EndpointConfiguration`，并经 `JimmerRest` 插件安装时同步到
`Configuration.endpoint`，用户可用 `endpoint { ... }` 配置块覆盖。

已收录的字面量：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `batchPath` | `batch` | 批量端点路径 |
| `batchIdsParameterName` | `ids` | 批量删除 id 参数名 |
| `sortParameterName` | `sort` | 动态排序参数名 |
| `countPath` | `count` | 计数端点路径 |
| `existsPath` | `exists/{id}` | 存在性判断端点路径 |

规则：

1. 路由函数的默认参数从 `Configuration.endpoint` 读取（如 `path: String = Configuration.endpoint.countPath`）。
2. `Api.kt` 接线中不允许出现硬编码路径/参数名字面量（用默认参数即可）。
3. 新增端点时，同步扩展 `EndpointConfiguration` 并在 `JimmerRestPlugin` 中接线。

---

## JitPack 发版配置（已确认约定）

框架通过 JitPack 对外发布（仓库：eimsound/ktor-jimmer-rest）。JitPack 构建机在国内网络不可达，
任何国内镜像（腾讯 Maven/Gradle 镜像）都会导致构建超时或失败，发版时必须遵守：

1. `jitpack.yml` 的 `jdk` 只能用 JitPack 支持的版本（最高 `openjdk21`），不支持 `openjdk25`。
2. `jitpack.yml` 用 `subdirectory: "ktor-jimmer-rest"` 声明工程子目录，`install` 中再
   `cd ./ktor-jimmer-rest` 进入 Gradle 根目录后执行 `./gradlew publishToMavenLocal --no-daemon`
   （该写法已在 JitPack 构建验证通过，勿改为其它路径组合）。
3. `settings.gradle.kts` 依赖仓库按 `System.getenv("JITPACK") == "true"` 分支：JitPack 构建机
   只用 `mavenCentral()`；本地开发保留腾讯镜像加速。
4. `gradle/wrapper/gradle-wrapper.properties` 的 `distributionUrl` 必须指向
   `services.gradle.org`（官方发行版），不能用国内镜像。
5. `gradle.properties` 中 `version` 即发布版本；打 tag 前先本地跑
   `JITPACK=true bash gradlew publishToMavenLocal` 验证。
6. JitPack 构建环境变量：`JITPACK=true`、`GIT_COMMIT`、`VERSION`（tag 名）。

---

## Filter DSL 关联过滤（已确认约定）

对关联表（如 `Book.authors`）做过滤时，必须使用隐式子查询（EXISTS 语义），
禁止用 `joinList` / `asTableEx()` 做显式 JOIN——多对多场景会数据重复并破坏分页。

1. 入口：`where(Book::authors) { ... }`（`FilterScope.where(prop, block)` 重载，
   与 `where(...)` / `where {}` 并存，Kotlin 重载自动区分）。
2. 块内 receiver 是 `AssociationFilterScope`，可用全部 filter 操作符
   （`eq?`/`ilike?`/`between?` 等，receiver 已泛化为 `FilterQueryScope`）。
3. 参数名 = 关联属性名（复数）+ 分隔符 + 属性名，如 `Book::authors` + `firstName`
   → `authors_firstName`（`subParameterSeparator` 默认 `_`）。
4. `AssociationFilterScope` 不能委托 `KNonNullProps`（属性引用 receiver 会解析错乱），
   块内一律写 `table::firstName` 而不是裸 `firstName`。
5. 新增 filter 操作符时，receiver 声明为 `FilterQueryScope<T>`，使根表与子表块都可用。
6. 支持嵌套关联过滤：`where(Book::authors) { where(Author::books) { ... } }`，
   参数名前缀逐层累积（`authors` → `authors_books`），内层 `where` 返回 EXISTS 谓词。

---

## Testing Requirements

<!-- What level of testing is expected -->

(To be filled by the team)

---

## Code Review Checklist

<!-- What reviewers should check -->

(To be filled by the team)
