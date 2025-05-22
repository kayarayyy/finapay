package com.example.finapay.ui.landing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finapay.data.models.PlafondModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.repositories.PlafondRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback

class LandingViewModel : ViewModel() {
    private val repository = PlafondRepository()

    private val _plafondsSuccess = MutableLiveData<List<PlafondModel>>()
    val plafonds: LiveData<List<PlafondModel>> = _plafondsSuccess

    private val _plafondsError = MutableLiveData<String>()
    val plafondsError: LiveData<String> = _plafondsError

    fun getPlafonds() {
        repository.getPlafonds().enqueue(object : Callback<ApiResponse<List<PlafondModel>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<PlafondModel>>>,
                response: retrofit2.Response<ApiResponse<List<PlafondModel>>>
            ) {
                if (response.isSuccessful) {
                    val plafondsResponse = response.body()?.data
                    if (plafondsResponse != null) {
                        _plafondsSuccess.postValue(plafondsResponse)
                    } else {
                        _plafondsError.postValue("Data tidak ditemukan.")
                    }
                } else {
                    try {
                        val gson = Gson()
                        val type = object : TypeToken<ApiResponse<List<PlafondModel>>>() {}.type
                        val errorResponse = gson.fromJson<ApiResponse<List<PlafondModel>>>(
                            response.errorBody()?.charStream(), type
                        )
                        _plafondsError.postValue(errorResponse?.message ?: "Terjadi kesalahan pada server.")
                    } catch (e: Exception) {
                        _plafondsError.postValue("Gagal membaca error dari server." + e )
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<PlafondModel>>>, t: Throwable) {
                _plafondsError.postValue(t.localizedMessage ?: "Gagal terhubung ke server.")
            }
        })
    }

}