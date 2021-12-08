# 9장 더 큰 설계 문제
- 지금까지 작성 했던 코드를 더 큰 설계 관점에 대해서 생각해본다.
- 단일 책임 원칙, CQRS 관점 에서도 살펴본다.

## Profile 클래스와 SRP

```java
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class Profile {
    private Map<String, Answer> answers = new HashMap<>();
    private int score;
    private String name;

    public Profile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void add(Answer answer) {
        answers.put(answer.getQuestionText(), answer);
    }

    public boolean matches(Criteria criteria) {
        calculateScore(criteria);
        if (doesNotMeetAnyMustMatchCriterion(criteria)) {
            return false;
        }
        return anyMatches(criteria);
    }

    private boolean doesNotMeetAnyMustMatchCriterion(Criteria criteria) {
        for (Criterion criterion : criteria) {
            boolean match = criterion.matches(answerMatching(criterion));
            if (!match && criterion.getWeight() == Weight.MustMatch) {
                return true;
            }
        }
        return false;
    }

    private void calculateScore(Criteria criteria) {
        score = 0;
        for (Criterion criterion : criteria) {
            if (criterion.matches(answerMatching(criterion))) {
                score += criterion.getWeight().getValue();
            }
        }
    }

    private boolean anyMatches(Criteria criteria) {
        boolean anyMatches = false;
        for (Criterion criterion : criteria) {
            anyMatches |= criterion.matches(answers.answerMatching(criterion));
        }
        return anyMatches;
    }

    private Answer answerMatching(Criterion criterion) {
        return answers.get(criterion.getAnswer().getQuestionText());
    }

    public List<Answer> find(Predicate<Answer> predicate) {
        return answers.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());;
    }
}
```
- 전체코드가 100줄이 안되고, Profile 클래스는 과하게 크거나 복잡해보이진 않는다.
- 하지만 이상적인 설계는 아니다.
- 그 이유는 Profile 이 가지는 책임이 많기 때문이다.
    1. **회사/인물 정보** 를 추적 및 관리한다.
    2. 조건 집합이 프로파일과 매칭되는지 여부/점수를 계산한다.
- 즉 Profile 클래스는 SRP 를 위반하고 있다.
  - SRP 는 클래스를 변경하는 이유는 단 한가지여야함을 의미한다.
- 클래스에 많은 책임이 존재할수록 클래스에 있는 코드를 수정할 때 기존 다른 동작을 깨기 쉽다.
- 더 작고 응집된 클래스를 만드는것이 중요하다.

## 새로운 클래스 추출
- 현재 Profile 클래스가 가진 책임을 정리하면 다음과 같다.
  - 프로파일 관련 정보 관리
  - 조건 집합이 프로파일에 매칭되는지 혹은 점수 계산
- 위 두 책임을 분리하여 각 클래스로 할당하고 SRP 를 준수하도록 리팩토링 한다.
- 조건 집합이 프로파일에 **매칭** 되는지 에 대한 책임을 갖는 클래스를 MatchSet 으로 추출한다.

```java
import java.util.Map;

class MatchSet {
    private Map<String, Answer> answers;
    private Criteria criteria;
    private int score = 0;

    public MatchSet(Map<String, Answer> answers, Criteria criteria) {
        this.answers = answers;
        this.criteria = criteria;
        calculateScore(criteria);
    }

    public int getScore() {
        return score;
    }

    public boolean matches() {
        if (doesNotMeetAnyMustMatchCriterion()) {
            return false;
        }
        return anyMatches();
    }

    private void calculateScore() {
        score = 0;
        for (Criterion criterion : criteria) {
            if (criterion.matches(answerMatching(criterion))) {
                score += criterion.getWeight().getValue();
            }
        }
    }

    private Answer answerMatching(Criterion criterion) {
        return answers.get(criterion.getAnswer().getQuestionText());
    }

    private boolean doesNotMeetAnyMustMatchCriterion() {
        for (Criterion criterion : criteria) {
            boolean match = criterion.matches(answers.answerMatching(criterion));
            if (!match && criterion.getWeight() == Weight.MustMatch) {
                return true;
            }
        }
        return false;
    }
}
```
- MatchSet 클래스는, 프로파일의 매칭여부와, 매칭되는 정도의 점수 계산의 책임을 가지고 있다.

## CQRS
- MatchSet 을 활용한 리팩토링 이후 Profile 클래스의 matches 메소드를 보면 다음과 같다.

```java
class Profile {
    // ...
    public boolean matches(Criteria criteria) {
        /**
         * MatchSet 으로 추출한 뒤 score 필드에 값을 저장하는것 자체가 괴리감이 있다.
         * score 를 얻어오려면 matches 를 계산해야 한다. CQRS 위반
         */
        MatchSet matchSet = new MatchSet(answers, criteria);
        score = matchSet.getScore();
        return matchSet.matches();
    }
}
```
- matches() 로 매칭 여부를 판단하는데, MatchSet 객체를 활용해 **score 필드에 점수 계산결과를 저장** 하는 행위는 괴리감이 있다.
- score 를 얻으려면 matches 를 호출해야한다.
- 반대로 매칭 여부를 판단하는데, 점수 계산까지 발생하게 된다. (이는 함수의 부작용에 해당한다.)
- 어떤 값을 반환하면서 부작용까지 발생시키는 메소드는 CQRS (명령-질의 분리) 원칙을 위반한다.
- 이 원칙은, 어떤 메소드는 명령을 실행하거나, 질의하거나 둘중 한 작업만을 수행함을 의미한다.
- 이를 위반하는 예는 java.util.Iterator
- next() 는 다음 객체를 가리키고, 현재 객체 포인터를 증가시킨다.
- 이 문제를 해결하기 위해 **클라이언트가 원할때 MatchSet 객체를 다루도록 변경** 한다.

```java
import java.util.HashMap;
import java.util.Map;

class Profile {
    private Map<String, Answer> answers = new HashMap<>();
    private String name;
    
    public Profile(String name) {
        this.name = name;
    }

    public void add(Answer answer) {
        answers.put(answer.getQuestionText(), answer);
    }

    /**
     * 클라이언트가 원할때 MatchSet 객체를 다루도록 MatchSet 객체를 반환하는 메소드를 추가한다.
     */
    public MatchSet getMatchSet(Criteria criteria) {
        return new MatchSet(answers, criteria);
    }

    public List<Answer> find(Predicate<Answer> predicate) {
        return answers.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }
}
```
- 그리고 score 메소드와 score 필드까지 제거한다. 리팩토링 결과 SRP 를 준수하는 좋은 예제가 되었다.

## 단위 테스트의 유지보수 비용
- Profile 인터페이스를 변경하면서 기존의 ProfileTest 메소드가 깨지게 되었다.
- 먼저 이것을 고치도록 노력해야 하며 이는 단위 테스트를 소유하는 비용에 해당한다.
- 더 나아가 실패하는 테스트의 정보를 부정적인 설계 지표로 인식하는 것도 생각해봐야한다.
- 더 많은 테스트가 동시에 깨질수록 더욱 더 많은 설계문제가 있을 것 이라는 신호이다.

## 자신을 보호하는 방법
- 코드 중복은 가장 큰 설계 문제중 하나이다.
- 테스트 관점에서 여러 테스트에 걸친 코드 중복은 문제가 있다.
  - 테스트를 따르기가 어려워 진다.
  - 작은 코드 조각들을 단일 메소드로 추출해 코드 조각을 한군데로 모아야 한다.
- 단위 테스트를 준비하기 위한 코드가 수십줄이 필요하다면 그것은 시스템 설계에 문제가 있을 수도 있다는 신호이다.
- SRP 를 위반하면 클래스는 점점 커지고 테스트시에도 더 많은 노력이 요구된다.
  - 큰 클래스를 분할해야 한다.
- private 메소드 (세부 구현) 을 테스트할 필요가 있다면 그것은 클래스가 필요 이상으로 커졌다는 의미이다.
- private 메소드가 자꾸 늘어난다면, 새로운 클래스를 추출하고 public 으로 만드는 것이 좋다.

> 설계를 개선해 단위 테스트를 쉽게 만드는 것이 중요하다. 코드 품질이 낮을수록 단위 테스트 비용이 커진다.

## 다른 설계에 대한 고민들
- MatchSet 클래스의 생성자는 **점수를 계산하는 작업** 을 수행한다.
- 계산된 점수를 클라이언트가 사용하지 않는다면, 그것은 낭비가 될 것이다.
  - 생성자에서는 실질적인 작업을 피해야 한다.

```java
public class MatchSet {

  private AnswerCollection answers;
  private Criteria criteria;

  public MatchSet(AnswerCollection answers, Criteria criteria) {
    this.answers = answers;
    this.criteria = criteria;
    // MatchSet 생성자에서 score 를 계산하고 있다.
    // 이 점수를 클라이언트가 사용하지 않는다면 상당한 낭비가 된다.
    // 생성자에서 실질적인 작업을 피해야 함
//        calculateScore();
  }

  /**
   * 점수를 요청할때 지연계산 하도록 변경
   */
  public int getScore() {
    int score = 0;
    for (Criterion criterion : criteria) {
      if (criterion.matches(answers.answerMatching(criterion))) {
        score += criterion.getWeight().getValue();
      }
    }
    return score;
  }
  // ...
}
```
- score 필드도 사라지고 calculateScore 메소드는 getScore 안으로 인라이닝 되었다.
- 만약 메소드 호출시 계산을 하는것이 성능 저하로 문제가 된다면 LAZY-INIT 을 사용할 수도 있다.
- 기존 구현을 보면 Answer 컬렉션을 다루는 부분이 여기저기에 산재되어 있다.
- 어떻게 답변을 찾고, 점수를 구하는지에 대한 정보를 너무 많이 알고 있다는 의미.
- 해당 컬렉션을 인메모리가 아닌 DB 로 변경한다거나 하는 일이 생긴다면 여러군데를 고쳐야 한다.
  - 또한 데이터 상태에도 혼란이 올 수 있다.
- `AnswerCollection` 이라는 클래스로 이를 추출하여 해결한다.

```java
/**
 * Profile 객체가 질문의 내용을 키로 갖는 Map<String, Answer> 객체를 생성한다.
 * 또한 이를 MatchSet 으로 넘긴다. 즉 답변 객체를 다루는 방법이 여기저기에 혼재되어 있다.
 * 어떻게 답변을 탐색하고, 점수를 구하는지에 대한 정보를 너무 많이 가지고 있다.
 * 여러 클래스에 구현의 상태가 흩어져 있을때의 코드 냄새를 기능의 산재라고 한다.
 * 만약 인메모리가 아닌, DB 로 통해 관리하게 되는등 작업이 필요해 진다면 고칠 부분이 너무 많아진다.
 * 때문에 AnswerCollection 이라는 답변 저장소를 분리한다.
 */
public class AnswerCollection {

    private Map<String, Answer> answers = new HashMap<>();

    public void add(Answer answer) {
        answers.put(answer.getQuestionText(), answer);
    }

    public Answer answerMatching(Criterion criterion) {
        return answers.get(criterion.getAnswer().getQuestionText());
    }
}
```

## 정리
- SRP 와 CQRS 와 같은 커다란 설계 원칙을 기반으로 설계를 개선하는데 집중해야 한다.
- 설계에 관한 "작은" 개념들과 작은 코드 리팩토링이 어떻게 커다란 차이를 만들어 내는지도 이해해야 한다.
- 유연한 설계는 더 작고 잘 조직된 구성요소로 시작된다.