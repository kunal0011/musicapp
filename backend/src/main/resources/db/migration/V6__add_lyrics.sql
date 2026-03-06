-- V6: Lyrics table

CREATE TABLE IF NOT EXISTS lyrics (
    id         BIGSERIAL PRIMARY KEY,
    track_id   BIGINT NOT NULL UNIQUE REFERENCES tracks(id) ON DELETE CASCADE,
    lrc_content TEXT,
    plain_text  TEXT
);

CREATE INDEX IF NOT EXISTS idx_lyrics_track_id ON lyrics(track_id);
