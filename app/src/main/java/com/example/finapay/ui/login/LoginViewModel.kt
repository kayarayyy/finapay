package com.example.finapay.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finapay.data.models.ApiResponse
import com.example.finapay.data.models.LoginResponse
import com.example.finapay.data.repositories.AuthRepository
import com.example.finapay.utils.SharedPreferencesHelper
import retrofit2.Call
import retrofit2.Callback

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginSuccess = MutableLiveData<LoginResponse?>()
    val loginSuccess: LiveData<LoginResponse?> = _loginSuccess

    private val _loginError = MutableLiveData<String>()
    val loginError: LiveData<String> = _loginError

    fun login(email: String, password: String, context: Context) {
        repository.login(email, password).enqueue(object: Callback<ApiResponse<LoginResponse>>{
            override fun onResponse(call: Call<ApiResponse<LoginResponse>>, response: retrofit2.Response<ApiResponse<LoginResponse>>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()?.data
                    loginResponse?.let {
                        val sharedPreferencesHelper = SharedPreferencesHelper(context)
                        sharedPreferencesHelper.saveUserData(it)
                        _loginSuccess.postValue(it)
                    }
                } else {
                    _loginError.postValue("Login gagal: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<ApiResponse<LoginResponse>>, t: Throwable) {
                _loginError.postValue("Terjadi kesalahan: ${t.localizedMessage}")
            }
        })
    }
}