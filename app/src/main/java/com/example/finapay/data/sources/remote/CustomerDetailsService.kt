package com.example.finapay.data.sources.remote

import com.example.finapay.data.models.CustomerDetailModel
import com.example.finapay.data.models.response.ApiResponse
import retrofit2.Call
import retrofit2.http.GET

interface CustomerDetailsService {
    @GET("customer-details/by-email")
    fun getCustomerDetailByEmail(): Call<ApiResponse<CustomerDetailModel>>
}