package com.example.finapay.data.models

data class AuthModel(
    val email: String,
    val name: String,
    val role: RoleModel,
    val token: String,
    val features: List<String>,
    val is_active: Boolean
)