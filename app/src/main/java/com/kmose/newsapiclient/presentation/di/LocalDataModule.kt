package com.kmose.newsapiclient.presentation.di

import com.kmose.newsapiclient.data.db.ArticleDAO
import com.kmose.newsapiclient.data.repository.dataSource.NewsLocalDataSource
import com.kmose.newsapiclient.data.repository.dataSourceImpl.NewsLocalDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocalDataModule {
    @Singleton
    @Provides
    fun providesLocalDataSource(articleDAO: ArticleDAO): NewsLocalDataSource{
        return NewsLocalDataSourceImpl(articleDAO)
    }
}