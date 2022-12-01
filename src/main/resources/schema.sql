CREATE TABLE IF NOT EXISTS users (
    id      BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name    VARCHAR(255)        NOT NULL,
    email   VARCHAR(512)        NOT NULL,
    CONSTRAINT uq_user_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS requests (
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    description     TEXT        NOT NULL,
    requester_id    BIGINT      NOT NULL    REFERENCES  users (id) ON DELETE CASCADE,
    created         TIMESTAMP   NOT NULL
);

CREATE TABLE IF NOT EXISTS items (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name        VARCHAR(255)    NOT NULL,
    description TEXT            NOT NULL,
    available   BOOLEAN         NOT NULL,
    owner_id    BIGINT          NOT NULL    REFERENCES users (id) ON DELETE CASCADE,
    request_id  BIGINT                      REFERENCES requests (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS bookings (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    start_date  TIMESTAMP       NOT NULL,
    end_date    TIMESTAMP       NOT NULL,
    item_id     BIGINT          NOT NULL    REFERENCES items (id) ON DELETE CASCADE,
    booker_id   BIGINT          NOT NULL    REFERENCES users (id) ON DELETE CASCADE,
    status      VARCHAR(20)     NOT NULL
);

CREATE TABLE IF NOT EXISTS comments (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    text        TEXT            NOT NULL,
    item_id     BIGINT          NOT NULL    REFERENCES items (id) ON DELETE CASCADE,
    author_id   BIGINT          NOT NULL    REFERENCES users (id) ON DELETE CASCADE,
    created     TIMESTAMP       NOT NULL
);