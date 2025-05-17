package com.example.finapay.data.sources

import com.example.finapay.data.sources.remote.AuthService
import com.example.finapay.data.sources.remote.CustomerDetailsService
import com.example.finapay.data.sources.remote.LoanService
import com.example.finapay.data.sources.remote.PlafondService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
//    private const val BASE_URL = "http://localhost:8080/api/v1/"
//    private const val BASE_URL = "http://34.60.203.63/api/v1/"
    private const val BASE_URL = "https://e576-180-252-116-15.ngrok-free.app/api/v1/"

    private var tokenProvider: (() -> String?)? = null

    fun init(tokenProvider: () -> String?) {
        this.tokenProvider = tokenProvider
    }

    private val client: OkHttpClient
        get() = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
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
