# 13장 까다로운 테스트

## 멀티스레드 코드테스트
- 동작하는 **동시성 (concurrent)** 코드를 작성하는 것은 어렵다.
- 동시성 처리가 필요한 애플리케잇녀 코드를 테스트하는 것은, 기술적으로 **단위 테스트의 영역이 아니다.**
  - 이는 통합 테스트의 영역

## 멀티스레드 코드 테스트시 따를 두가지 사항
- 스레드 통제와 애플리케이션 코드 사이의 중첩을 최소화
  - **스레드 없이 애플리케이션 코드를 단위 테스트할 수 있도록 설계를 변경** 해야 한다.
  - 그 이후 스레드에 대한 집중적인 테스트를 작성하라.
- 다른 사람의 작업을 믿어라
  - java.util.concurrent 패키지
  - 생산자/소비자 문제를 직접 코딩하지 말고 BlockingQueue 를 사용

## 모든 매칭 찾기
- ProfileMatcher 의 멀티스레드 코드
- findMatchingProfiles 메소드는 스레드풀을 생성해 동시다발적으로 처리를하고, 핵심 애플리케이션 코드또한 녹아있다. 
- **실제 애플리케이션 로직을 추출** 하는 작업이 필요하다.

```java
public class ProfileMatcher {

    private static final int DEFAULT_POOL_SIZE = 4;

    private Map<String, Profile> profiles = new HashMap<>();

    public void add(Profile profile) {
        profiles.put(profile.getId(), profile);
    }

    /**
     * 멀티 스레드 코드
     */
    public void findMatchingProfiles(Criteria criteria, MatchListener listener) {
        ExecutorService executors = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);

        List<MatchSet> matchSets = profiles.values().stream()
            .map(p -> p.getMatchSet(criteria))
            .collect(Collectors.toList());
        for (MatchSet matchSet : matchSets) {
            Runnable runnable = () -> {
                if (matchSet.matches()) {
                    listener.foundMatch(profiles.get(matchSet.getProfileId()), matchSet);
                }
            };
            executors.execute(runnable);
        }
        executors.shutdown();
    }

}
```

### 애플리케이션 로직 추출 및 테스트

```java
public class ProfileMatcher {

    private static final int DEFAULT_POOL_SIZE = 4;

    private Map<String, Profile> profiles = new HashMap<>();

    public void add(Profile profile) {
        profiles.put(profile.getId(), profile);
    }

    /**
     * 멀티 스레드 코드
     */
    public void findMatchingProfiles(Criteria criteria, MatchListener listener) {
        ExecutorService executors = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);

        for (MatchSet matchSet : collectMatchSets(criteria)) {
            Runnable runnable = () -> process(listener, matchSet);
            executors.execute(runnable);
        }
        executors.shutdown();
    }

    List<MatchSet> collectMatchSets(Criteria criteria) {
        return profiles.values().stream()
            .map(p -> p.getMatchSet(criteria))
            .collect(Collectors.toList());
    }

    void process(MatchListener listener, MatchSet matchSet) {
        if (matchSet.matches()) {
            listener.foundMatch(profiles.get(matchSet.getProfileId()), matchSet);
        }
    }
}
```
- 실제 matchSet 을 모으는 작업과 listener 를 통해 match 를 찾는 작업을 각각의 메소드로 추출한다.
- findMatchingProfiles 메소드에는, 실제 멀티스레드 작업에 대한 코드만 남아 있다.

```java
class ProfileMatcherTest {

    private BooleanQuestion question;
    private Criteria criteria;
    private ProfileMatcher matcher;
    private Profile matchingProfile;
    private Profile nonMatchingProfile;
    private MatchListener listener;

    @BeforeEach
    void setUp() {
        question = new BooleanQuestion(1, "");
        criteria = new Criteria();
        criteria.add(new Criterion(matchingAnswer(), Weight.MustMatch));
        matchingProfile = createMatchingProfile("matching");
        nonMatchingProfile = createNonMatchingProfile("nonMatching");

        matcher = new ProfileMatcher();
        listener = mock(MatchListener.class);
    }

    @Test
    public void collectsMatchSets() {
        matcher.add(matchingProfile);
        matcher.add(nonMatchingProfile);

        List<MatchSet> sets = matcher.collectMatchSets(criteria);

        assertThat(sets.stream()
                .map(MatchSet::getProfileId).collect(Collectors.toSet()),
            equalTo(new HashSet<>
                (Arrays.asList(matchingProfile.getId(), nonMatchingProfile.getId()))));
    }

    @Test
    void processNotifiesListenerOnMatch() {
        matcher.add(matchingProfile);
        MatchSet matchSet = matchingProfile.getMatchSet(criteria);

        matcher.process(listener, matchSet);

        verify(listener).foundMatch(matchingProfile, matchSet);
    }

    private Profile createMatchingProfile(String name) {
        Profile profile = new Profile(name);
        profile.add(matchingAnswer());
        return profile;
    }

    private Profile createNonMatchingProfile(String name) {
        Profile profile = new Profile(name);
        profile.add(nonMatchingAnswer());
        return profile;
    }

    private Answer matchingAnswer() {
        return new Answer(question, Bool.TRUE);
    }

    private Answer nonMatchingAnswer() {
        return new Answer(question, Bool.FALSE);
    }
}
```

## 스레드 로직 테스트를 위한 재설계
- 애플리케이션 로직을 추출한 이후 findMatchingProfiles 메소드의 코드는 대부분이 스레드 로직이다.
- 이를 테스트하기 위해선 약간의 재 설계가 필요하다.

```java
public class ProfileMatcher {

    private static final int DEFAULT_POOL_SIZE = 4;
    private ExecutorService executors = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);

    private Map<String, Profile> profiles = new HashMap<>();

    ExecutorService getExecutors() {
        return executors;
    }

    public void add(Profile profile) {
        profiles.put(profile.getId(), profile);
    }

    /**
     * 멀티 스레드 코드
     */
    public void findMatchingProfiles(
        Criteria criteria,
        MatchListener listener,
        List<MatchSet> matchSets,
        BiConsumer<MatchListener, MatchSet> processFunction
    ) {
        for (MatchSet matchSet : matchSets) {
            Runnable runnable = () -> processFunction.accept(listener, matchSet);
            executors.execute(runnable);
        }
        executors.shutdown();
    }

    public void findMatchProfiles(Criteria criteria, MatchListener listener) {
        findMatchingProfiles(criteria, listener, collectMatchSets(criteria), this::process);
    }

    List<MatchSet> collectMatchSets(Criteria criteria) {
        return profiles.values().stream()
            .map(p -> p.getMatchSet(criteria))
            .collect(Collectors.toList());
    }

    void process(MatchListener listener, MatchSet matchSet) {
        if (matchSet.matches()) {
            listener.foundMatch(profiles.get(matchSet.getProfileId()), matchSet);
        }
    }
}
```
- 기존의 process() 메소드는 이미 테스트를 완료 했기 때문에, 잘 동작한다고 가정할 수 있다.
- 때문에 실제 멀티스레드 코드인 findMatchingProfiles 를 테스트하기 위해 process() 메소드의 동작을 스텁 처리한다.
  - 이를 위해 FunctionalInterface 를 인자로 받음
- 실제 기존 메소드는 이 처리를 findMatchingProfiles 로 위임한다.

```java
/**
 * 스레드 로직 테스트
 */
@Test
void gathersMatchingProfiles() {
    Set<String> processedSets = Collections.synchronizedSet(new HashSet<>());
    BiConsumer<MatchListener, MatchSet> processFunction = (listener, set) -> {
        processedSets.add(set.getProfileId());
    };
    List<MatchSet> matchSets = createMatchSets(100);

    matcher.findMatchingProfiles(criteria, listener, matchSets, processFunction);

    while (!matcher.getExecutors().isTerminated()) {

    }
    assertThat(
        processedSets,
        equalTo(matchSets.stream().map(MatchSet::getProfileId).collect(Collectors.toSet()))
    );
}

private List<MatchSet> createMatchSets(int count) {
    List<MatchSet> matchSets = new ArrayList<>();
    for (int i = 0; i < count; i++) {
        matchSets.add(new MatchSet(String.valueOf(i), null, null));
    }
    return matchSets;
}
```
- processFunction 은 실제 process 메소드의 동작을 대신한다.
  - processFunction 은 process 동작 대신 profileId 를 인자로 받아 이를 컬렉션에 저장한다. (멀티 스레드로 처리될 로직)
- 실제 기대값과 processedSets 에 담긴 값을 비교해 검증한다.

> 애플리케이션 로직과, 스레드 로직의 관심사를 분리해 테스트를 작성

## 데이터베이스 테스트
- QuestionController 는 JPA 를 사용하는 포스트그레(RDB) 와 통신하기 때문에 테스트하기 어렵다.
- QuestionController 의 대부분의 로직을 JPA 인터페이스를 통해 처리를 위임하고 있다.
- JPA 에 대한 의존성을 고립시켜 좋은 설계이지만, **테스트 관점에서는 의문**
- JPA 관련 인터페이스에 대한 모든 스텁을 만들어 테스트할 수도 있지만, 이는 노력도 많이 들고 테스트도 힘들다.
- 그 대신 **진짜 포스트그레 와 상호작용하는 테스트를 작성**

```java
public class QuestionController {
   private Clock clock = Clock.systemUTC();
   // ...

   private static EntityManagerFactory getEntityManagerFactory() {
      return Persistence.createEntityManagerFactory("postgres-ds");
   }
   
   public Question find(Integer id) {
      return em().find(Question.class, id);
   }
   
   public List<Question> getAll() {
      return em().createQuery("select q from Question q", Question.class).getResultList();
   }
   
   public List<Question> findWithMatchingText(String text) {
      return em()
            .createQuery(
                "select q from Question q where q.text like '%" + text + "%'", Question.class)
            .getResultList();
   }
   
   public int addPercentileQuestion(String text, String[] answerChoices) {
      return persist(new PercentileQuestion(text, answerChoices));
   }

   public int addBooleanQuestion(String text) {
      return persist(new BooleanQuestion(text));
   }

   void setClock(Clock clock) {
      this.clock = clock;
   }
   // ...

   void deleteAll() {
      executeInTransaction(
            (em) -> em.createNativeQuery("delete from Question").executeUpdate());
   }
   
   private void executeInTransaction(Consumer<EntityManager> func) {
      EntityManager em = em();

      EntityTransaction transaction = em.getTransaction();
      try {
         transaction.begin();
         func.accept(em);
         transaction.commit();
      } catch (Throwable t) {
         t.printStackTrace();
         transaction.rollback();
      }
      finally {
        em.close();
      }
   }
   
   private int persist(Persistable object) {
      object.setCreateTimestamp(clock.instant());
      executeInTransaction((em) -> em.persist(object));
      return object.getId();
   }
   
   private EntityManager em() {
      return getEntityManagerFactory().createEntityManager();
   }
}
```

### 데이터 문제
- Junit 의 대다수는 속도가 빠르길 원한다.
- 영속적인 상호작용을 시스템 한 곳으로 고립시킬 수 있다면, 통합 테스트의 대상은 상당히 소규모로 줄어들 것이다.
  - 테스트 목적으로 H2 같은 메모리 디비를 사용할 수 도 있지만, **프로덕션 디비와의 미묘한 차이 때문에 벌어지는 문제점도 존재** 한다.
- 테스트용 공용 디비를 사용하는 환경이라면, 매 테스트마다 초기화/롤백 하는 형태가 바람직하다.
- 통합테스트는 작성 및 유지보수가 어렵지만, 테스트 전략의 필수적인 부분이다.

## 정리
- 관심사를 분리 시켜라.
- 애플리케이션 로직은, 스레드/데이터베이스 와 같은 다른 의존성과 분리해야 한다.
  - 의존적인 코드를 고립시켜 코드베이스에 만연하지 않도록 해야한다.
- 느리거나 휘발적인 코드를 목으로 대체해 단위 테스트의 의존성을 끊어라.
- 필요한 경우 통합 테스트를 작성하되, 단순하고 집중적으로 만들어야 한다.