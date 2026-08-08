# AGENTS.md

This file provides guidance to AI coding agents (Claude Code, Codex, Cursor, etc.) working in this
repository.

## Project

Mini Doodle — a backend service for managing personal time slots and converting them into meetings,
with an aggregated free/busy view per user. Java 25, Spring Boot 4.1, PostgreSQL, Flyway, Gradle.

## Commands

- Build: `./gradlew build`
- Run all tests: `./gradlew test`
- Run a single test class: `./gradlew test --tests "com.minidoodle.minidoodle.MiniDoodleApplicationTests"`
- Run a single test method: `./gradlew test --tests "com.minidoodle.minidoodle.MiniDoodleApplicationTests.contextLoads"`
- Run locally with an auto-provisioned Postgres via Testcontainers dev services (no external DB needed):
  `./gradlew bootTestRun` (uses `TestMiniDoodleApplication` / `TestcontainersConfiguration` in `src/test`)
- Run normally: `./gradlew bootRun` — requires a reachable Postgres per `application.yaml`.
- Run everything via Docker: `docker-compose up --build` — starts Postgres 17 plus the app, built from
  the multi-stage `Dockerfile` (Liberica OpenJDK 25 build image, Liberica JRE 25 slim-musl runtime,
  non-root `appuser`). App on `:8080`, Postgres on `:5432`.
- Format code: `./gradlew spotlessApply` — applies Google Java Format (via the `com.diffplug.spotless`
  plugin) plus unused-import removal, trailing-whitespace trimming, and a trailing newline. `spotlessCheck`
  is wired into `check`, so `./gradlew build` already fails on unformatted Java — run `spotlessApply`
  before committing rather than hand-formatting.

Tests use JUnit 5 and Testcontainers (Postgres) — running them requires Docker to be available.
Mockito (`mockito-core`/`mockito-junit-jupiter`) is also on the test classpath, for plain unit tests
(e.g. a `domain/service` class with its `port/out` dependencies mocked) that don't need Spring context
or a database at all.

Dependencies use Spring Boot 4.1's split starter names (e.g. `spring-boot-starter-webmvc`,
`spring-boot-starter-data-jpa` + matching `-test` starters), not the older `spring-boot-starter-web`
convention — match this style when adding dependencies to `build.gradle`. Lombok is available on
both main and test source sets.

All HTTP routes are served under the `/api` prefix (`spring.mvc.servlet.path` in `application.yaml`,
which maps the `DispatcherServlet` rather than setting the servlet context path).

## Documentation

- [docs/api.md](docs/api.md) — full endpoint reference, examples, API trade-offs
- [docs/database.md](docs/database.md) — schema, why PostgreSQL, DB trade-offs, future scaling ideas
- [docs/superpowers/specs/](docs/superpowers/specs/) — dated design specs written before implementing a
  feature (via the brainstorming workflow); check here for the reasoning behind a slice's scope before
  assuming something is a bug or an oversight

`README.md` stays a short overview with links out to `docs/`. Add new `{topic}.md` files there as
the `Slot`/`Meeting`/`Availability` docs grow, rather than growing README back out — and keep this
file pointing at those docs rather than duplicating their content.

## Commit messages

Follow [Conventional Commits](https://www.conventionalcommits.org/) — `type(scope): summary`, e.g.
`feat(slot): add slot creation endpoint`, `fix(booking): handle race on double booking`,
`docs(database): document sharding and CQRS ideas`.

## Architecture

The codebase is being restructured into hexagonal (ports & adapters) architecture. Package layout
under `com.minidoodle.minidoodle`:

- `domain/model` — framework-free domain types (plain records/classes, no Spring/JPA imports)
- `domain/service` — use case implementations; depend only on `port/in` and `port/out` interfaces
- `port/in` — inbound use-case interfaces that driving adapters call (e.g. implemented by `domain/service`)
- `port/out` — outbound interfaces that `domain/service` depends on and driven adapters implement
- `adapter/in/api` — REST controllers (driving side), calling `port/in` use cases; each request DTO
  (e.g. `UserCreateRequest`) and response DTO (e.g. `UserResponse`) lives here too, never the raw
  domain model — controllers return the DTO, not `domain/model` types. A single `{Feature}ControllerMapper`
  per aggregate (e.g. `UserControllerMapper`) handles both request→command and domain→response mapping;
  don't split that into separate request/response mapper classes.
- `adapter/out/persistence` — JPA entities, Spring Data repositories, and adapter classes implementing
  `port/out` interfaces, plus mappers between domain models and JPA entities

Dependency direction is strictly `adapter → port → domain`. `domain/model` must stay framework-free
(plain records/classes, no Spring/JPA/HTTP imports) — JPA entities and API request/response DTOs are
kept separate from domain models (mapped via a dedicated mapper class per aggregate on each side), not
reused or annotated directly. `domain/service` classes are allowed to carry Spring stereotype/validation
annotations (`@Service`, `@Component`, `@Validated`) for DI convenience — this is a deliberate, confirmed
exception to hexagonal purity for this project, not a bug to flag or "fix".

Only the `User` slice is scaffolded so far (create/get); `Slot`, `Meeting`, `Participant`, and
`Availability` still need to be built out following the same layering — see
[docs/api.md](docs/api.md) for the target endpoint set. `ApiExceptionHandler` (`adapter/in/api`) is
the single `@RestControllerAdvice`; currently it only maps `MethodArgumentNotValidException` to
`400` — new use cases that introduce their own failure modes (conflicts, not-found, etc.) should add
handlers there rather than letting exceptions fall through to a generic `500`.

The two `User` endpoints implemented so far live under `UserController`'s actual
`@RequestMapping("v1/users")` — i.e. `/api/v1/users` and `/api/v1/users/{id}` (GET), not the
unversioned `/api/users` paths in `docs/api.md`. Reconcile that (drop `/v1` here, or add it to the
docs) before building out `Slot`/`Meeting`/`Participant` so all endpoints share one convention.

## Domain model

- **User** — owns a calendar. *Calendar* exists as a domain concept only and is never exposed
  through the API.
- **Slot** — a discrete, stored, editable time interval owned by a user, created by slicing a time
  range into consecutive slots of a configurable duration.
- **Meeting** — created by booking exactly one slot (1:1). Has a title, description and participants.
- **Participant** — an external contact (`name` + `email`), not a platform user.

See [docs/database.md](docs/database.md) for how these map to tables (incl. how FREE/BUSY status is
derived rather than stored, and UTC timestamp handling).

## Migrations

The full target schema (see [docs/database.md](docs/database.md)) is already applied via
`src/main/resources/db/migration/V2026.08.07_19.30__init_schema.sql`, ahead of the `Slot`/`Meeting`/
`Participant` application code that will use it.

Migration files are versioned `V<UTC timestamp yyyy.MM.dd_HH.mm>__description.sql` instead of
sequential integers, so migrations authored on parallel branches don't collide on the same version
number when merged. `spring.flyway.out-of-order` is `false` in `application.yaml` (Flyway's default,
made explicit) so that if a migration with an older timestamp is merged in after a newer one has
already run, it fails loudly instead of being silently applied out of order.
`spring.flyway.validate-migration-naming` is also `true`, so a misnamed migration file fails fast at
startup instead of being silently skipped.

## Key design decisions

- **Slot/Meeting is 1:1.** Booking a longer block requires creating a slot of that duration up
  front; slots are not merged across bookings.
- **No cross-calendar conflict checking.** Booking validates and locks only the slot being booked.
  Participants are external contacts, so nothing is checked/blocked on their behalf. Group
  availability matching (Doodle-poll style) is out of scope.
- **No authentication/authorization, recurring slots/meetings, outbound notifications, or
  microservice split** — explicitly out of scope; see README "Not implemented" for the full list.

See [docs/database.md](docs/database.md) and [docs/api.md](docs/api.md) for DB- and API-specific
design decisions and trade-offs (PostgreSQL vs. NoSQL, read caching, no Spring Data REST, etc.).