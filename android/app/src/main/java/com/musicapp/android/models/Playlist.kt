package com.musicapp.android.models

data class Playlist(
    val id: Long,
    val name: String,
    val coverUrl: String,
    val tracks: List<Track>
)
