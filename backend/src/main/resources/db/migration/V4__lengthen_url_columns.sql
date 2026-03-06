-- Lengthen URL columns that were created as VARCHAR(255) by old ddl-auto=update
-- JPA entities define length=1000 for these columns
ALTER TABLE tracks ALTER COLUMN cover_art_url TYPE VARCHAR(1000);
ALTER TABLE tracks ALTER COLUMN stream_url TYPE VARCHAR(1000);
ALTER TABLE playlists ALTER COLUMN cover_url TYPE VARCHAR(1000);
