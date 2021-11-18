package me.june.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import me.june.domain.Bearing.BearingOutOfRangeException;
import org.junit.jupiter.api.Test;

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