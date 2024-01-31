# Spring Batch 에러 대응 전략

<br/>

### 기존의 배치 로직

![image](https://github.com/soobinJung/Naver-Sport-News-Batch-V2/assets/66097044/87927ffd-dd0c-4b71-a3c2-00136c8100f5)

- 기본 로직을 위와 같이 구성하였습니다.


![image](https://github.com/soobinJung/Naver-Sport-News-Batch-V2/assets/66097044/93363fad-5bd9-4636-b2f1-f1eeb89c5cbc)

- Chunk 를 이용해 데이터를 분할하여 처리합니다.


<br/><br/>

### 마주친 에러 상황

![image](https://github.com/soobinJung/Naver-Sport-News-Batch-V2/assets/66097044/faf18928-4362-44b1-b8b9-fa9b9ef802d6)

- 기존에 글자 수에 대한 유효성 처리를 할 수도 있었지만 만약에를 가정하여 설명을 진행하겠습니다.

<br/><br/>

### 어떤식으로 대응을 하고 싶었냐면요

- chunk 3 개에 대한 처리 중에 1 개에 데이터에서 에러가 발생을 한다면
    - 정상 데이터 2개는 정상 처리 되도록 하고 싶었습니다.
    - 에러 데이터 1개는 특정 테이블에 에러 정보를 저장해놓고 싶었습니다.  

 
<br/><br/>

### 같은 에러는 보지 않도록 예외 처리하기

- 에러 데이터는 skip 하도록 **SkipPolicy** 를 구현하였습니다.
  
```
@Component
public class NaverSportNewsTitleSkipPolicy implements SkipPolicy {

    @Override
    public boolean shouldSkip(Throwable throwable, long skipCount) throws SkipLimitExceededException {

        if(throwable instanceof DataIntegrityViolationException){
            return true;
        }

        return false;
    }
}
```
<br/>

- 에러 데이터는 특정 테이블에 저장할 수 있도록 **SkipListener** 를 구현하였습니다.


```
@Component
@RequiredArgsConstructor
public class NaverSportNewsTitleSkipListener<T, S> implements SkipListener<T, S> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void onSkipInRead(Throwable t) {
        jdbcTemplate.update("INSERT INTO NAVER_SPORT_NEWS_TITLE_ERROR (error) VALUES(?)", t.getMessage());
    }

    @Override
    public void onSkipInWrite(S item, Throwable t) {
        jdbcTemplate.update("INSERT INTO NAVER_SPORT_NEWS_TITLE_ERROR (error) VALUES(?)", t.getMessage());
    }
}
```
