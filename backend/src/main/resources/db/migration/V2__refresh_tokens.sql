-- ── V2: Refresh token table ───────────────────────────────────
CREATE TABLE IF NOT EXISTS refresh_token (
    id              BIGSERIAL PRIMARY KEY,
    token           VARCHAR(512)  NOT NULL UNIQUE,
    utilisateur_id  BIGINT        NOT NULL REFERENCES utilisateur(id) ON DELETE CASCADE,
    expires_at      TIMESTAMP     NOT NULL,
    created_at      TIMESTAMP     DEFAULT NOW(),
    is_revoked      BOOLEAN       DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_token ON refresh_token(token);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user  ON refresh_token(utilisateur_id);
