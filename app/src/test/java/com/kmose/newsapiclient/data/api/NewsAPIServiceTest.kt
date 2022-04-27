package com.kmose.newsapiclient.data.api

import com.google.common.truth.Truth.assertThat
import com.kmose.newsapiclient.BuildConfig
import com.kmose.newsapiclient.data.model.Article
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsAPIServiceTest {
    private lateinit var service: NewsAPIService
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        // MockWebServer 인스턴스 생성
        server = MockWebServer()
        // Retrofit 인스턴스 생성
        service = Retrofit.Builder()
            .baseUrl(server.url(""))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsAPIService::class.java)
    }

    // \main\resources\newsresponse.json 파일 읽어와서 MockResponse 객체에 응답 추가
    private fun enqueueMockResponse(
        fileName:String
    ) {
        val inputStream = javaClass.classLoader!!.getResourceAsStream(fileName)
        val source = inputStream.source().buffer()
        val mockResponse = MockResponse()
        mockResponse.setBody(source.readString(Charsets.UTF_8))
        server.enqueue(mockResponse)
    }

    @Test
    fun getTopHeadlines_sentRequest_receivedExpected() {
        runBlocking {
            enqueueMockResponse("newsresponse.json")
            val responseBody = service.getTopHeadlines("kr", 1).body()
            val request = server.takeRequest()
            val path = "/v2/top-headlines?country=kr&page=1&apiKey=" + BuildConfig.NEWS_API
            assertThat(responseBody).isNotNull()
            assertThat(request.path).isEqualTo(path)
        }
    }

    @Test
    fun getTopHeadlines_receivedResponse_correctContent() {
        runBlocking {
            enqueueMockResponse("newsresponse.json")
            val responseBody = service.getTopHeadlines("kr", 1).body()
            val articleList = responseBody!!.articles
            val article = articleList[0]
            assertThat(articleList.size).isEqualTo(20)
            assertThat(article.author).isEqualTo("정래원")
            assertThat(article.url).isEqualTo("https://www.yna.co.kr/view/AKR20220426122600504")
            assertThat(article.publishedAt).isEqualTo("2022-04-26T06:26:28Z")
        }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }
}