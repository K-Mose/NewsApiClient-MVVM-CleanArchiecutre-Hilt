package com.kmose.newsapiclient.data.repository.dataSource

import com.kmose.newsapiclient.data.model.Article

interface NewsLocalDataSource {
    suspend fun saveArticleToDB(article: Article)
}