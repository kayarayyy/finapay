package com.example.finapay.data.sources.remote

import android.provider.ContactsContract.CommonDataKinds.Email
import com.example.finapay.data.models.ApiResponse
import com.example.finapay.data.models.LoginResponse
import com.example.finapay.data.models.requests.LoginRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<ApiResponse<LoginResponse>>
}