# 10장 목 객체 사용
- 목 객체를 도입해 고통을 주는 협력자에 대한 의존성을 끊는 방법과 도구 활용법 살펴보기

## 테스트 도전 과제

```java
public class AddressRetriever {

    public Address retrieve(double latitude, double longitude)
        throws IOException, ParseException {
        String parms = String.format("lat=%.6f&lon=%.6f", latitude, longitude);
        String response = new HttpImpl().get(
            "http://open.mapquestapi.com/nominatim/v1/reverse?format=json&"
                + parms);

        JSONObject obj = (JSONObject)new JSONParser().parse(response);

        JSONObject address = (JSONObject)obj.get("address");
        String country = (String)address.get("country_code");
        if (!country.equals("us"))
            throw new UnsupportedOperationException(
                "cannot support non-US addresses at this time");

        String houseNumber = (String)address.get("house_number");
        String road = (String)address.get("road");
        String city = (String)address.get("city");
        String state = (String)address.get("state");
        String zip = (String)address.get("postcode");
        return new Address(houseNumber, road, city, state, zip);
    }
}
```
- 위도 경로 좌표를 기반으로 Address 를 반환하는 클래스
- 얼핏 봤을때는 테스트 작성하는것이 쉬워보이지만 사실 그렇지 않다.
- 실제 호출에 대한 테스트는, 나머지 대다수의 빠른 테스트에 비해 속도가 느릴 것
- 해당 API 가 항상 가용한지 보장할 수 없다, 통제권 밖에 있다.

## 번거로운 동작을 스텁으로 대체
- HTTP 호출에서 반환되는 JSON 응답을 이용해 Address 객체를 생성하는 로직을 검증하는데 집중해야 한다.
- 그러기 위해서는 HttpImpl 의 get 메소드 동작을 변경할 필요가 있다.
- 테스트 용도의 값을 반환하는 구현체를 사용하도록 한다. (이를 스텁이라고 함)

```java
class AddressRetrieverTest {

    private static final String MOCK_RESULT = "{\"address\":{"
        + "\"house_number\":\"324\","
        + "\"road\":\"North Tejon Street\","
        + "\"city\":\"Colorado Springs\","
        + "\"state\":\"Colorado\","
        + "\"postcode\":\"80903\","
        + "\"country_code\":\"us\"}"
        + "}";

    /**
     * 람다를 활용한 스텁 구현
     */
    Http http = (String url) -> {
        if (!url.contains("lat=38.000000&lon=-104.000000")) {
            fail("url" + url + " does not contains correct params");
        }
        return MOCK_RESULT;
    };

    @Test
    void answerAppropriateAddressForValidCoordinates() throws Exception {
        ArressRetriever retriever = new ArressRetriever(http);
        Address address = retriever.retrieve(38.0, -104.0);

        assertThat(address.houseNumber, equalTo("324"));
        assertThat(address.road, equalTo("North Tejon Street"));
        assertThat(address.city, equalTo("Colorado Springs"));
        assertThat(address.state, equalTo("Colorado"));
        assertThat(address.zip, equalTo("80903"));
    }

}
```
- 스텁을 정의하고, 이를 프로덕션 구현 대신 스텁을 사용하도록 기존 클래스를 변경해야 한다.
- 여기서 의존성 주입 기법을 활용한다.

```java
public class AddressRetriever {

    private Http http;

    public AddressRetriever(Http http) {
        this.http = http;
    }

    public Address retrieve(double latitude, double longitude)
        throws IOException, ParseException {
        String parms = String.format("lat=%.6f&lon=%.6f", latitude, longitude);
        String response = http.get(
            "http://open.mapquestapi.com/nominatim/v1/reverse?format=json&"
                + parms);

        JSONObject obj = (JSONObject)new JSONParser().parse(response);

        JSONObject address = (JSONObject)obj.get("address");
        String country = (String)address.get("country_code");
        if (!country.equals("us"))
            throw new UnsupportedOperationException(
                "cannot support non-US addresses at this time");

        String houseNumber = (String)address.get("house_number");
        String road = (String)address.get("road");
        String city = (String)address.get("city");
        String state = (String)address.get("state");
        String zip = (String)address.get("postcode");
        return new Address(houseNumber, road, city, state, zip);
    }
}
```

## 테스트를 위한 설계 변경
- 테스트를 위해 설계를 조금 변경하게 되었다.
- 이는 나쁜 일인가 ? -> NO
- 테스트 하기 힘들다면, 오히려 그것이 나쁜 설계일 확률이 높다.
- 좋은 설계라면 테스트하기 쉽다.

## 목 도구를 활용한 단순화
- 스텁을 목으로 변환하는 작업을 진행
- 그러기 위해서는 다음 일들이 필요하다.
  - 테스트에서 어떤 인자를 기대하는지 명시
  - get 메소드에 넘겨진 인자들을 잡아 저장
  - get 메소드에 저장된 인자들이 기대하는 인자들인지 테스트 완료시점에 검증
- 이런 역할을 수행하는 목 객체를 매번 직접 구현하는 것은 과한 일
- 이를 빠르게 만들 수 있도록 도와주는 도구의 도입이 필요하다. -> 모키토

```java
class AddressRetrieverTest {

    private static final String MOCK_RESULT = "{\"address\":{"
        + "\"house_number\":\"324\","
        + "\"road\":\"North Tejon Street\","
        + "\"city\":\"Colorado Springs\","
        + "\"state\":\"Colorado\","
        + "\"postcode\":\"80903\","
        + "\"country_code\":\"us\"}"
        + "}";

    @Mock
    Http http;

    @InjectMocks
    AddressRetriever retriever;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void answerAppropriateAddressForValidCoordinates() throws Exception {
        when(http.get(contains("lat=38.000000&lon=-104.000000"))).thenReturn(MOCK_RESULT);

        Address address = retriever.retrieve(38.0, -104.0);

        assertThat(address.houseNumber, equalTo("324"));
        assertThat(address.road, equalTo("North Tejon Street"));
        assertThat(address.city, equalTo("Colorado Springs"));
        assertThat(address.state, equalTo("Colorado"));
        assertThat(address.zip, equalTo("80903"));
    }

}
```

## 목 사용시 중요한 것
- 모키토의 when..then 구조를 사용해 테스트 의 기대사항을 한 줄로 표현할 수 있다.
- 목을 사용한 테스트는 진행하기 원하는 내용을 **분명하게 기술** 해야 한다.
- 프로덕션 코드를 직접 테스트하지 않는 다는것을 기억해야 한다.
- 목을 도입하면, 커버리지에서 갭을 형성할 수 있음을 인지해야 한다.
- 실제 통합 테스트도 필요한 부분이다.
  - 목은 단위테스트의 구멍을 만들 수 있기 때문에 통합 테스트를 작성해 이를 막아야 한다.