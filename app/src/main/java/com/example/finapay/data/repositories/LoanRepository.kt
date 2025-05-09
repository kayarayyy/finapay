package com.example.finapay.data.repositories

import com.example.finapay.data.models.LoanModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.sources.ApiClient
import retrofit2.Call

class LoanRepository {
    fun getLoanRequestOnGoing(): Call<ApiResponse<List<LoanModel>>> {
        return ApiClient.loanService.getLoanRequestOnGoing()
    }
}