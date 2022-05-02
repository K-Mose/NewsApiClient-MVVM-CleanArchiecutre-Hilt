package com.kmose.newsapiclient.data.repository.dataSourceImpl

import com.kmose.newsapiclient.data.api.NewsAPIService
import com.kmose.newsapiclient.data.model.APIResponse
import com.kmose.newsapiclient.data.repository.dataSource.NewsRemoteDataSource
import retrofit2.Response

class NewsRemoteDataSourceImpl(
    private val newsAPIService: NewsAPIService
) : NewsRemoteDataSource {
    override suspend fun getTopHeadlines(country:String, page:Int): Response<APIResponse> {
        return newsAPIService.getTopHeadlines(country, page)
    }

    override suspend fun getSearchedTopHeadlines(
        country: String,
        searchQuery: String,
        page: Int
    ): Response<APIResponse> {
        return newsAPIService.getSearchedTopHeadlines(country, searchQuery, page)
    }
}