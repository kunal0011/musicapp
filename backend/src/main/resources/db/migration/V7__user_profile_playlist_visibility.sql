-- V7: User profile fields, playlist visibility, and HLS URL

-- User profile
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar_url    VARCHAR(1000);
ALTER TABLE users ADD COLUMN IF NOT EXISTS display_name  VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio           TEXT;

-- Playlist ownership and visibility
ALTER TABLE playlists ADD COLUMN IF NOT EXISTS is_public BOOLEAN DEFAULT TRUE;
ALTER TABLE playlists ADD COLUMN IF NOT EXISTS owner_id  BIGINT REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_playlists_owner_id ON playlists(owner_id);

-- HLS URL on tracks
ALTER TABLE tracks ADD COLUMN IF NOT EXISTS hls_url VARCHAR(1000);
