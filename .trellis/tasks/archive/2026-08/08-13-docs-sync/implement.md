# 文档同步：执行计划

## 有序检查清单

1. `ktor-jimmer-rest/README.md`：更新 Usage（filter 操作符表 + sort、create/edit/patch/batch 配置、endpoint 配置、count/exists、错误 envelope、action），更新端点清单表。
2. `docs/docs/zh/quick-start.md`：新增小节（过滤与排序、写操作配置、count/exists、统一错误、endpoint 配置、自定义动作）。
3. `docs/docs/en/quick-start.md`：同步英文版。
4. `docs/docs/zh/index.md` / `docs/docs/en/index.md`：特性列表同步。
5. `sample/README.md`：curl 示例更新。
6. mkdocs build 验证（复用 /tmp/mkdocs-venv）。
7. 提交（子模块 + sample + 指针），归档 docs-sync。

## 验证命令

```bash
/tmp/mkdocs-venv/bin/mkdocs build -f ktor-jimmer-rest/docs/mkdocs.yml -d /tmp/mkdocs-site
rg -n "TODO" ktor-jimmer-rest/docs
```

## 完成后的检查

- build 通过、无死链/TODO。
- 示例与源码一致（新 DSL 均已在测试中使用，可对照）。
