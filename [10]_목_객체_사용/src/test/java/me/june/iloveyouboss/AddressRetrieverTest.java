package me.june.iloveyouboss;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

class AddressRetrieverTest {

    /**
     * 람다를 활용한 스텁 구현
     */
    Http http = (String url) ->
        "{\"address\":{"
            + "\"house_number\":\"324\","
            + "\"road\":\"North Tejon Street\","
            + "\"city\":\"Colorado Springs\","
            + "\"state\":\"Colorado\","
            + "\"postcode\":\"80903\","
            + "\"country_code\":\"us\"}"
            + "}";


    @Test
    void answerAppropriateAddressForValidCoordinates() throws Exception {
        AddressRetriever retriever = new AddressRetriever(http);
        Address address = retriever.retrieve(38.0, -104.0);

        assertThat(address.houseNumber, equalTo("324"));
        assertThat(address.road, equalTo("North Tejon Street"));
        assertThat(address.city, equalTo("Colorado Springs"));
        assertThat(address.state, equalTo("Colorado"));
        assertThat(address.zip, equalTo("80903"));
    }

}