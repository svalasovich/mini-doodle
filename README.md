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
