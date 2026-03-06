package com.musicapp.android.models

data class Artist(
    val id: Long,
    val name: String,
    val bio: String? = null,
    val imageUrl: String? = null,
    val followerCount: Long = 0
)
