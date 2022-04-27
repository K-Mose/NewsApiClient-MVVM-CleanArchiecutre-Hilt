package com.kmose.newsapiclient.domain.usecase

import com.kmose.newsapiclient.data.model.APIResponse
import com.kmose.newsapiclient.data.util.Resource
import com.kmose.newsapiclient.domain.repository.NewsRepository

class GetNewsHeadlinesUseCase(private val newsRepository: NewsRepository) {
    suspend fun execute(country:String, page:Int): Resource<APIResponse> {
        return newsRepository.getNewsHeadlines(country, page)
    }
}