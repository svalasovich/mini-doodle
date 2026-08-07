# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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
- Run normally: `./gradlew bootRun` — requires a reachable Postgres per `application.yaml`. The README
  describes `docker-compose up --build` as the intended way to provide one, but no `docker-compose.yml`
  exists in the repo yet.

Tests use JUnit 5 and Testcontainers (Postgres) — running them requires Docker to be available.

Dependencies use Spring Boot 4.1's split starter names (e.g. `spring-boot-starter-webmvc`,
`spring-boot-starter-data-jpa` + matching `-test` starters), not the older `spring-boot-starter-web`
convention — match this style when adding dependencies to `build.gradle`. Lombok is available on
both main and test source sets.

All HTTP routes are served under the `/api` context path (`server.servlet.context-path` in
`application.yaml`).

## Architecture

The codebase is being restructured into hexagonal (ports & adapters) architecture. Package layout
under `com.minidoodle.minidoodle`:

- `domain/model` — framework-free domain types (plain records/classes, no Spring/JPA imports)
- `domain/service` — use case implementations; depend only on `port/in` and `port/out` interfaces
- `port/in` — inbound use-case interfaces that driving adapters call (e.g. implemented by `domain/service`)
- `port/out` — outbound interfaces that `domain/service` depends on and driven adapters implement
- `adapter/in/api` — REST controllers (driving side), calling `port/in` use cases
- `adapter/out/persistence` — JPA entities, Spring Data repositories, and adapter classes implementing
  `port/out` interfaces, plus mappers between domain models and JPA entities

Dependency direction is strictly `adapter → port → domain`; the domain layer must not import Spring,
JPA, or any other framework type. JPA entities are kept separate from domain models (mapped via a
dedicated mapper class per aggregate), not annotated directly.

Only the `User` slice is scaffolded so far (create/get); `Slot`, `Meeting`, `Participant`, and
`Availability` still need to be built out following the same layering.

## Domain model

- **User** — owns a calendar. *Calendar* exists as a domain concept only and is never exposed
  through the API.
- **Slot** — a discrete, stored, editable time interval owned by a user, created by slicing a time
  range into consecutive slots of a configurable duration.
- **Meeting** — created by booking exactly one slot (1:1). Has a title, description and participants.
- **Participant** — an external contact (`name` + `email`), not a platform user.

A slot's FREE/BUSY status is not stored — it is derived from whether `slot.meeting_id` is set, so it
can never drift from the actual booking state. All timestamps are stored/returned in UTC
(`TIMESTAMPTZ`); timezone conversion for display is a client responsibility.

## API surface (target)

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/users` | Create a user |
| `POST` | `/api/users/{userId}/slots` | Slice `{start, end, duration}` into consecutive free slots |
| `GET` | `/api/users/{userId}/slots?from=&to=` | List raw slots in a time frame |
| `PUT` | `/api/users/{userId}/slots/{slotId}` | Replace a slot's time range (only if not booked) |
| `DELETE` | `/api/users/{userId}/slots/{slotId}` | Delete a slot (only if not booked) |
| `POST` | `/api/meetings` | Book a slot: `{slotId, title, description, participants: [{name, email}]}` |
| `GET` | `/api/meetings/{meetingId}` | Meeting details incl. participants and linked slot |
| `DELETE` | `/api/meetings/{meetingId}` | Cancel the meeting; the slot is freed automatically |
| `GET` | `/api/users/{userId}/availability?from=&to=` | Aggregated free/busy view |

Booking returns `409 Conflict` if the slot is already taken, handled via a conditional
`UPDATE slots SET meeting_id = ? WHERE id = ? AND meeting_id IS NULL` — if no rows are affected,
another request won the race. Availability aggregation merges adjacent slots with the same status
into a single interval, regardless of the slot granularity used at creation time.

The full target schema (see README) is already applied via
`src/main/resources/db/migration/V20260807193034__init_schema.sql`, ahead of the `Slot`/`Meeting`/
`Participant` application code that will use it. It includes a Postgres `EXCLUDE USING gist`
constraint on `(user_id, tstzrange(start_time, end_time))` to reject overlapping slots at the
database level, and a `UNIQUE` constraint on `slots.meeting_id` to enforce the 1:1 slot/meeting link
— integrity is enforced by the database rather than re-implemented in application code.

Migration files are versioned `V<UTC timestamp yyyyMMddHHmmss>__description.sql` instead of sequential
integers, so migrations authored on parallel branches don't collide on the same version number when
merged. `spring.flyway.out-of-order` is set to `false` in `application.yaml` (Flyway's default, made
explicit) so that if a migration with an older timestamp is merged in after a newer one has already
run, it fails loudly instead of being silently applied out of order.

## Key design decisions

- **Slot/Meeting is 1:1.** Booking a longer block requires creating a slot of that duration up
  front; slots are not merged across bookings.
- **No cross-calendar conflict checking.** Booking validates and locks only the slot being booked.
  Participants are external contacts, so nothing is checked/blocked on their behalf. Group
  availability matching (Doodle-poll style) is out of scope.
- **PostgreSQL, not NoSQL.** The data is relational and booking requires strong consistency (a slot
  must never be double booked).
- **Read caching planned for availability and slot listing** (read-heavy endpoints), invalidated per
  user on booking, cancellation, and slot create/delete.
- **No Spring Data REST** — none of the resources are pure CRUD (slicing, booking, and availability
  are all derived/composite operations), and mixing HAL with plain JSON responses was rejected.
- **No authentication/authorization, recurring slots/meetings, outbound notifications, or
  microservice split** — explicitly out of scope; see README "Not implemented" for the full list.