package com.kmose.newsapiclient.presentation.di

import android.app.Application
import com.kmose.newsapiclient.domain.usecase.GetNewsHeadlinesUseCase
import com.kmose.newsapiclient.domain.usecase.GetSearchedNewsUseCase
import com.kmose.newsapiclient.presentation.viewmodel.NewsViewModelFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FactoryModule {
    @Singleton
    @Provides
    fun providesViewModelFactory(
        app: Application,
        getNewsHeadlinesUseCase: GetNewsHeadlinesUseCase,
        getSearchedNewsUseCase: GetSearchedNewsUseCase
    ): NewsViewModelFactory {
        return NewsViewModelFactory(app, getNewsHeadlinesUseCase, getSearchedNewsUseCase)
    }
}