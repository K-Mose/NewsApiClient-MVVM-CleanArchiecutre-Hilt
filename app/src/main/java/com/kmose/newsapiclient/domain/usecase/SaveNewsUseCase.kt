package com.kmose.newsapiclient.domain.usecase

import com.kmose.newsapiclient.domain.repository.NewsRepository

class SaveNewsUseCase(private val newsRepository: NewsRepository) {
}