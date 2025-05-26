package com.example.finapay.di

import com.example.finapay.data.sources.AuthInterceptorDI
import com.example.finapay.data.sources.remote.CustomerDetailsService
import com.example.finapay.data.sources.remote.LoanService
import com.example.finapay.utils.TokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    //    private const val BASE_URL = "http://localhost:8080/api/v1/"
    private const val BASE_URL = "http://34.60.203.63/api/v1/"
//    private const val BASE_URL = "https://d9fe-180-252-121-38.ngrok-free.app/api/v1/"

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenProvider: TokenProvider): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor(AuthInterceptorDI { tokenProvider.getToken() })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideLoanService(retrofit: Retrofit): LoanService {
        return retrofit.create(LoanService::class.java)
    }

    @Provides
    @Singleton
    fun provideCustomerDetailService(retrofit: Retrofit): CustomerDetailsService {
        return retrofit.create(CustomerDetailsService::class.java)
    }
}
