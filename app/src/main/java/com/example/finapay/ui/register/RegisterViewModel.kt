package com.example.finapay.ui.register

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finapay.data.models.AuthModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.repositories.AuthRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback

class RegisterViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _registerSuccess = MutableLiveData<AuthModel?>()
    val registerSuccess: LiveData<AuthModel?> = _registerSuccess

    private val _registerError = MutableLiveData<String>()
    val registerError: LiveData<String> = _registerError

    fun register(email: String, password: String, name: String, context: Context){
        repository.register(email, password, name).enqueue(object: Callback<ApiResponse<AuthModel>>{
            override fun onResponse(call: Call<ApiResponse<AuthModel>>, response: retrofit2.Response<ApiResponse<AuthModel>>) {
                if (response.isSuccessful) {
                    val registerResponse = response.body()?.data
                    registerResponse?.let {
                        _registerSuccess.postValue(it)
                    }
                } else {
                    val gson = Gson()
                    val type = object : TypeToken<ApiResponse<AuthModel>>() {}.type
                    val errorResponse: ApiResponse<AuthModel>? = gson.fromJson(response.errorBody()?.charStream(), type)
                    _registerError.postValue(errorResponse?.message ?: "Terjadi kesalahan")
                }
            }

            override fun onFailure(call: Call<ApiResponse<AuthModel>>, t: Throwable) {
                _registerError.postValue("${t.localizedMessage}")
            }
        })

    }

}