package com.musicapp.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.musicapp.android.models.Track

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val coverArtUrl: String?,
    val streamUrl: String,
    val durationMs: Long = 0L,
    val localFilePath: String? = null,
    val isDownloaded: Boolean = false
)

fun TrackEntity.toTrack() = Track(
    id = id,
    title = title,
    artist = artist,
    album = album,
    coverArtUrl = coverArtUrl,
    streamUrl = if (isDownloaded && localFilePath != null) localFilePath else streamUrl,
    durationMs = durationMs,
    isDownloaded = isDownloaded,
    localFilePath = localFilePath
)

fun Track.toEntity() = TrackEntity(
    id = id,
    title = title,
    artist = artist,
    album = album,
    coverArtUrl = coverArtUrl,
    streamUrl = streamUrl,
    durationMs = durationMs,
    localFilePath = localFilePath,
    isDownloaded = isDownloaded
)
