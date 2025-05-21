package com.example.finapay.data.repositories

import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.models.AuthModel
import com.example.finapay.data.models.requests.ChangePasswordRequest
import com.example.finapay.data.models.requests.GoogleSignInRequest
import com.example.finapay.data.models.requests.LoginRequest
import com.example.finapay.data.models.requests.RegisterRequest
import com.example.finapay.data.sources.ApiClient
import retrofit2.Call

class AuthRepository {
    fun login(email: String, password: String, fcmToken: String): Call<ApiResponse<AuthModel>> {
        val loginRequest = LoginRequest(email, password, fcmToken)
        return ApiClient.authService.login(loginRequest)
    }

    fun signInGoogle(tokenId: String, fcmToken: String): Call<ApiResponse<AuthModel>> {
        val signInRequest = GoogleSignInRequest(tokenId, fcmToken)
        return ApiClient.authService.signInGoogle(signInRequest)
    }

    fun register(email: String, password: String, name: String): Call<ApiResponse<AuthModel>> {
        val registerRequest = RegisterRequest(email, password, name)
        return ApiClient.authService.register(registerRequest)
    }

    fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String): Call<ApiResponse<Boolean?>> {
        val changePaswordRequest = ChangePasswordRequest(oldPassword, newPassword, confirmPassword)
        return ApiClient.authService.changePassword(changePaswordRequest)
    }

    fun logout(fcmToken: String): Call<ApiResponse<Unit>> {
        return ApiClient.authService.logout(fcmToken)
    }
}