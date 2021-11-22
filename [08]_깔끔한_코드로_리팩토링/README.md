# 8장 깔끔한 코드로 리팩토링

- 유지보수에 있어 코드를 이해하는 비용 또한 상당하다.
- 깔끔하고 좋은 구조를 갖춘 코드는 10분이면 변경할 수 있다.
- 하지만 그 반대의 경우라면 몇 시간이 필요하다.
- 낮은 중복성과 높은 명확성 이라는 두 가지 목표를 합리적인 비용과 놀라운 투자 수익률 (ROI) 로 달성할 수 있다.

## 작은 리팩토링

- 리팩토링을 위해 마음대로 코드 구조를 바꾸는 것은 위험한 일이다.
- 이를 위한 적절한 보호장치가 필요한데, 그것이 바로 테스트 코드이다.

`리팩토링의 기회`

```java
public boolean matches(Criteria criteria){
    score=0;

    boolean kill=false;
    boolean anyMatches=false;
    for(Criterion criterion:criteria){
    Answer answer=answers.get(
    criterion.getAnswer().getQuestionText());
    boolean match=
    criterion.getWeight()==Weight.DontCare||
    answer.match(criterion.getAnswer());

    if(!match&&criterion.getWeight()==Weight.MustMatch){
    kill=true;
    }
    if(match){
    score+=criterion.getWeight().getValue();
    }
    anyMatches|=match;
    }
    if(kill){
    return false;
    }
    return anyMatches;
    }
```

- Profile 클래스의 핵심메소드인 matches 를 살펴보자.
- 메소드의 구현은 크게 길진 않지만 빽빽하고 꽤나 많은 로직이 들어있어 가독성이 좋지 않다.
- 이는 리팩토링의 대상이라는 신호

`메소드 추출`

- 리팩토링의 가장 중요한 일은 이름 짓기
    - 클래스, 메소드, 변수
- 명확성은 코드 의도를 선언한느 것이고, 좋은 이름은 코드 의도를 전달하는 가장 좋은 수단이다.
- matches() 메소드의 복잡도를 줄여 코드가 무엇을 하는지 쉽게 이해하도록 만들어야 한다.
- 조건문의 경우 세부 로직이 많을수록 잘 읽히지가 않는다.
- 이를 메소드 추출로 개선한다.

```java
class Profile {

    public boolean matches(Criteria criteria) {
        score = 0;

        boolean kill = false;
        boolean anyMatches = false;
        for (Criterion criterion : criteria) {
            Answer answer = answers.get(
                criterion.getAnswer().getQuestionText());
            boolean match = matches(criterion, answer);

            if (!match && criterion.getWeight() == Weight.MustMatch) {
                kill = true;
            }
            if (match) {
                score += criterion.getWeight().getValue();
            }
            anyMatches |= match;
        }
        if (kill) {
            return false;
        }
        return anyMatches;
    }

    /**
     * 리팩토링 메소드 추출
     */
    private boolean matches(Criterion criterion, Answer answer) {
        return criterion.getWeight() == Weight.DontCare ||
            answer.match(criterion.getAnswer());
    }
}
```

## 메소드를 위한더 좋은집 찾기

- 반복문이 메소드 추출로 인해 이전보다 가독성이 향상되었다.
- 하지만 새롭게 추출한 matches 는 Profile 클래스와는 관계가 없다.
- 이는 Criterion 클래스에 있는것이 좀 더 적절해 보인다.
    - Criterion 객체는 Answer 객체를 이미 알고 있다.
    - Answer 객체는 Criterion 객체를 모른다.
    - Answer 객체에 메소드를 두는것은 적절하지 않다. (양방향 의존관계가 된다.)

```java
public class Criterion implements Scoreable {

    // ...

    /**
     * 리팩토링 올바른 위치로 이동
     */
    public boolean matches(Answer answer) {
        return this.weight == Weight.DontCare ||
            answer.match(this.answer);
    }
}
```

```java
class Profile {

    public boolean matches(Criteria criteria) {
        score = 0;

        boolean kill = false;
        boolean anyMatches = false;
        for (Criterion criterion : criteria) {
            Answer answer = answers.get(
                criterion.getAnswer().getQuestionText());
            boolean match = criterion.matches(answer);

            if (!match && criterion.getWeight() == Weight.MustMatch) {
                kill = true;
            }
            if (match) {
                score += criterion.getWeight().getValue();
            }
            anyMatches |= match;
        }
        if (kill) {
            return false;
        }
        return anyMatches;
    }
}
```

- 개선된 코드는 이전보다 나아졌지만 한가지 걸리는 점이 있다.
    - Answer 타입의 변수를 할당하는 부분
    - 이는 디미터의 법칙을 위반하고 있고 깔끔하지도 않다.

```java
class Profile {

    public boolean matches(Criteria criteria) {
        score = 0;

        boolean kill = false;
        boolean anyMatches = false;
        for (Criterion criterion : criteria) {
            Answer answer = answerMatching(criterion);
            boolean match = criterion.matches(answer);

            if (!match && criterion.getWeight() == Weight.MustMatch) {
                kill = true;
            }
            if (match) {
                score += criterion.getWeight().getValue();
            }
            anyMatches |= match;
        }
        if (kill) {
            return false;
        }
        return anyMatches;
    }

    /**
     * 디미터의 법칙 (이를 메소드로 추출해서 가독성을 향상)
     */
    private Answer answerMatching(Criterion criterion) {
        return answers.get(criterion.getAnswer().getQuestionText());
    }
}
```

## 자동 및 수동 리팩토링

- 리팩토링한 코드를 살펴보면 answer 라는 지역변수는, 코드의 명확성을 높이지 않으며 한 번만 사용한다.
- 이를 제거하고 인라이닝 한다.

```java
class Profile {

    public boolean matches(Criteria criteria) {
        score = 0;

        boolean kill = false;
        boolean anyMatches = false;
        for (Criterion criterion : criteria) {
            boolean match = criterion.matches(answerMatching(criterion));

            if (!match && criterion.getWeight() == Weight.MustMatch) {
                kill = true;
            }
            if (match) {
                score += criterion.getWeight().getValue();
            }
            anyMatches |= match;
        }
        if (kill) {
            return false;
        }
        return anyMatches;
    }

    /**
     * 디미터의 법칙 (이를 메소드로 추출해서 가독성을 향상)
     */
    private Answer answerMatching(Criterion criterion) {
        return answers.get(criterion.getAnswer().getQuestionText());
    }
}
```

- matches() 메소드의 세부 사항을 제거 했기에 고수준의 정책을 쉽게 이해할 수 있다.
- 메소드의 핵심 목표는 다음 세가지 이다.
    - 매칭되는 조건의 **가중치를 합하여 점수를 계산**
    - 필수 항목이 프로필 답변과 매칭되지 않는다면 false 반환
    - 위 조건이 맞지 않은 경우, 그외 매칭 여부를 반환 (true/false)

## 과한 리팩토링 ?
- matches 메소드를 리팩토링 하여 좀 더 구조화한다.

```java
class Profile {
    /**
     * match 의 흐름
     * - 매칭되는 조건의 가중치를 합해서 점수를 계산
     * - 필수 항목이 답변과 매칭되지 않으면 false 반환
     * - 필수 항목이 아니고 매칭되는것이 있다면 true, 아니라면 false 반환
     */
    /**
     * 리팩토링으로 인해 반복문이 3개가 되었다
     * 이게 더 나은 설계
     * 기존에는 반복문 하나에 성격과 목적이 다른 코드가 뒤섞여 있어 알아보기가 힘들었음
     * 만약 기존 구조에 기능추가가 계속해서 일어난다면 헬게이트오픈
     * 성능상 손해를 볼 수 있지만 성격과 목적이 다른 코드를 반복문에서 떼어내는 것이 중요하다.
     * - 마치 Java Stream API 로 처리하듯이...
     * - 데이터의 흐름을 기술..
     * -> 성능상 문제가 되지 않는다면 가독성을 최우선시 하라.
     */
    public boolean matches(Criteria criteria) {
        calculateScore(criteria);
        if (doesNotMeetAnyMustMatchCriterion(criteria)) {
            return false;
        }
        return anyMatches(criteria);
    }

    /**
     * 매칭되는 조건의 가중치를 합해서 점수를 계산
     */
    private void calculateScore(Criteria criteria) {
        score = 0;
        for (Criterion criterion : criteria) {
            if (criterion.matches(answerMatching(criterion))) {
                score += criterion.getWeight().getValue();
            }
        }
    }

    /**
     * 필수 항목이 답변과 매칭되지 않으면 false 반환
     */
    private boolean doesNotMeetAnyMustMatchCriterion(Criteria criteria) {
        for (Criterion criterion : criteria) {
            boolean match = criterion.matches(answerMatching(criterion));
            if (!match && criterion.getWeight() == Weight.MustMatch) {
                return true;
            }
        }
        return false;
    }

    /**
     * 필수 항목이 아니고 매칭되는것이 있다면 true, 아니라면 false 반환
     */
    private boolean anyMatches(Criteria criteria) {
        boolean anyMatches = false;
        for (Criterion criterion : criteria) {
            anyMatches |= criterion.matches(answerMatching(criterion));
        }
        return anyMatches;
    }

    /**
     * 디미터의 법칙 (이를 메소드로 추출해서 가독성을 향상)
     */
    private Answer answerMatching(Criterion criterion) {
        return answers.get(criterion.getAnswer().getQuestionText());
    }
}
```
- matches() 메소드를 세 군데로 구조화 하였지만 새로운 메소드 3개와 반복문 3개가 생겨났다.
- 더 안좋은 구조가 된 것 같지만 오히려 좋은 구조가 될 수 있다.

### 명확하고 테스트 가능한 단위
- matches() 메소드는 즉시 이해가능할 정도로 전체 알고리즘이 깔끔하게 정리되었다.
  - 주어진 조건에 따라 점수를 계산한다.
  - 프로파일이 필수조건에 부합하면 false 를 반환한다.
  - 그렇지 않으면 어떤 조건에 맞는지 여부를 반환한다.
- 이전 버전에 비해 가독성이 훨씬 좋아졌으며 헬퍼 메소드를 통해 명확하고 고립된 방식으로 잘 표현되어 있다.

### 성능 우려 ?
- 리팩토링 결과로 3개의 반복문이 되어 버렸다.
- 이는 성능문제로 이어지진 않을까 ?
- 성능은 중요하다.
- **성능 보다는 코드 품질 (가독성/유지보수) 를 우선시 해야한다.**
- 성능 문제가 야기 된다면 그때 최적화해도 늦지 않는다.
- 오히려 깔끔한 설계가 성능 최적화시 더 유리하게 작용할 수 있다.

## 정리
- 좋은 설계를 우선시 해야 한다.
- 반복문 하나에 성격이 다른 코드들이 뒤섞여 있다면 이해하기 어렵다.
- 만약 기존 구조에 기능추가가 계속해서 일어난다면 헬게이트오픈
  - 성능상 손해를 볼 수 있지만 성격과 목적이 다른 코드를 반복문에서 떼어내는 것이 중요하다.
  -> 성능상 문제가 되지 않는다면 가독성을 최우선시 하라.