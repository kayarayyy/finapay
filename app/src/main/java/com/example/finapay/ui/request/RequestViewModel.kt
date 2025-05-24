package com.example.finapay.ui.request

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finapay.data.models.AuthModel
import com.example.finapay.data.models.LoanModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.repositories.LoanRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val loanRepository: LoanRepository
) : ViewModel() {
    private val _uploadResult = MutableLiveData<ApiResponse<LoanModel>>()
    val uploadResult: LiveData<ApiResponse<LoanModel>> = _uploadResult

    private val _uploadError = MutableLiveData<String>()
    val uploadError: LiveData<String> = _uploadError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun uploadPengajuan(
        refferal: String?,
        amount: String,
        tenor: String,
        latitude: String,
        longitude: String
    ) {
        _isLoading.postValue(true)

        fun String.toRequestBody() = toRequestBody("text/plain".toMediaTypeOrNull())

        val amountCleaned = amount.replace(".", "")
            .replace(",", ".")
            .replace("Rp", "")
            .replace(" ", "")
        val refferalStr = refferal ?: ""

        viewModelScope.launch {
            try {
                // Add artificial delay to show loading state (remove in production)
                delay(1000)

                loanRepository.postLoanRequest(refferalStr.toRequestBody(), amountCleaned.toRequestBody(), tenor.toRequestBody(), latitude.toRequestBody(), longitude.toRequestBody())
                    .enqueue(object : Callback<ApiResponse<LoanModel>> {
                        override fun onResponse(
                            call: Call<ApiResponse<LoanModel>>,
                            response: Response<ApiResponse<LoanModel>>
                        ) {
                            _isLoading.postValue(false)
                            if (response.isSuccessful) {
                                response.body()?.let {
                                    _uploadResult.postValue(it)
                                } ?: run {
                                    _uploadError.postValue("Terjadi kesalahan: Response body kosong")
                                }
                            } else {
                                handleErrorResponse(response)
                            }
                        }

                        override fun onFailure(call: Call<ApiResponse<LoanModel>>, t: Throwable) {
                            _isLoading.postValue(false)
                            Log.e(TAG, "API call failure", t)
                            _uploadError.postValue(getErrorMessage(t))
                        }
                    })
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Log.e(TAG, "Exception during API call", e)
                _uploadError.postValue("Terjadi kesalahan: Gagal terhubung ke server")
            }
        }
    }

    /**
     * Handle different HTTP error responses with appropriate user-friendly messages
     */
    private fun handleErrorResponse(response: Response<ApiResponse<LoanModel>>) {
        val gson = Gson()
        val type = object : TypeToken<ApiResponse<AuthModel>>() {}.type
        val errorResponse: ApiResponse<AuthModel>? = gson.fromJson(response.errorBody()?.charStream(), type)
        val errorMessage = when (response.code()) {
            400 -> errorResponse?.message ?: "Data yang dikirim tidak valid"
            401 -> "Sesi telah berakhir, silakan login kembali"
            403 -> "Anda tidak memiliki akses untuk fitur ini"
            404 -> "Layanan tidak ditemukan"
            500, 502, 503 -> "Terjadi gangguan pada server, coba lagi nanti"
            else -> "Terjadi kesalahan: ${response.code()}"
        }

        Log.e(TAG, "API error: ${response.code()} - ${errorResponse?.message}")
        _uploadError.postValue(errorMessage)
    }

    /**
     * Convert exception to user-friendly error message
     */
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
        private const val TAG = "RequestViewModel"
    }
}