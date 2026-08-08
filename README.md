# Mini Doodle — Meeting Scheduling Service

A backend service for managing personal time slots and converting them into meetings, with an
aggregated free/busy view per user.

**Stack:** Java, Spring Boot, PostgreSQL, Flyway, Docker Compose.

## Running locally

```bash
docker-compose up --build
```

- Service: `http://localhost:8080`
- API docs (OpenAPI/Swagger UI): `http://localhost:8080/swagger-ui.html`
- Health & metrics (Actuator): `http://localhost:8080/actuator/health`, `/actuator/metrics`
- PostgreSQL: `localhost:5432`

Schema is applied automatically on startup via Flyway migrations.

## CI/CD

GitHub Actions (`.github/workflows/ci.yml`) runs on every pull request and on push to `main`, with
two independent jobs:

- **lint** — `./gradlew spotlessCheck`, fails the build on unformatted Java
- **test** — `./gradlew test` (JUnit 5 + Testcontainers; the runner needs Docker available)

There's no deployment step yet — CI is verification-only.

## Code style

Java is formatted with [Spotless](https://github.com/diffplug/spotless) using Google Java Format,
plus unused-import removal, trailing-whitespace trimming, and a trailing newline (see the
`spotless { java { ... } }` block in `build.gradle`). Run `./gradlew spotlessApply` before
committing — `spotlessCheck` is wired into `check`, so `./gradlew build`, and CI, both fail on
unformatted code.

## Domain model

- **User** — owns a calendar. *Calendar* exists as a domain concept only and is never exposed
  through the API.
- **Slot** — a discrete, stored, editable time interval owned by a user. Created by slicing a time
  range into consecutive slots of a configurable duration.
- **Meeting** — created by booking exactly one slot (1:1). Has a title, description and participants.
- **Participant** — an external contact (`name` + `email`), not a platform user.

## Docs

- [Database](docs/database.md) — schema/DDL, why PostgreSQL, DB-related trade-offs
- [API](docs/api.md) — full endpoint reference, examples, API-related trade-offs

## Key design decisions & trade-offs

- **Slot/Meeting is 1:1**, per the task wording ("A slot can be booked as a meeting"). Booking a
  longer block requires creating a slot of that duration up front; slots are not merged across
  bookings.
- **No cross-calendar conflict checking.** Booking validates and locks only the slot being booked.
  Participants are external contacts, so nothing is checked or blocked on their behalf. Group
  availability matching (Doodle-poll style) is a materially different feature and not implemented.
- **Slot lookups for update/delete are scoped to `(id, userId)` together, not `id` alone**, even
  though `id` is already globally unique. This makes the `userId` in
  `/users/{userId}/slots/{slotId}` load-bearing — a slot can only be reached through the user it
  actually belongs to, instead of any `userId` in the path resolving the same row by its primary key
  (which would be a broken-object-level-authorization gap).

See the individual docs above for database- and API-specific trade-offs (DB choice, caching, API
framework choice).

## Not implemented

- Authentication and authorization — endpoints assume a trusted caller.
- Recurring slots or meetings (each slot is a one-off record on a concrete date).
- Outbound notifications or calendar invites to participants — not requested by the task.
- Server-side timezone handling beyond storing UTC.
- Splitting into microservices or adding auxiliary services (analytics, notifications). At this
  scale, and given the requirement to run everything through a single `docker-compose`, a monolith
  is the right fit; a split would add operational overhead with no corresponding benefit.
