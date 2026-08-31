-- OAuth2 users (Google login) never set a local password, so it must become optional.
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;

-- Tracks which identity provider owns the account: LOCAL (email/password) or GOOGLE (OAuth2).
ALTER TABLE users ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';

-- The provider's unique subject/user id (e.g. Google's "sub" claim). NULL for LOCAL accounts.
ALTER TABLE users ADD COLUMN provider_id VARCHAR(255);

-- One account per (provider, provider_id) pair; partial index because provider_id is NULL for LOCAL users.
CREATE UNIQUE INDEX idx_users_provider_provider_id ON users(provider, provider_id) WHERE provider_id IS NOT NULL;

-- Refresh tokens let a client get a new short-lived access token without re-authenticating.
-- Only the SHA-256 hash is stored so a leaked database dump can't be replayed as a valid token.
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
