# Database

## Schema

### User

| Attribute | Type | Key / Constraints |
|---|---|---|
| id | BIGSERIAL | PK |
| name | VARCHAR(255) | NOT NULL |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| createdAt | TIMESTAMPTZ | NOT NULL, default now() |

### Meeting

| Attribute | Type | Key / Constraints |
|---|---|---|
| id | BIGSERIAL | PK |
| title | VARCHAR(255) | NOT NULL |
| description | TEXT | |
| createdAt | TIMESTAMPTZ | NOT NULL, default now() |
| updatedAt | TIMESTAMPTZ | NOT NULL, default now() |

### MeetingParticipant

| Attribute | Type | Key / Constraints |
|---|---|---|
| id | BIGSERIAL | PK |
| meetingId | BIGINT | FK → Meeting.id, NOT NULL, ON DELETE CASCADE, indexed |
| name | VARCHAR(255) | NOT NULL |
| email | VARCHAR(255) | NOT NULL |

### Slot

| Attribute | Type | Key / Constraints |
|---|---|---|
| id | BIGSERIAL | PK |
| userId | BIGINT | FK → User.id, NOT NULL, ON DELETE CASCADE |
| meetingId | BIGINT | FK → Meeting.id, UNIQUE, nullable, ON DELETE SET NULL |
| startTime | TIMESTAMPTZ | NOT NULL |
| endTime | TIMESTAMPTZ | NOT NULL, CHECK endTime > startTime |
| createdAt | TIMESTAMPTZ | NOT NULL, default now() |
| updatedAt | TIMESTAMPTZ | NOT NULL, default now() |

Indexed on `(userId, startTime, endTime)` for range queries, and on `userId` where `meetingId IS
NULL` for free-slot lookups. An exclusion constraint (`EXCLUDE USING gist`, requires the
`btree_gist` extension) rejects overlapping time ranges for the same `userId`.

### Relationships

| Relationship | Cardinality | Enforced by |
|---|---|---|
| User → Slot | 1 to many | `Slot.userId` FK, `ON DELETE CASCADE` |
| Meeting → MeetingParticipant | 1 to many | `MeetingParticipant.meetingId` FK, `ON DELETE CASCADE` |
| Meeting → Slot | 1 to 0..1 | `Slot.meetingId` FK, `UNIQUE`, `ON DELETE SET NULL` |

Integrity is enforced by the database rather than re-implemented in application code: overlapping
slots for the same user are rejected by the exclusion constraint on `Slot`, and the 1:1 slot/meeting
link is guaranteed by the `UNIQUE` constraint on `Slot.meetingId`. Indexes target the dominant access
pattern — range queries scoped to a single user.

A slot's FREE/BUSY status is not stored — it is derived from whether `slot.meeting_id` is set. This
keeps a single source of truth that cannot drift from the actual booking state. All timestamps are
stored and returned in UTC (`TIMESTAMPTZ`); timezone conversion for display is a client
responsibility.

## Why PostgreSQL

PostgreSQL over NoSQL (MongoDB, Cassandra). The data is inherently relational and booking requires
strong consistency — a slot must never be double booked. Cassandra targets write-heavy,
eventually-consistent, massively distributed workloads, which neither matches this scale nor the
consistency needed. MongoDB would mean denormalizing or joining in application code, and range-based
free/busy aggregation would have to be built by hand.

## Trade-offs

- **Read caching for availability and slot listing.** These are read-heavy and frequently requested;
  a cache reduces read latency and DB load. Invalidated per user on booking, cancellation and slot
  create/delete — the operations that change availability.

## Future scaling ideas

Not implemented — current scale doesn't need them — but worth noting as the natural next steps if
load outgrows a single PostgreSQL instance:

- **Sharding users across multiple DB instances.** Almost all access (slots, availability, booking)
  is scoped to a single user, so `userId` is a natural shard key — each user's data lives entirely on
  one shard, avoiding cross-shard joins or transactions for the common path. Only cross-user
  operations (e.g. a global admin view) would need fan-out.
- **CQRS to split read and write paths.** Writes (slot creation, booking, cancellation) need the
  strong consistency PostgreSQL provides; reads (availability, slot listing) are heavier and more
  frequent, and could be served from a separate read-optimized store or replica kept eventually
  consistent via events. This decouples read scaling from write consistency guarantees instead of
  tuning one instance for both.
