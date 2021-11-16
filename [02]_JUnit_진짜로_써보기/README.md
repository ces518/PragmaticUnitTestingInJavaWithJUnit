# 2장 진짜로 JUnit 써보기

## 테스트 대상 이해 : Profile 클래스

- iloveyouboss 라는 애플리케이션의 일부에 대한 테스트를 작성한다.
    - 이 애플리케이션은 잡코리아/사람인과 같은 구직 웹 사이트
    - 잠재적인 구인자에게 유망한 구직자를 매칭하고 반대 방향에 대한 서비스로 제공한다.
- 구인자와 구직자는 둘 다 다수의 객관식 또는 YES/NO 에 해당하는 대답을 하는 프로파일을 생성한다.

`Profile`

```java
public class Profile {

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

    /***
     * matches 메소드는 부작용을 포함한다.
     * 함수형 프로그래밍에서 부작용 : 함수 외부의 객체 / 변수의 내용을 변경하는 것
     * -> 로그 출력도 부작용의 예
     */
    public boolean matches(Criteria criteria) {
        score = 0;

        boolean kill = false;
        boolean anyMatches = false;
        for (Criterion criterion : criteria) {
            Answer answer = answers.get(
                criterion.getAnswer().getQuestionText());
            boolean match =
                criterion.getWeight() == Weight.DontCare ||
                    answer.match(criterion.getAnswer());

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

    public int score() {
        return score;
    }
}
```

- Profile 클래스는 어떤 사람이 회사 혹은 구직자에게 물어볼 수 있는 적절한 **질문에 대한 답변** 을 가지고 있다.
    - ex) 회사가 구직자에게 "이직 생각이 있냐?" 라고 물어 볼 수 있고, 구직자의 Profile 은 그에 대한 TRUE/FALSE 답을 가진 Answer 객체를 가지고
      있을 수 있다.
- add() 메소드를 통해 Answer 객체를 Profile 에 추가할 수 있다.
- Question 객체는 **질문의 내용과 답변** 이 가능한 범위를 포함한다.
    - 예/아니오 질문이라면 TRUE/FALSE 값을 포함한다.
- Answer 객체는 그에 대응하는 Question 객체를 참고하고, 그에 대한 적절한 값을 포함한다.
- Criteria 객체는 다수의 Criterion 객체를 담는 컨테이너이다.
- Criterion 객체는 고용주가 구직자를 찾거나 그 반대의 경우를 의미한다.
    - Answer 객체와 그 질문의 중요도인 Weight 객체를 포함한다.
- Profile.matches() 메소드는 꽤나 복잡하고 이것이 의도한대로 동작하는지 테스트 코드를 작성해 보자.

## 어떤 테스트를 작성할 수 있는지 결정

- 복잡한 메소드라면 테스트코드를 작게는 수십개 혹은 수백개 작성할 수도 있다.
- 이때 **얼마나 많은 테스트코드를 작성해야하는가** 에 대해 고민해 볼 필요가 있다.
- 코드의 분기점이나 잠재적으로 영향이 클법한 데이터 조작을 고려해 볼 수 있다.
    - 반복문, 조건문 등...
    - 데이터가 null 이거나 0 이면 어떻게 되는지 ?..
- Criteria 객체가 Criterion 객체를 하나만 포함하는 행복경로를 넘어서, 아래 조건들은 테스트 케이스에 영향을 줄 수 있는 목록이다.
    1. Criteria 인스턴스가 Criterion 객체를 하나도 포함하고 있지 않을 경우
    2. Criteria 인스턴스가 다수의 Criterion 객체를 포함하는 경우
    3. answers.get() 에서 반환된 Answer 가 null 일 경우
    4. criterion.getAnswer() || criterion.getAnswer().getQuestionText() 가 null 일 경우
    5. criterion.getWeight() 가 Weight.DontCare 라 match 변수가 true 인 경우
    6. value 변수와 criterion.getWeight() 가 매칭되어 match 변수가 true 인 경우
    7. 두 조건이 모두 false 라 결과적으로 match 변수가 false 인 경우
    8. match 변수가 false 이고 criterion.getWeight() 가 Weight.MustMatch 라 kill 변수가 true 인 경우
    9. match 변수가 true 라 kill 변수가 변하지 않는 경우
    10. criterion.getWeight() 가 Weight.MustMatch 가 아니라 kill 변수가 변하지 않는 경우
    11. match 변수가 true 이기 때문에 score 변수가 변경되었을 경우
    12. match 변수가 false 이기 때문에 score 변수가 변경되지 않은 경우
    13. kill 변수가 true 이기 때문에 matches 변수가 false 를 반환하는 경우
    14. kill 변수가 false 이고 anyMatches 변수가 true 라 matches 메소드 결과가 true 인 경우
    15. kill 변수가 false 이고 anyMatches 변수가 false 라 matches 메소드 결과가 false 인 경우
- 위 목록은 코드를 **분기** 하거나 **데이터 번형** 이 서로 다른 결과를 나타내는 것들이다.
- 테스트를 작성하고 나면 코드가 실제 어떻게 동작하는지 더 잘 이해할 수 있다.
- 위 중 일부는 특정 조건을 충족해야 되는 조건이고, 이런 조건들은 테스트 하나로 묶을 수 있다.
- 중요한 점은 테스트 코드 작성시 **가장 신경쓰이는 부분이 어디인지 알고 있어야 한다.** 는 점

## 테스트 만들기

- matches() 메소드에서 다수의 **흥미로운** 부분은 for 반복문 내부에 있다.
- 한 가지 경우를 커버하는 테스트 케이스를 작성해 본다.

```java
class ProfileTest {

    /**
     * 가중치가 MustMatch 인 질문이 매칭되지 않았을 경우 matches 메소드는 False 를 반환한다.
     */
    @Test
    void matchAnswersFalseWhenMustMatchCriteriaNotMet() {
        // given
        Profile profile = new Profile("Bull Hockey, Inc.");

        // 질문
        Question question = new BooleanQuestion(1, "Got bonuses?");

        Answer profileAnswer = new Answer(question, Bool.FALSE);
        profile.add(profileAnswer);

        // 답변
        Answer criteriaAnswer = new Answer(question, Bool.TRUE);

        // 답변 / 가중치
        Criterion criterion = new Criterion(criteriaAnswer, Weight.MustMatch);

        // 질문 목록
        Criteria criteria = new Criteria();
        criteria.add(criterion);

        // when
        boolean matches = profile.matches(criteria);

        // then
        assertFalse(matches);
    }
}
```

- 우리가 테스트할 메소드는 Profile#matches
- 테스트 **준비** 를 위해 준비해야 하는 객체들이 많다.
- Profile, Question, Answer, Criterion, Criteria...
- 만약 여기서 두번째 케이스를 커버하는 테스트를 추가 작성한다면 다음과 같은 그림이 될 것

```java
class ProfileTest {

    /**
     * 가중치가 MustMatch 인 질문이 매칭되지 않았을 경우 matches 메소드는 False 를 반환한다.
     */
    @Test
    void matchAnswersFalseWhenMustMatchCriteriaNotMet() {
        // given
        Profile profile = new Profile("Bull Hockey, Inc.");

        // 질문
        Question question = new BooleanQuestion(1, "Got bonuses?");

        Answer profileAnswer = new Answer(question, Bool.FALSE);
        profile.add(profileAnswer);

        // 답변
        Answer criteriaAnswer = new Answer(question, Bool.TRUE);

        // 답변 / 가중치
        Criterion criterion = new Criterion(criteriaAnswer, Weight.MustMatch);

        // 질문 목록
        Criteria criteria = new Criteria();
        criteria.add(criterion);

        // when
        boolean matches = profile.matches(criteria);

        // then
        assertFalse(matches);
    }

    @Test
    void matchAnswersTrueForAnyDontCareCriteria() throws Exception {
        // given
        Profile profile = new Profile("Bull Hockey, Inc.");

        Question question = new BooleanQuestion(1, "Got milk?");

        Answer profileAnswer = new Answer(question, Bool.FALSE);
        profile.add(profileAnswer);

        Answer criteriaAnswer = new Answer(question, Bool.TRUE);

        Criterion criterion = new Criterion(criteriaAnswer, Weight.DontCare);

        Criteria criteria = new Criteria();
        criteria.add(criterion);

        // when
        boolean matches = profile.matches(criteria);

        // then
        assertTrue(matches);
    }
}
```
- 현재 테스트의 가장 큰 문제 -> 무엇을 테스트하는지 알아보기 어렵다.
  - 테스트를 위한 준비과정이 많아 가독성이 떨어진다.
- 테스트 준비 과정에서 중복되는 코드가 있다.

## @Before/@BeforeEach

```java
class ProfileTest {

    private Profile profile;
    private BooleanQuestion question;
    private Criteria criteria;

    /**
     * 공통적인 테스트 준비코드가 있다면, @BeforeEach 로 이동시켜라.
     */
    @BeforeEach
    void setUp() {
        profile = new Profile("Bull Hockey, Inc.");
        question = new BooleanQuestion(1, "Got bonuses?");
        criteria = new Criteria();
    }

    /**
     * 가중치가 MustMatch 인 질문이 매칭되지 않았을 경우 matches 메소드는 False 를 반환한다.
     */
    @Test
    void matchAnswersFalseWhenMustMatchCriteriaNotMet() {
        // given
        profile.add(new Answer(question, Bool.FALSE));
        criteria.add(new Criterion(new Answer(question, Bool.TRUE), Weight.MustMatch));

        // when
        boolean matches = profile.matches(criteria);

        // then
        assertFalse(matches);
    }

    @Test
    void matchAnswersTrueForAnyDontCareCriteria() throws Exception {
        // given
        profile.add(new Answer(question, Bool.FALSE));
        criteria.add(new Criterion(new Answer(question, Bool.TRUE), Weight.DontCare));

        // when
        boolean matches = profile.matches(criteria);

        // then
        assertTrue(matches);
    }
}
```
- ProfileTest 클래스의 모든 테스트코드에 존재하는 공통적인 초기화 코드를 리팩토링 한다.
- 초기화 로직들은 @BeforeEach 메소드로 이동하고, 테스트 메소드에서는 존재하지 않게 되어 가독성도 한결 좋아졌다.
- 기존에 지역변수로 선언했던 부분을 inlining 하여 가독성을 추가로 높혔다.

## JUnit 동작 순서
- JUnit 은 새로운 ProfileTest 인스턴스를 생성하고, profile, question, criteria 필드는 초기화하지 않는다.
- @Before/@BeforeEach 를 호출하여 profile, question, criteria 변수를 적절한 인스턴스로 초기화한다.
- 실행할 테스트 메소드를 실행하고 통과/실패 여부를 표기한다.
- 아직 실행할 테스트가 남아있기 때문에 ProfileTest 인스턴스를 새롭게 생성한뒤 이전 동작을 반복한다.

> JUnit 은 위와 같은 방식으로 모든 테스트를 독립적으로 만든다. <br/>
> 테스트 코드 작성시 다른 테스트에 영향을 주는것을 **최소화** 해야한다. (static 필드를 피해야함)