package com.example.finapay.ui.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.repositories.AuthRepository
import com.example.finapay.utils.SharedPreferencesHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferencesHelper = SharedPreferencesHelper(application)
    private val repository = AuthRepository()

    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> = _logoutSuccess

    private val _logoutError = MutableLiveData<String>()
    val logoutError: LiveData<String> = _logoutError

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val user = sharedPreferencesHelper.getUserData()
        _email.value = user?.email ?: ""
        _name.value = user?.name ?: ""
    }

    fun logout(fcmToken: String, onResult: (Boolean) -> Unit) {
        repository.logout(fcmToken).enqueue(object : Callback<ApiResponse<Unit>> {
            override fun onResponse(call: Call<ApiResponse<Unit>>, response: Response<ApiResponse<Unit>>) {
                if (response.isSuccessful) {
                    Log.d("Logout", "Berhasil logout")
                    onResult(true)
                } else {
                    Log.e("Logout", "Gagal logout: ${response.message()}")
                    onResult(false)
                }
            }

            override fun onFailure(call: Call<ApiResponse<Unit>>, t: Throwable) {
                Log.e("Logout", "Error jaringan: ${t.localizedMessage}")
                onResult(false)
            }
        })
    }

}
