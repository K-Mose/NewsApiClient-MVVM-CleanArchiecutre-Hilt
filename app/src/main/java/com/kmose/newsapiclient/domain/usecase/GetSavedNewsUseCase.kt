package com.kmose.newsapiclient.domain.usecase

import com.kmose.newsapiclient.data.model.Article
import com.kmose.newsapiclient.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow

class GetSavedNewsUseCase(private val newsRepository: NewsRepository) {
    fun execute(): Flow<List<Article>> {
        return newsRepository.getSavedNews()
    }
}