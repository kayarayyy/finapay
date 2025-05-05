package com.example.finapay.data.models

data class LoginResponse(
    val email: String,
    val name: String,
    val role: RoleModel,
    val token: String,
    val features: List<String>,
    val is_active: Boolean
)