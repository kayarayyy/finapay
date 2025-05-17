package com.example.finapay.data.sources.remote

import com.example.finapay.data.models.CustomerDetailModel
import com.example.finapay.data.models.response.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CustomerDetailsService {
    @GET("customer-details/by-email")
    fun getCustomerDetailByEmail(): Call<ApiResponse<CustomerDetailModel>>

    @Multipart
    @POST("customer-details")
    fun createCustomerDetail(
        @Part("street") street: RequestBody,
        @Part("district") district: RequestBody,
        @Part("province") province: RequestBody,
        @Part("postal_code") postalCode: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("ttl") ttl: RequestBody,
        @Part("no_telp") noTelp: RequestBody,
        @Part("nik") nik: RequestBody,
        @Part("mothers_name") mothersName: RequestBody,
        @Part("job") job: RequestBody,
        @Part("salary") salary: RequestBody,
        @Part("no_rek") noRek: RequestBody,
        @Part("house_status") houseStatus: RequestBody,
        @Part selfieKtp: MultipartBody.Part,
        @Part house: MultipartBody.Part,
        @Part ktp: MultipartBody.Part
    ): Call<ApiResponse<CustomerDetailModel>>

}