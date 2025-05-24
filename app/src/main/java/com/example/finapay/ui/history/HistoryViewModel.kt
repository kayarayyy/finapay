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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val loanRepository: LoanRepository
) : ViewModel() {

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
                val response = loanRepository.getAllLoanRequestByEmail()
                if (response.status == "success" && response.data != null) {
                    _loanHistory.postValue(response.data)
                } else {
                    _error.postValue(response.message ?: "Gagal mengambil data")
                }
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    400 -> "Data yang dikirim tidak valid"
                    401 -> "Sesi telah berakhir, silakan login kembali"
                    403 -> "Anda tidak memiliki akses untuk fitur ini"
                    404 -> "Layanan tidak ditemukan"
                    in 500..599 -> "Terjadi gangguan pada server, coba lagi nanti"
                    else -> "Terjadi kesalahan: ${e.code()}"
                }
                _error.postValue(message)
                Log.e(TAG, "HttpException: ${e.code()} - ${e.message()}", e)
            } catch (e: IOException) {
                _error.postValue("Tidak dapat terhubung ke server. Periksa koneksi internet Anda.")
                Log.e(TAG, "IOException: ${e.message}", e)
            } catch (e: Exception) {
                _error.postValue("Terjadi kesalahan: ${e.message}")
                Log.e(TAG, "Exception: ${e.message}", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    companion object {
        private const val TAG = "HistoryViewModel"
    }
}
