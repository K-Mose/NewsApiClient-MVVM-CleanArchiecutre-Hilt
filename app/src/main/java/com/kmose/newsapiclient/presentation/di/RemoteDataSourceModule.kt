package com.kmose.newsapiclient.presentation.di

import com.kmose.newsapiclient.data.api.NewsAPIService
import com.kmose.newsapiclient.data.repository.dataSource.NewsRemoteDataSource
import com.kmose.newsapiclient.data.repository.dataSourceImpl.NewsRemoteDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RemoteDataSourceModule {
    //NewsRemoteDataSourceImpl이 NewsAPIService를 생성자에서 필요로 함
    @Singleton
    @Provides
    fun providesNewsRemoteDataSource(
        newsAPIService: NewsAPIService
    ): NewsRemoteDataSource {
        return NewsRemoteDataSourceImpl(newsAPIService)
    }
}