package com.example.finapay.data.models.response

data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T,
    val status_code: Int
)
