package com.example.finapay.data.repositories

import com.example.finapay.data.models.LoanModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.sources.ApiClient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call

class LoanRepository {
    fun getLoanRequestOnGoing(): Call<ApiResponse<List<LoanModel>>> {
        return ApiClient.loanService.getLoanRequestOnGoing()
    }

    fun postLoanRequest(
        refferal: RequestBody,
        amount: RequestBody,
        tenor: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody
    ): Call<ApiResponse<LoanModel>> {
        return ApiClient.loanService.uploadKtpImage(refferal, amount, tenor, latitude, longitude)
    }
}