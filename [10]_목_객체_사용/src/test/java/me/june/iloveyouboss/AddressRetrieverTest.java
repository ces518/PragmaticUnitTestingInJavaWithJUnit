package me.june.iloveyouboss;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
//    Http http = (String url) -> {
//        if (!url.contains("lat=38.000000&lon=-104.000000")) {
//            fail("url" + url + " does not contains correct params");
//        }
//        return MOCK_RESULT;
//    };

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