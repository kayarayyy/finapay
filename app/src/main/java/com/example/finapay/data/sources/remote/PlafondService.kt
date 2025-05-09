package com.example.finapay.data.sources.remote

import com.example.finapay.data.models.PlafondModel
import com.example.finapay.data.models.response.ApiResponse
import retrofit2.Call
import retrofit2.http.GET

interface PlafondService {
    @GET("plafonds")
    fun getPlafonds(): Call<ApiResponse<List<PlafondModel>>>
}