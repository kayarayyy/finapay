package com.example.finapay.ui.request

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finapay.data.models.LoanModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.repositories.LoanRepository
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

    fun uploadKtpWithLocation(
        amount: RequestBody,
        tenor: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        imagePart: MultipartBody.Part
    ) {
        _isLoading.postValue(true)

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
                            _uploadError.postValue("Response kosong")
                        }
                    } else {
                        _uploadError.postValue("Upload gagal: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<LoanModel>>, t: Throwable) {
                    _isLoading.postValue(false)
                    Log.e("Gagal upload KTP", t.toString())
                    _uploadError.postValue("Terjadi kesalahan: ${t.message}")
                }
            })
    }
}
