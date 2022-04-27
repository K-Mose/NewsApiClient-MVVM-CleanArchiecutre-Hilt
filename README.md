# NewsApiClient-MVVM-CleanArchiecutre-Hilt
https://newsapi.org/ 를 이용한 NewsApi App (with MVVM, Clean Architecture, Hilt)
### Introduction
Retrofit을 이용하여 newsapi.org에서 최신 뉴스 헤드라인을 검색하여 API로 가져옵니다. 
헤드라인을 클릭하면 기사의 전문을 볼 수 있고, 
뉴스 기사 안에서는 저장버튼을 통해서 뉴스를 저장하고, 저장된 뉴스 리스트에서는 저장된 뉴스를 밀어서 뉴스를 삭제할 수 있습니다. 

## UseCase 
우선 위의 소개된 시나리오에 따라서 필요 기능을 생각하고 UseCase를 작성합니다. 
<img src="https://user-images.githubusercontent.com/55622345/165213342-193928e0-5a5f-4607-af8d-15ce77e9d37a.png" width="600px"/>

뉴스의 헤드라인을 가져오기 위해서 `GetNewsHeadlinesUseCase` 클래스를 생성하고, `SaveNewsUseCase`클래스는 선택한 기사를 저장하기 위해 생성하고, 저장된 뉴스를 가져오기 위해서 `GetSavedNewsUseCsae`클래스를 작성합니다. 그리고 저장된 뉴스를 삭제하기 위해서 `DeleteSavedNewsUseCase`클래스를 생성합니다. 
마지막으로 뉴스의 헤드라인을 검색하기 위한 `GetSearchedNewsUseCase`클래스를 생성합니다. 

각각의 UseCase는 Repository를 통해서 Data Layer에 접근할 수 있으므로 `NewsRepository`인터페이스를 생성한 뒤 각각의 UseCase 생성자의 파라메터로 추가합니다. `class *UseCase(private val newsRepository: NewsRepository)`

*Package* <br>
![image](https://user-images.githubusercontent.com/55622345/165213769-753ad65f-0e56-43f9-b100-64a79684edf2.png)<br>


## Data Model 
데이터 클래스를 작성하기 앞서 https://newsapi.org/v2/top-headlines?country=us&apiKey=1feb16503cf04195add78d20bf82adcf 에서 JSON 형태를 파악합니다. 

***※ JSON to Kotlin 플러그인을 설치하면 쉽게 데이터 클래스를 추가할 수 있습니다.*** <br/>
<img src="https://user-images.githubusercontent.com/55622345/165216558-2da043ee-aef0-4dfb-90dd-a57eb1a49dd0.png" max-width="600px"/> <br/>
Kotlin data class file from JSON 을 선택한 뒤 위 API의 전문을 복사하여 아래와 같이 붙여 넣은 뒤 클래스명을 입력하고 생성합니다. <br/>
<img src="https://user-images.githubusercontent.com/55622345/165216809-b432452a-7b2c-47f2-9357-9393259d29eb.png" max-width="800px"/><br/>
**※ app level `build.gradle`에 GSON 추가되지 않았다면 @SerializedName 어노테이션이 오류가 뜨므로 dependency를 추가합니다.** <br/>
`implementation 'com.google.code.gson:gson:2.9.0'`

<details>
  <summary>generated data classes</summary>

### APIResponse  
  ```kotlin 
import com.google.gson.annotations.SerializedName

data class APIResponse(
    @SerializedName("articles")
    val articles: List<Article>,
    @SerializedName("status")
    val status: String,
    @SerializedName("totalResults")
    val totalResults: Int
)
  ```

### Article
  ```kotlin 
import com.google.gson.annotations.SerializedName

data class Article(
    @SerializedName("author")
    val author: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("publishedAt")
    val publishedAt: String,
    @SerializedName("source")
    val source: Source,
    @SerializedName("title")
    val title: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("urlToImage")
    val urlToImage: String
)
  ```

### Source
  ```kotlin 
import com.google.gson.annotations.SerializedName

data class Source(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String
)
  ```
</details>
</br>
 
API에서 응답되는 데이터가 status를 포함하기 때문에 status에 따라 응답될 수 있게 Util 패키지를 만들어 클래스를 추가합니다. 
<a href="https://medium.com/codex/kotlin-sealed-classes-for-better-handling-of-api-response-6aa1fbd23c76">비슷한 예</a>
```kotlin
package com.kmose.newsapiclient.data.util

sealed class Resource<T> (
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Error<T>(message:String, data: T? = null) : Resource<T>(data, message)
}
```

### Repository & UseCase
Data 클래스가 추가되었으니 이에 맞게 응답 데이터의 Repository와 UseCase를 작성합니다. 
```kotlin 
interface NewsRepository {
    suspend fun getNewsHeadlines(): Resource<APIResponse> 
    suspend fun getSearchedNews(searchQuery: String): Resource<APIResponse>
    suspend fun saveNews(article: Article)
    suspend fun deleteNews(article: Article)
    fun getSavedNews(): Flow<List<Article>> 
}
```
각각의 get 메서드에서는 API의 응답 status에 따라서 반응할 수 있도록 위에서 작성한 `Resource` 클래스로 `APIResponse`데이터를 받습니다. </br>

save와 delete 데이터는 저장과 삭제를 위해 `Article'객체만 있으면 되므로 파라메터로 추가합니다. </br>

저장된 뉴스의 리스트를 실시간으로 업데이트 하기위해 `getSavedNews()`메서드의 리턴 타입을 `Flow<T>`로 작성합니다. 
Flow는 비동기식으로 데이터 스트림을 계산하여 방출하기 위해 Coroutine위에 작성되었습니다. </br>

***Flow를 사용하기 위해서는  Coroutine core를 dependency에 추가해야 합니다.*** </br>

`implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1"` </br>

또한 <a href="https://developer.android.com/codelabs/basic-android-kotlin-training-intro-room-flow#8">Flow는 Room에서 수행되는 데이터의 변경을 지속적으로 방출</a>합니다. <br>
`suspend fun`대신 Flow를 사용함으로 리스트의 다중 값들을 한 번에 받을 수 있습니다. (<a href="https://ngodinhduyquang.medium.com/coroutines-flow-vs-suspend-function-sequence-and-livedata-108a8dc72787">참고 Flow vs suspend fun</a>)

이제 각각 UseCase에서 데이터에 접근할 Repository를 작성합니다. 
<details>
  <summary><b>UseCases</b></summary>
  
  ### GetNewsHeadlinesUseCase
  ```kotlin
  class GetNewsHeadlinesUseCase(private val newsRepository: NewsRepository) {
      suspend fun execute(): Resource<APIResponse> {
          return newsRepository.getNewsHeadlines()
      }
  }  
  ```
  
  ### GetSearchedNewsUseCase
  ```kotlin
  class GetNewsHeadlinesUseCase(private val newsRepository: NewsRepository) {
      suspend fun execute(): Resource<APIResponse> {
          return newsRepository.getNewsHeadlines()
      }
  }
  ```
  
  ### SaveNewsUseCase
  ```kotlin
  class SaveNewsUseCase(private val newsRepository: NewsRepository) {
      suspend fun execute(article: Article) = newsRepository.saveNews(article)
  }  
  ```

  ### DeleteSavedNewsUseCase
  ```kotlin
  class DeleteSavedNewsUseCase(private val newsRepository: NewsRepository) {
      suspend fun execute(article: Article) = newsRepository.deleteNews(article)
  }  
  ```

  ### GetSavedNewsUseCase
  ```kotlin
  class GetSavedNewsUseCase(private val newsRepository: NewsRepository) {
      fun execute(): Flow<List<Article>> {
          return newsRepository.getSavedNews()
      }
  }  
  ```
</details>
</br>
</br>
  
### Remote DataSource 
우선 `NewsRepository`인터페이스에서 `getNewsHeadlines()`메소드의 뉴스의 헤드라인만 받기 위한 준비를 하겠습니다. 

#### Adding Dependency
API로 원격 데이터를 받기 위해 Retrofit과 gson converter를 추가합니다. 
```
def retrofit_version = "2.9.0"
implementation "com.squareup.retrofit2:retrofit:$retrofit_version"
implementation "com.squareup.retrofit2:converter-gson:$retrofit_version"
```

#### NewsAPIService
이제 data 계층에 외부 API와 연결할 `NewsAPIService`인터페이스를 추가합니다.  </br>
***package*** <br>
![image](https://user-images.githubusercontent.com/55622345/165235882-bd094e5c-8131-4082-ab91-b0a2b8a39b0b.png) <br>
```kotlin
interface NewsAPIService {
    @GET("/v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("country")
        country: String,
        @Query("page")
        page: Int,
        @Query("apiKey")
        apiKey: String = BuildConfig.NEWS_API
    ): Response<APIResponse>
}
```
NewsApi docs에서 나오듯이 Top headlines은 `apiKey`를 필수로 받고, 뉴스를 가져올 나라를 선택하기 위해서 `country`를 추가합니다. 그리고 가져올 페이지를 선택하기 위해 `page`파라메터를 추가합니다. 

#### NewsRemoteDataSource & Impl
`NewsAPIService`인터페이스가 추가 됐다면, `NewsAPIService`인터페이스를 Repository와 연결할 dataSource를 추가합니다. </br>
***package*** <br>
![image](https://user-images.githubusercontent.com/55622345/165236972-1a0d2060-a490-458c-ad4d-14433968c8ee.png)
```kotlin
interface NewsRemoteDataSource {
    suspend fun getTopHeadlines(): Response<APIResponse>
}

class NewsRemoteDataSourceImpl(
    private val newsAPIService: NewsAPIService,
    private val country:String,
    private val page:Int
) : NewsRemoteDataSource {
    override suspend fun getTopHeadlines(): Response<APIResponse> {
        return newsAPIService.getTopHeadlines(country, page)
    }
}
```
`NewsRemoteDataSource`인터페이스의 리턴 타입이 Retrofit의 `Response`로 설정한 후, 인터페이스를 구현하는 `NewsRemoteDataSourceImpl`클래스 내에서 생성자로 `NewsAPIService`인터페이스를 받아 `getTopHeadlines()`함수에서 `Response`타입으로 리턴합니다. 

#### NewsRepositoryImpl
마지막으로 domain layer에서 작성된 `NewsRepository`인터페이스를 구현할 `NewsRepositoryImpl`클래스를 생성 후 
DataSource와 연결합니다. 
```kotlin
class NewsRepositoryImpl(
    private val newsRemoteDataSource: NewsRemoteDataSource
) : NewsRepository {
    override suspend fun getNewsHeadlines(): Resource<APIResponse> {
        return responseToResource(newsRemoteDataSource.getTopHeadlines())
    }

    private fun responseToResource(response: Response<APIResponse>): Resource<APIResponse> {
        if(response.isSuccessful) {
            response.body()?.let { result ->
                return Resource.Success(result)
            }
        }
        return Resource.Error(response.message())
    }
    ……
}
```
`responseToResource(response: Response<APIResponse>)`함수는 Response의 status 결과에 따라서 성공과 실패로 나눠 `Resource`타입으로 반환하는 함수입니다. 반환된 결과에 따라서 정상 응답의 body와 error메세지를 출력합니다. 

## Unit Testing With MockWebServer
안드로이드 유닛테스트는 <a href="https://github.com/K-Mose/UnitTestFundamentals">여기</a>를 보시기 바랍니다.

Retrofit으로 작성된 `NewsAPIService`인터페이스를 가지고 테스트를 하겠습니다. 테스트는 test source set에서 JUnit4로 진행합니다. 

<details>
  <summary><b>FULL-Test Scenario</b></summary>
  
테스트에 앞서 app level `build.gradle`에 의존성을 추가합니다. 
```
    implementation "com.squareup.okhttp3:okhttp:4.9.3"
    
    testImplementation "com.squareup.okhttp3:mockwebserver:4.9.3"
    testImplementation "com.google.truth:truth:1.1.3"
```
OKHTTP가 MockWebServer와 통신할 때 okHttpName 오류가 날 수도 있기 떄문에 okhttp 의존성도 추가합니다.  
  
**!!! 유닛 테스트 중 Retrofit 2.9.0 버전에서는 오류가 발생하여서 2.7.0 버전으로 다운그레이드 하여 진행하였습니다.**

그리고 테스트용의 API 결과 값을 설정하기 위해 `https://newsapi.org/v2/top-headlines?country=kr&page=1&apiKey={API_Key}`의 결과 값을 아래 경로에 `newsresponse.json`파일로 생성합니다. (app\src\main\resources\newsresponse.json) </br>
![image](https://user-images.githubusercontent.com/55622345/165420784-7afbac31-26bd-4c4e-922d-2697bfc73d00.png)

이제 테스트를 작성하겠습니다. 
우선 같은 경로로 패키지를 만든 후 테스트 클래스를 만듭니다. </br>
![image](https://user-images.githubusercontent.com/55622345/165419953-6e20aa85-a122-41b2-a2e0-870f88e94d22.png)

전역변수에 `NewsAPIService`인터페이스의 인스턴스와 `MockWebServer`의 인스턴스를 추가합니다. 
```kotlin
class NewsAPIServiceTest {
    private lateinit var service: NewsAPIService
    private lateinit var server: MockWebServer
    
    ……
```

그리고 `setUp()`함수와 `tearDown()`함수를 JUnit4로 생성합니다. 
```kotlin
    @Before
    fun setUp() {
        server = MockWebServer()
        service = Retrofit.Builder()
            .baseUrl(server.url(""))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsAPIService::class.java)
    }
    
    @After
    fun tearDown() {
        server.shutdown()
    }
```
`setUp()`함수에서는 `MockWebServer`와 `NewsAPIService`의 객체를 생성해주고 `tearDown()`함수에서는 server를 종료합니다.

테스트 케이스 작성에 앞서 `MockWebServer`에 Mock 테스트 응답을 추가하는 함수를 작성하겠습니다. 
```kotlin
    private fun enqueueMockResponse(
        fileName:String
    ) {
        val inputStream = javaClass.classLoader!!.getResourceAsStream(fileName)
        val source = inputStream.source().buffer()
        val mockResponse = MockResponse()
        mockResponse.setBody(source.readString(Charsets.UTF_8))
        server.enqueue(mockResponse)
    }
```
해당 파일은 위에서 작성했던 `newsresponse.json`파일을 읽고 `MockResponse`객체에 결과 값의 body를 추가 한 후 생성된 `MockWebServer`에 응답을 추가하는 함수입니다. 자세한 내용은 <a href="https://github.com/square/okhttp/tree/master/mockwebserver">MockWebServcer Git</a>을 확인하길 바랍니다. 

마지막으로 테스트케이스를 추가하겠습니다. 
```kotlin
    @Test
    fun getTopHeadlines_sentRequest_receivedExpected() {
        runBlocking {
            enqueueMockResponse("newsresponse.json")
            val responseBody = service.getTopHeadlines("kr", 0).body()
            val request = server.takeRequest()
            val path = "/v2/top-headlines?country=kr&page=1&apiKey=" + BuildConfig.NEWS_API
            assertThat(responseBody).isNotNull()
            assertThat(request.path).isEqualTo(path)
        }
    }
```
Coroutine Scope로 테스트하기위해서 `runBlocking`을 사용하였고 `enqueueMockResponse`함수에 fileName을 넘겨주어 응답 값을 준비합니다. 
`service`객체에서 `getTopHeadlines`함수를 호출함으로 `MockWebServer`와 통신하고 `service`객체는 앞에서 준비한 응답 값을 받게 됩니다. 여기서 `getTopHeadlines`함수에 넘긴 값으로 API의 주소가 설정되므로 request의 paht 값을 확인하여 실제 예상했던 주소와 일치하는지 확인 할 수 있습니다.   

정확한 기사가 왔는지 확인하기 위해 테스트 케이스를 작성합니다. 아래는 api가 가져온 첫 기사입니다. 
```josn
    "articles":
    [
        {"source":
            {"id":null,"name":"Yna.co.kr"},
            "author":"정래원",
            "title":……,
            "description":……,
            "url":"https://www.yna.co.kr/view/AKR20220426122600504",
            "urlToImage":……,
            "publishedAt":"2022-04-26T06:26:28Z",
        }, 
        ……
```
위 기사와 같은 값을 갖는지 확인하기 위해 아래와 같이 테스트 케이스를 작성한 후 테스트틀 합니다. 
```kotlin 
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
```
![image](https://user-images.githubusercontent.com/55622345/165424941-04af19be-80c7-4213-a585-b52fd3dd3469.png)<br>
테스트가 성공적으로 마쳤습니다. 

</details>


## Presentation Layer - ViewModel 
데이터를 View에 출력하기 위해 UseCase에 접근하는 ViewModel을 작성합니다. 

우선 presentation layer에 viewmodel 패키지를 생성합니다. 그리고 `NewsViewModel`클래스를 생성합니다. 
![image](https://user-images.githubusercontent.com/55622345/165432885-48c1804c-b235-4985-89c9-02b90eb1f73d.png)

`NewsViewModel`클래스 내부에서 UseCase에 접근하기 위해 생성자에 `GetNewsHeadlinesUseCase`객체를 파라메터로 지정합니다. 
```kotlin 
class NewsViewModel(
    val getNewsHeadlinesUseCase: GetNewsHeadlinesUseCase
) : ViewModel()
```

프로젝트 시나리오에서 Retrofit의 응답은 `Resource`클래스로 받아서 `Success||Loading||Error` 세 가지 중 하나의 상태를 받게 됩니다. 
`Resource`의 상태에 따라 응답하기위해 ViewModel에서 사용되는 LiveData도 Resource를 받는 LiveData로 전역변수로 선언합니다. </br>
`val newsHeadlines: MutableLiveData<Resource<APIResponse>> = MutableLiveData()` </br>

```kotlin
newsHeadlines.postValue(Resource.Loading())
val apiResult = getNewsHeadlinesUseCase.execute(country, page)
newsHeadlines.postValue(apiResult)
```
처음 상태는 응답을 기다리므로 Loading으로 설정합니다. 그후 응답받은 API 값을 Background Thread에서 변경하기 위해서 `newsHeadlines.postValue(apiResult)`로 값을 변경합니다. 해당 응답이 성공적인지는 `NewsRepositoryImpl`클래스에서 다시한번 응답 값을 가지고 [`Resource`를 결정](#newsrepositoryimpl)하게 됩니다. 

API의 응답이 무조건 성공하는 것은 아닙니다. 인터넷 연결에 따라서 실패할 수도 있고, 응답을 처리하는 중에 에러가 발생할 수 있습니다. 
우선 인터넷의 연결을 확인하는 코드를 작성합니다. </br>
*(자주 사용되는 인터넷 상태를 확인하는 코드)*
```kotlin
    private fun isNetWorkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false

    }
```
`isNetWorkAvailable()`함수는 `Context`를 파라메터로 받습니다. ViewModel에서는 Application의 Context를 지원하지 못하므로 `NewsViewModel`의 부모를 `AndroidViewModel`로 변경하고 생성자에 `Application`의 인스턴스를 넘겨줍니다. 
```kotlin 
class NewsViewModel(
    val app: Application,
    val getNewsHeadlinesUseCase: GetNewsHeadlinesUseCase
) : AndroidViewModel(app)
```


이제 위 코드를 추가하여 뉴스의 헤드라인을 가져오는 `getNewsHeadlines()`함수를 작성합니다. 
```kotlin
    fun getNewsHeadlines(country:String, page:Int) = viewModelScope.launch(Dispatchers.IO) {
        try {
            if(isNetWorkAvailable(app)) {
                // normal flow of successful execution
                newsHeadlines.postValue(Resource.Loading())
                val apiResult = getNewsHeadlinesUseCase.execute(country, page)
                newsHeadlines.postValue(apiResult)
            } else {
                newsHeadlines.postValue(Resource.Error("Internet is not available"))
            }
        } catch (e: Exception) {
            newsHeadlines.postValue(Resource.Error(e.message.toString()))
        }
    }
```

<details>
<summary>Full-Code</summary>

```kotlin
class NewsViewModel(
    val app: Application,
    val getNewsHeadlinesUseCase: GetNewsHeadlinesUseCase
) : AndroidViewModel(app) {
    val newsHeadlines: MutableLiveData<Resource<APIResponse>> = MutableLiveData()

    fun getNewsHeadlines(country:String, page:Int) = viewModelScope.launch(Dispatchers.IO) {
        try {
            if(isNetWorkAvailable(app)) {
                newsHeadlines.postValue(Resource.Loading())
                val apiResult = getNewsHeadlinesUseCase.execute(country, page)
                newsHeadlines.postValue(apiResult)
            } else {
                newsHeadlines.postValue(Resource.Error("Internet is not available"))
            }
        } catch (e: Exception) {
            newsHeadlines.postValue(Resource.Error(e.message.toString()))
        }

    }

    private fun isNetWorkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false

    }
}  
```
</details>

### ViewModelFactory
ViewModel을 작성했으니 <a href="https://github.com/K-Mose/TwoWayDataBinding#viewmodel-with-viewmodelproviderfactory-">ViewModel을 생성해주는 Factory class</a>를 작성합니다. 
```kotlin
class NewsViewModelFactory(
    private val app: Application,
    private val getNewsHeadlinesUseCase: GetNewsHeadlinesUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewsViewModel(app, getNewsHeadlinesUseCase) as T
    }
}
```

### ※※ DataSource & UseCase & Repository Changes
ViewModel에서 `getNewsHeadlinesUseCase.execute(country, page)`로 `country`와 `page` 값을 넘겨주기 위해 아래와 같이 

<details>
<summary><b>DataSource & UseCase & Repository Changes</b></summary>

### NewsRemoteDataSourceImpl
```kotlin
class NewsRemoteDataSourceImpl(
    private val newsAPIService: NewsAPIService
) : NewsRemoteDataSource {
    override suspend fun getTopHeadlines(country:String, page:Int): Response<APIResponse> {
        return newsAPIService.getTopHeadlines(country, page)
    }
}
```
  
### NewsRemoteDataSource
```kotlin
interface NewsRemoteDataSource {
    suspend fun getTopHeadlines(country:String, page:Int): Response<APIResponse>
}
```

### NewsRepositoryImpl
```kotlin
class NewsRepositoryImpl(
    private val newsRemoteDataSource: NewsRemoteDataSource
) : NewsRepository {
    override suspend fun getNewsHeadlines(country:String, page:Int): Resource<APIResponse> {
        return responseToResource(newsRemoteDataSource.getTopHeadlines(country, page))
    }  
  ……
```

### NewsRepository
```kotlin
interface NewsRepository {
    suspend fun getNewsHeadlines(country:String, page:Int): Resource<APIResponse>  
    ……
```

### GetNewsHeadlinesUseCase
```kotlin
class GetNewsHeadlinesUseCase(private val newsRepository: NewsRepository) {
    suspend fun execute(country:String, page:Int): Resource<APIResponse> {
        return newsRepository.getNewsHeadlines(country, page)
    }
}
```  
</details>

  
  
  


## Ref. 
**Flow** - <br>
https://developer.android.com/kotlin/flow </br>
https://kotlinlang.org/docs/flow.html </br>
https://ngodinhduyquang.medium.com/coroutines-flow-vs-suspend-function-sequence-and-livedata-108a8dc72787 </br>

**NewsAPI** - <br>
https://newsapi.org/ </br>

**Resource Class** - <br>
https://medium.com/swlh/kotlin-sealed-class-for-success-and-error-handling-d3054bef0d4e </br>
https://medium.com/codex/kotlin-sealed-classes-for-better-handling-of-api-response-6aa1fbd23c76 </br>

**MockWebServer** - <br>
https://github.com/square/okhttp/tree/master/mockwebserver <br>
