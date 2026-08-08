# API

All routes are served under `/api`. API docs (OpenAPI/Swagger UI) are available at
`http://localhost:8080/swagger-ui.html` when running locally.

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

## Trade-offs

- **No Spring Data REST.** None of the resources are pure CRUD: slot creation slices a range into
  multiple records, deletion respects booking state, booking is a conditional atomic write, and
  availability is a derived read-model. Using it for the "simple" endpoints and custom controllers
  elsewhere would mix two response conventions (HAL vs plain JSON) in one API. It fits services that
  expose a database more or less directly — not this one.
