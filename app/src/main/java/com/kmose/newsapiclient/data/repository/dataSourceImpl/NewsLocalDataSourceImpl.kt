package com.kmose.newsapiclient.data.repository.dataSourceImpl

import com.kmose.newsapiclient.data.db.ArticleDAO
import com.kmose.newsapiclient.data.db.ArticleDatabase
import com.kmose.newsapiclient.data.model.Article
import com.kmose.newsapiclient.data.repository.dataSource.NewsLocalDataSource

class NewsLocalDataSourceImpl(
    private val articleDAO: ArticleDAO
) : NewsLocalDataSource {
    override suspend fun saveArticleToDB(article: Article) {
        articleDAO.insert(article)
    }
}