package com.example.finapay.ui.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finapay.data.models.AuthModel
import com.example.finapay.data.models.CustomerDetailModel
import com.example.finapay.data.models.LoanModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.repositories.LoanRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryViewModel : ViewModel() {

    private val repository = LoanRepository()

    private val _loanHistory = MutableLiveData<List<LoanModel>>()
    val loanHistory: LiveData<List<LoanModel>> = _loanHistory

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getLoanHistory() {
        _isLoading.postValue(true)

        viewModelScope.launch {
            try {
                // Contoh implementasi: Ganti dengan panggilan repository/API yang sebenarnya
                val result = fetchLoanHistoryFromRepo()
                _loanHistory.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "Terjadi kesalahan saat mengambil data"
            } finally {
                _isLoading.value = false
            }
        }

//        repository.getALlLoanRequestByEmail().enqueue(object : Callback<ApiResponse<List<LoanModel>>> {
//            override fun onResponse(
//                call: Call<ApiResponse<List<LoanModel>>>,
//                response: Response<ApiResponse<List<LoanModel>>>
//            ) {
//                _isLoading.postValue(false)
//
//                if (response.isSuccessful) {
//                    val data = response.body()?.data
//                    _loanHistory.postValue(data ?: emptyList())
//                } else {
//                    handleErrorResponse(response)
//                }
//            }
//
//            override fun onFailure(call: Call<ApiResponse<List<LoanModel>>>, t: Throwable) {
//                _isLoading.postValue(false)
//                val errorMessage = getErrorMessage(t)
//                _error.postValue(errorMessage)
//                Log.e(TAG, "onFailure: $errorMessage", t)
//            }
//        })
    }

    private suspend fun fetchLoanHistoryFromRepo(): List<LoanModel> {
        return listOf(
            LoanModel(
                id = "1",
                title = "Peminjaman #1",
                date = "20 Mei 2025",
                amount = "Rp 500.000",
                status = "Disetujui",
                tenor = "12",
                isApproved = true
            ),
            LoanModel(
                id = "2",
                title = "Peminjaman #2",
                date = "15 Mei 2025",
                amount = "Rp 1.000.000.000",
                status = "Ditolak",
                tenor = "6",
                isApproved = false
            ),
            LoanModel(
                id = "3",
                title = "Peminjaman #3",
                date = "10 Mei 2025",
                amount = "Rp 750.000",
                status = "Disetujui",
                tenor = "12",
                isApproved = true
            )
        )
    }

    private fun handleErrorResponse(response: Response<*>) {
        val gson = Gson()
        val type = object : TypeToken<ApiResponse<Any>>() {}.type

        val errorBody = response.errorBody()?.charStream()
        val errorResponse: ApiResponse<Any>? = try {
            gson.fromJson(errorBody, type)
        } catch (e: Exception) {
            null
        }

        val errorMessage = when (response.code()) {
            400 -> errorResponse?.message ?: "Data yang dikirim tidak valid"
            401 -> "Sesi telah berakhir, silakan login kembali"
            403 -> "Anda tidak memiliki akses untuk fitur ini"
            404 -> "Layanan tidak ditemukan"
            in 500..599 -> "Terjadi gangguan pada server, coba lagi nanti"
            else -> "Terjadi kesalahan: ${response.code()}"
        }

        Log.e(TAG, "API error: ${response.code()} - ${errorResponse?.message}")
        _error.postValue(errorMessage)
    }

    private fun getErrorMessage(throwable: Throwable): String {
        return when {
            throwable.message?.contains("timeout", ignoreCase = true) == true ->
                "Koneksi timeout, periksa jaringan Anda"
            throwable.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                "Tidak dapat terhubung ke server, periksa koneksi internet Anda"
            throwable.message?.contains("Failed to connect", ignoreCase = true) == true ->
                "Tidak dapat terhubung ke server, periksa koneksi internet Anda"
            else -> "Terjadi kesalahan: ${throwable.message}"
        }
    }

    companion object {
        private const val TAG = "HistoryViewModel"
    }
}