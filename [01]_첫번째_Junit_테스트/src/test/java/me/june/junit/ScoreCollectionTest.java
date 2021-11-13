package me.june.junit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ScoreCollectionTest {

    @Test
    void test() {
        fail("Not yet implemented");
    }

    @Test
    void answersArithmeticMeanOfTwoNumbers() {
        // given
        ScoreCollection collection = new ScoreCollection();
        collection.add(() -> 5);
        collection.add(() -> 7);

        // when
        int actualResult = collection.arithmeticMean();

        // then
        assertEquals(6, actualResult);
    }
}