package com.kmose.newsapiclient.domain.usecase

import com.kmose.newsapiclient.data.model.Article
import com.kmose.newsapiclient.domain.repository.NewsRepository

class DeleteSavedNewsUseCase(private val newsRepository: NewsRepository) {
    suspend fun execute(article: Article) = newsRepository.deleteNews(article)
}