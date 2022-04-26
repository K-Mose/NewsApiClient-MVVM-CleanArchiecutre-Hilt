package com.kmose.newsapiclient.domain.usecase

import com.kmose.newsapiclient.data.model.APIResponse
import com.kmose.newsapiclient.data.util.Resource
import com.kmose.newsapiclient.domain.repository.NewsRepository

class GetSearchedNewsUseCase(private val newsRepository: NewsRepository) {
    suspend fun execute(searchQuery: String): Resource<APIResponse> {
        return newsRepository.getSearchedNews(searchQuery)
    }
}