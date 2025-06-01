package com.example.finapay.data.sources

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestUrl = request.url.encodedPath

        val excludedEndpoints = listOf(
            "/api/v1/auth/login",
            "/api/v1/auth/login-google",
            "/api/v1/auth/register",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/change-password",
        )

        val shouldExclude = excludedEndpoints.any { requestUrl.endsWith(it) }

        val requestBuilder = request.newBuilder()

        if (!shouldExclude) {
            tokenProvider()?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}
