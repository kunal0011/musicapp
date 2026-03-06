-- V3: Track duration, genres, play history, search index

ALTER TABLE tracks ADD COLUMN IF NOT EXISTS duration_ms BIGINT DEFAULT 0;

CREATE TABLE IF NOT EXISTS genres (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS track_genres (
    track_id BIGINT NOT NULL REFERENCES tracks(id) ON DELETE CASCADE,
    genre_id BIGINT NOT NULL REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (track_id, genre_id)
);

CREATE TABLE IF NOT EXISTS play_history (
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    track_id  BIGINT      NOT NULL REFERENCES tracks(id) ON DELETE CASCADE,
    played_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_play_history_user_time ON play_history(user_id, played_at DESC);
CREATE INDEX IF NOT EXISTS idx_tracks_artist ON tracks(artist);
CREATE INDEX IF NOT EXISTS idx_tracks_title ON tracks(title);

-- Full-text search vector column
ALTER TABLE tracks ADD COLUMN IF NOT EXISTS search_vector TSVECTOR;

UPDATE tracks
SET search_vector = to_tsvector('english', coalesce(title, '') || ' ' || coalesce(artist, '') || ' ' || coalesce(album, ''))
WHERE search_vector IS NULL;

CREATE INDEX IF NOT EXISTS idx_tracks_search ON tracks USING GIN(search_vector);

-- Trigger to keep search_vector up to date
CREATE OR REPLACE FUNCTION tracks_search_vector_update() RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector :=
        to_tsvector('english',
            coalesce(NEW.title, '') || ' ' ||
            coalesce(NEW.artist, '') || ' ' ||
            coalesce(NEW.album, ''));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tracks_search_vector_trigger ON tracks;
CREATE TRIGGER tracks_search_vector_trigger
    BEFORE INSERT OR UPDATE ON tracks
    FOR EACH ROW EXECUTE FUNCTION tracks_search_vector_update();
