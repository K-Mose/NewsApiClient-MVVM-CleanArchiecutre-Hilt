package com.kmose.newsapiclient.data.repository.dataSource

import com.kmose.newsapiclient.data.model.APIResponse
import retrofit2.Response

interface NewsRemoteDataSource {
    suspend fun getTopHeadlines(country:String, page:Int): Response<APIResponse>
    suspend fun getSearchedTopHeadlines(country:String, searchQuery:String, page:Int): Response<APIResponse>
}