package com.example.finapay.data.repositories

import com.example.finapay.data.models.CustomerDetailModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.sources.ApiClient
import retrofit2.Call

class CustomerDetailsRepository {
    fun getCustomerDetailByEmail(): Call<ApiResponse<CustomerDetailModel>> {
        return ApiClient.customerDetailsService.getCustomerDetailByEmail()
    }
}