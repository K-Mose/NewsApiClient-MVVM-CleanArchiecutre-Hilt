package com.kmose.newsapiclient.presentation.di

import com.kmose.newsapiclient.domain.repository.NewsRepository
import com.kmose.newsapiclient.domain.usecase.GetNewsHeadlinesUseCase
import com.kmose.newsapiclient.domain.usecase.GetSearchedNewsUseCase
import com.kmose.newsapiclient.domain.usecase.SaveNewsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {
    @Singleton
    @Provides
    fun providesGetNewsHeadlinesUseCase(
        newsRepository: NewsRepository
    ): GetNewsHeadlinesUseCase {
        return GetNewsHeadlinesUseCase(newsRepository)
    }

    @Singleton
    @Provides
    fun providesGetSearchedNewsHeadlinesUseCase(
        newsRepository: NewsRepository
    ): GetSearchedNewsUseCase {
        return GetSearchedNewsUseCase(newsRepository)
    }

    @Singleton
    @Provides
    fun providesSaveNewsUseCase(
        newsRepository: NewsRepository
    ): SaveNewsUseCase {
        return SaveNewsUseCase(newsRepository)
    }
}