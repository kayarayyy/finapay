package com.example.finapay.data.models.requests

data class ChangePasswordRequest(
    val old_password: String,
    val new_password: String,
    val confirm_password: String,
)
