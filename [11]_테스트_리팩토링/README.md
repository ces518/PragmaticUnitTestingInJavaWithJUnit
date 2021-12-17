# 11장 테스트 리팩토링
- 테스트 코드는 상당한 투자를 의미한다.
- 테스트는 결함을 최소화하고 리팩토링으로 프로덕션 시스템을 깔끔하게 유지시켜 주지만, 이는 지속적인 비용
- 프로덕션 시스템을 리팩토링하는 것 처럼 테스트도 리팩토링이 필요하다.

## 이해 검색
- 애플리케이션의 검색 기능을 개선하고자 한다.
  - util.Search 클래스
- 하지만 누구도 Search 클래스가 어떤 동작을 하는지 정확히 알지 못한다.
- 때문에 테스트 코드를 살펴보았지만 이해하기 힘든 상황

```java
class SearchTest {

    /**
     * testSearch 는 무엇을 테스트하려는지 아무런 정보도 제공하지 않는다.
     */
    @Test
    public void testSearch() {
        try {
            String pageContent = "There are certain queer times and occasions "
                + "in this strange mixed affair we call life when a man "
                + "takes this whole universe for a vast practical joke, "
                + "though the wit thereof he but dimly discerns, and more "
                + "than suspects that the joke is at nobody's expense but "
                + "his own.";
            byte[] bytes = pageContent.getBytes();
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            // search
            Search search = new Search(stream, "practical joke", "1");
            Search.LOGGER.setLevel(Level.OFF);
            search.setSurroundingCharacterCount(10);
            search.execute();
            assertFalse(search.errored());
            List<Match> matches = search.getMatches();
            assertThat(matches, is(notNullValue()));
            assertTrue(matches.size() >= 1);
            Match match = matches.get(0);
            assertThat(match.searchString, equalTo("practical joke"));
            assertThat(match.surroundingContext,
                equalTo("or a vast practical joke, though t"));
            stream.close();

            // negative
            URLConnection connection =
                new URL("http://bit.ly/15sYPA7").openConnection();
            InputStream inputStream = connection.getInputStream();
            search = new Search(inputStream, "smelt", "http://bit.ly/15sYPA7");
            search.execute();
            assertThat(search.getMatches().size(), equalTo(0));
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail("exception thrown in test" + e.getMessage());
        }
    }
}
```
- 테스트 이름인 testSearch 는, 무엇을 테스트하려는지 아무런 정보도 제공하지 않는다.
- 이를 이해하기 쉽게 리팩토링하고, 더 깔끔하고 표현력이 좋은 테스트로 만들어 보자.

## 테스트 냄새 : 불필요한 테스트 코드
- 현재 테스트 코드는 try-catch 블록으로 인해 조금 난잡한 상황이다.
- 테스트 코드가 예외 발생을 기대하지 않는다면, 예외가 발생하도록 그대로 두는 것이 좋다.
  - 예외가 발생하면 테스트는 실패할 것이다.
- try-catch 블록을 제거하고, throws 로 예외를 던져라.
  - try-catch 블록으로 감싸서 얻는 이득이 없다.

```java
class SearchTest {

    /**
     * testSearch 는 무엇을 테스트하려는지 아무런 정보도 제공하지 않는다.
     */
    @Test
    public void testSearch() throws IOException {
        String pageContent = "There are certain queer times and occasions "
            + "in this strange mixed affair we call life when a man "
            + "takes this whole universe for a vast practical joke, "
            + "though the wit thereof he but dimly discerns, and more "
            + "than suspects that the joke is at nobody's expense but "
            + "his own.";
        byte[] bytes = pageContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        // search
        Search search = new Search(stream, "practical joke", "1");
        Search.LOGGER.setLevel(Level.OFF);
        search.setSurroundingCharacterCount(10);
        search.execute();
        assertFalse(search.errored());
        List<Match> matches = search.getMatches();
        assertThat(matches, is(notNullValue()));
        assertTrue(matches.size() >= 1);
        Match match = matches.get(0);
        assertThat(match.searchString, equalTo("practical joke"));
        assertThat(match.surroundingContext,
            equalTo("or a vast practical joke, though t"));
        stream.close();

        // negative
        URLConnection connection =
            new URL("http://bit.ly/15sYPA7").openConnection();
        InputStream inputStream = connection.getInputStream();
        search = new Search(inputStream, "smelt", "http://bit.ly/15sYPA7");
        search.execute();
        assertThat(search.getMatches().size(), equalTo(0));
        stream.close();
    }
}
```
- try-catch 블록을 제거하니 인덴트가 줄어 한 층 더 이해하기 쉬워 졌다.
- 다음은 not-null 단언 이다.

```java
List<Match> matches = search.getMatches();
assertThat(matches, is(notNullValue()));
assertTrue(matches.size() >= 1);
```
- 변수를 참조하기 전에 null 을 검사하는 것은 안전하게 좋은일 이다.
- 프로덕션 코드에서는 맞지만, 테스트에서 not-null 단언은 **군더더기** 일 뿐이다.
- null 을 참조한다면 예외가 던져질테고 이는 테스트 실패로 이어진다.
- 때문에 이 코드도 제거한다.

```java
class SearchTest {

    /**
     * testSearch 는 무엇을 테스트하려는지 아무런 정보도 제공하지 않는다.
     */
    @Test
    public void testSearch() throws IOException {
        String pageContent = "There are certain queer times and occasions "
            + "in this strange mixed affair we call life when a man "
            + "takes this whole universe for a vast practical joke, "
            + "though the wit thereof he but dimly discerns, and more "
            + "than suspects that the joke is at nobody's expense but "
            + "his own.";
        byte[] bytes = pageContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        // search
        Search search = new Search(stream, "practical joke", "1");
        Search.LOGGER.setLevel(Level.OFF);
        search.setSurroundingCharacterCount(10);
        search.execute();
        assertFalse(search.errored());
        List<Match> matches = search.getMatches();
//        assertThat(matches, is(notNullValue())); 프로덕션 코드에서 널을 체크하는것은 맞지만, 테스트에서는 군더더기일 뿐이다.
        assertTrue(matches.size() >= 1);
        Match match = matches.get(0);
        assertThat(match.searchString, equalTo("practical joke"));
        assertThat(match.surroundingContext,
            equalTo("or a vast practical joke, though t"));
        stream.close();

        // negative
        URLConnection connection =
            new URL("http://bit.ly/15sYPA7").openConnection();
        InputStream inputStream = connection.getInputStream();
        search = new Search(inputStream, "smelt", "http://bit.ly/15sYPA7");
        search.execute();
        assertThat(search.getMatches().size(), equalTo(0));
        stream.close();
    }
}
```

## 테스트 냄새 : 추상화 누락
- 잘 구성된 테스트는, 데이터 준비 / 동작 / 결과 단언 의 세 가지 관점으로 보여준다.
- 세부 사항을 추상화하여 이해하기 쉽게 해야 한다.
- 추상화로 필수적인 개념을 최대화하고, 불필요한 세부 사항은 감춰야 한다.

```java
List<Match> matches = search.getMatches();
assertTrue(matches.size() >= 1);
Match match = matches.get(0);
assertThat(match.searchString, equalTo("practical joke"));
assertThat(match.surroundingContext,
    equalTo("or a vast practical joke, though t"));
```
- 위 5줄의 코드를 추상화 사용자 정의 단언 문으로 이해하기 쉽게 만든다.

```java
public class ContainsMatches extends TypeSafeMatcher<List<Match>> {

    private Match[] expected;

    public ContainsMatches(Match[] expected) {
        this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(List<Match> actual) {
        if (actual.size() != expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (!equals(expected[i], actual.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("<" + expected.toString() + ">");
    }

    @Factory
    public static <T> Matcher<List<Match>> containsMatches(Match[] expected) {
        return new ContainsMatches(expected);
    }

    private boolean equals(Match expected, Match actual) {
        return expected.searchString.equals(actual.searchString) &&
            expected.surroundingContext.equals(actual.surroundingContext);
    }
}
```

```java
// 추상화를 통해 필수적인 개념을 최대화하고 불필요한 세부사항을 감춘다.
// 사용자 정의 단언 사용
assertThat(search.getMatches(), containsMatches(new Match[]{
    new Match("1", "practical joke", "or a vast practical joke, though t")
}));
```
- 세부 구현은 감추고, 추상화를 통해 이해하기 쉬운 단언이 됨
- 다음은 크기가 0인지 비교하는 단언 이다.

```java
assertThat(search.getMatches().size(), equalTo(0));
```
- 사이즈가 0이라는 것은, 비어 있다 라는 개념
- 단언 바꾸면 이를 이해하기 좀 더 쉬워진다.

```java
assertTrue(search.getMatches().isEmpty());
```

## 테스트 냄새 : 부적절한 정보
- 잘 추상화된 테스트는 코드를 이해하는데 중요한 것을 부각시킨다.
- 테스트에 사용된 데이터는 스토리를 말하는데 도움을 주어야 한다.

```java
Search search = new Search(stream, "practical joke", "1");
// ...
assertThat(search.getMatches(), containsMatches(new Match[]{
    new Match("1", "practical joke", "or a vast practical joke, though t")
}));
```
- 이 두 코드를 보면 "1" 라는 값이 무엇을 의미하는지 알 수 없다.
- 의미가 불분명한 **매직 리터럴** 이다.
- 이런 경우 의미있는 상수를 도입해 즉사 파악 가능하도록 변경해야 한다.

```java
class SearchTest {

    private static final String A_TITLE = "1";

    /**
     * testSearch 는 무엇을 테스트하려는지 아무런 정보도 제공하지 않는다.
     */
    @Test
    public void testSearch() throws IOException {
        String pageContent = "There are certain queer times and occasions "
            + "in this strange mixed affair we call life when a man "
            + "takes this whole universe for a vast practical joke, "
            + "though the wit thereof he but dimly discerns, and more "
            + "than suspects that the joke is at nobody's expense but "
            + "his own.";
        byte[] bytes = pageContent.getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        // search
        Search search = new Search(stream, "practical joke", A_TITLE);
        Search.LOGGER.setLevel(Level.OFF);
        search.setSurroundingCharacterCount(10);
        search.execute();
        assertFalse(search.errored());

        // 추상화를 통해 필수적인 개념을 최대화하고 불필요한 세부사항을 감춘다.
        // 사용자 정의 단언 사용
        assertThat(search.getMatches(), containsMatches(new Match[]{
            new Match(A_TITLE, "practical joke", "or a vast practical joke, though t")
        }));
        List<Match> matches = search.getMatches();
//        assertThat(matches, is(notNullValue())); 프로덕션 코드에서 널을 체크하는것은 맞지만, 테스트에서는 군더더기일 뿐이다.
        assertTrue(matches.size() >= 1);
        stream.close();

        // negative
        URLConnection connection =
            new URL("http://bit.ly/15sYPA7").openConnection();
        InputStream inputStream = connection.getInputStream();
        search = new Search(inputStream, "smelt", A_TITLE);
        search.execute();
        assertTrue(search.getMatches().isEmpty());
        stream.close();
    }
}
```
- 상수명이 ANY_TITLE 과 같은 형태여도 좋다.

## 테스트 냄새 : 부푼 생성
- Search 객체를 생성할 때 InputStream 객체를 넘겨야 한다.
- 테스트 코드를 살펴보면 Search 객체를 만들기 위한 준비 과정이 너무 장황하다.

```java
String pageContent = "There are certain queer times and occasions "
    + "in this strange mixed affair we call life when a man "
    + "takes this whole universe for a vast practical joke, "
    + "though the wit thereof he but dimly discerns, and more "
    + "than suspects that the joke is at nobody's expense but "
    + "his own.";
byte[] bytes = pageContent.getBytes();
ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
// search
Search search = new Search(stream, "practical joke", A_TITLE);
```
- 과도한 생성 로직을 간소화하고, 헬퍼 메소드를 도입해야 한다.
- 정신 산만한 세부 사항을 숨기고 테스트를 더 이해하기 쉽게 해준다.

```java
class SearchTest {

    private static final String A_TITLE = "1";

    /**
     * testSearch 는 무엇을 테스트하려는지 아무런 정보도 제공하지 않는다.
     */
    @Test
    public void testSearch() throws IOException {
        InputStream stream = streamOn("There are certain queer times and occasions "
            + "in this strange mixed affair we call life when a man "
            + "takes this whole universe for a vast practical joke, "
            + "though the wit thereof he but dimly discerns, and more "
            + "than suspects that the joke is at nobody's expense but "
            + "his own.");
        // search
        Search search = new Search(stream, "practical joke", A_TITLE);
        Search.LOGGER.setLevel(Level.OFF);
        search.setSurroundingCharacterCount(10);
        search.execute();
        assertFalse(search.errored());

        // 추상화를 통해 필수적인 개념을 최대화하고 불필요한 세부사항을 감춘다.
        // 사용자 정의 단언 사용
        assertThat(search.getMatches(), containsMatches(new Match[]{
            new Match(A_TITLE, "practical joke", "or a vast practical joke, though t")
        }));
        List<Match> matches = search.getMatches();
//        assertThat(matches, is(notNullValue())); 프로덕션 코드에서 널을 체크하는것은 맞지만, 테스트에서는 군더더기일 뿐이다.
        assertTrue(matches.size() >= 1);
        stream.close();

        // negative
        URLConnection connection =
            new URL("http://bit.ly/15sYPA7").openConnection();
        InputStream inputStream = connection.getInputStream();
        search = new Search(inputStream, "smelt", A_TITLE);
        search.execute();
        assertTrue(search.getMatches().isEmpty());
        stream.close();
    }

    private InputStream streamOn(String pageContent) {
        return new ByteArrayInputStream(pageContent.getBytes());
    }
}
```

## 테스트 냄새 : 다수의 단언
- 테스트 마다 단언 하나로 가는 방향은 좋은 생각이다.
- 때로는 단일 테스트에 사후 조건에 대한 단언이 필요하기도 하지만, 그보다 더 자주 여러 단언이 있다는 것은 테스트 케이스를 두 개 포함하고 있다는 증거
- 테스트를 두 개로 분할해 각각의 맥락에 맞게 행동을 기술해야 한다.

```java
class SearchTest {

    private static final String A_TITLE = "1";

    /**
     * testSearch 는 무엇을 테스트하려는지 아무런 정보도 제공하지 않는다.
     */
    @Test
    public void returnsMatchesShowingContextWhenSearchStringInContent() throws IOException {
        InputStream stream = streamOn("There are certain queer times and occasions "
            + "in this strange mixed affair we call life when a man "
            + "takes this whole universe for a vast practical joke, "
            + "though the wit thereof he but dimly discerns, and more "
            + "than suspects that the joke is at nobody's expense but "
            + "his own.");
        // search
        Search search = new Search(stream, "practical joke", A_TITLE);
        Search.LOGGER.setLevel(Level.OFF);
        search.setSurroundingCharacterCount(10);
        search.execute();
        assertFalse(search.errored());

        // 추상화를 통해 필수적인 개념을 최대화하고 불필요한 세부사항을 감춘다.
        // 사용자 정의 단언 사용
        assertThat(search.getMatches(), containsMatches(new Match[]{
            new Match(A_TITLE, "practical joke", "or a vast practical joke, though t")
        }));
        List<Match> matches = search.getMatches();
//        assertThat(matches, is(notNullValue())); 프로덕션 코드에서 널을 체크하는것은 맞지만, 테스트에서는 군더더기일 뿐이다.
        assertTrue(matches.size() >= 1);
        stream.close();
    }

    /**
     * 테스트에 여러 단언이 존재한다면, 테스트 케이스 두개를 포함하고 있다는 증거
     * 테스트를 분할해야 한다.
     */
    @Test
    public void noMatchesReturnedWhenSearchStringNotInContent() throws IOException {
        // negative
        URLConnection connection =
            new URL("http://bit.ly/15sYPA7").openConnection();
        InputStream stream = connection.getInputStream();
        Search search = new Search(stream, "smelt", A_TITLE);
        search.execute();
        assertTrue(search.getMatches().isEmpty());
        stream.close();
    }


    private InputStream streamOn(String pageContent) {
        return new ByteArrayInputStream(pageContent.getBytes());
    }
}
```

> 테스트 마다 단언 하나를 유지하면 테스트 이름을 깔끔하게 만들기 쉽다.

## 테스트 냄새 : 테스트와 무관한 세부 사항들
- 테스트 실행시 로그를 끄지만, 이는 테스트의 정수를 이해하는데 방해가 될 수 있다.
- 이런 군더더기들을 @Before/@After 로 분할 해야 한다.

```java
class SearchTest {

    private static final String A_TITLE = "1";
    private InputStream stream;

    @BeforeEach
    void setUp() {
        Search.LOGGER.setLevel(Level.OFF);
    }

    @AfterEach
    void tearDown() throws IOException {
        stream.close();
    }

    /**
     * testSearch 는 무엇을 테스트하려는지 아무런 정보도 제공하지 않는다.
     */
    @Test
    public void returnsMatchesShowingContextWhenSearchStringInContent() throws IOException {
        stream = streamOn("There are certain queer times and occasions "
            + "in this strange mixed affair we call life when a man "
            + "takes this whole universe for a vast practical joke, "
            + "though the wit thereof he but dimly discerns, and more "
            + "than suspects that the joke is at nobody's expense but "
            + "his own.");
        // search
        Search search = new Search(stream, "practical joke", A_TITLE);
        search.setSurroundingCharacterCount(10);
        search.execute();
        assertFalse(search.errored());

        // 추상화를 통해 필수적인 개념을 최대화하고 불필요한 세부사항을 감춘다.
        // 사용자 정의 단언 사용
        assertThat(search.getMatches(), containsMatches(new Match[]{
            new Match(A_TITLE, "practical joke", "or a vast practical joke, though t")
        }));
        List<Match> matches = search.getMatches();
//        assertThat(matches, is(notNullValue())); 프로덕션 코드에서 널을 체크하는것은 맞지만, 테스트에서는 군더더기일 뿐이다.
        assertTrue(matches.size() >= 1);
    }

    /**
     * 테스트에 여러 단언이 존재한다면, 테스트 케이스 두개를 포함하고 있다는 증거 테스트를 분할해야 한다.
     */
    @Test
    public void noMatchesReturnedWhenSearchStringNotInContent() throws IOException {
        // negative
        URLConnection connection =
            new URL("http://bit.ly/15sYPA7").openConnection();
        stream = connection.getInputStream();
        Search search = new Search(stream, "smelt", A_TITLE);
        search.execute();
        assertTrue(search.getMatches().isEmpty());
    }

    private InputStream streamOn(String pageContent) {
        return new ByteArrayInputStream(pageContent.getBytes());
    }
}
```

## 테스트 냄새 : 잘못된 조직
- 테스트에서 어느 부분이 준비/실행/단언 인지 아는 것은 테스트를 빠르게 인지할 수 있게 한다.
- AAA 를 활용해 의도를 분명하게 해야 한다.

```java
@Test
public void returnsMatchesShowingContextWhenSearchStringInContent() throws IOException {
    // 준비
    stream = streamOn("There are certain queer times and occasions "
        + "in this strange mixed affair we call life when a man "
        + "takes this whole universe for a vast practical joke, "
        + "though the wit thereof he but dimly discerns, and more "
        + "than suspects that the joke is at nobody's expense but "
        + "his own.");
    // search
    Search search = new Search(stream, "practical joke", A_TITLE);
    search.setSurroundingCharacterCount(10);

    // 실행
    search.execute();

    // 단언
    assertFalse(search.errored());
    // 추상화를 통해 필수적인 개념을 최대화하고 불필요한 세부사항을 감춘다.
    // 사용자 정의 단언 사용
    assertThat(search.getMatches(), containsMatches(new Match[]{
        new Match(A_TITLE, "practical joke", "or a vast practical joke, though t")
    }));
//        assertThat(matches, is(notNullValue())); 프로덕션 코드에서 널을 체크하는것은 맞지만, 테스트에서는 군더더기일 뿐이다.
    assertFalse(search.getMatches().isEmpty());
}
```

## 테스트 냄새 : 암시적 의미
- 각 테스트가 분명하게 가져야할 것은, **왜 그런 결과를 기대하는가 ?** 이다.
- 테스트 준비와 단언 부분을 상호연관 지을 수 있어야 한다.
- 단언이 기대하는 이유가 분명하지 않다면 코드를 읽는 사람은 해답을 얻기 위해 다른 코드를 뒤져가며 시간을 낭비할 것이다.
- 테스트 데이터가 아무리 멋지더라도 테스트를 이해하려고 노력을 많이 해야 한다면 그것은 좋지 않다..

```java
class SearchTest {

    private static final String A_TITLE = "1";
    private InputStream stream;

    @BeforeEach
    void setUp() {
        Search.LOGGER.setLevel(Level.OFF);
    }

    @AfterEach
    void tearDown() throws IOException {
        stream.close();
    }

    /**
     * testSearch 는 무엇을 테스트하려는지 아무런 정보도 제공하지 않는다.
     */
    @Test
    public void returnsMatchesShowingContextWhenSearchStringInContent() throws IOException {
        stream = streamOn("1234567890search term1234567890");
        // search
        Search search = new Search(stream, "search term", A_TITLE);
        search.setSurroundingCharacterCount(10);

        search.execute();

        assertFalse(search.errored());
        // 추상화를 통해 필수적인 개념을 최대화하고 불필요한 세부사항을 감춘다.
        // 사용자 정의 단언 사용
        assertThat(search.getMatches(), containsMatches(new Match[]{
            new Match(A_TITLE, "search term", "1234567890search term1234567890")
        }));
//        assertThat(matches, is(notNullValue())); 프로덕션 코드에서 널을 체크하는것은 맞지만, 테스트에서는 군더더기일 뿐이다.
        assertFalse(search.getMatches().isEmpty());
    }

    /**
     * 테스트에 여러 단언이 존재한다면, 테스트 케이스 두개를 포함하고 있다는 증거 테스트를 분할해야 한다.
     */
    @Test
    public void noMatchesReturnedWhenSearchStringNotInContent() throws IOException {
        // negative
        stream = streamOn("any text");
        Search search = new Search(stream, "text that doesn't match", A_TITLE);

        search.execute();

        assertTrue(search.getMatches().isEmpty());
    }

    private InputStream streamOn(String pageContent) {
        return new ByteArrayInputStream(pageContent.getBytes());
    }
}
```
- 검색어를 포함하면서 슬쩍 보아도 이해가 가능한 내용으로 변경
- 주변 맥락 정보를 명시적으로 개수를 세지 않아도 되도록 변경

> 테스트를 걸쳐 상호 관련성을 향상시키는 방법은 무한하다. <br/>
> 의미 있는상수, 좋은 변수이름, 좋은 테스트데이터 등등..