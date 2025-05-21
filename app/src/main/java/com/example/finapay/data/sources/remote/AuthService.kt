package com.example.finapay.data.sources.remote

import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.models.AuthModel
import com.example.finapay.data.models.requests.ChangePasswordRequest
import com.example.finapay.data.models.requests.GoogleSignInRequest
import com.example.finapay.data.models.requests.LoginRequest
import com.example.finapay.data.models.requests.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthService {
    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<ApiResponse<AuthModel>>

    @POST("auth/login-google")
    fun signInGoogle(@Body googleSignInRequest: GoogleSignInRequest): Call<ApiResponse<AuthModel>>

    @POST("auth/register")
    fun register(@Body registerRequest: RegisterRequest): Call<ApiResponse<AuthModel>>

    @PUT("auth/change-password")
    fun changePassword(@Body changePasswordRequest: ChangePasswordRequest): Call<ApiResponse<Boolean?>>

    @DELETE("auth/logout/{fcmToken}")
    fun logout(@Path("fcmToken") fcmToken: String): Call<ApiResponse<Unit>>

}