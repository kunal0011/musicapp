package com.musicapp.android.models

data class Album(
    val id: Long,
    val title: String,
    val artist: Artist? = null,
    val coverArtUrl: String? = null,
    val releaseDate: String? = null
)
