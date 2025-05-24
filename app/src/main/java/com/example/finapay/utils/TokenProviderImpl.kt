package com.example.finapay.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenProviderImpl @Inject constructor(
    @ApplicationContext context: Context
) : TokenProvider {

    private val sharedPrefsHelper = SharedPreferencesHelper(context)

    override fun getToken(): String? {
        return sharedPrefsHelper.getUserData()?.token
    }
}
