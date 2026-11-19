# Agent Instructions

Rules for every AI coding agent working in this repository (e.g. Claude Code, OpenCode).

## Project

A ticket booking showcase built largely by AI agents. Backend: **Java + Spring Boot microservices** (Maven). Frontend: **Vue 3 + Vite + TypeScript**, living in a Maven module. Each module's `pom.xml` is the source of truth for Java and Spring Boot versions.

```
pom.xml                  parent / aggregator
services/<name>-service/ Spring Boot microservice (event-service, booking-service)
gateway/                 single entry point for the frontend (Spring Cloud Gateway)
frontend/                Vue 3 + Vite app, built by frontend-maven-plugin
```

- Every service, the gateway and the frontend is its own Maven module. Register new modules in the aggregator `pom.xml`.
- The frontend talks **only to the gateway**, never directly to a service.
- Services communicate through REST; each service owns its data and never reads another service's database.
- Every service exposes an **OpenAPI** contract. A change to a service's API must update the frontend client in the same task.

## Build and test

Run from the repository root. Maven is the only entry point, including for the frontend (Node is installed by `frontend-maven-plugin`, do not rely on a global Node).

| Goal | Command |
|---|---|
| Compile one module (fast check) | `mvn -q test-compile -pl <module> -am` |
| Test one module | `mvn -B verify -pl <module> -am` |
| Run a single test class | `mvn -B test -pl <module> -am -Dtest=<TestClass> -Dsurefire.failIfNoSpecifiedTests=false` |
| Build and test everything | `mvn -B verify` |

`<module>` is e.g. `services/booking-service`, `gateway` or `frontend` (the frontend runs `npm ci`, `npm run build` and Vitest through Maven).

- Compile after **each subtask**, not only at the end, and fix errors before moving on.
- Add or update tests for new and changed behavior, including at least one negative case.
- **All tests must pass.** Before finishing, run the tests of every module you touched (`mvn verify` for the whole repo if you changed shared code, poms or API contracts). If anything fails, keep working on the code until it is green. Never finish with a red build.
- Never make a test pass by deleting, skipping or weakening it. If you really cannot fix a failure, say so explicitly in your final message with the failing output instead of claiming success.

## Code conventions

- **No Lombok** in new or modified code. Write getters, setters and constructors by hand, use Java records for DTOs, constructor injection (no field `@Autowired`) and an explicit SLF4J `LoggerFactory` logger.
- Respect each module's Java and Spring Boot version; do not upgrade versions or add dependencies the task does not need.
- Follow the existing package structure and code style of the module you are changing.

**Backend**
- REST controllers return DTO records, never JPA entities. Errors use RFC 9457 Problem Details.
- Use `RestClient` for HTTP calls to other services, Flyway for relational schema changes (never edit an applied migration, add a new one) and Bean Validation on request DTOs.
- Tests: JUnit 5 + Mockito; `@WebMvcTest` / `@DataJpaTest` slices; Testcontainers where a real database or broker is needed.

**Frontend**
- TypeScript only, `strict` mode, no `any` (use `unknown` and narrow). Vue 3 Composition API with `<script setup lang="ts">`; no Options API.
- Backend calls go through the typed API client in `src/api/`, not ad-hoc `fetch` in components. The base URL points at the gateway.
- Tests with Vitest and Vue Test Utils; test what the user sees, not implementation details.
- Commit `package.json` and `package-lock.json` with pinned versions. Add npm dependencies only when the task needs them.

## Git

- Never commit or push directly to `master`. Work on a branch named `issue-<number>-<short-kebab-case-description>`, created from an up-to-date `master` (continue on it if it already exists).
- Commit in small, logical steps that each compile, with clear messages that reference the issue (`Refs #<number>`). Do not use `--no-verify` or force-push.
- Stage files explicitly (`git add <path>`), never `git add -A` / `.`. Before committing, check `git status` and `git diff --cached --stat`.
- Never commit build output (`target/`, `node_modules/`, `dist/`, `frontend/node/`), IDE files, logs, secrets or `.env` files. Keep logs and temporary files outside the repository.
- Open a pull request with `Closes #<number>` in the description; do not merge it yourself.
- After finishing a task that comes from a GitHub issue, post a short comment on it (2-4 bullets): what changed, branch and PR link, test result and anything left open.

## Scope

Implement only what the task describes, with no unrelated refactoring. If the task is ambiguous, choose the simplest reasonable interpretation and state the assumption.
