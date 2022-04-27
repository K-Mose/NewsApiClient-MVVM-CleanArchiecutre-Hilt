package com.kmose.newsapiclient.presentation.di

import com.kmose.newsapiclient.data.repository.NewsRepositoryImpl
import com.kmose.newsapiclient.data.repository.dataSource.NewsRemoteDataSource
import com.kmose.newsapiclient.domain.repository.NewsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Singleton
    @Provides
    fun providesNewsRepository(
        newsRemoteDataSource: NewsRemoteDataSource
    ): NewsRepository {
        return NewsRepositoryImpl(newsRemoteDataSource)
    }
}