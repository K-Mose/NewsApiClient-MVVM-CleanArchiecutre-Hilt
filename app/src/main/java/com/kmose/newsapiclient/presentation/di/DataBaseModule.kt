package com.kmose.newsapiclient.presentation.di

import android.app.Application
import androidx.room.Room
import com.kmose.newsapiclient.data.db.ArticleDAO
import com.kmose.newsapiclient.data.db.ArticleDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataBaseModule {
    @Singleton
    @Provides
    fun providesNewsDataBase(app:Application): ArticleDatabase {
        return Room.databaseBuilder(app, ArticleDatabase::class.java, "news_db")
            .fallbackToDestructiveMigration() // Allow Room to destructively replace database tables
            .build()
    }

    @Singleton
    @Provides
    fun providesNewsDAO(articleDatabase: ArticleDatabase): ArticleDAO {
        return articleDatabase.getArticleDAO()
    }
}