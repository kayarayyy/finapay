package com.example.finapay.data.sources.remote

import com.example.finapay.data.models.LoanModel
import com.example.finapay.data.models.response.ApiResponse
import retrofit2.Call
import retrofit2.http.GET

interface LoanService {
    @GET("loan-requests")
    fun getLoanRequestOnGoing(): Call<ApiResponse<List<LoanModel>>>
}