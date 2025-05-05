package com.example.finapay.data.repositories

import com.example.finapay.data.models.ApiResponse
import com.example.finapay.data.models.LoginResponse
import com.example.finapay.data.models.requests.LoginRequest
import com.example.finapay.data.sources.remote.ApiClient
import retrofit2.Call

class AuthRepository {
    fun login(email: String, password: String): Call<ApiResponse<LoginResponse>> {
        val loginRequest = LoginRequest(email, password)
        return ApiClient.authService.login(loginRequest)
    }
}