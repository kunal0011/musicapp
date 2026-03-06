-- V1: Baseline schema (existing tables from Hibernate ddl-auto=update)
-- Using IF NOT EXISTS so this is safe to run on an existing DB

CREATE TABLE IF NOT EXISTS tracks (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    artist      VARCHAR(255),
    album       VARCHAR(255),
    cover_art_url VARCHAR(1000),
    stream_url  VARCHAR(1000) NOT NULL
);

CREATE TABLE IF NOT EXISTS playlists (
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    cover_url VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS playlist_tracks (
    playlist_id BIGINT NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
    track_id    BIGINT NOT NULL REFERENCES tracks(id) ON DELETE CASCADE,
    PRIMARY KEY (playlist_id, track_id)
);
