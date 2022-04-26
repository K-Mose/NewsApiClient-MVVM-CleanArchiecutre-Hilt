package com.kmose.newsapiclient.data.repository.dataSource

import com.kmose.newsapiclient.data.model.APIResponse
import retrofit2.Response

interface NewsRemoteDataSource {
    suspend fun getTopHeadlines(): Response<APIResponse>
}