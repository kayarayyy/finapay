package com.example.finapay.data.models.requests

data class LoginRequest(
    val email: String,
    val password: String,
    val fcmToken: String
)