package com.example.finapay.data.sources

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptorDI(private val tokenProvider: () -> String?)  : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenProvider()

        val requestBuilder = originalRequest.newBuilder()
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }

        return chain.proceed(requestBuilder.build())
    }
}