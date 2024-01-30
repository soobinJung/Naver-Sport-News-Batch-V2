# Spring Batch 에러 대응 전략

## 서론
#### 실무에서 Spring Batch를 적용하며 경험한 문제 해결 과정을 공유합니다. 피드백은 언제나 환영입니다.

## 개발 전략의 초기 가정
- Read와 Write 작업이 정상적으로 수행되면 충분하다고 생각했습니다.
  
## 에러 처리의 필요성
실제로 배치 작업은 예상치 못한 에러들로 인해 복잡해질 수 있습니다. 예측 가능한 예외 상황을 미리 고려하지 않으면, 처리 과정에서 문제가 발생할 수 있습니다. 이는 Step과 Job 수준에서도 마찬가지입니다.


## 문제 상황
개발 초기 단계에서 예외 처리에 대한 충분한 고려 없이 개발을 진행했습니다. 이후 실제로 어떤 예외들이 발생했는지에 대해 논의합니다.

#### 개발된 프로세스 로직
1. 사용자가 대용량 Excel 파일을 업로드합니다.
2. Spring Batch를 통해 Excel 파일을 읽어 데이터베이스에 저장합니다.

#### 테스트 과정에서 발견된 에러
DataIntegrityViolationException: 글자수 제한이 없어 데이터 무결성 위반 예외가 발생합니다.

### 에러 처리 방안

#### 요구사항
- 에러가 발생한 데이터는 제외하고, 나머지 데이터는 정상적으로 저장되어야 합니다.
- 에러가 발생한 데이터와 예외 메시지를 별도의 에러 관련 데이터베이스 테이블에 기록하고자 합니다.

#### 해결 방안
- SkipPolicy: 에러가 발생할 경우 스킵할 데이터를 정의합니다.
```
@Component
public class SoobinSkipPolicy implements SkipPolicy {

    @Override
    public boolean shouldSkip(Throwable throwable, long skipCount) throws SkipLimitExceededException {

        throwable.printStackTrace();

        if(throwable instanceof DataIntegrityViolationException){
            return true;
        }

        return false;
    }
}
```
  
- SkipListener: 스킵된 데이터에 대한 후속 작업을 수행합니다. 예를 들어, 에러 로깅이나 대체 처리 등을 말합니다.

```  
@Component
@RequiredArgsConstructor
public class SoobinSkipListener<T, S> implements SkipListener<T, S> {

    private final SoobinErrorRepository soobinErrorRepository;

    @Override
    public void onSkipInRead(Throwable t) {
        soobinErrorRepository.save(
            SoobinDataUploadError.builder()
                    .errorType("READ")
                    .errorMsg(t.getMessage())
                    .build()
        );
        soobinErrorRepository.flush();
    }

    @Override
    public void onSkipInWrite(S item, Throwable t) {
        soobinErrorRepository.save(
                SoobinDataUploadError.builder()
                        .errorType("WRITE")
                        .errorObj(item.toString())
                        .errorMsg(t.getMessage())
                        .build()
        );
        soobinErrorRepository.flush();
    }
}
```
