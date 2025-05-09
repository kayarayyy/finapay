package com.example.finapay.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finapay.data.models.CustomerDetailModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.repositories.CustomerDetailsRepository
import retrofit2.Call
import retrofit2.Callback

class HomeViewModel : ViewModel() {
    private val repository = CustomerDetailsRepository()
    private val _customerDetailsSuccess = MutableLiveData<CustomerDetailModel>()
    val customerDetailsSuccess: LiveData<CustomerDetailModel> = _customerDetailsSuccess
    private val _customerDetailsError = MutableLiveData<String>()
    val customerDetailsError: LiveData<String> = _customerDetailsError

    fun getCustomerDetails() {
        repository.getCustomerDetailByEmail().enqueue(object :
            Callback<ApiResponse<CustomerDetailModel>> {
                override fun onResponse(
                    call: Call<ApiResponse<CustomerDetailModel>>,
                    response: retrofit2.Response<ApiResponse<CustomerDetailModel>>
                )   {

                    if (response.isSuccessful) {
                        val customerDetailsResponse = response.body()?.data
                        if (customerDetailsResponse != null) {
                            _customerDetailsSuccess.postValue(customerDetailsResponse)
                        } else {
                            _customerDetailsError.postValue("Data tidak ditemukan.")
                        }
                    } else {
                        _customerDetailsError.postValue("Terjadi kesalahan pada server.")
                    }
                }
                override fun onFailure(call: Call<ApiResponse<CustomerDetailModel>>, t: Throwable) {
                        _customerDetailsError.postValue(t.localizedMessage ?: "Gagal terhubung ke server.")
                }
            }
        )

    }
}
