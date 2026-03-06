package com.musicapp.android.models

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val coverArtUrl: String?,
    val streamUrl: String,
    val durationMs: Long = 0L,
    val liked: Boolean = false,
    val hlsUrl: String? = null,
    val artistId: Long? = null,
    val albumId: Long? = null,
    val albumTitle: String? = null,
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null
)
