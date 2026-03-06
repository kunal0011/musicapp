package com.musicapp.android.models

data class LyricsResponse(
    val id: Long,
    val lrcContent: String? = null,
    val plainText: String? = null
)
