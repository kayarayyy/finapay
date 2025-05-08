package com.example.finapay.data.sources

import com.example.finapay.data.sources.remote.AuthApiService
import com.example.finapay.data.sources.remote.CustomerDetailsService
import com.example.finapay.data.sources.remote.PlafondApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://34.72.143.21/api/v1/"

    private var tokenProvider: (() -> String?)? = null

    fun init(tokenProvider: () -> String?) {
        this.tokenProvider = tokenProvider
    }

    private val client: OkHttpClient
        get() = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenProvider?.invoke() })
            .build()

    private val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val authService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val plafondService: PlafondApiService by lazy {
        retrofit.create(PlafondApiService::class.java)
    }

    val customerDetailsService: CustomerDetailsService by lazy {
        retrofit.create(CustomerDetailsService::class.java)
    }
}
