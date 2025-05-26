package com.example.finapay.data.repositories

import com.example.finapay.data.models.LoanModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.sources.remote.LoanService
import okhttp3.RequestBody
import retrofit2.Call
import javax.inject.Inject

class LoanRepository @Inject constructor(
    private val loanService: LoanService
) {

    suspend fun getAllLoanRequestByEmail(): ApiResponse<List<LoanModel>> {
        return loanService.getAllLoanRequestByEmail()
    }

    suspend fun getAllLoanRequestByEmailAndStatus(status: String? = null): ApiResponse<List<LoanModel>> {
        return loanService.getAllLoanRequestByEmail(status)
    }

    fun postLoanRequest(
        refferal: RequestBody,
        amount: RequestBody,
        tenor: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody
    ): Call<ApiResponse<LoanModel>> {
        return loanService.postLoanRequest(refferal, amount, tenor, latitude, longitude)
    }
}