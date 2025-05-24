package com.example.finapay.di

import com.example.finapay.utils.TokenProvider
import com.example.finapay.utils.TokenProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TokenProviderModule {

    @Binds
    @Singleton
    abstract fun bindTokenProvider(
        impl: TokenProviderImpl
    ): TokenProvider
}
