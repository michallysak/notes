CREATE TABLE note_shares
(
    id      UUID PRIMARY KEY,
    note_id UUID NOT NULL,
    user_id UUID NOT NULL,
    permissions jsonb,
    CONSTRAINT fk_note_shares_note
        FOREIGN KEY (note_id) REFERENCES notes (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_note_shares_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_note_shares_note_id ON note_shares (note_id);
CREATE INDEX idx_note_shares_user_id ON note_shares (user_id);

