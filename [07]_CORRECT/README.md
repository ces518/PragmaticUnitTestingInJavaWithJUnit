# 7장 CORRECT
- 단위 테스트는 종종 경계 조건들에 관계된 결함들을 미연에 방지하는데 도움이 된다.
- CORRECT 는 테스트를 만들때 고려해야 할 경계 조건을 생각하는데 도움이 된다.
  - Conformance (준수) : 값이 기대한 양식을 준수하고 있는가
  - Ordering (순서) : 값의 집합이 가지는 순서가 정확한가
  - Range (범위) : 이성적인 최솟값과 최댓값 범위내에 있는가 
  - Reference (참조) : 코드 자체에서 통제할 수 없는 외부 참조를 포함하는가 
  - Existence (존재) : 값이 존재하는가 
  - Cardinality (기수) : 정확한 값들이 충분히 들어있는가
  - Time (절대적 혹은 상대적 시간) : 기대한 순서대로 동작하는가

## Conformance (준수) : 값이 기대한 양식을 준수하고 있는가
- 많은 데이터 요소는 특정 양식을 따라야 한다.
- 이메일 주소라면 다음과 같은 형식이다.
  - name@somedomain
- 보통 이메일 주소를 파싱하여 이름만 추출하거나 하는 등의 작업이 있을 수 있다.
- 하지만 이때 @ 가 없거나 이름이 비어있는 경우에도 대처해야 한다.
- 이때 null 을 반환하거나 예외를 던지는 등의 설계는 개발자의 몫
- 각 경계조건이 발생했을때 어떤 일이 일어나는지 알려주는 테스트코드를 작성해야 한다.

## Ordering (순서) : 값의 집합이 가지는 순서가 정확한가
- 데이터 순서 혹은 컬렉션에 있는 데이터 하나의 위치는 코드게 쉽게 잘못될 수 있는 조건에 해당한다.
- 각 회사들이 조건에 잘 맞는지 여부를 점수로 목록화 하는 기능이 있다고 가정
- 그 순으로 좋은 회사와 최악의 회사를 나열해야한다.
- 이런 문제들은 잘못되기 쉽다.

## Range (범위) : 이성적인 최솟값과 최댓값 범위내에 있는가 
- 자바 기본 자료형으로 변수를 만들때 대부분은 필요한 것 이상의 용량을 가진다.
- int 타입으로 사람의 나이를 표현한다면, 적어도 수백만 세기를 표현할 만큼 충분하다.
- 음수인 나이가 생기거나 말도안되게 많은 나이를 가진 사람이 생길 수 있다.
- 기본형의 과도한 사용에 대한 코드 냄새를, **기본형 중독 (primitive obsession)** 이라고 한다.
- 객체지향 언어의 장점은 사용자 정의 추상화 클래스를 만들 수 있다는 것이다.
- 다음 예시를 살펴보자.

`Bearing`

```java
public class Bearing {

    public static final int MAX = 359;
    private int value;

    public Bearing(int value) {
        if (value < 0 || value > MAX) {
            throw new BearingOutOfRangeException();
        }
        this.value = value;
    }

    public int value() {
        return value;
    }

    public int angleBetween(Bearing bearing) {
        return value - bearing.value();
    }

    static class BearingOutOfRangeException extends RuntimeException {}
}
```
- 원은 360도 이다.
- 이동 방향을 자바 기본형으로 다루기 보다는, **Bearing** 이라는 클래스를 정의하고, 범위를 제약하는 로직을 캡슐화 할 수 있다.
- BearingTest 클래스는, 유효하지 않은 값으로 방위를 지정하면 어떻게 될지 보여준다.
- 방위를 **Bearing** 이라는 클래스로 다룸으로 인해 클라이언트 코드가 범위를 벗어나지 않도록 할 수 있다.

`BearingTest`

```java
class BearingTest {

    @Test
    void throwsOnNegativeNumber() {
        assertThrows(BearingOutOfRangeException.class, () -> new Bearing(-1));
    }

    @Test
    void throwsWhenBearingTooLarge() {
        assertThrows(BearingOutOfRangeException.class, () -> new Bearing(Bearing.MAX + 1));
    }

    @Test
    void answersValidBearing() {
        assertEquals(Bearing.MAX, new Bearing(Bearing.MAX).value());
    }

    @Test
    void answersAngleBetweenItAndAnotherBearing() {
        assertEquals(3, new Bearing(15).angleBetween(new Bearing(12)));
    }

    @Test
    void angleBetweenIsNegativeWhenThisBearingSmaller() {
        assertEquals(-3, new Bearing(12).angleBetween(new Bearing(15)));
    }

}
```

`Rectangle`

```java
public class Rectangle {

    private Point origin;
    private Point opposite;

    public Rectangle(Point origin, Point oppositeCorner) {
        this.origin = origin;
        this.opposite = oppositeCorner;
    }

    public Rectangle(Point origin) {
        this.opposite = this.origin = origin;
    }

    public int area() {
        return (int)(Math.abs(origin.x - opposite.x) *
            Math.abs(origin.y - opposite.y));
    }

    public void setOppositeCorner(Point opposite) {
        this.opposite = opposite;
    }

    public Point origin() {
        return origin;
    }

    public Point opposite() {
        return opposite;
    }

    @Override
    public String toString() {
        return "Rectangle(origin " + origin + " opposite " + opposite + ")";
    }
}
```
- 그 외에도 Rectangle 이라는 좌표를 정의한 클래스를 만들 수도 있다.
- 점 두개룰 x,y 라는 튜플로 정의하고, 각 변의 100 이하여야 한다.
- 좌표에 영향을 줄 수 있는 동작에 대해 범위를 단언하고 싶을 수 있다.
- 이때 이를 활용해 x,y 좌표의 범위를 유효하게 유지할 수 있다.

`불변성을 검사하는 사용자 정의 매처`

```java
class RectangleTest {

    private Rectangle rectangle;

    @AfterEach
    void ensureInvariant() {
        assertThat(rectangle, constrainsSidesTo(100));
    }

    @Test
    public void answersArea() {
        rectangle = new Rectangle(new Point(5, 5), new Point (15, 10));
        assertEquals(50, rectangle.area());
    }
}
```
- 매 테스트마다 @AfterEach 를 사용해, 좌표가 유효한지를 검사한다.
- 이때 **사용자 정의 매처** 를 사용했다.
- 사용자 정의 매처를 구현하려면 `org.hamcrest.TypeSafeMatcher` 를 구현해야 한다.
- 
```java
public class ConstrainsSidesTo extends TypeSafeMatcher<Rectangle> {

    private int length;

    public ConstrainsSidesTo(int length) {
        this.length = length;
    }

    /**
     * 단언 실패시 제공할 메세지
     */
    @Override
    public void describeTo(Description description) {
        description.appendText("both sides must be <= " + length);
    }

    /**
     * 메소드 제약에 해당
     */
    @Override
    protected boolean matchesSafely(Rectangle rect) {
        return
            Math.abs(rect.origin().x - rect.opposite().x) <= length &&
                Math.abs(rect.origin().y - rect.opposite().y) <= length;
    }

    /**
     * 매처 인스턴스를 반환하는 팩토리 메소드를 제공해야 한다.
     * 단언 작성시 이를 활용함
     */
    @Factory
    public static <T> Matcher<Rectangle> constrainsSidesTo(int length) {
        return new ConstrainsSidesTo(length);
    }
}
```
- describeTo() : 단언 실패시 제공할 메세지를 구현
- matchesSafely() : **제약조건** 
- 또한 사용자 정의 매처 클래스는, 매처 인스턴스를 반환하는 팩토리 메소드를 제공해야 한다.
- 이를 위해 constrainsSidesTo 라는 메소드로 구현했다.

`인덱스를 다룰때 고려해야할 시나리오`
- 시작과 마지막 인덱스는 동일하면 안된다.
- 시작이 마지막보다 크면 안된다.
- 인덱스는 음수가 아니어야 한다.
- 인덱스가 허용된 것보다 크면 안된다.
- 개수가 실제 항목 개수와 일치해야 한다.

## Reference (참조) : 코드 자체에서 통제할 수 없는 외부 참조를 포함하는가
- 메소드 테스트시 고려사항은 다음과 같다.
  - 범위를 넘어서는 것을 참조하고 있는가 ?
  - 외부 의존성은 무엇인가 ?
  - 특정 상태에 있는 객체를 의존하고 있는가 ?
  - 반드시 존재해야 하는 그 외 조건들을 만족하고 있는가 ?
- 고객 계정의 히스토리를 표현하는 앱은 **고객이 먼저 로그인한 상태** 여야 한다.
- 차량의 변속기를 **주행** 에서 **주차** 로 변경할 때는 먼저 차를 멈추어야 한다.
- 어떤 상태에 대해 **가정** 할 때는 그 가정이 맞지 않은 경우 코드가 합리적으로 잘 동작하는지 검사해야 한다.

`Transmission`

```java
public class Transmission {

    private Gear gear;
    private Moveable moveable;

    public Transmission(Moveable moveable) {
        this.moveable = moveable;
    }

    public void shift(Gear gear) {
        // begs for a state-machine implementation
        if (moveable.currentSpeedInMph() > 0 && gear == Gear.PARK) {
            return;
        }
        this.gear = gear;
    }

    public Gear getGear() {
        return gear;
    }
}
```

```java
/**
 * 사전조건 : 자동차가 달릴수 있는 상태가 되어야함
 * - 기어를 파킹 상태로 놓으려면 반드시 정지상태어야 한다.
 * 사전조건이 맞지 않은 상태에서 파킹 상태로 요청할 경우 -> 무시해야함 (우아하게 동작하기 위함)
 *
 * 사후조건 : 코드가 참을 유지해야 하는 조건
 * - 사이드이펙트를 검사해야할 경우도 있다.
 * - brakeToStop 을 호출하면 속도가 0으로 지정되는 사이드이펙트
 *
 */
class TransmissionTest {

    Transmission transmission;
    Car car;

    @BeforeEach
    void setUp() {
        car = new Car();
        transmission = new Transmission(car);
    }

    /**
     * 가속후에는 기어가 계속 "드라이브" 이다.
     */
    @Test
    void remainsInDriverAfterAcceleration() {
        transmission.shift(Gear.DRIVE);
        car.accelerateTo(35);
        assertEquals(Gear.DRIVE, transmission.getGear());
    }

    /**
     * 운전중엔 기어를 "파킹" 으로 놓을 수 없다.
     */
    @Test
    void ignoreShiftToParkWhileInDrive() {
        transmission.shift(Gear.DRIVE);
        car.accelerateTo(30);

        transmission.shift(Gear.PARK);

        assertEquals(Gear.DRIVE, transmission.getGear());
    }

    /**
     * 브레이크를 밟은 후 기어를 "파킹" 으로 놓을 수 있다.
     */
    @Test
    void allowsShiftToParkWhenNotMoving() {
        transmission.shift(Gear.DRIVE);
        car.accelerateTo(30);
        car.brakeToStop();

        transmission.shift(Gear.PARK);

        assertEquals(Gear.PARK, transmission.getGear());
    }
}
```
- 사전조건 : 자동차가 달릴 수 있는 상태가 되어야 한다.
  - 자동차가 반드시 정지 상태여야만 변속기를 주차로 놓을 수 있다.
- 사후 조건 : 코드가 참을 유지해야 하는 조건들
  - 테스트의 단언으로 명시해야 한다.
  - 단순히 메소드의 반환값일 수도 있으며, 메소드 호출의 결과로 상태 가 변화는 경우 (사이드 이펙트) 일 수도 있따.

## Existence (존재) : 값이 존재하는가 
- 스스로에게 **"주어진 값이 존재하는가 ?"** 라고 물어봄으로써 많은 잠재적 결함을 발견할 수 있다.
- 어떤 인자를 허용하거나, 필드를 유지하는 메소드에 대해 값이 null, 0 또는 비어있는 경우라면 어떤일이 발생할지 생각해보아야 한다.
- 자바 라이브러리는 데이터가 존재하지 않거나 초기화되지 않은 상태에서 사용할 경우 **숨막히게 예외를 던지는 경향** 이 있다.
  - 이는 예외 발생시 매우 긴 스택 트레이스가 일어나는데 이를 표현한 것
- null 값 만으로는 문제 원인을 이해하기 어려울 수 있다.
- "프로파일 명이 설정되지 않음" 과 같이 특정 메세지를 예외에서 알려주면 문제를 추적하는 과정을 단순하게 만들 수 있다.
- 행복경로 테스트가 아닌 **불행 경로 테스트에 집중** 해야 한다.
- null 을 반환한다거나, 기대하는 파일이 없다거나, 네트워크가 다운되었을때 어떤일이 발생할지에 대한 테스트를 작성해야 한다.

## Cardinality (기수) : 정확한 값들이 충분히 들어있는가
- 울타리 기둥 오류는 한 끗 차이로 발생하는 수 많은 경우 중 한 가지를 의미한다.
- 개수를 어떻게 잘 세어 테스트할지 고민하고, 얼마나 많은지 확인할 필요가 있다.
- 집합을 다루는 개수는 다음 세 가지 경우에 흥미롭다.
    - 0
    - 1
    - N
- 이것을 0-1-N 법칙이라고 한다.
- 특정 컬렉션을 다룬다면 보통 10개, 100개 또는 1000개를 다루는것과 유사하다.
- 테스트코드는 0, 1, N 이라는 경계조건에만 집중하고, N 은 비즈니스 요구사항에 따라 바뀔 수 있다.

## Time (절대적 혹은 상대적 시간) : 기대한 순서대로 동작하는가
- 시간에 대해서 살펴볼 측면은 다음과 같다.
  - 상대적 시간 (시간 순서)
  - 절대적 시간 (측정 시간)
  - 동시성 문제
- 상대적 시간은 메소드 호출 순서에 대한 문제를 포함한다.
  - 로그아웃을 하기위해서는, 로그인이 되어야 한다는 등..
  - 타임아웃 문제도 포함될 수 있따.
- 절대적 시간은 벽시계 시간 문제가 될 수도 있다.
  - UTC 와 DST 에 대한 문제..
  - 이에 대한 해결책은 시스템 시계에 의존하는 테스트를 작성하는  것이다.
  - 테스트 코드에 통제할 수 있는 곳에서 얻어오는 시간을 사용하도록 변경해야 한다.
- 동시성 문제는 많은 문제를 야기한다.
  - 동시에 다수의 스레드가 접근하면 어떻게 되는지 ?
  - 전역 인스턴스의 데이터나 메소드에 동기화를 어떻게 할 것인지 ?