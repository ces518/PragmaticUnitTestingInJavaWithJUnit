package me.june.domain;

import org.hamcrest.*;

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
