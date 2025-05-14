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
        amount: RequestBody,
        tenor: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        ktpImage: MultipartBody.Part,
    ): Call<ApiResponse<LoanModel>> {
        return ApiClient.loanService.uploadKtpImage(amount, tenor, latitude, longitude, ktpImage)
    }
}