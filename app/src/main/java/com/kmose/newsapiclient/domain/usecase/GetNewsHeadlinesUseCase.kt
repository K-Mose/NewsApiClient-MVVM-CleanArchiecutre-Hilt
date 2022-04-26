package com.kmose.newsapiclient.domain.usecase

import com.kmose.newsapiclient.domain.repository.NewsRepository

class GetNewsHeadlinesUseCase(private val newsRepository: NewsRepository) {
}