package com.musicapp.android.models

data class LoginRequest(val username: String, val password: String)

data class RegisterRequest(val username: String, val email: String, val password: String)

data class AuthResponse(val token: String, val username: String, val email: String)

data class PagedResponse<T>(
    val content: List<T>,
    val totalElements: Long = 0,
    val totalPages: Int = 1,
    val number: Int = 0,
    val last: Boolean = true
)
