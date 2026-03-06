-- V5: Artists, Albums, and track relationships

CREATE TABLE IF NOT EXISTS artists (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    bio            TEXT,
    image_url      VARCHAR(1000),
    follower_count BIGINT DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS albums (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    artist_id     BIGINT REFERENCES artists(id) ON DELETE SET NULL,
    cover_art_url VARCHAR(1000),
    release_date  DATE
);

-- Add artist/album FK columns to tracks (nullable for backward compat)
ALTER TABLE tracks ADD COLUMN IF NOT EXISTS artist_id BIGINT REFERENCES artists(id) ON DELETE SET NULL;
ALTER TABLE tracks ADD COLUMN IF NOT EXISTS album_id  BIGINT REFERENCES albums(id)  ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_tracks_artist_id ON tracks(artist_id);
CREATE INDEX IF NOT EXISTS idx_tracks_album_id  ON tracks(album_id);
CREATE INDEX IF NOT EXISTS idx_albums_artist_id ON albums(artist_id);

-- Follow relationship
CREATE TABLE IF NOT EXISTS user_followed_artists (
    artist_id BIGINT NOT NULL REFERENCES artists(id) ON DELETE CASCADE,
    user_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (artist_id, user_id)
);
