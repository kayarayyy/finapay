package com.example.finapay.data.sources.remote

import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.models.AuthModel
import com.example.finapay.data.models.requests.LoginRequest
import com.example.finapay.data.models.requests.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<ApiResponse<AuthModel>>

    @POST("auth/register")
    fun register(@Body registerRequest: RegisterRequest): Call<ApiResponse<AuthModel>>
}