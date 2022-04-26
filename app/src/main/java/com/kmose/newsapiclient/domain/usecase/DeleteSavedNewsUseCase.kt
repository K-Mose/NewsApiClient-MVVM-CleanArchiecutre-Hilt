package com.kmose.newsapiclient.domain.usecase

import com.kmose.newsapiclient.domain.repository.NewsRepository

class DeleteSavedNewsUseCase(private val newsRepository: NewsRepository) {
}