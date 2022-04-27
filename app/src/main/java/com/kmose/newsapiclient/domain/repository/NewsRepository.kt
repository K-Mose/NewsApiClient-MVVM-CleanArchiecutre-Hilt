package com.kmose.newsapiclient.domain.repository

import com.kmose.newsapiclient.data.model.APIResponse
import com.kmose.newsapiclient.data.model.Article
import com.kmose.newsapiclient.data.util.Resource
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    suspend fun getNewsHeadlines(country:String, page:Int): Resource<APIResponse> // API Response의 Status에 따라 응답하기 위해서
    suspend fun getSearchedNews(searchQuery: String): Resource<APIResponse>
    suspend fun saveNews(article: Article)
    suspend fun deleteNews(article: Article)
    // https://developer.android.com/kotlin/flow
    // Article 업데이트마다 알 수 있도록 LiveData로 설정할 수 있지만
    // Repository에서 Livedata를 설정하는 것은 unexpected threading issue 발생으로 권장되지 않음
    // Flow를 사용하면 비동기식으로 데이터 스트림을 다룰 수 있고, Room도 Flow를 통해 데이터 핸들링을 허용함
    fun getSavedNews(): Flow<List<Article>> // Flow로인해 suspend fun으로 설정 할 필요가 없음

}