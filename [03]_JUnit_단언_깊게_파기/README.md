# 3장 JUnit 단언 깊게 파기

## JUnit 단언
- JUnit 단언 -> 테스트에 사용가능한 정적 메소드 호출
- 각 단언은 어떤 조건이 참인지 검증하는 방법이며, 참이 아니라면 테스트는 중단되고 실패를 보고한다.
- JUnit 은 두 가지 단언 스타일을 제공한다.
- 전통적인 단언 스타일은 JUnit 패키지에 포함되어 있으며, 좀 더 표현력이 좋은 단언은 햄크레스트 라고 알려져 있다.
- 두 가지 방식을 섞어 쓰기보단, 한 가지 방식으로 사용하는 것이 좋다.

`AssertTrue`

```java
@Test
void hasPositiveBalance() {
    account.deposit(50);
    assertTrue(account.hasPositiveBalance());
}

@Test
void depositIncreasesBalance() throws Exception {
    int initialBalance = account.getBalance();
    account.deposit(100);
    assertTrue(account.getBalance() > initialBalance);
}
```
- 가장 기본적인 단언인 assertTrue()
- 주어진 조건이 참일 경우 테스트를 통과한다.

`AssertThat`
- assertThat 은 명확한 값을 비교할때 사용한다.
- 대부분의 경우 기대하는 값과 반환된 실제 값을 비교하는 식의 테스트를 작성하기 때문에 많이 사용될 것

```java
assertThat(account.getBalance(), equalTo(100));
```
- 일반적인 단언 보다는 햄크레스트 단언이 실패할 경우 오류 메세지에서 더 많은 정보를 알 수 있다.

## 중요한 햄크레스트 매처 살펴보기
- JUnit 패키지에 포함되어 있는 햄크레스트 CoreMatchers 는 바로 사용가능한 매처들을 제공한다.
- 매처를 몇개만 사용해도 되지만, 더 많은 햄크레스트 매처를 사용할수록 테스트코드의 표현력은 깊어 진다.

`자바 배열 혹은 컬렉션 비교`

```java
assertThat(new String[] {"a", "b", "c"}, equalTo(new String[] {"a", "b"}));
```
- 배열 또는 컬렉션 비교시 equalTo 를 사용하며, 예상한대로 동작합니다.
- 경우에 따라 `is` 장식자 (decorator) 를 추가해 매처 표현의 가독성을 높을 수 있다.
- is 는 단지 넘겨받은 매처를 반환할 뿐 아무것도 하지 않는다.

```java
Account account = new Account("my big fat acct");
assertThat(account.getName(), is(equalTo("my big fat acct")));
```
- 부정하는 단언이 필요하다면 `not` 매처를 사용하면 된다.

```java
Account account = new Account("my big fat acct");
assertThat(account.getName(), not(equalTo("hello ~")));
```
- null 검사시 다음과 같은 방식을 사용합니다.

```java
assertThat(account.getName(), is(not(nullValue())));
assertThat(account.getName(), is(notNullValue()));
```

- 햄크레스트 매처를 사용하면 다음과 같은 처리가 가능하다.
  1. 객체 타입검사
  2. 두 객체 참조가 동일한 인스턴스 인지 검사
  3. 여러 매처를 조합해 둘다 혹은 어떤 것이든 성공하는지 검사
  4. 어떤 컬렉션이 요소를 포함하거나 조건에 부합하는지 검사
  5. 어떤 컬렉션이 아이템 몇개를 모두 포함하는지 검사
  6. 어떤 컬렉션에 있는 모든 요소가 매처를 준수하는지 검사

`부동 소수점 비교`
- 컴퓨터는 모든 부동소수점을 표현할 수 없다.
- 때문에 자바의 부동소수점 타입 (float/double) 은 근사치로 표현해야 한다.

```java
assertTrue(Math.abs((2.32 * 3) - 6.96) < 0.0005);
```
- 때문에 float, double 비교시엔 오차범위를 지정해야 한다.
- 위와 같이 작성도 가능하지만 이는 가독성이 좋지 않다.

```java
assertThat(2.32 * 3, closeTo(6.96, 0.0005));
```
- 햄크레스트에서 제공하는 closeTo 매처를 사용하면 훨씬 수월해진다.

`단언 설명`
- 모든 JUnit 단언의 형식에는 message 라는 인자가 있다.
- 이는 단언의 근거를 설명해주는 용도

```java
@Test
void testWithWorthlessAssertionComment() {
    account.deposit(50);
    assertThat("account balance is 100", account.getBalance(), equalTo(50));
}
```
- 하지만 해당 주석이 테스트를 정확하게 설명한다는 보장은 없다.
- 이 보다 더 좋은 방법은 테스트 코드 자체만으로 이해할 수 있게 작성하는 것

## 예외 테스트
- JUnit 은 세 가지 방식으로 기대한 예외가 발생하는지에 대한 테스트를 할 수 있다.

`애노테이션 방식`

```java
@Test(expected = InsufficientFundsException.class)
void throwsWhenWithdrawingTooMuch() {
    account.withdraw(100);
}
```

`고전적인 방식`

```java
@Test
void throwsWhenWithdrawingTooMuch() {
    try {
        account.withdraw(100);
        fail();
    } catch (InsufficientFundsException expected) {}
}
```
- try-catch 의 흐름제어를 이용한 방식
- 예외가 발생하지 않으면 테스트를 의도적으로 실패시키고, 기대한 예외가 발생한다면 테스트는 통과한다.
- 예외 변수명을 expected 로 지정하여 코드를 읽는사람에게 예외를 예상했고 잡았음을 강조할 수 있다.

`Rule 방식`
- JUnit Rule 은 AOP 와 유사한 기능을 제공한다.
- 자동으로 테스트 집합에 대한 종단 관심사를 부착할 수 있따.
- 이는 단순한 방식과 고전 방식의 장점만을 가져와 사용할 수 있다.

```java
@Rule
ExpectedException thrown = ExpectedException.none();

@Test
void exceptionRule() {
    thrown.expect(InsufficientFundsException.class);
    thrown.expectMessage("balance only 0");
    
    account.withdraw(100);
}
```

> 위 방식들 외에도 JUni5 로 올라가면서 추가된 방식들도 있고, Fishbowl 등 여러 라이브러리를 활용할 수 있다.