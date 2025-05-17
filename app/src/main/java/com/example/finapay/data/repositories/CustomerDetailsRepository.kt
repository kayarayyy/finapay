package com.example.finapay.data.repositories

import com.example.finapay.data.models.CustomerDetailModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.sources.ApiClient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call

class CustomerDetailsRepository {
    fun getCustomerDetailByEmail(): Call<ApiResponse<CustomerDetailModel>> {
        return ApiClient.customerDetailsService.getCustomerDetailByEmail()
    }

    fun createCustomerDetails(
        street: RequestBody,
        district: RequestBody,
        province: RequestBody,
        postalCode: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        gender: RequestBody,
        ttl: RequestBody,
        noTelp: RequestBody,
        nik: RequestBody,
        mothersName: RequestBody,
        job: RequestBody,
        salary: RequestBody,
        noRek: RequestBody,
        houseStatus: RequestBody,
        selfieKtp: MultipartBody.Part,
        house: MultipartBody.Part,
        ktp: MultipartBody.Part
    ): Call<ApiResponse<CustomerDetailModel>> {
        return ApiClient.customerDetailsService.createCustomerDetail(
            street,
            district,
            province,
            postalCode,
            latitude,
            longitude,
            gender,
            ttl,
            noTelp,
            nik,
            mothersName,
            job,
            salary,
            noRek,
            houseStatus,
            selfieKtp,
            house,
            ktp
        )
    }

}