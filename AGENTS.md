# AGENTS.md

Use `hardening` as the working base branch.

- Create work branches only from `hardening`: `harden/<topic>`
- Merge finished work back into `hardening`
- Delete the topic branch after merge
- Do not target `main` for this work
- When opening a PR, target the **mirror repository** (`Starlight258/yagubogu-mirror`), not `origin` (`woowacourse-teams/2025-yagu-bogu`). With `gh pr create`, pass `--repo Starlight258/yagubogu-mirror` explicitly — `gh` defaults to `origin`'s repo otherwise. Base branch is still `hardening` (exists on the mirror too).
