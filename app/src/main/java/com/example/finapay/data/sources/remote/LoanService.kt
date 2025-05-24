package com.example.finapay.data.sources.remote

import com.example.finapay.data.models.LoanModel
import com.example.finapay.data.models.response.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface LoanService {
    @GET("loan-requests")
    fun getLoanRequestOnGoing(): Call<ApiResponse<List<LoanModel>>>

    @GET("loan-requests/by-email")
    suspend fun getAllLoanRequestByEmail(): ApiResponse<List<LoanModel>>

    @GET("loan-requests/by-email")
    suspend fun getAllLoanRequestByEmailAndStatus(
        @Query("status") status: String? = null
    ): ApiResponse<List<LoanModel>>

    @Multipart
    @POST("loan-requests") // Ganti sesuai endpoint-mu
    fun uploadKtpImage(
        @Part("refferal") refferal: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part("tenor") tenor: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody
    ): Call<ApiResponse<LoanModel>>
}