package me.june.util;

import static me.june.util.ContainsMatches.containsMatches;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @Test
    public void returnsErroredWhenUnableToReadStream() {
        stream = createStreamThrowingErrorWhenRead();
        Search search = new Search(stream, "", "");

        search.execute();

        assertTrue(search.errored());
    }

    @Test
    public void erroredReturnsFalseWhenReadSucceed() {
        stream = streamOn("");
        Search search = new Search(stream, "", "");

        search.execute();

        assertFalse(search.errored());
    }

    private InputStream createStreamThrowingErrorWhenRead() {
        return new InputStream() {

            @Override
            public int read() throws IOException {
                throw new IOException();
            }
        };
    }

    private InputStream streamOn(String pageContent) {
        return new ByteArrayInputStream(pageContent.getBytes());
    }
}