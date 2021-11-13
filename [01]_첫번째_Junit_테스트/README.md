# 1장 첫 번째 Junit 테스트 만들기

## JUnit 의 기본 : 첫 번째 테스트
- 첫 예제에서는 `ScoreCollection` 이라는 클래스를 테스트한다.
  - Scoreable 객체 컬렉션의 평균을 반환한다.
  - Scoreable 객체는 점수를 가지고 있다.

`Scoreable`

```java
@FunctionalInterface
public interface Scoreable {
    int getScore();
}
```

`ScoreCollection`

```java
public class ScoreCollection {

    private List<Scoreable> scores = new ArrayList<>();

    public void add(Scoreable scoreable) {
        scores.add(scoreable);
    }

    public int arithmeticMean() {
        int total = scores.stream()
            .mapToInt(Scoreable::getScore)
            .sum();
        return total / scores.size();
    }
}
```

`ScoreCollectionTest`

```java
class ScoreCollectionTest {

    @Test
    void test() {
        fail("Not yet implemented");
    }
}
```
- fail(); 메소드는 AssertionFailedError 를 발생시키며 테스트를 실패시킨다.

## 테스트 준비, 실행, 단언
- ScoreCollection 에 대해 테스트 코드를 작성한다.
- 타깃 코드에 대해 기대 행동을 제공하는 **테스트 케이스 (test case)**
- 테스트 케이스 -> 5 + 7 을 더하면 6을 반환하는지 확인한다.

```java
class ScoreCollectionTest {

    @Test
    void answersArithmeticMeanOfTwoNumbers() {
        // given
        ScoreCollection collection = new ScoreCollection();
        collection.add(() -> 5);
        collection.add(() -> 7);

        // when
        int actualResult = collection.arithmeticMean();

        // then
        assertEquals(6, actualResult);
    }
}
```
- given, when, then 3 가지 구문으로 구성한다.
- given : 테스트의 상태를 설정하는 **준비** 단계
  - ScoreCollection 인스턴스를 생성하고, add 메소드를 2번 호출한다.
- when : 테스트 준비 후 검증하려는 코드인 arithmeticMean 메소드 **실행 (act)** 단계
- then : 기대하는 결과 **단언 (assert)** 단계 

> 실패한 단언 문은 오류를 보고하는것 이상의 일을 한다. <br/>
> JUnit 에서 런타임 예외를 던져 테스트를 중단시킴 <br/>

## 테스트가 정말 뭔가를 테스트하는가 ?
- 항상 그 테스트가 **실패** 하는지 확인하라.
- 의도하지 않게 우리가 생각하는 것을 실제로 검증하지 않고 품이 많이 드는 테스트를 작성할 수도 있다.
- TDD 를 따르는 개발자들은 항상 **실패하는 테스트** 를 먼저 작성한다.

> 테스트가 정상적으로 동작하는지 증명하기 위해 테스트를 실패해 보라.

## 정리
- ScoreCollection 클래스에 대한 한 가지 테스트를 작성해 보았다.
- 테스트를 분석해보고 생각해 볼 만한 점 두가지
  - 코드가 정상동작하는지 확신하기 위해 추가적인 테스트를 작성할 필요가 있는가 ?
  - 클래스에서 결함/한계점을 드러낼 수 있는 테스트를 작성할 수 있을까 ?