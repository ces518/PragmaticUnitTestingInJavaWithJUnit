# 12장 테스트 주도 개발

## 단순한 시작
- TDD 는 세 가지 사이클로 구성된다.
  - 실패하는 코드 작성
  - 테스트 통과
  - 이전 두 단계에서 추가되거나 변경된 코드 개선
- 첫 번째 사이클은, 추가하고자 하는 **동작을 정의하는 테스트 코드** 를 작성하는 것

```java
class ProfileTest {

    @Test
    void matchesNothingWhenProfileEmpty() {
        Profile profile = new Profile();
        Question question = new BooleanQuestion(1, "Relocation package?");
        Criterion criterion = new Criterion(new Answer(question, Bool.TRUE), Weight.DontCare);

        boolean result = profile.matches(criterion);

        assertFalse(result);
    }
}

public class Profile {

  public boolean matches(Criterion criterion) {
    return false;
  }
}
```
- 구현을 보면 바보같아 보이지만, TDD 의 점진적인 사고방식을 따르는 것이 중요하다.
- Profile 클래스의 **작은 부분** 을 만들었고, 이를 동작하는 코드를 구현 했다는 것이 중요하다.

## 또 다른 증분 추가
- 실패에는 테스트에 대해 그 테스트를 통과할 수 있는 코드만 작성해야 한다.
- 가능한 가장 작은 증분을 추가하는 것이 중요하다.
- 테스트가 나타내는 **명세** 를 정확히 코딩 하라.
- 테스트 코드는 잠재적으로 시스템을 무엇을 하는지 문서화 한다.

```java
class ProfileTest {

  @Test
  void matchesNothingWhenProfileEmpty() {
    Profile profile = new Profile();
    Question question = new BooleanQuestion(1, "Relocation package?");
    Criterion criterion = new Criterion(new Answer(question, Bool.TRUE), Weight.DontCare);

    boolean result = profile.matches(criterion);

    assertFalse(result);
  }

  @Test
  void matchesWhenProfileContainsMatchingAnswer() {
    Profile profile = new Profile();
    Question question = new BooleanQuestion(1, "Relocation package?");
    Answer answer = new Answer(question, Bool.TRUE);
    profile.add(answer);
    Criterion criterion = new Criterion(answer, Weight.Important);

    boolean result = profile.matches(criterion);

    assertTrue(result);
  }
}

public class Profile {

    private Answer answer;

    public boolean matches(Criterion criterion) {
        return answer != null;
    }

    public void add(Answer answer) {
        this.answer = answer;
    }
}
```

## 테스트 정리
- TDD 사이클에서 두 번째 테스트를 통과한 후 코드를 정리해야 한다.
  - 이는 테스트 코드에 대한 정리
- 테스트는 짧고 깔끔해야 한다.

```java
class ProfileTest {

    private Profile profile;
    private BooleanQuestion questionIsThereRelocation;
    private Answer answerThereIsRelocation;

    @BeforeEach
    void setUp() {
        profile = new Profile();
        questionIsThereRelocation = new BooleanQuestion(1, "Relocation Package?");
        answerThereIsRelocation = new Answer(questionIsThereRelocation, Bool.TRUE);
    }

    @Test
    void matchesNothingWhenProfileEmpty() {
        Criterion criterion = new Criterion(answerThereIsRelocation, Weight.DontCare);

        boolean result = profile.matches(criterion);

        assertFalse(result);
    }

    @Test
    void matchesWhenProfileContainsMatchingAnswer() {
        profile.add(answerThereIsRelocation);
        Criterion criterion = new Criterion(answerThereIsRelocation, Weight.Important);

        boolean result = profile.matches(criterion);

        assertTrue(result);
    }
}
```
- 반복되는 부분을, setUp 메소드로 이동해, 각 테스트 메소드에서 테스트에 집중할 수 있게 한다.
- 대부분의 리팩토링은 쉽지만 효과가 크다.
- 작은 코드 조각을 의도를 알 수 있는 헬퍼 메소드로 추출하면 테스트를 향상시키는데 도움이 된다.

## 다수의 응답 지원
- 프로파일은 다수의 응답을 포함할 수 있어야 한다.
- 프로파일 객체가 다수의 Answer 객체를 가지기 위해 이를 Map 자료구조에 젖아 하기로 한다.

```java
class ProfileTest {

    private Profile profile;
    private BooleanQuestion questionIsThereRelocation;
    private BooleanQuestion questionReimbursesTuition;
    private Answer answerThereIsRelocation;
    private Answer answerThereIsNotRelocation;
    private Answer answerReimbursesTuition;

    @BeforeEach
    void setUp() {
        profile = new Profile();
        questionIsThereRelocation = new BooleanQuestion(1, "Relocation Package?");
        questionReimbursesTuition = new BooleanQuestion(1, "Reimburses tuition?");
        answerThereIsRelocation = new Answer(questionIsThereRelocation, Bool.TRUE);
        answerThereIsNotRelocation = new Answer(questionIsThereRelocation, Bool.FALSE);
        answerReimbursesTuition = new Answer(questionReimbursesTuition, Bool.TRUE);
    }

    @Test
    void matchesNothingWhenProfileEmpty() {
        Criterion criterion = new Criterion(answerThereIsRelocation, Weight.DontCare);

        boolean result = profile.matches(criterion);

        assertFalse(result);
    }

    @Test
    void matchesWhenProfileContainsMatchingAnswer() {
        profile.add(answerThereIsRelocation);
        Criterion criterion = new Criterion(answerThereIsRelocation, Weight.Important);

        boolean result = profile.matches(criterion);

        assertTrue(result);
    }

    @Test
    void matchesWhenContainsMultipleAnswers() {
        profile.add(answerThereIsRelocation);
        profile.add(answerReimbursesTuition);
        Criterion criterion = new Criterion(answerThereIsRelocation, Weight.Important);

        boolean matches = profile.matches(criterion);

        assertTrue(matches);
    }
}

public class Profile {

  private Map<String, Answer> answers = new HashMap<>();

  public boolean matches(Criterion criterion) {
    Answer answer = getMatchingProfileAnswer(criterion);
    return answer != null &&
            answer.match(criterion.getAnswer());
  }

  public void add(Answer answer) {
    answers.put(answer.getQuestionText(), answer);
  }

  private Answer getMatchingProfileAnswer(Criterion criterion) {
    return answers.get(criterion.getAnswer().getQuestionText());
  }
}
```
- TDD 를 할 때 다른 코드를 전혀 건들이지 않고, Profile 클래스만 변경할 필요는 없다.
- 필요한 사항이 있따면 설계를 변경해 다른 클래스로 넘어가도 된다.

## 인터페이스 확장
- matches() 메소드가 컬렉션인 Criteria 객체를 받는 인터페이스를 개발 한다.

```java
class ProfileTest {

  private Profile profile;
  private BooleanQuestion questionIsThereRelocation;
  private BooleanQuestion questionReimbursesTuition;
  private Answer answerThereIsRelocation;
  private Answer answerThereIsNotRelocation;
  private Answer answerReimbursesTuition;
  private Answer answerDoesNotReimburseTuition;
  private Criteria criteria;

  @BeforeEach
  void setUp() {
    profile = new Profile();
    questionIsThereRelocation = new BooleanQuestion(1, "Relocation Package?");
    questionReimbursesTuition = new BooleanQuestion(1, "Reimburses tuition?");
    answerThereIsRelocation = new Answer(questionIsThereRelocation, Bool.TRUE);
    answerThereIsNotRelocation = new Answer(questionIsThereRelocation, Bool.FALSE);
    answerReimbursesTuition = new Answer(questionReimbursesTuition, Bool.TRUE);
    answerDoesNotReimburseTuition = new Answer(questionReimbursesTuition, Bool.FALSE);
    criteria = new Criteria();
  }

  @Test
  void matchesNothingWhenProfileEmpty() {
    Criterion criterion = new Criterion(answerThereIsRelocation, Weight.DontCare);

    assertFalse(profile.matches(criterion));
  }

  @Test
  void matchesWhenProfileContainsMatchingAnswer() {
    profile.add(answerThereIsRelocation);
    Criterion criterion = new Criterion(answerThereIsRelocation, Weight.Important);

    assertTrue(profile.matches(criterion));
  }

  @Test
  void matchesWhenContainsMultipleAnswers() {
    profile.add(answerThereIsRelocation);
    profile.add(answerReimbursesTuition);
    Criterion criterion = new Criterion(answerThereIsRelocation, Weight.Important);

    assertTrue(profile.matches(criterion));
  }

  @Test
  void doesNotMatchWhenNoneOfMultipleCriteriaMatch() {
    profile.add(answerDoesNotReimburseTuition);
    criteria.add(new Criterion(answerThereIsRelocation, Weight.Important));
    criteria.add(new Criterion(answerReimbursesTuition, Weight.Important));

    assertFalse(profile.matches(criteria));
  }

  @Test
  void matchesWhenAnyOfMultipleCriteriaMatch() {
    profile.add(answerThereIsRelocation);
    criteria.add(new Criterion(answerThereIsRelocation, Weight.Important));
    criteria.add(new Criterion(answerReimbursesTuition, Weight.Important));

    assertTrue(profile.matches(criteria));
  }

  @Test
  void doesNotMatchWhenAnyMustMeetCriteriaNotMet() {
    profile.add(answerThereIsRelocation);
    profile.add(answerDoesNotReimburseTuition);
    criteria.add(new Criterion(answerThereIsRelocation, Weight.Important));
    criteria.add(new Criterion(answerReimbursesTuition, Weight.Important));
    criteria.add(new Criterion(answerReimbursesTuition, Weight.MustMatch));

    assertFalse(profile.matches(criteria));
  }
}

public class Profile {

    private Map<String, Answer> answers = new HashMap<>();

    public boolean matches(Criterion criterion) {
        Answer answer = getMatchingProfileAnswer(criterion);
        return answer != null &&
            answer.match(criterion.getAnswer());
    }

    public boolean matches(Criteria criteria) {
        boolean matches = false;
        for (Criterion criterion : criteria) {
            if (matches(criterion)) {
                matches = true;
            } else if (criterion.getWeight() == Weight.MustMatch) {
                return false;
            }
        }
        return matches;
    }

    public void add(Answer answer) {
        answers.put(answer.getQuestionText(), answer);
    }

    private Answer getMatchingProfileAnswer(Criterion criterion) {
        return answers.get(criterion.getAnswer().getQuestionText());
    }
}
```

## 문서로서의 테스트
- ProfileTest 클래스에 있는 테스트 이름들을 살펴보면, Profile 클래스의 동작에 관해 설명을 하고 있는 것과 동일하다.
- 세심하게 테스트를 작성할수록 테스트는 Profile 클래스에 의도적으로 설계된 동작들을 더 많이 문서화 할 수 있다.
- 테스트 주도 클래스를 잘 이해하려면, **테스트 이름 먼저 살펴보라.**
- 포괄적인 테스트 일므 집합은 클래스의 의도된 용량을 전체적인 관점 요약으로 제공한다.
- 테스트 이름이 다른것들과 함께 깔끔하고 일관성 있을수록 더 신뢰 가능한 테스트 문서가 될 것이다.
- 각 테스트 클래스 또는 픽스쳐로 나누면 연관된 동작 그룹에 집중할 수 있다.

```java
class Profile_MatchesCriterionTest {
    // ..
}

class Profile_MatchesCriteriaTest {
    // ..
}

class Profile_ScoreTest {
    // ..
}
```