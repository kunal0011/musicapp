package com.musicapp.android.models

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val coverArtUrl: String?,
    val streamUrl: String
)
