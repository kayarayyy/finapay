package com.example.finapay.ui.change_passowrd

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finapay.data.models.AuthModel
import com.example.finapay.data.models.CustomerDetailModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.repositories.AuthRepository
import com.example.finapay.ui.my_account.MyAccountViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordViewModel: ViewModel() {
    private val repository = AuthRepository()

    private val _changePasswordSuccess = MutableLiveData<Boolean>()
    val changePasswordSuccess: LiveData<Boolean> = _changePasswordSuccess

    private val _changePasswordError = MutableLiveData<String>()
    val changePasswordError: LiveData<String> = _changePasswordError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        _isLoading.postValue(true)
        repository.changePassword(oldPassword, newPassword, confirmPassword).enqueue(object: Callback<ApiResponse<Boolean?>> {
            override fun onResponse(call: Call<ApiResponse<Boolean?>>, response: Response<ApiResponse<Boolean?>>) {
                _isLoading.postValue(false)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _changePasswordSuccess.postValue(it.data)
                    } ?: _changePasswordError.postValue("Terjadi kesalahan: Response body kosong")
                } else {
                    handleErrorResponse(response)
                }
            }

            override fun onFailure(
                call: Call<ApiResponse<Boolean?>>,
                t: Throwable
            ) {
                _isLoading.postValue(false)
                Log.e(TAG, "API call failure", t)
                _changePasswordError.postValue(getErrorMessage(t))
            }
        })
    }

    private fun handleErrorResponse(response: Response<ApiResponse<Boolean?>>) {
        val gson = Gson()
        val type = object : TypeToken<ApiResponse<Boolean?>>() {}.type
        val errorResponse: ApiResponse<Boolean?>? = gson.fromJson(response.errorBody()?.charStream(), type)
        val errorMessage = when (response.code()) {
            400 -> errorResponse?.message ?: "Data yang dikirim tidak valid"
            401 -> "Sesi telah berakhir, silakan login kembali"
            403 -> "Anda tidak memiliki akses untuk fitur ini"
            404 -> "Layanan tidak ditemukan"
            500, 502, 503 -> "Terjadi gangguan pada server, coba lagi nanti"
            else -> "Terjadi kesalahan: ${response.code()}"
        }

        Log.e(TAG, "API error: ${response.code()} - ${errorResponse?.message}")
        _changePasswordError.postValue(errorMessage)
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
        private const val TAG = "ChangePasswordViewModel"
    }
}