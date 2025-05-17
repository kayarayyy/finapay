package com.example.finapay.ui.my_account

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finapay.data.models.CustomerDetailModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.repositories.CustomerDetailsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyAccountViewModel : ViewModel() {
    private val repository = CustomerDetailsRepository()

    private val _customerDetails = MutableLiveData<CustomerDetailModel>()
    val customerDetails: LiveData<CustomerDetailModel> = _customerDetails

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun submitCustomerDetails(
        street: RequestBody,
        district: RequestBody,
        province: RequestBody,
        postalCode: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        gender: RequestBody,
        ttl: RequestBody,
        noTelp: RequestBody,
        nik: RequestBody,
        mothersName: RequestBody,
        job: RequestBody,
        salary: RequestBody,
        noRek: RequestBody,
        houseStatus: RequestBody,
        selfieKtp: MultipartBody.Part,
        house: MultipartBody.Part,
        ktp: MultipartBody.Part
    ) {
        _isLoading.postValue(true)

        // Validate inputs before making API call
        try {
            // Add some validation if needed here in addition to UI validation
        } catch (e: Exception) {
            _isLoading.postValue(false)
            _error.postValue("Data tidak valid: ${e.message}")
            return
        }

        viewModelScope.launch {
            try {
                delay(1000)

                repository.createCustomerDetails(
                    street,
                    district,
                    province,
                    postalCode,
                    latitude,
                    longitude,
                    gender,
                    ttl,
                    noTelp,
                    nik,
                    mothersName,
                    job,
                    salary,
                    noRek,
                    houseStatus,
                    selfieKtp,
                    house,
                    ktp
                ).enqueue(object : Callback<ApiResponse<CustomerDetailModel>> {
                    override fun onResponse(
                        call: Call<ApiResponse<CustomerDetailModel>>,
                        response: Response<ApiResponse<CustomerDetailModel>>
                    ) {
                        _isLoading.postValue(false)
                        if (response.isSuccessful) {
                            response.body()?.let {
                                _customerDetails.postValue(it.data)
                            } ?: run {
                                _error.postValue("Terjadi kesalahan: Response body kosong")
                            }
                        } else {
                            Log.e("API Error", "Terjadi kesalahan: ini ${response.message()} ${response.code()} ${response.errorBody()} ${response.code()}")
                            _error.postValue("Terjadi kesalahan: ini ${response.message()} ${response.code()} ${response.errorBody()} ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CustomerDetailModel>>, t: Throwable) {
                        _isLoading.postValue(false)
                        _error.postValue("Terjadi kesalahan: atau ini ${t.message}")
                    }
                })
            } catch (e: Exception) {
                _isLoading.postValue(false)
                _error.postValue("Terjadi kesalahan: atau malah ini ${e.message}")
            }
        }
    }
}