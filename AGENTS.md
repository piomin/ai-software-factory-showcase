# Agent Instructions

These rules apply to every AI coding agent working in this repository (e.g. OpenCode, Claude Code).

## Project

A showcase system developed largely by AI agents. Main technology: **Java + Spring Boot**, built with **Maven**. Each module's `pom.xml` is the source of truth for Java and Spring Boot versions.

## Execution modes

Agents run in one of two modes. Rules below marked *(interactive only)* do not apply in the pipeline mode.

- **Interactive** (a developer's machine, e.g. Claude Code): follow all sections, including git, pull request and issue reporting.
- **Pipeline** (OpenShift, Tekton pipeline `issue-agent`, OpenCode with a self-hosted Qwen3 Coder model), triggered per GitHub issue. In this mode:
  - The pipeline clones the repo from `master`, creates the branch `issue-<number>-<slug>`, and runs the agent in a single non-interactive `opencode run`.
  - **The agent only edits files.** It must not run `git commit`/`git push`, open a pull request, comment on the issue or touch secrets. The pipeline commits (`git add -A`, excluding `**/target/**`), pushes, opens the PR and comments on the issue.
  - Available tools are only `bash`, `read`, `edit`, `write`, `glob`, `grep` (no todo, task or skill tools; no web access). Never pass a `timeout` parameter to `bash`.
  - Write logs and temporary files to `/tmp` or `/workspace/tmp`, never into the repository.
  - When done, write a 2-4 bullet summary to `.agent-summary.md` in the repository root (what changed, test result, open points). The pipeline posts it as the issue comment and keeps it out of the commit.
  - After the agent finishes, the pipeline runs `mvn -B -q -DskipTests test-compile` on every touched Maven module. On failure it re-runs the agent with the compiler output (max 2 fix attempts), so keep changes compilable at all times. The agent run is time-limited (75 min, fix runs 25 min).
  - An empty change set fails the pipeline, so do make the requested change.
- `pipeline.yaml` is git-ignored and kept locally only as a reference for how the pipeline works; do not edit or commit it.

## Git workflow *(interactive only)*

- **Never commit or push directly to `master`.** All changes go to a dedicated branch.
- When working on a GitHub issue, create the branch from an up-to-date `master` and make its name unique and traceable to the issue:

  `issue-<issue-number>-<short-kebab-case-description>`

  Example: `issue-6-reviews-pagination`
- If a branch for the issue already exists, continue on it instead of creating a new one.
- Use clear, descriptive commit messages and reference the issue (e.g. `Refs #6`).
- Commit in small, logical steps (ideally one per subtask), each of which compiles.
- Do not bypass checks (`--no-verify`, skipping or disabling tests) and do not force-push.
- Do not merge the branch yourself. Opening a pull request (with `Closes #<issue-number>` in the description) is welcome, but merging is left to a human.

## Build output and commit hygiene

- **Never commit build output**, in particular `target/` directories (compiled classes, jars, `maven-status`, `maven-archiver`, etc.), nor IDE files (`.idea/`, `*.iml`), logs or OS files (`.DS_Store`). Only source, test, configuration and documentation files belong in a change.
- Make sure `.gitignore` covers `target/`, `.idea/`, `*.iml`, `*.log` and `.DS_Store`; add missing entries if needed. Never add secrets, credentials or `.env` files.
- **Stage files explicitly** (`git add <path>`); do not use `git add -A` / `git add .` / `git commit -a`.
- **Before every commit** run `git status` and `git diff --cached --stat` and verify that no `target/` or other generated files are staged. If `target/` files show up (modified, deleted or untracked), leave them out and do not restore or edit them by hand; if they are already tracked, tell the human instead of fixing it silently.

## Issue reporting *(interactive only)*

After finishing a task that originates from a GitHub issue, **post a comment on that issue** (e.g. with `gh issue comment <number> --body "..."`).

The comment must be very short (2-4 sentences or bullets) and include:
- what was changed, in plain language,
- the name of the branch (and PR link, if one was opened),
- the test result, and anything left undone or worth a reviewer's attention.

Do not paste diffs or long explanations into the comment.

## Scope

- Implement only what the issue describes. Do not make unrelated refactorings.
- If the issue is ambiguous, choose the simplest reasonable interpretation, and state the assumption in the issue comment.

## Build and verify

- Split the work into small subtasks. After **each subtask** (not only at the end), make sure the code compiles: run `mvn -q test-compile` in the affected Maven module (for multi-module projects from the aggregator root: `mvn -q test-compile -pl <module> -am`). Fix compilation errors before starting the next subtask.
- Add or update tests for new and changed behavior, including at least one negative case where applicable.
- Before finishing, run the relevant module's tests (`mvn test`, or `mvn verify` if integration tests exist) and report the result.
- Never leave the build broken: if you cannot fix a failure, say so explicitly in the summary instead of claiming success. Do not "fix" a failing test by deleting or weakening it.

## Code conventions for generated code

- **Do not use Lombok** in new or modified code (no `@Data`, `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor`, `@Slf4j`, ...). Write getters, setters and constructors by hand, use Java records for DTOs, constructor injection (no field `@Autowired`), and an explicit SLF4J `LoggerFactory` logger. Do not add Lombok dependencies. Do not mass-remove Lombok from untouched files unless the issue asks for it.
- Respect each module's Java and Spring Boot version from its `pom.xml`; do not upgrade versions or add dependencies that the issue does not need.
- Follow the existing package structure and code style of the module you are changing.
