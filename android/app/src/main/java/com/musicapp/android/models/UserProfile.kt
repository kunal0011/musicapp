package com.musicapp.android.models

data class UserProfile(
    val id: Long,
    val username: String,
    val email: String,
    val displayName: String,
    val bio: String = "",
    val avatarUrl: String = ""
)
