package com.example.finapay.data.models.requests

data class FcmRegisterRequest(
    val userEmail: String?,
    val fcmToken: String
)
