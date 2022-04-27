package com.kmose.newsapiclient.presentation.di

import com.google.gson.Gson
import com.kmose.newsapiclient.BuildConfig
import com.kmose.newsapiclient.data.api.NewsAPIService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetModule {
    @Singleton
    @Provides
    fun providesRetrofit(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.NEWS_URL)
            .build()

    }

    @Singleton
    @Provides
    fun providesNewsAPIService(
        retrofit: Retrofit
    ): NewsAPIService {
        return retrofit.create(NewsAPIService::class.java)
    }
}