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
  <summary id="UseCases"><b>UseCases</b></summary>
  
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

<details>
<summary>ViewModel Dependencies</summary>

```
plugins {
   ……
  id 'kotlin-kapt'
}
……  
dependencies {
    // ViewModel
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"
    // LiveData
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version"
    // Annotation processor
    kapt "androidx.lifecycle:lifecycle-compiler:$lifecycle_version"
}
```
</details>

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
ViewModel에서 `getNewsHeadlinesUseCase.execute(country, page)`로 `country`와 `page` 값을 넘겨주기 위해 아래와 같이 수정합니다.

<details>
  <summary>
    <b>DataSource & UseCase & Repository Changes</b>
  </summary>

  <b>NewsRemoteDataSourceImpl</b>
  ```kotlin
  class NewsRemoteDataSourceImpl(
      private val newsAPIService: NewsAPIService
  ) : NewsRemoteDataSource {
      override suspend fun getTopHeadlines(country:String, page:Int): Response<APIResponse> {
          return newsAPIService.getTopHeadlines(country, page)
      }
  }
  ```
  
  <b>NewsRemoteDataSource</b>
  ```kotlin
  interface NewsRemoteDataSource {
      suspend fun getTopHeadlines(country:String, page:Int): Response<APIResponse>
  }
  ```

  <b>NewsRepositoryImpl</b>
  ```kotlin
  class NewsRepositoryImpl(
      private val newsRemoteDataSource: NewsRemoteDataSource
  ) : NewsRepository {
      override suspend fun getNewsHeadlines(country:String, page:Int): Resource<APIResponse> {
          return responseToResource(newsRemoteDataSource.getTopHeadlines(country, page))
      }  
    ……
  ```

  <b>NewsRepository</b>
  ```kotlin
  interface NewsRepository {
      suspend fun getNewsHeadlines(country:String, page:Int): Resource<APIResponse>  
      ……
  ```

  <b>GetNewsHeadlinesUseCase</b>
  ```kotlin
  class GetNewsHeadlinesUseCase(private val newsRepository: NewsRepository) {
      suspend fun execute(country:String, page:Int): Resource<APIResponse> {
          return newsRepository.getNewsHeadlines(country, page)
      }
  }
  ```  
  
</details>
  
## Dependency Injection With Hilt 
<details>
<summary>build.gradle</summary>

우선 DI를 위해서 project level `build.gradle` dependencies에 
```
classpath 'com.google.dagger:hilt-android-gradle-plugin:2.38.1'
```
app level `build.gradle`에  plugin과 dependencies를 추가합니다.
```
plugins {
    id 'dagger.hilt.android.plugin' 
}
  
dependencies {
    // Hilt
    implementation "com.google.dagger:hilt-android:2.38.1"
    kapt "com.google.dagger:hilt-compiler:2.38.1"  
}
```
</details>

Hilt로 DI를 하기 위해서 어떤 클래스(인터페이스)들이 의존성 주입이 필요한지 정렬해 보겠습니다. 
1. Interface **NewsAPIServie** : `NewsAPIService`인터페이스는 [Retrofit의 객체를 생성](https://github.com/K-Mose/RetrofitWithCoroutines#create-insatnace)이 필요하고 이것은 외부 Retrofit 라이브러에서부터 생성되므로 추가 할 필요가 있습니다. 
  
2. Interface **NewsRemoteDataSource** : `NewsRemoteDataSource`인터페이스의 구현체 `NewsRemoteDataSourceImpl`클래스는 Retrofit으로 구현한 `NewsAPIService`인터페이스의 `getTopHeadlines`메서드를 필요로 합니다. 
  그러므로 `NewsRemoteDataSourceImpl`클래스는 `NewAPIService`에 의존성을 갖고 또, `NewsRemoteDataSource`인터페이스의 구현 객체를 필요로 하는 객체에 의존성을 주입하기 위해 추가하여아 합니다. 
  
3. Interface **NewsRepository** : `NewsRepository`인터페이스의 구현체 `NewsRepositoryImpl`클래스는 `getNewsHeadlines`메서드에서 `NewsRemoteDataSource`인터페이스의 `getTopHeadlines`메서드를 필요로 합니다. 
  그러므로 `NewsRepositoryImpl`클래스는 `NewsRemoteDataSource`에 의존성을 갖고 또, `NewsRepository`인터페이스의 구현 객체를 필요로 하는 객체에 의존성을 주입하기 위해 추가하여야 합니다. 
  
4. Class **GetNewsHeadlinesUseCase** : `GetNewsHeadlinesUseCase`클래스는 `execute`메서드에서 `NewsRepository`인터페이스의 `getNewsHeadlines`메서드를 필요로 합니다. 
  그러므로 `GetNewsHeadlinesUseCase`클래스는 NewsRepository`인터페이스에 의존성을 갖고 UseCase는 ViewModel에서 접근할 수 있게 인스턴스를 제공해주어야 하므로 추가하여야 합니다. 
  
5. Class **NewsViewModelFactory** : `NewsViewModelFactory`클래스는 View에게 ViewModel 객체를 제공하고, ViewModel에게는 UseCase 객체를 제공해야 하므로 추가하여야 합니다. 
 
의존성 필요한 객체들을 나열해보면 위에서부터 아래까지 외부 API에서부터 내부 View까지 연결해주는 순서로 이어져있습니다. 

위의 의존성을 추가하기 위해서 presentation 패키지에 di 패키지를 생성 후 아래와 같이 모듈들을 추가합니다. </br>
![image](https://user-images.githubusercontent.com/55622345/165511804-a3317b30-5206-4b02-8ebb-4512f2000550.png) </br>
  
<details>
<summary>Modules</summary>

  ### NetModule
  ```kotlin
@Module
@InstallIn(SingletonComponent::class)
class NetModule {
    @Singleton
    @Provides
    fun providesRetrofit(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BuildConfig.NEWS_URL)
            .build()
    }
  
    @Singleton
    @Provides
    fun providesNewsAPIService(
        retrofit: Retrofit
    ): NewsAPIService {
        return retrofit.create(NewsAPIService::class.java)
    }
}  
  ```
  
  ### RemoteDataSourceModule
  ```kotlin
@Module
@InstallIn(SingletonComponent::class)
class RemoteDataSourceModule {
    @Singleton
    @Provides
    fun providesNewsRemoteDataSource(
        newsAPIService: NewsAPIService
    ): NewsRemoteDataSource {
        return NewsRemoteDataSourceImpl(newsAPIService)
    }
}
  ```
  
  ### RepositoryModule
  ```kotlin
@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Singleton
    @Provides
    fun providesNewsRepository(
        newsRemoteDataSource: NewsRemoteDataSource
    ): NewsRepository {
        return NewsRepositoryImpl(newsRemoteDataSource)
    }
}
  ```
  
  ### UseCaseModule
  ```kotlin
@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {
    @Singleton
    @Provides
    fun providesNewsHeadlinesUseCase(
        newsRepository: NewsRepository
    ): GetNewsHeadlinesUseCase {
        return GetNewsHeadlinesUseCase(newsRepository)
    }
}
  ```
  
  ### FactoryModule
  ```kotlin
@Module
@InstallIn(SingletonComponent::class)
class FactoryModule {
    @Singleton
    @Provides
    fun providesViewModelFactory(
        app: Application,
        getNewsHeadlinesUseCase: GetNewsHeadlinesUseCase
    ): NewsViewModelFactory {
        return NewsViewModelFactory(app, getNewsHeadlinesUseCase)
    }
}
  ```
</details>
  
## Layout - Fragment & Navigation Components  
모든 View 구현에 앞서 
MainActivity 하단에 Navigation을 추가하여 New Headlines fragment와 Saved News fragment를 이동하는 화면을 구현하겠습니다. 

<details>
  <summary>build.gradle</summary>
navigation과 ViewBinding을 사용하기 위해 project와 app level 각각의 build.gradle에 아래와 같이 추가합니다. 
  
project level  
```
dependencies {
  def nav_version = "2.4.2"
  classpath "androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version"  
}
```
app level 
```
plugins {
    id 'androidx.navigation.safeargs.kotlin'
}
……
buildFeatures {
    viewBinding true
}
……
dependencies {
  def nav_version = "2.4.2"
  implementation "androidx.navigation:navigation-fragment-ktx:$nav_version"
  implementation "androidx.navigation:navigation-ui-ktx:$nav_version"
}
```
</details>

### Navigation 
[Navigation Fundmental](https://github.com/K-Mose/NavigationArchitectureComponent) <br>
Android Resource File로 Navigation을 추가 후 아래와 같이 Destination을 추가합니다. 
<details>
<summary>Navigation-Destinations</summary>
<img src="https://user-images.githubusercontent.com/55622345/165532324-e9c3a318-ef51-4201-a392-ab617c569dc0.png" width="300px"/> </br>
<img src="https://user-images.githubusercontent.com/55622345/165531946-0be01374-847f-4595-b62b-d022919c58ce.png" width="400px"/>

```xml
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/nav_graph"
    app:startDestination="@id/newsFragment">

    <fragment
        android:id="@+id/newsFragment"
        android:name="com.kmose.newsapiclient.NewsFragment"
        android:label="fragment_news"
        tools:layout="@layout/fragment_news" >
        <action
            android:id="@+id/action_newsFragment_to_infoFragment"
            app:destination="@id/infoFragment" />
    </fragment>
    <fragment
        android:id="@+id/savedFragment"
        android:name="com.kmose.newsapiclient.SavedFragment"
        android:label="fragment_saved"
        tools:layout="@layout/fragment_saved" >
        <action
            android:id="@+id/action_savedFragment_to_infoFragment"
            app:destination="@id/infoFragment" />
    </fragment>
    <fragment
        android:id="@+id/infoFragment"
        android:name="com.kmose.newsapiclient.infoFragment"
        android:label="fragment_info"
        tools:layout="@layout/fragment_info" />
</navigation>
```
</details>

그리고 아래와 같이 menu를 추가합니다. 
<details>
<summary>Menu</summary>
  
<img src="https://user-images.githubusercontent.com/55622345/165533056-fdf4cdf7-0a6e-4af6-a21c-7e4d13189d4d.png" width="250px"/> </br>
```
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- same id from fragment -->
    <item
        android:title="News Headlines"
        android:icon="@drawable/ic_news_headline_24"
        android:id="@+id/newsFragment"
        />
    <item
        android:id="@+id/savedFragment"
        android:title="Saved News"
        android:icon="@drawable/ic_saved_news_24"
        />

</menu>
```
각 menu의 item의 id는 연동될 fragment의 id와 동일하게 설정합니다. 
</details>
 
`activity_main.xml`에 `FragmentContainerView`와 `BottomNavigationView`를 아래와 같이 추가합니다. 
<details>
<summary>activity_main</summary>
  
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">


    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/fragmentContainerView"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="7"
        app:defaultNavHost="true"
        app:navGraph="@navigation/nav_graph" />
    
    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/btm_nav_news"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        app:menu="@menu/bottom_menu"
        />
</LinearLayout>
```
</details>
  
마지막으로 `MainActivity`에서 navigation을 연결합니다. 
```kotlin 
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        binding.btmNavNews.setupWithNavController(navController)
    }
}
```
  
## Layout - RecyclerView
헤드라인 리스트를 출력할 RecyclerView를 추가합니다. RecyclerView에 리스트로 출력될 아이템은 뉴스의 제목, 이미지, 내용, 발행일 그리고 신문사 이름입니다. 

### RecyclerView - layout
layout resource 폴더에 위 내용을 포함한 `news_list_item.xml`을 추가합니다.
<details>
<summary>news_list_item</summary>

`news_list_item.xml`를 추가하기 앞서 `colors.xml`에 아래 색들을 추가합니다.
```xml
    <color name="list_text">#FFFFFFFF</color>
    <color name="list_background">#2F2C2A</color>
    <color name="layout_background">#000000</color>
```
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="@color/list_background"
    android:layout_marginTop="10dp"
    >
    <TextView
        android:id="@+id/tvTitle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:ellipsize="end"
        android:maxLines="3"
        android:textColor="@color/list_text"
        android:textSize="15sp"
        android:textStyle="bold"
        android:layout_marginBottom="10sp"
        />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        >
        <ImageView
            android:id="@+id/ivArticleImage"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="2"
            />

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="3"
            android:orientation="vertical"
            android:layout_marginStart="10dp"
            >
            <TextView
                android:id="@+id/tvDescription"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:maxLines="5"
                android:text="DESCRIPTION"
                android:textColor="@color/list_text"
                android:textSize="15sp"
                android:layout_weight="3"
                />
            <TextView
                android:id="@+id/tvPublishedAt"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="@color/list_text"
                />

            <TextView
                android:id="@+id/tvSource"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:textColor="@color/list_text"
                />

        </LinearLayout>

    </LinearLayout>

</LinearLayout>
```
</details>

이제 `fragment_news.xml`에 RecyclerView를 추가하고 Retrofit의 응답이 Loading일 때를 위해 ProgressBar를 추가합니다. 
<details>
<summary>fragment_news</summary>
  
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/layout_background"
    tools:context=".NewsFragment">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rv_news"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="5dp"
        />
    <ProgressBar
        android:id="@+id/progress_bar"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:visibility="invisible"
        />
</LinearLayout>
```
</details>

### RecyclerView - Adapter
RecyclerView의 Layout이 준비되었으니 이제 Adapter를 만들어 `NewsFragment`에 적용하겠습니다. 

presentation layer에 adapter 패키지를 추가한 후 `NewsAdapter`클래스를 생성합니다. <br>
![image](https://user-images.githubusercontent.com/55622345/165896811-6a88af8a-b4f6-4ee9-a7c1-482cd372e370.png)
<details>
<summary>NewsAdapter</summary>
  
```kotlin
class NewsAdapter : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {
    private val callback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, callback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        var binding =  NewsListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = differ.currentList[position]
        holder.bind(article)
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class NewsViewHolder(val binding: NewsListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.tvTitle.text = article.title
            binding.tvDescription.text = article.description
            binding.tvPublishedAt.text = article.description
            binding.tvSource.text = article.publishedAt
            Glide.with(binding.ivArticleImage.context)
                .load(article.urlToImage)
                .into(binding.ivArticleImage)
        }
    }
}
```
응답된 뉴스 리스트를 비교하기 위해서 List를 비교하는 [`DiffUtil` 클래스](https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil#calculateDiff(androidx.recyclerview.widget.DiffUtil.Callback))를 사용하였습니다. 그리고 이미지 로딩을 위해 Gild를 적용하였습니다. 
```
    // Glide to app level's build.gradle
    implementation 'com.github.bumptech.glide:glide:4.13.0'
    kapt 'com.github.bumptech.glide:compiler:4.13.0'
```  
</details>

### init RecyclerView 
RecyclerView를 위한 Adapter도 준비가 끝났으니 이제 `NewsFragment`에 `NewsAdapter`를 추가합니다. 
  
<details>
<summary>NewsFragment</summary>

```kotlin
class NewsFragment : Fragment() {
    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var fragmentNewsBinding: FragmentNewsBinding
    private var country = "kr"
    private var page = 1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentNewsBinding = FragmentNewsBinding.bind(view)
        viewModel = (activity as MainActivity).viewModel
        initRecyclerView()
        viewNewsList()
    }

    private fun viewNewsList() {
        viewModel.getNewsHeadlines(country, page)
        viewModel.newsHeadlines.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let {
                        newsAdapter.differ.submitList(it.articles.toList())
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        Toast.makeText(activity, "An Error Occurred : $it", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
    }

    private fun initRecyclerView() {
        newsAdapter = NewsAdapter()
        fragmentNewsBinding.rvNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun showProgressBar() {
        fragmentNewsBinding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        fragmentNewsBinding.progressBar.visibility = View.INVISIBLE
    }
}
```
  
`onViewCreated`에 Adapter를 작성하여 View가 완전히 생성된 직후에 실행되는 함수로 [`onCreateView`에서 일어날 수 있는 초기화 에러](https://stackoverflow.com/questions/25119090/difference-between-oncreateview-and-onviewcreated-in-fragment)를 방지합니다
</details>

### ViewModel in MainActivity
`NewsFragment`에서 사용되는 ViewModel은 `(activity as MainActivity).viewModel`로 `MainActivity`에서 가져와 사용합니다. `MainActivity`에서 ViewModel을 생성하여 여러 Fragment에서 공유하여 사용함으로 Singleton과 같은 효과를 낼 수 있습니다. 
```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var factory: NewsViewModelFactory
    lateinit var viewModel: NewsViewModel
    ……
    override fun onCreate(savedInstanceState: Bundle?) {
        ……
        viewModel = ViewModelProvider(this, factory)[NewsViewModel::class.java]
    }
}
```

### DI - RecyclerView Adapter 
`NewsFragment`의 `initRecyclerView()`메소드에서 ``newsAdapter = NewsAdapter()``로 바로 생성자를 생성해서 받은 것을 DI형식으로 변경하겠습니다. 

DI를 위해서 우선 di 패키지에 Adapter의 Module을 생성합니다. 
```kotlin
@Module
@InstallIn(SingletonComponent::class)
class AdapterModule {
    @Singleton
    @Provides
    fun providesNewsAdapter(): NewsAdapter {
        return NewsAdapter()
    }
}
```
  
그리고 `MainActivity`에서 아래의 전역변수를 추가합니다.
  ```kotlin 
    @Inject
    lateinit var newsAdapter: NewsAdapter
  ```
마지막으로 `NewsFragment`에서 `newsAdapter = NewsAdapter()`를 제거한 후 `viewModel`을 추가한 방식과 같이 `newsAdapter`를 추가합니다. 
```
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentNewsBinding = FragmentNewsBinding.bind(view)
        viewModel = (activity as MainActivity).viewModel
        newsAdapter = (activity as MainActivity).newsAdapter
        initRecyclerView()
        viewNewsList()
    }
```  
  
## Paging With RecyclerView
JetPack에서 [Paging 라이브러리](https://developer.android.com/topic/libraries/architecture/paging/v3-overview)를 제공하지만 여기서는 수동으로 기본적인 페이징을 적용해보겠습니다. 
  
페이징을 적용하기 앞서 페이징이 언제 일어나는지, 어떻게 일어나는지 생각해봅시다. <br>
![image](https://user-images.githubusercontent.com/55622345/165933463-20db07b4-9547-4d4a-8291-d4148cf4faad.png)
한 번에 가져오는 뉴스의 갯수는 기본으로 20개 입니다. 스크롤을 끝까지 내렸을 때 마지막 기사의 위치가 20번째라면 다음 페이지로 넘기는 페이징 처리를 하면 될 것입니다. 
  
그렇다면 이제 페이징 처리를 위한 스크롤 리스너를 추가하겠습니다. 
### [RecyclerView.OnScrollListener](https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.OnScrollListener)
스크롤 이벤트를 위한 `RecyclerView.OnScrollListener`객체를 `NewsFragment`안에 생성합니다. 
```kotlin 
private val onScrollingListener = object : RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)  
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)    
    }
}
```
`OnScrollListener`는 스크롤 상태변화를 감지하는 `onScrollStateChanged`메소드와 스크롤이 되었을 때를 불러지는 `onScrolled`메소드가 있습니다.
  
각각의 매소드를 작성하기 전에 페이징에 필요한 전역변수들을 추가합니다.
```kotlin
    private var isScrolling = false
    private var isLoading = false
    private var isLastPage = false
    private var pages = 0
```
`isScrolling`은 스크롤중 여부를 반환하고, `isLoading은` `Resource`의 상태가 `Loading` 여부를 반환하고, `isLastPage`는 마지막 페이지인지 여부를 반환합니다. `pages`는 현재 리스트를 계산하여 페이지를 나타냅니다. 
  
그리고 Retrofit의 응답이 성공 했을 때 리스트 값을 계산하여 현재 페이지가 마지막인지 확인합니다. 
```kotlin
private fun viewNewsList() {
    ……
    is Resource.Success -> {
        hideProgressBar()
        response.data?.let {
            newsAdapter.differ.submitList(it.articles.toList())
            if (it.totalResults%20 == 0) {
                pages = it.totalResults / 20
            } else {
                pages = it.totalResults / 20 + 1
            }
            isLastPage = page == pages
        }
    }
    ……
}
```

`onScrollStateChanged`메소드에서 화면이 터치될 때 `isScrolling`의 값을 변경시켜줍니다. 
```kotlin
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            isScrolling = true
        }
    }
```

`onScrolled`메소드에서는 LayoutManger 객체를 가져와 현재 리스트의 크기, 보여지는 아이탬의 갯수와 맨위에 있는 아이템의 위치 값을 가져와서 리스트가 끝에 도달했는지 계산합니다. 
```kotlin
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val layoutManager = fragmentNewsBinding.rvNews.layoutManager as LinearLayoutManager
        val sizeOfTheCurrentList = layoutManager.itemCount
        val visibleItems = layoutManager.childCount
        val topPosition = layoutManager.findFirstVisibleItemPosition()

        val hasReachedToEnd = topPosition + visibleItems >= sizeOfTheCurrentList
        val shouldPaginate = !isLoading && !isLastPage && hasReachedToEnd && isScrolling
        if (shouldPaginate) {
            page ++
            viewModel.getNewsHeadlines(country, page)
            isScrolling = false
        }
    }
```
리스트가 끝까지 도달했다면 페이지네이션이 필요한지 확인후 페이지네이션을 실행합니다. 
  
<details>
<summary>Full-Code</summary>

```kotlin
class NewsFragment : Fragment() {
    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var fragmentNewsBinding: FragmentNewsBinding
    private var country = "kr"
    private var page = 1
    private var isScrolling = false
    private var isLoading = false
    private var isLastPage = false
    private var pages = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentNewsBinding = FragmentNewsBinding.bind(view)
        viewModel = (activity as MainActivity).viewModel
        newsAdapter = (activity as MainActivity).newsAdapter
        initRecyclerView()
        viewNewsList()
    }

    private fun viewNewsList() {
        viewModel.getNewsHeadlines(country, page)
        viewModel.newsHeadlines.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let {
                        newsAdapter.differ.submitList(it.articles.toList())
                        if (it.totalResults%20 == 0) {
                            pages = it.totalResults / 20
                        } else {
                            pages = it.totalResults / 20 + 1
                        }
                        isLastPage = page == pages
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        Toast.makeText(activity, "An Error Occurred : $it", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
    }

    private fun initRecyclerView() {
        fragmentNewsBinding.rvNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@NewsFragment.onScrollingListener)
        }
    }

    private fun showProgressBar() {
        isLoading = true
        fragmentNewsBinding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        isLoading = false
        fragmentNewsBinding.progressBar.visibility = View.INVISIBLE
    }

    private val onScrollingListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = fragmentNewsBinding.rvNews.layoutManager as LinearLayoutManager
            // get size, count, starting poision from layoutManager
            val sizeOfTheCurrentList = layoutManager.itemCount
            val visibleItems = layoutManager.childCount
            val topPosition = layoutManager.findFirstVisibleItemPosition()

            val hasReachedToEnd = topPosition + visibleItems >= sizeOfTheCurrentList
            val shouldPaginate = !isLoading && !isLastPage && hasReachedToEnd && isScrolling
            if (shouldPaginate) {
                page ++ 
                viewModel.getNewsHeadlines(country, page)
                isScrolling = false
            }
        }
    }

}
```
</details>

  
## Displaying Article - WebView
헤드라인을 선택하면 `WebView`를 이용해서 기사의 상세내용을 보여주는 화면을 작성하겠습니다. 
헤드라인을 선택하게 되면 선택된 기사의 내용 즉, `Article`데이터 클래스를 `InfoFragment`로 넘겨주어 프래그먼트 내에있는 `WebView`를 통해 뉴스를 출력하게 됩니다. <br>
  이를 위해 `RecyclerView`에는 `onClickListener`, `InfoFragment`내에는 `Webview`, `Navigation Graph`에는 전달 액션과 `argument`가 필요합니다. 
  
  우선 `Article`데이터 클래스가 전송될 수 있도록 `Serializable`을 상속시킵니다. 
```kotlin
data class Article(
    ……
) : Serializable
```
  

  이제 `NewsAdapter`에 `onClickListener`리스너를 생성합니다. 우선 리스너 객체를 생성한 뒤 setter로 외부에서 등록할 수 있도록 합니다. 
```kotlin
    private var onItemClickListener: ((Article)->Unit)? = null

    fun setOnItemClickListener(listener: (Article)->Unit) {
        onItemClickListener = listener
    }
```
ViewHolder 클래스 내부에서 item layout이 생성될 때 item마다 Listener가 등록될 수 있도록 `bind`함수 내부에 Listener를 등록합니다.
```kotlin
inner class NewsViewHolder(val binding: NewsListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(article: Article) {
        ……
        binding.root.setOnClickListener {
            onItemClickListener?.let {
                it(article)
            }
        }
    }
}
```
  
  
`NewsFragment`에서 RecyclerView Adapter에 Listener를 넘겨줍니다. 
```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    ……
    newsAdapter = (activity as MainActivity).newsAdapter
    newsAdapter.setOnItemClickListener {
        val bundle = Bundle().apply {
            putSerializable("selected_article", it)
        }
        findNavController().navigate( R.id.action_newsFragment_to_infoFragment, bundle)
    }
    initRecyclerView()
    viewNewsList()
}
```
위 `Budle`값을 `InfoFragment`에서 받기 위해 `nav_graph`에 아래와 같이 Argument를 추가합니다. <br>
![image](https://user-images.githubusercontent.com/55622345/166104877-a7767d8e-a36d-4189-96bb-52beac7e6beb.png)
```xml
    <fragment
        android:id="@+id/infoFragment"
        android:name="com.kmose.newsapiclient.InfoFragment"
        android:label="fragment_info"
        tools:layout="@layout/fragment_info" >
        <argument
            android:name="selected_article"
            app:argType="com.kmose.newsapiclient.data.model.Article" />
    </fragment>
```
  
  
기사의 상세 내용을 띄울 `InfoFragment`프래그먼트의 layout 파일인 `fragment_info.xml`에 `WebView`를 추가합니다. 
```kotlin
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".InfoFragment">

    <WebView
        android:id="@+id/wv_info"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
    />

</LinearLayout>
```

  
마지막으로 `Article`객체의 url로 `WebView`를 띄울 수 있게 `InfoFragment`를 수정합니다. 
```kotlin
class InfoFragment : Fragment() {
    private lateinit var fragmentInfoBinding: FragmentInfoBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentInfoBinding = FragmentInfoBinding.bind(view)
        val args: InfoFragmentArgs by navArgs()
        val article = args.selectedArticle
        fragmentInfoBinding.wvInfo.apply {
            webViewClient = WebViewClient()
            if(article.url != "") {
                loadUrl(article.url)
            }
        }
    }
}
```
`nav_graph.xml`에서 `<fragment@InfoFragment/>`에 `<argument/>`를 추가했기 때문에 `Bundle`로 전송한 데이터를 `InfoFragmentArgs`로 객체를 받습니다. 
 그리고 `WebViewClient`를 사용해서 `WebView`를 초기화 후 url 값을 확인하여 `WebView`에 전달하여 화면을 출력합니다. 
  
**※ CLEAR_TEXT_TRAFFIC ERROR ※** <br>
  `WebView`에서 페이지를 띄울 때 `CLEAR_TEXT_TRAFFIC ERROR`가 발생한다면 `Manifest`의 `<application/>`에 아래와 같이 추가합니다. 
  ```xml
<application
    ……
    android:usesCleartextTraffic="true">
```
  
## Searching View - Search Headlines 
뉴스의 헤드라인을 가져오는 `NewsFragement`에서 `SearchView`를 추가하여 뉴스 검색 기능을 추가하겠습니다. 

우선 `fragment_news.xml`에서 아래와 같이 `SearchView`를 추가합니다. 
```xml
<LinearLayout
    ……>
    <androidx.appcompat.widget.SearchView
        android:id="@+id/sv_news"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@color/search_background"
        />
    ……
</LinearLayout>
```

<details>
<summary>RetrofitService & RemoteDataSource & Repository & UseCase 수정</summary>

뉴스의 검색 기능은 NewsAPI의 요청 값에서 ![image](https://user-images.githubusercontent.com/55622345/166185182-3dea590e-4410-48d0-9370-0ea8d1767f9c.png)이 필요로 하므로 해당 값을 포함한 요청을 `NewsAPIService`인터페이스에 아래와 같이 추가하겠습니다.
```kotlin
interface NewsAPIService {
    ……
    @GET("/v2/top-headlines")
    suspend fun getSearchedTopHeadlines(
        @Query("country")
        country: String,
        @Query("q")
        searchQuery: String,
        @Query("page")
        page: Int,
        @Query("apiKey")
        apiKey: String = BuildConfig.NEWS_API
    ): Response<APIResponse>
}
```

다음은 `NewsAPIService`를 사용하는 `NewsRemoteDataSource`인터페이스와 구현 클래스에 검색 함수를 추가합니다. 
```kotlin
interface NewsRemoteDataSource {
    suspend fun getTopHeadlines(country:String, page:Int): Response<APIResponse>
    suspend fun getSearchedTopHeadlines(country:String, searchQuery:String, page:Int): Response<APIResponse>
}

class NewsRemoteDataSourceImpl … {
    ……
    override suspend fun getSearchedTopHeadlines(
        country: String,
        searchQuery: String,
        page: Int
    ): Response<APIResponse> {
        return newsAPIService.getSearchedTopHeadlines(country, searchQuery, page)
    }
}  
```
  
`NewsRemoteDataSource`를 수정했으므로 이제 `NewsRepository`를 수정합니다.
```kotlin
interface NewsRepository {
    ……
    suspend fun getSearchedNews(country:String, searchQuery: String, page:Int): Resource<APIResponse>
    ……
}

class NewsRepositoryImpl … {
    ……
    override suspend fun getSearchedNews(
        country: String,
        searchQuery: String,
        page: Int
    ): Resource<APIResponse> {
        return responseToResource(newsRemoteDataSource.getSearchedTopHeadlines(country, searchQuery, page))
    }
    ……
}
```

검색 기능이 수행되는 `GetSearchedNewsUseCase`를 수정합니다. 
```kotlin 
class GetSearchedNewsUseCase(private val newsRepository: NewsRepository) {
    suspend fun execute(country:String, searchQuery: String, page:Int): Resource<APIResponse> {
        return newsRepository.getSearchedNews(country, searchQuery, page)
    }
}
```
  
마지막으로 `NewsViewModel`과 `NewsViewModelFactory`를 수정합니다.
```kotlin
class NewsViewModel(
    private val app: Application,
    private val getNewsHeadlinesUseCase: GetNewsHeadlinesUseCase,
    private val getSearchedNewsUseCase: GetSearchedNewsUseCase
) : AndroidViewModel(app) {
    ……
    // saerch
    val searchedNews: MutableLiveData<Resource<APIResponse>> = MutableLiveData()

    fun searchNews(country: String, searchQuery: String, page: Int) = viewModelScope.launch {
        searchedNews.postValue(Resource.Loading())
        try {
            if (isNetWorkAvailable(app)) {
                val response = getSearchedNewsUseCase.execute(country, searchQuery, page)
                searchedNews.postValue(response)
            } else {
                searchedNews.postValue(Resource.Error("No Internet Connection"))
            }
        } catch (e: Exception) {
            searchedNews.postValue(Resource.Error(e.message.toString()))
        }
    }
}
```
ViewModel에서 `searchNews`함수는 헤드라인을 가져오는 `getNewsHeadlines`함수와 비슷하게 수행됩니다. 
```kotlin
class NewsViewModelFactory(
    private val app: Application,
    private val getNewsHeadlinesUseCase: GetNewsHeadlinesUseCase,
    private val getSearchedNewsUseCase: GetSearchedNewsUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewsViewModel(app, getNewsHeadlinesUseCase, getSearchedNewsUseCase) as T
    }
}
```
Factory에는 UseCase를 추가합니다. 
</details>

추가된 함수들이 의존성이 주입될 수 있도록 DI도 수정합니다.
<details>
<summary></summary>


### UseCaseModule
```kotlin
@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {
    @Singleton
    @Provides
    fun providesGetNewsHeadlinesUseCase(
        newsRepository: NewsRepository
    ): GetNewsHeadlinesUseCase {
        return GetNewsHeadlinesUseCase(newsRepository)
    }

    @Singleton
    @Provides
    fun providesGetSearchedNewsHeadlinesUseCase(
        newsRepository: NewsRepository
    ): GetSearchedNewsUseCase {
        return GetSearchedNewsUseCase(newsRepository)
    }
}
```
  
### FactoryModule
```kotlin
@Module
@InstallIn(SingletonComponent::class)
class FactoryModule {
    @Singleton
    @Provides
    fun providesViewModelFactory(
        app: Application,
        getNewsHeadlinesUseCase: GetNewsHeadlinesUseCase,
        getSearchedNewsUseCase: GetSearchedNewsUseCase
    ): NewsViewModelFactory {
        return NewsViewModelFactory(app, getNewsHeadlinesUseCase, getSearchedNewsUseCase)
    }
}
```
</details>

이제 `NewsFragment`에서 추가된 `SearchView`가 작동 되도록 검색 기능을 추가하겠습니다. 

우선 API의 응답을 받을 `viewSearchedNews`함수를 작성합니다. 
```kotlin
    fun viewSearchedNews() {
        viewModel.searchedNews.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let {
                        newsAdapter.differ.submitList(it.articles.toList())
                        pages = if (it.totalResults%20 == 0) {
                            it.totalResults / 20
                        } else {
                            it.totalResults / 20 + 1
                        }
                        isLastPage = page == pages
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.data?.let {
                        Toast.makeText(activity, "An Error Occurred : $it", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }

        }
    }
```
그리고 `SearchView`에 Listener를 추가하기 위한 함수 `setSearchView`를 작성합니다. 
```kotlin
    private fun setSearchView() {
        fragmentNewsBinding.svNews.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchNews("kr", query!!, page)
                viewSearchedNews()
                return false
            }

            // query를 입력할 때 invoked
            override fun onQueryTextChange(newText: String?): Boolean {
                MainScope().launch {
                    delay(1500)
                    viewModel.searchNews("kr", newText!!, page)
                    viewSearchedNews()
                }
                return false
            }
        })
        fragmentNewsBinding.svNews.setOnCloseListener(object : SearchView.OnCloseListener{
            override fun onClose(): Boolean {
                initRecyclerView()
                viewNewsList()
                return false
            }
        })
    }
```
`OnQueryTextListener`에서 `onQueryTextSubmit`는 검색후 `Enter`버튼을 눌렀을 때 실행되고, `onQueryTextChange`는 검색 값을 입력할 때 마다 실행되는 함수 입니다. `OnCloseListener`의 `onClose`는 검색창을 닫을 때 실행되는 함수입니다. 

이제 `setSearchView`함수를 `onViewCreated`함수에 추가합니다. 
```kotlin 
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ……
        initRecyclerView()
        viewNewsList()
        setSearchView()
    }
```
  
## Saved Articles
마지막으로 기사를 저장하고 저장된 기사 리스트를 출력하고 저장된 기사를 삭제하는 기능을 추가하겠습니다. 
  
  [Room Database](https://github.com/K-Mose/RoomDemo)를 이용하기위해 아래의 dependencies를 추가합니다. 
```xml
    def room_version = "2.4.1"
    implementation "androidx.room:room-ktx:$room_version"
    kapt "androidx.room:room-compiler:$room_version"
```

### Database & DAO
그리고 `Article`데이터 클래스에 @Entity 어노테이션을 추가합니다. 
```kotlin
@Entity(tableName = "articles")
data class Article(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    ……
)
```
  
data 패키지에 Room DataBase 연결 클래스들을 생성할 db 패키지를 생성합니다. <br>
  ![image](https://user-images.githubusercontent.com/55622345/166231477-12b96008-2f3f-46a8-b31c-1bf6a16820d5.png)  <br>
  db 패키지 안에 DAO 인터페이스와 RoomDatabase 클래스를 생성합니다. <br>
![image](https://user-images.githubusercontent.com/55622345/166231590-2b293e0b-e358-4020-afc2-778212f71261.png) <br>

`Article`데이터 클래스의 source는 `Source`데이터 클래스를 타입으로 갖는데 이 클래스를 Database의 Table로 생성하는 것 보다 `TypeConverter`를 생성하여 `Source`데이터 클래스의 인스턴스를 `Article`데이터 클래스에게 제공하는 것이 효율적이므로 `Converters`클래스 를 생성합니다.
```kotlin 
class Converters {
    @TypeConverter
    fun fromSource(source: Source):String {
        return source.name
    }

    @TypeConverter
    fun toSource(name: String): Source {
        return Source(name, name)
    }
}
```
  
  우선 Database의 DAO 객체를 작성하겠습니다. 
```kotlin
@Dao
interface ArticleDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: Article)

    @Query("SELECT * FROM articles")
    fun getAllArticles(): Flow<List<Article>>

    @Delete
    suspend fun deleteArticle(article: Article)
}
```  
`ArticleDAO`인테페이스는 [Repository](#repository--usecase)에서 작성된 것 처럼 기사를 가져오는 것은 `LiveData`를 Data Layer에서 사용하지 못하므로 `Flow`객체로 가져오고, 저장과 삭제는 코루틴을 이용하여 `suspend fun`로 작성합니다. 

  DAO를 작성했다면 Database 객체를 생성할 `ArticleDatabase`클래스를 생성합니다. 
```kotlin
@Database(
    entities = [Article::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ArticleDatabase : RoomDatabase() {
    abstract fun getArticleDAO(): ArticleDAO
}
```

### DataSource
Room의 Database클래스까지 추가했다면 이제 Local Data에 연결할 `LocalDataSource`인터페이스와 클래스를 추가합니다. <br>
![image](https://user-images.githubusercontent.com/55622345/166233637-184ab22c-4d25-458d-a469-7e78f18fed09.png) <br>

  `NewsLocalDataSource`인터페이스의 구현체가 `ArticleDAO`인테페이스에직접 연결되므로 같은 형식으로 작서합니다. 
```kotlin
interface NewsLocalDataSource {
    suspend fun saveArticleToDB(article: Article)
    fun getSavedArticles():Flow<List<Article>>
    suspend fun deleteArticleFromDB(article: Article)
}
```
  
  그리고 `NewsLocalDataSourceImpl`클래스 안에서 각각의 메소드가 (java코드로 자동으로 생성되어 의존성이 주입되는)`ArticleDAO`(구현체)에 접근하여 실행되므로 각각 DAO의 메소드에 맞게 구현합니다. 
<details>
<summary>ArticleDAO_impl</summary>

  ![image](https://user-images.githubusercontent.com/55622345/166234100-93673f2e-b1ff-4a50-b3cd-fdd1fc108b4d.png)
```kotlin
public final class ArticleDAO_Impl implements ArticleDAO {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Article> __insertionAdapterOfArticle;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<Article> __deletionAdapterOfArticle;

  public ArticleDAO_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfArticle = new EntityInsertionAdapter<Article>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `articles` (`id`,`author`,`content`,`description`,`publishedAt`,`source`,`title`,`url`,`urlToImage`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Article value) {
        if (value.getId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindLong(1, value.getId());
        }
        if (value.getAuthor() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getAuthor());
        }
        if (value.getContent() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getContent());
        }
        if (value.getDescription() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getDescription());
        }
        if (value.getPublishedAt() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getPublishedAt());
        }
        final String _tmp = __converters.fromSource(value.getSource());
        if (_tmp == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, _tmp);
        }
        if (value.getTitle() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getTitle());
        }
        if (value.getUrl() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.getUrl());
        }
        if (value.getUrlToImage() == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, value.getUrlToImage());
        }
      }
    };
    this.__deletionAdapterOfArticle = new EntityDeletionOrUpdateAdapter<Article>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `articles` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Article value) {
        if (value.getId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindLong(1, value.getId());
        }
      }
    };
  }

  @Override
  public Object insert(final Article article, final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfArticle.insert(article);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object deleteArticle(final Article article,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfArticle.handle(article);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Flow<List<Article>> getAllArticles() {
    final String _sql = "SELECT * FROM articles";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[]{"articles"}, new Callable<List<Article>>() {
      @Override
      public List<Article> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfPublishedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "publishedAt");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
          final int _cursorIndexOfUrlToImage = CursorUtil.getColumnIndexOrThrow(_cursor, "urlToImage");
          final List<Article> _result = new ArrayList<Article>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Article _item;
            final Integer _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getInt(_cursorIndexOfId);
            }
            final String _tmpAuthor;
            if (_cursor.isNull(_cursorIndexOfAuthor)) {
              _tmpAuthor = null;
            } else {
              _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            }
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpPublishedAt;
            if (_cursor.isNull(_cursorIndexOfPublishedAt)) {
              _tmpPublishedAt = null;
            } else {
              _tmpPublishedAt = _cursor.getString(_cursorIndexOfPublishedAt);
            }
            final Source _tmpSource;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfSource);
            }
            _tmpSource = __converters.toSource(_tmp);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpUrl;
            if (_cursor.isNull(_cursorIndexOfUrl)) {
              _tmpUrl = null;
            } else {
              _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            }
            final String _tmpUrlToImage;
            if (_cursor.isNull(_cursorIndexOfUrlToImage)) {
              _tmpUrlToImage = null;
            } else {
              _tmpUrlToImage = _cursor.getString(_cursorIndexOfUrlToImage);
            }
            _item = new Article(_tmpId,_tmpAuthor,_tmpContent,_tmpDescription,_tmpPublishedAt,_tmpSource,_tmpTitle,_tmpUrl,_tmpUrlToImage);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}  
```
</details>



```kotlin
class NewsLocalDataSourceImpl(
    private val articleDAO: ArticleDAO
) : NewsLocalDataSource {
    override suspend fun saveArticleToDB(article: Article) {
        articleDAO.insert(article)
    }

    override fun getSavedArticles(): Flow<List<Article>> {
        return articleDAO.getAllArticles()
    }

    override suspend fun deleteArticleFromDB(article: Article) {
        articleDAO.deleteArticle(article)
    }
}
```

### Repository 
[Repository](#repository--usecase)에서 작성된 `NewsRepository`인터페이스의 구현체에 기사 저장 함수들을 추가합니다. 
```kotlin
class NewsRepositoryImpl(
    private val newsRemoteDataSource: NewsRemoteDataSource,
    private val newsLocalDataSource: NewsLocalDataSource
) : NewsRepository {
    ……
    override suspend fun saveNews(article: Article) {
        newsLocalDataSource.saveArticleToDB(article)
    }

    override suspend fun deleteNews(article: Article) {
        newsLocalDataSource.deleteArticleFromDB(article)
    }

    override fun getSavedNews(): Flow<List<Article>> {
        return newsLocalDataSource.getSavedArticles()
    }
}
``` 
  

  
## Save Article
기사를 저장하기 앞서 `InfoFragment`의 Layout에 저장 버튼을 추가합니다. 
<details>
<summary>fragment_info.xml</summary>
  
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:orientation="vertical"
    tools:context=".InfoFragment">

    <WebView
        android:id="@+id/wv_info"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
    />

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab_save"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@drawable/ic_saved_news_24"
        app:layout_constraintVertical_bias="0.95"
        app:layout_constraintHorizontal_bias="0.95"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        />

</androidx.constraintlayout.widget.ConstraintLayout>
```
</details>

그리고 `NewsRepository`와 연결할 [`SaveNewsUseCase`](#UseCase)는 이미 구현되어 있으니 Fragment를 작성합니다. 
`InfoFragment`에서  전역 변수로 `private lateinit var viewModel: NewsViewModel`를 추가하여 ViewModel을 통해 UseCase에 접근 할 수 있도록 합니다. 
그리고 `FloatingActionButton`버튼의 리스너를 추가합니다. 
```kotlin 
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentInfoBinding = FragmentInfoBinding.bind(view)
        viewModel = (activity as MainActivity).viewModel
        ……
        fragmentInfoBinding.fabSave.setOnClickListener {
            viewModel.saveArticle(article)
            Snackbar.make(view, "Saved Successfully", Snackbar.LENGTH_LONG).show()
        }
    }
```

  
  
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

