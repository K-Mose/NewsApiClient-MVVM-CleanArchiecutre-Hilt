package com.kmose.newsapiclient.domain.usecase

import com.kmose.newsapiclient.domain.repository.NewsRepository

class GetSavedNewsUseCase(private val newsRepository: NewsRepository) {
}