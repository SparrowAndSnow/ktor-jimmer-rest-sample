# 文档站部署：MkDocs + GitHub Pages

## Goal

目标：让 docs/ 的 MkDocs Material 文档站可构建并自动部署到 GitHub Pages。步骤：1) 本地验证 mkdocs build 通过；2) 新增 .github/workflows/docs.yml（push main 时 build + deploy Pages）；3) 确认 site_url 与 Pages 域名一致；4) sample/README 文档链接验证。验收：本地构建产物生成、workflow 配置正确、文档站 URL 可访问（push 后由 GitHub Actions 验证）。

## Requirements

- TBD

## Acceptance Criteria

- [ ] TBD

## Notes

- Keep `prd.md` focused on requirements, constraints, and acceptance criteria.
- Lightweight tasks can remain PRD-only.
- For complex tasks, add `design.md` for technical design and `implement.md` for execution planning before `task.py start`.
