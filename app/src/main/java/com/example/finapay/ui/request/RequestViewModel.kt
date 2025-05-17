package com.example.finapay.ui.request

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finapay.data.models.LoanModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.repositories.LoanRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RequestViewModel : ViewModel() {

    private val repository = LoanRepository()

    private val _uploadResult = MutableLiveData<ApiResponse<LoanModel>>()
    val uploadResult: LiveData<ApiResponse<LoanModel>> = _uploadResult

    private val _uploadError = MutableLiveData<String>()
    val uploadError: LiveData<String> = _uploadError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Configuration values
    private val minLoanAmount = 1_000_000
    private val maxLoanAmount = 50_000_000
    private val minTenor = 1
    private val maxTenor = 36

    fun uploadKtpWithLocation(
        amount: RequestBody,
        tenor: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        imagePart: MultipartBody.Part
    ) {
        _isLoading.postValue(true)

        // Validate inputs before making API call
        try {
            // Add some validation if needed here in addition to UI validation
        } catch (e: Exception) {
            _isLoading.postValue(false)
            _uploadError.postValue("Data tidak valid: ${e.message}")
            return
        }

        // Use viewModelScope to handle cancelation automatically if the ViewModel is cleared
        viewModelScope.launch {
            try {
                // Add artificial delay to show loading state (remove in production)
                delay(1000)

                repository.postLoanRequest(amount, tenor, latitude, longitude, imagePart)
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
                _uploadError.postValue("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    /**
     * Handle different HTTP error responses with appropriate user-friendly messages
     */
    private fun handleErrorResponse(response: Response<ApiResponse<LoanModel>>) {
        val errorBody = response.errorBody()?.string()
        val errorMessage = when (response.code()) {
            400 -> "Data yang dikirim tidak valid"
            401 -> "Sesi telah berakhir, silakan login kembali"
            403 -> "Anda tidak memiliki akses untuk fitur ini"
            404 -> "Layanan tidak ditemukan"
            500, 502, 503 -> "Terjadi gangguan pada server, coba lagi nanti"
            else -> "Terjadi kesalahan: ${response.code()}"
        }

        Log.e(TAG, "API error: ${response.code()} - $errorBody")
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
            else -> "Terjadi kesalahan: ${throwable.message}"
        }
    }

    /**
     * Validate loan request parameters
     */
    fun validateLoanRequest(amount: Int, tenor: Int): Pair<Boolean, String?> {
        return when {
            amount < minLoanAmount ->
                Pair(false, "Minimum pinjaman Rp $minLoanAmount")
            amount > maxLoanAmount ->
                Pair(false, "Maksimum pinjaman Rp $maxLoanAmount")
            tenor < minTenor ->
                Pair(false, "Minimum tenor $minTenor bulan")
            tenor > maxTenor ->
                Pair(false, "Maksimum tenor $maxTenor bulan")
            else -> Pair(true, null)
        }
    }

    companion object {
        private const val TAG = "RequestViewModel"
    }
}