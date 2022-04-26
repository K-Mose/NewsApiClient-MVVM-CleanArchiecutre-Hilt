package com.kmose.newsapiclient.domain.usecase

import com.kmose.newsapiclient.domain.repository.NewsRepository

class GetSearchedNewsUseCase(private val newsRepository: NewsRepository) {
}