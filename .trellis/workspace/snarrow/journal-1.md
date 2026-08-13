# Journal - snarrow (Part 1)

> AI development session journal
> Started: 2026-08-13

---



## Session 1: 更新项目文档：README、文档站（中英）与示例说明

**Date**: 2026-08-13
**Task**: 更新项目文档：README、文档站（中英）与示例说明
**Branch**: `main`

### Summary

分析 ktor-jimmer-rest 框架并更新文档：框架 README 补特性/模块/安装/路由表，文档站中文 index+quick-start 从 TODO 补全并修复 start.md 死链，新增英文 index+quick-start，重写 sample README（服务清单/端口/运行步骤）。mkdocs 构建验证通过。提交未 push。

### Git Commits

| Hash | Message |
|------|---------|
| `0c718d6` | (see git log) |

### Status

[OK] **Completed**


## Session 2: 测试地基：route 级测试与参数映射测试

**Date**: 2026-08-13
**Task**: 测试地基：route 级测试与参数映射测试
**Branch**: `main`

### Summary

为框架建立测试基线：JUnit5 + Ktor test host + H2 内存库 + jimmer-ksp 测试实体。19 个测试覆盖 5 条 CRUD 路由（命中/404、过滤/分页/关闭分页/空列表、校验/转换、更新、幂等删除、解析错误 400）与参数映射（根表/嵌套表/ext/缺参）。src/main 零改动。发现 sample HttpClient.kt 既有编译问题（jackson 包迁移 + ConsulFeature 未定义），留给后续任务。

### Git Commits

| Hash | Message |
|------|---------|
| `1160239` | (see git log) |

### Status

[OK] **Completed**


## Session 3: 参数映射加固与路由小坑修正

**Date**: 2026-08-13
**Task**: 参数映射加固与路由小坑修正
**Branch**: `main`

### Summary

F2+D1+D2+D3：新增 ParameterNames 组件（bound receiver 清晰报错、javaTable getter 按类缓存、根/嵌套参数名解析），filter 扩展接入并加 D3 根属性同名冲突 fail-fast；id/remove 按注册 pathVariable 取参（嵌套路由不再取错）；key() 语义补 KDoc；收敛删除旧反射 helper。28 个测试全绿，sample 无新增编译错误。

### Git Commits

| Hash | Message |
|------|---------|
| `ffec5a6` | (see git log) |

### Status

[OK] **Completed**


## Session 4: filter DSL 补齐：操作符、ext 与动态排序

**Date**: 2026-08-13
**Task**: filter DSL 补齐：操作符、ext 与动态排序
**Branch**: `main`

### Summary

B1+B2+B3：eq? 改为确定性解析（无后缀优先、__exact 兜底）；新增 in?/notIn?/lt?/gt?/le?/ge?/notEq?/isNull；新增 sort() 动态排序（多字段多方向，非法输入 400）；Condition.kt 重命名为 FilterDsl.kt。45 个测试全绿，sample 无新增编译错误。

### Git Commits

| Hash | Message |
|------|---------|
| `6b5f12d` | (see git log) |

### Status

[OK] **Completed**


## Session 5: 写操作增强与 Provider/Route 架构重构

**Date**: 2026-08-13
**Task**: 写操作增强与 Provider/Route 架构重构
**Branch**: `main`

### Summary

api<T> 改为注册期求值（block 只执行一次，ApiConfig 纯配置，filter/key 请求期 lambda）；create/edit 独立配置块、SaveMode 可配置（UPSERT）、响应 fetcher 投影、PATCH 按需注册；id/remove 提取 resolveKey，findById 投影去重。52 个测试全绿，sample 适配导入后仅剩既有 HttpClient.kt 错误。

### Git Commits

| Hash | Message |
|------|---------|
| `54d3138` | (see git log) |

### Status

[OK] **Completed**


## Session 6: 批量操作与 endpoint 配置化

**Date**: 2026-08-13
**Task**: 批量操作与 endpoint 配置化
**Branch**: `main`

### Summary

C2 批量端点：batch {} 按需注册 POST/PUT/DELETE {path}/batch（BatchConfig 可配路径/操作开关/删除参数名）；patch {} 独立配置（与 PUT 分离）；批量/单条共用 SaveProvider.prepareEntity；字面量收进 EndpointConfiguration（batchPath/batchIdsParameterName/sortParameterName）。61 个测试全绿，sample 示例更新。

### Git Commits

| Hash | Message |
|------|---------|
| `4bc0896` | (see git log) |

### Status

[OK] **Completed**


## Session 7: count/exists 端点与统一错误响应

**Date**: 2026-08-13
**Task**: count/exists 端点与统一错误响应
**Branch**: `main`

### Summary

C6：GET {path}/count（过滤计数，fetchUnlimitedCount）与 GET {path}/exists/{id}，始终注册，路径进 EndpointConfiguration（countPath/existsPath）；C3：ApiError envelope（status/code/message/errors）+ jimmerRestErrors() 一行接线 StatusPages，404 带 body，sample 改用。65 个测试全绿。约定已记录到 .trellis/spec/backend/quality-guidelines.md（端点字面量必须进配置）。

### Git Commits

| Hash | Message |
|------|---------|
| `6dd15a4` | (see git log) |

### Status

[OK] **Completed**


## Session 8: 自定义动作扩展点与复合主键边界

**Date**: 2026-08-13
**Task**: 自定义动作扩展点与复合主键边界
**Branch**: `main`

### Summary

C7：api<T> 新增 action {} 配置块，注册期注册自定义路由（内置路由之后）；C8：实证 Jimmer KSP 拒绝多 @Id，entityIdType 增加防御性校验与 KDoc，复合主键明确不支持并推荐代理主键 + @Key。67 个测试全绿。

### Git Commits

| Hash | Message |
|------|---------|
| `82d5c1e` | (see git log) |

### Status

[OK] **Completed**


## Session 9: 文档同步与收尾（E1 + 全量验收）

**Date**: 2026-08-13
**Task**: 文档同步与收尾（E1 + 全量验收）
**Branch**: `main`

### Summary

docs-sync：README/文档站中英 quick-start/首页/sample README 同步全部新 DSL（filter 操作符、sort、create/edit/patch/batch、endpoint 配置、count/exists、ApiError envelope、action），mkdocs build 通过无死链；sample 两个服务迁移 jackson3 并完整编译（book-service HttpClient、order-service Serialization/HttpClient/Frameworks）；父任务 framework-issues 跨任务验收完成（A1-A4 全部达成）并归档，crud-features 归档。67 个测试全绿。

### Git Commits

| Hash | Message |
|------|---------|
| `cac8822` | (see git log) |

### Status

[OK] **Completed**
