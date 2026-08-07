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

A slot's FREE/BUSY status is not stored — it is derived from whether `slot.meeting_id` is set. This
keeps a single source of truth that cannot drift from the actual booking state.

All timestamps are stored and returned in UTC (`TIMESTAMPTZ`); timezone conversion for display is a
client responsibility.

## API

### Users
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/users` | Create a user |

### Slots
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/users/{userId}/slots` | Slice `{start, end, duration}` into consecutive free slots |
| `GET` | `/api/users/{userId}/slots?from=&to=` | List raw slots in a time frame |
| `PUT` | `/api/users/{userId}/slots/{slotId}` | Replace a slot's time range (only if not booked) |
| `DELETE` | `/api/users/{userId}/slots/{slotId}` | Delete a slot (only if not booked) |

### Meetings
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/meetings` | Book a slot: `{slotId, title, description, participants: [{name, email}]}` |
| `GET` | `/api/meetings/{meetingId}` | Meeting details incl. participants and linked slot |
| `DELETE` | `/api/meetings/{meetingId}` | Cancel the meeting; the slot is freed automatically |

Booking returns `409 Conflict` if the slot is already taken. Concurrency is handled by a conditional
`UPDATE slots SET meeting_id = ? WHERE id = ? AND meeting_id IS NULL` — if no rows are affected,
another request won the race.

### Availability
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/users/{userId}/availability?from=&to=` | Aggregated free/busy view |

Adjacent slots with the same status are merged into a single interval, regardless of the slot
granularity used at creation time:

```json
[
  {"start": "2026-08-10T09:00:00Z", "end": "2026-08-10T10:00:00Z", "status": "FREE", "slotIds": [1, 2]},
  {"start": "2026-08-10T10:00:00Z", "end": "2026-08-10T10:30:00Z", "status": "BUSY", "slotIds": [3]}
]
```

## Database schema

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE meetings
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE meeting_participants
(
    id         BIGSERIAL PRIMARY KEY,
    meeting_id BIGINT       NOT NULL REFERENCES meetings (id) ON DELETE CASCADE,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL
);
CREATE INDEX idx_participants_meeting ON meeting_participants (meeting_id);

CREATE TABLE slots
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    meeting_id BIGINT UNIQUE REFERENCES meetings (id) ON DELETE SET NULL,
    start_time TIMESTAMPTZ   NOT NULL,
    end_time   TIMESTAMPTZ   NOT NULL,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT chk_slot_time CHECK (end_time > start_time),
    CONSTRAINT excl_no_overlap EXCLUDE USING gist (
        user_id WITH =,
        tstzrange(start_time, end_time) WITH &&
        )
);
CREATE INDEX idx_slots_user_time ON slots (user_id, start_time, end_time);
CREATE INDEX idx_slots_free ON slots (user_id) WHERE meeting_id IS NULL;
```

Integrity is enforced by the database rather than re-implemented in application code: overlapping
slots for the same user are rejected by an exclusion constraint, and the 1:1 slot/meeting link is
guaranteed by a unique constraint. Indexes target the dominant access pattern — range queries scoped
to a single user.

## Key design decisions & trade-offs

- **Slot/Meeting is 1:1**, per the task wording ("A slot can be booked as a meeting"). Booking a
  longer block requires creating a slot of that duration up front; slots are not merged across
  bookings.
- **No cross-calendar conflict checking.** Booking validates and locks only the slot being booked.
  Participants are external contacts, so nothing is checked or blocked on their behalf. Group
  availability matching (Doodle-poll style) is a materially different feature and not implemented.
- **PostgreSQL over NoSQL (MongoDB, Cassandra).** The data is inherently relational and booking
  requires strong consistency — a slot must never be double booked. Cassandra targets write-heavy,
  eventually-consistent, massively distributed workloads, which neither matches this scale nor the
  consistency needed. MongoDB would mean denormalizing or joining in application code, and
  range-based free/busy aggregation would have to be built by hand.
- **Read caching for availability and slot listing.** These are read-heavy and frequently requested;
  a cache reduces read latency and DB load. Invalidated per user on booking, cancellation and slot
  create/delete — the operations that change availability.
- **No Spring Data REST.** None of the resources are pure CRUD: slot creation slices a range into
  multiple records, deletion respects booking state, booking is a conditional atomic write, and
  availability is a derived read-model. Using it for the "simple" endpoints and custom controllers
  elsewhere would mix two response conventions (HAL vs plain JSON) in one API. It fits services that
  expose a database more or less directly — not this one.

## Not implemented

- Authentication and authorization — endpoints assume a trusted caller.
- Recurring slots or meetings (each slot is a one-off record on a concrete date).
- Outbound notifications or calendar invites to participants — not requested by the task.
- Server-side timezone handling beyond storing UTC.
- Splitting into microservices or adding auxiliary services (analytics, notifications). At this
  scale, and given the requirement to run everything through a single `docker-compose`, a monolith
  is the right fit; a split would add operational overhead with no corresponding benefit.