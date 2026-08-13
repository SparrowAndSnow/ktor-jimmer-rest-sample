# ktor-jimmer-rest-sample

[ktor-jimmer-rest](../ktor-jimmer-rest/README.md) 框架的示例项目，包含两个 Ktor 服务：

- `book-service`：完整的 CRUD 示例。用一行 `api<Book>("/book")` 注册五条 REST 路由，并演示
  `filter`（查询条件）、`fetcher`（字段投影）、`input`（校验 + 转换）三个子 DSL
- `order-service`：预留的脚手架服务，演示如何在一个多服务架构中集成该框架

## 技术栈

- Kotlin 2.4.10 / Ktor 3.5.2
- Jimmer 0.11.5（ORM，KSP 代码生成）
- Koin 4.2.2（依赖注入）
- PostgreSQL（也可切换 MySQL）
- Gradle 8.x（Kotlin DSL）

## 目录结构

```text
sample/
├── book-service/            # 图书服务（端口 8081）
│   └── src/main/kotlin/com/book/
│       ├── Application.kt   # 应用入口
│       ├── Frameworks.kt    # Koin、JimmerRest、StatusPages 配置
│       ├── Routing.kt       # api<Book>("/book") DSL 示例
│       └── domain/entity/   # Book / BookStore / Author 实体
└── order-service/           # 订单服务脚手架（端口 8080）
```

## 运行

### 1. 启动数据库

仓库根目录的 `.devcontainer/docker-compose.yml` 会启动 PostgreSQL，并自动执行
`init-postgres.sql` 初始化表结构和示例数据：

```bash
docker compose -f .devcontainer/docker-compose.yml up -d db
```

数据库连接信息（见 `book-service/src/main/resources/application.conf`）：

| 配置 | 值 |
|------|-----|
| URL | `jdbc:postgresql://localhost:5432/ktor_jimmer` |
| 用户名 / 密码 | `postgres` / `postgres` |

> 如果 bind mount 的宿主机路径（`/data/docker-compose/postgres`）不可写，请修改
> `docker-compose.yml` 中的 `volumes` 或改用 Docker volume。

### 2. 启动 book-service

```bash
cd sample
./gradlew :book-service:run
```

服务默认监听 `http://localhost:8081`。

## 体验接口

```bash
# 列表 + 过滤 + 分页
curl "http://localhost:8081/book?name__start=GraphQL&price__ge=50&pageIndex=0&pageSize=10"

# 动态排序
curl "http://localhost:8081/book?sort=price,desc&sort=id,asc"

# 计数 / 存在性
curl http://localhost:8081/book/count
curl http://localhost:8081/book/exists/1

# 查询单个
curl http://localhost:8081/book/1

# 创建（价格超出 0~100 会返回 400 和校验错误）
curl -X POST http://localhost:8081/book \
  -H "Content-Type: application/json" \
  -d '{"name":"Learning GraphQL","edition":1,"price":50}'

# 更新
curl -X PUT http://localhost:8081/book \
  -H "Content-Type: application/json" \
  -d '{"id":1,"name":"Learning GraphQL","edition":1,"price":55}'

# 部分更新（PATCH）
curl -X PATCH http://localhost:8081/book \
  -H "Content-Type: application/json" \
  -d '{"id":1,"price":55}'

# 批量创建 / 批量删除
curl -X POST http://localhost:8081/book/batch \
  -H "Content-Type: application/json" \
  -d '[{"name":"Learning GraphQL","edition":1,"price":50},{"name":"Effective TypeScript","edition":1,"price":73}]'
curl -X DELETE "http://localhost:8081/book/batch?ids=1,2"

# 删除
curl -X DELETE http://localhost:8081/book/1
```

查询参数的映射规则（`eq?`、`in?`、`lt?`/`gt?`、`ilike?`、`between?`、`sort()`、嵌套表字段等）见
[快速开始](../ktor-jimmer-rest/docs/docs/zh/quick-start.md)。
