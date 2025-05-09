package com.example.finapay.data.sources

import com.example.finapay.data.sources.remote.AuthService
import com.example.finapay.data.sources.remote.CustomerDetailsService
import com.example.finapay.data.sources.remote.LoanService
import com.example.finapay.data.sources.remote.PlafondService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
//    private const val BASE_URL = "http://34.72.143.21/api/v1/"
    private const val BASE_URL = "https://726f-180-243-241-251.ngrok-free.app/api/v1/"

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

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    val plafondService: PlafondService by lazy {
        retrofit.create(PlafondService::class.java)
    }

    val customerDetailsService: CustomerDetailsService by lazy {
        retrofit.create(CustomerDetailsService::class.java)
    }

    val loanService: LoanService by lazy {
        retrofit.create(LoanService::class.java)
    }
}
