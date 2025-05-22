package com.example.finapay.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finapay.data.models.CustomerDetailModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.repositories.CustomerDetailsRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {
    private val repository = CustomerDetailsRepository()
    private val _customerDetailsSuccess = MutableLiveData<CustomerDetailModel>()
    val customerDetailsSuccess: LiveData<CustomerDetailModel> = _customerDetailsSuccess
    private val _customerDetailsError = MutableLiveData<ApiResponse<String>>()
    val customerDetailsError: LiveData<ApiResponse<String>> = _customerDetailsError

    fun getCustomerDetails() {
        repository.getCustomerDetailByEmail().enqueue(object :
            Callback<ApiResponse<CustomerDetailModel>> {
                override fun onResponse(
                    call: Call<ApiResponse<CustomerDetailModel>>,
                    response: Response<ApiResponse<CustomerDetailModel>>
                )   {
                    Log.d("API Response", response.toString())
                    if (response.isSuccessful) {
                        val customerDetailsResponse = response.body()?.data
                        if (customerDetailsResponse != null) {
                            _customerDetailsSuccess.postValue(customerDetailsResponse!!)
                        } else {
                            ApiResponse("failed", response.message().toString(), "", response.code())?.let {
                                _customerDetailsError.postValue(
                                    it
                                )
                            }
                        }
                    } else {
                        ApiResponse("failed", response.message().toString(), "", response.code())?.let {
                            _customerDetailsError.postValue(
                                it
                            )
                        }
                    }
                }
                override fun onFailure(call: Call<ApiResponse<CustomerDetailModel>>, t: Throwable) {
                    Log.e("API Response", t.toString())
                    ApiResponse("failed", "Gagal terhubung ke server", "", 408)?.let {
                        _customerDetailsError.postValue(
                            it
                        )
                    }
                }
            }
        )

    }
}
