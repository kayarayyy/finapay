package com.example.finapay.data.repositories

import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.models.AuthModel
import com.example.finapay.data.models.requests.LoginRequest
import com.example.finapay.data.models.requests.RegisterRequest
import com.example.finapay.data.sources.ApiClient
import retrofit2.Call

class AuthRepository {
    fun login(email: String, password: String): Call<ApiResponse<AuthModel>> {
        val loginRequest = LoginRequest(email, password)
        return ApiClient.authService.login(loginRequest)
    }

    fun register(email: String, password: String, name: String): Call<ApiResponse<AuthModel>> {
        val registerRequest = RegisterRequest(email, password, name)
        return ApiClient.authService.register(registerRequest)
    }
}