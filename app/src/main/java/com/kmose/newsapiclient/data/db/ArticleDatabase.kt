package com.kmose.newsapiclient.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kmose.newsapiclient.data.model.Article

@Database(
    entities = [Article::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ArticleDatabase : RoomDatabase() {
    abstract fun getArticleDAO(): ArticleDAO

}