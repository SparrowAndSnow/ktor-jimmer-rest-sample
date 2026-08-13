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

## Testing Requirements

<!-- What level of testing is expected -->

(To be filled by the team)

---

## Code Review Checklist

<!-- What reviewers should check -->

(To be filled by the team)
