CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(128) NOT NULL,
    email      VARCHAR(128) NOT NULL UNIQUE,
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

CREATE INDEX idx_slots_user_time ON slots (user_id, start_time);
CREATE INDEX idx_slots_free ON slots (user_id) WHERE meeting_id IS NULL;