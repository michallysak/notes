CREATE TABLE users
(
    id      UUID PRIMARY KEY,
    email   VARCHAR(255)             NOT NULL UNIQUE,
    created TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE user_credentials
(
    id      UUID PRIMARY KEY,
    created TIMESTAMP WITH TIME ZONE NOT NULL,
    value   TEXT                     NOT NULL,
    user_id UUID                     NOT NULL,
    CONSTRAINT fk_user_credentials_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE TABLE notes
(
    id        UUID PRIMARY KEY,
    author_id UUID                     NOT NULL,
    title     VARCHAR(255)             NOT NULL,
    content   TEXT                     NOT NULL,
    created   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated   TIMESTAMP WITH TIME ZONE,
    pinned    BOOLEAN                  NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_notes_author
        FOREIGN KEY (author_id) REFERENCES users (id)
);