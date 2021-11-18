package me.june.domain;

import static me.june.domain.ConstrainsSidesTo.constrainsSidesTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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