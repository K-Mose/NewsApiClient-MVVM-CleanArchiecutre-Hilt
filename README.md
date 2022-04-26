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
