package com.example.finapay.data.repositories

import com.example.finapay.data.models.PlafondModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.sources.ApiClient
import retrofit2.Call

class PlafondRepository {
    fun getPlafonds(): Call<ApiResponse<List<PlafondModel>>> {
        return ApiClient.plafondService.getPlafonds()
    }
}