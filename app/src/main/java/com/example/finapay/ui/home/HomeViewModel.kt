package com.example.finapay.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finapay.data.models.CustomerDetailModel
import com.example.finapay.data.models.LoanModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.repositories.CustomerDetailsRepository
import com.example.finapay.data.repositories.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val customerRepository: CustomerDetailsRepository,
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _customerDetailsSuccess = MutableLiveData<CustomerDetailModel>()
    val customerDetailsSuccess: LiveData<CustomerDetailModel> = _customerDetailsSuccess

    private val _customerDetailsError = MutableLiveData<ApiResponse<String>>()
    val customerDetailsError: LiveData<ApiResponse<String>> = _customerDetailsError

    private val _internetErrorError = MutableLiveData<ApiResponse<String>>()
    val internetErrorError: LiveData<ApiResponse<String>> = _internetErrorError

    private val _loanHistorySuccess = MutableLiveData<List<LoanModel>>()
    val loanHistorySuccess: LiveData<List<LoanModel>> = _loanHistorySuccess

    private val _loanHistoryError = MutableLiveData<ApiResponse<String>>()
    val loanHistoryError: LiveData<ApiResponse<String>> = _loanHistoryError

    private val _loanOngoingSuccess = MutableLiveData<List<LoanModel>>()
    val loanOngoingSuccess: LiveData<List<LoanModel>> = _loanOngoingSuccess

    private val _loanOngoingError = MutableLiveData<ApiResponse<String>>()
    val loanOngoingError: LiveData<ApiResponse<String>> = _loanOngoingError

    fun getLoanHistory(status: String) {
        viewModelScope.launch {
            try {
                val response = loanRepository.getAllLoanRequestByEmailAndStatus(status)
                if (response.status == "success") {
                    _loanHistorySuccess.postValue(response.data)
                } else {
                    _loanHistoryError.postValue(
                        ApiResponse("failed", response.message ?: "Gagal mengambil data", "", response.status_code)
                    )
                }
            } catch (e: Exception) {
                _loanHistoryError.postValue(
                    ApiResponse("failed", "Terjadi kesalahan: ${e.message}", "", 500)
                )
            }
        }
    }

    fun getLoanOngoing() {
        viewModelScope.launch {
            try {
                val response = loanRepository.getAllLoanRequestByEmailAndStatus("ongoing")
                if (response.status == "success") {
                    _loanOngoingSuccess.postValue(response.data)
                } else {
                    _loanOngoingError.postValue(
                        ApiResponse("failed", response.message ?: "Gagal mengambil data", "", response.status_code)
                    )
                }
            } catch (e: Exception) {
                _loanOngoingError.postValue(
                    ApiResponse("failed", "Terjadi kesalahan: ${e.message}", "", 500)
                )
            }
        }
    }

    fun getCustomerDetails() {
        customerRepository.getCustomerDetailByEmail().enqueue(object :
            Callback<ApiResponse<CustomerDetailModel>> {
            override fun onResponse(
                call: Call<ApiResponse<CustomerDetailModel>>,
                response: Response<ApiResponse<CustomerDetailModel>>
            ) {
                if (response.isSuccessful) {
                    val customerDetailsResponse = response.body()?.data
                    customerDetailsResponse?.let {
                        _customerDetailsSuccess.postValue(it)
                    } ?: run {
                        _customerDetailsError.postValue(
                            ApiResponse("failed", response.message(), "", response.code())
                        )
                    }
                } else {
                    _customerDetailsError.postValue(
                        ApiResponse("failed", response.message(), "", response.code())
                    )
                }
            }

            override fun onFailure(
                call: Call<ApiResponse<CustomerDetailModel>>,
                t: Throwable
            ) {
                val errorResponse = ApiResponse("failed", "Gagal terhubung ke server", "", 408)
                _customerDetailsError.postValue(errorResponse)
                _internetErrorError.postValue(errorResponse)
            }
        })
    }
}
