package com.example.finapay.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.models.AuthModel
import com.example.finapay.data.repositories.AuthRepository
import com.example.finapay.utils.SharedPreferencesHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginSuccess = MutableLiveData<AuthModel?>()
    val loginSuccess: LiveData<AuthModel?> = _loginSuccess

    private val _loginError = MutableLiveData<String>()
    val loginError: LiveData<String> = _loginError

    private val _forgotPasswordSuccess = MutableLiveData<String>()
    val forgotPasswordSuccess: LiveData<String> = _forgotPasswordSuccess

    private val _forgotPasswordError = MutableLiveData<String>()
    val forgotPasswordError: LiveData<String> = _forgotPasswordError

    fun signInGoogle(tokenId: String, fcmToken: String, context: Context) {
        repository.signInGoogle(tokenId, fcmToken)
            .enqueue(object : Callback<ApiResponse<AuthModel>> {
                override fun onResponse(
                    call: Call<ApiResponse<AuthModel>>,
                    response: Response<ApiResponse<AuthModel>>
                ) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()?.data
                        loginResponse?.let {
                            val sharedPreferencesHelper = SharedPreferencesHelper(context)
                            sharedPreferencesHelper.saveUserData(it, fcmToken)
                            _loginSuccess.postValue(it)
                        }
                    } else {
                        val gson = Gson()
                        val type = object : TypeToken<ApiResponse<AuthModel>>() {}.type
                        val errorResponse: ApiResponse<AuthModel>? =
                            gson.fromJson(response.errorBody()?.charStream(), type)
                        _loginError.postValue(errorResponse?.message ?: "Terjadi kesalahan")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<AuthModel>>, t: Throwable) {
                    _loginError.postValue("${t.localizedMessage}")
                }
            })
    }

    fun login(email: String, password: String, fcmToken: String, context: Context) {
        repository.login(email, password, fcmToken)
            .enqueue(object : Callback<ApiResponse<AuthModel>> {
                override fun onResponse(
                    call: Call<ApiResponse<AuthModel>>,
                    response: Response<ApiResponse<AuthModel>>
                ) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()?.data
                        loginResponse?.let {
                            val sharedPreferencesHelper = SharedPreferencesHelper(context)
                            sharedPreferencesHelper.saveUserData(it, fcmToken)
                            _loginSuccess.postValue(it)
                        }
                    } else {
                        val gson = Gson()
                        val type = object : TypeToken<ApiResponse<AuthModel>>() {}.type
                        val errorResponse: ApiResponse<AuthModel>? =
                            gson.fromJson(response.errorBody()?.charStream(), type)
                        _loginError.postValue(errorResponse?.message ?: "Terjadi kesalahan")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<AuthModel>>, t: Throwable) {
                    _loginError.postValue("${t.localizedMessage}")
                }
            })
    }

    fun forgotPassword(email: String) {
        repository.forgotPassword(email).enqueue(object : Callback<ApiResponse<String?>> {
            override fun onResponse(
                call: Call<ApiResponse<String?>>,
                response: Response<ApiResponse<String?>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        _forgotPasswordSuccess.postValue(body.message)
                    } else {
                        _forgotPasswordError.postValue("Respon kosong dari server")
                    }
                } else {
                    _forgotPasswordError.postValue("Gagal: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<String?>>, t: Throwable) {
                _forgotPasswordError.postValue("Kesalahan jaringan: Tidak dapat terhubung ke server")
            }
        })
    }

    fun clearForgotPasswordState() {
        _forgotPasswordSuccess.value = null
        _forgotPasswordError.value = null
    }


}