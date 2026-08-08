# Slot — add, update, remove, list

## Context

`Slot` is the next hexagonal slice to build, following the pattern already established by `User`
(`domain/model` → `port/in`/`port/out` → `domain/service` → `adapter/in/api`/`adapter/out/persistence`).
The `slots` table already exists (migration `V2026.08.07_19.30__init_schema.sql`), including a
Postgres `EXCLUDE USING gist` constraint that rejects overlapping `(user_id, tstzrange(start_time, end_time))`
ranges at the database level, and a `CHECK (end_time > start_time)` constraint.

## Scope

Four endpoints under `/api/v1/users/{userId}/slots` (the `/v1` prefix is kept as-is; the project's
target API table in `CLAUDE.md` will be updated to show `/v1` rather than dropping it from the code):

- Add one or more slots
- Update a slot's time range
- Remove a slot
- List a user's slots, paginated and filtered by date range and availability

**Out of scope for this pass:**
- Meeting/booking logic. The `meeting_id` column exists in the schema but stays unused (always `NULL`).
- Server-side slicing of a time range by a duration. Rejected in favor of the client sending the exact
  list of `{start, end}` ranges it wants created.
- **Explicit error handling for "user not found," "slot not found," and "overlapping range."** This is
  a deliberate cut, not an oversight — see "Success-only behavior" below. It will be added in a
  follow-up pass once the happy path is in place.

## Success-only behavior

Per explicit direction: no sealed result types, no existence checks, no overlap catching. Each use case
does the operation and returns the domain object directly — there is exactly one outcome to handle.
Concretely, this means:

- **Create**: does not check the user exists first. If `userId` doesn't reference a real user, the
  insert fails on the `slots.user_id` foreign key and the exception propagates unhandled (`500`, via
  Spring's default error handling). Same for an overlapping range — the DB's exclusion constraint
  rejects it, and that also propagates unhandled (`500`).
- **Update**: does not check the slot exists first, and does not report overlap. If `(id, userId)`
  doesn't match any row, the update is a silent no-op — the endpoint still returns `200` with the
  range the caller asked for, even though nothing changed in the database. An overlapping new range
  fails the same way as create (unhandled `500`).
- **Delete**: does not check the slot exists first. If `(id, userId)` doesn't match any row, deleting 0
  rows is not an error — the endpoint still returns `204`.
- **List**: unaffected — it never had an error branch (a nonexistent `userId` was already designed to
  just yield an empty page).

## Endpoints

### `POST /v1/users/{userId}/slots` — add

Request body: a bare JSON array of ranges, not wrapped in an object:

```json
[
  { "start": "2026-08-10T09:00:00Z", "end": "2026-08-10T09:30:00Z" },
  { "start": "2026-08-10T09:30:00Z", "end": "2026-08-10T10:00:00Z" }
]
```

Behavior:
1. Validate each item's `start`/`end` via bean validation (see below) — `400` if any item is invalid.
2. Bulk-insert all ranges as new slots.
3. Success: `200` with the array of created slots.

### `PUT /v1/users/{userId}/slots/{slotId}` — update

Request body: `{ "start": ..., "end": ... }`.

Behavior:
1. Validate `start`/`end` via bean validation — `400` if invalid.
2. Update the slot's range in place.
3. Success: `200` with the (intended) updated slot — see "Success-only behavior" for the no-op-if-missing
   caveat.

### `DELETE /v1/users/{userId}/slots/{slotId}` — remove

Behavior:
1. Delete the slot.
2. Success: `204 No Content`, regardless of whether a row actually existed.

### `GET /v1/users/{userId}/slots` — list

Query params, all optional:
- `from`, `to` (`Instant`) — filter on `start_time` as a half-open range: `from <= start_time` and
  `start_time < to`. Each applies independently if the other is omitted.
- `available` (`Boolean`) — `true` = only unbooked slots (`meeting_id IS NULL`), `false` = only booked
  (`meeting_id IS NOT NULL`), omitted = both.
- `page`, `size`, `sort` — Spring's standard `Pageable` binding (auto-configured; no extra setup needed
  since both `spring-boot-starter-webmvc` and `spring-boot-starter-data-jpa` are on the classpath).

No user-existence check — a nonexistent `userId` just yields an empty page.

Success: `200` with Spring's standard `Page<SlotResponse>` JSON envelope (`content`, `totalElements`,
`totalPages`, `number`, `size`).

### Request validation

`{start, end}` is validated with:
- `@NotNull` on both fields (existing pattern, e.g. `UserCreateRequest`)
- A new class-level constraint `@ValidTimeRange` (with its `ConstraintValidator`) checking
  `end.isAfter(start)`, applied to the shared `SlotRangeRequest` record

Both go through the existing `MethodArgumentNotValidException` → `400` handler in `ApiExceptionHandler`
— no changes needed there.

## Domain model (`domain/model`)

- `Slot(Long id, Long userId, Long meetingId, Instant startTime, Instant endTime, Instant createdAt, Instant updatedAt)`
  — already exists from an earlier attempt at this feature; reused as-is, plus a new `isAvailable()`
  helper (`meetingId == null`).
- `SlotRange(Instant start, Instant end)` — plain value object, used inside the create/update commands.
- `SlotCreateCommand(Long userId, List<SlotRange> slots)`
- `SlotUpdateCommand(Long userId, Long slotId, SlotRange range)`
- `SlotListQuery(Long userId, Instant from, Instant to, Boolean available)` — all fields except `userId`
  are nullable filters.

No result types, no custom exceptions — see "Success-only behavior."

## Ports

`port/in`:
- `SlotCreateUseCase.create(SlotCreateCommand) -> List<Slot>`
- `SlotUpdateUseCase.update(SlotUpdateCommand) -> Slot`
- `SlotDeleteUseCase.delete(Long userId, Long slotId) -> void`
- `SlotListUseCase.list(SlotListQuery, Pageable) -> Page<Slot>` — `Pageable`/`Page` are Spring Data
  types used directly in the port signature (not wrapped in a custom paging type); `domain/model`
  itself stays framework-free since `Pageable` only appears as a method parameter, not inside a model
  record.

`port/out`:
- `SlotCreatePort.createAll(List<Slot>) -> List<Slot>`
- `SlotUpdatePort.update(Slot) -> void` — the service already knows every field the caller asked for
  (`id`, `userId`, the new range); it doesn't need anything back from the adapter to build the response.
- `SlotDeletePort.delete(Long userId, Long slotId) -> void`
- `SlotListPort.list(SlotListQuery, Pageable) -> Page<Slot>`

No `SlotGetPort` — nothing needs to fetch-then-branch anymore.

## Domain service

`SlotService` implements all four inbound use cases. Depends only on the four Slot out-ports — no
`UserGetPort` dependency (create no longer checks the user exists).

- `create`: build `Slot` instances (`id`/`meetingId`/timestamps null) from the command's ranges →
  `slotCreatePort.createAll(...)` → return the result.
- `update`: build a `Slot` from the command (`id = slotId`, `meetingId = null`, the new range) →
  `slotUpdatePort.update(...)` → return that same constructed `Slot` (not re-fetched).
- `delete`: `slotDeletePort.delete(userId, slotId)`.
- `list`: pure delegation to `slotListPort.list(query, pageable)`.

## Persistence (`adapter/out/persistence`)

- `SlotEntity` — JPA entity mirroring the `slots` table. Unlike `UserEntity` (`@Immutable`, since users
  are never updated), `SlotEntity` is a plain mutable entity because `PUT` updates it. Fields:
  `id` (`IDENTITY`), `userId`, `meetingId` (nullable, always null for now), `startTime`, `endTime`,
  `createdAt` (`@CreationTimestamp`), `updatedAt` (`@UpdateTimestamp`).
- `SlotRepository extends JpaRepository<SlotEntity, Long>, JpaSpecificationExecutor<SlotEntity>` with a
  derived delete query `deleteByIdAndUserId(Long id, Long userId)` (returns `void` or a count — either
  way, deleting zero rows is not an error). `JpaSpecificationExecutor` (already available transitively
  via `spring-boot-starter-data-jpa`) is the idiomatic way to compose `list`'s several independent
  optional filters without hand-written null-checking JPQL.
- `SlotEntityMapper` — `Slot` ↔ `SlotEntity`, same shape as `UserEntityMapper`.
- `SlotAdapter` implements all four out-ports, no exception handling in any of them:
  - `createAll`: `slotRepository.saveAll(entities)`, map back to `List<Slot>`. A DB-level rejection
    (FK violation, exclusion constraint) propagates unhandled.
  - `update`: fetches by `findByIdAndUserId(...)` and, **only if present**, sets the new `startTime`/
    `endTime` on the managed entity and calls `save`. This is *not* a "not found" error branch exposed
    to the caller — it's purely how a partial field update is done correctly in JPA (a raw
    `UPDATE ... SET` bulk query would bypass `@UpdateTimestamp` and risk clobbering `createdAt`). If no
    row matches, the method just returns without writing anything — the silent no-op described above.
  - `delete`: `slotRepository.deleteByIdAndUserId(userId, slotId)`.
  - `list`: builds a `Specification<SlotEntity>` combining a mandatory `userId` predicate with optional
    `startTime >= from`, `startTime < to`, and `meetingId IS NULL`/`IS NOT NULL` predicates (only added
    when the corresponding query field is non-null), then calls `slotRepository.findAll(spec, pageable)`
    and maps the resulting `Page<SlotEntity>` to `Page<Slot>` via `.map(slotEntityMapper::toModel)`.

## API (`adapter/in/api`)

- `SlotController` — `@RequestMapping("v1/users/{userId}/slots")`, four handlers. Each simply calls its
  use case and wraps the result in a `ResponseEntity` — no branching, since every use case now has one
  outcome.
- `SlotRangeRequest(@NotNull Instant start, @NotNull Instant end)` with `@ValidTimeRange` — reused for
  both the create endpoint's array items and the update endpoint's body.
- `SlotResponse(Long id, Long userId, Instant startTime, Instant endTime, boolean available)` — the
  record component is named `available` (not `isAvailable`) so it serializes as plain JSON
  `"available": true/false`; records don't do JavaBean `is`-prefix stripping the way POJOs do, so
  naming it `isAvailable` would keep the `is` in the JSON output.
- `SlotControllerMapper` — request → command mapping (including the `list` query params →
  `SlotListQuery`) and `Slot` → `SlotResponse` mapping, same role as `UserControllerMapper`.

## Cleanup

The earlier interrupted attempts at this feature left some stale files on disk
(`Slot.java` is reusable as-is; `SlotService.java`, `SlotControllerMapper.java`, `SlotCreatePort.java`,
and the persistence classes reference abandoned designs — a "slice by duration" approach and, later, a
sealed-result-type approach — and will be rewritten to match this spec).

## Testing

- Unit tests for `SlotService` (Mockito, no Spring context) covering the one happy-path outcome per
  operation: create returns the created slots, update returns the constructed slot, delete completes,
  list delegates to `SlotListPort` and returns its result unchanged. Error-path tests (missing user,
  overlap, missing slot) are deferred along with the behavior itself.
- Existing `MiniDoodleApplicationTests` (Testcontainers-backed context load) continues to pass.

## Documentation

Update `CLAUDE.md`'s target API table to show the `/v1` prefix (matching the decision to keep `/v1`
rather than drop it from `UserController`), and mark the four Slot endpoints as implemented once done.
