package me.june.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SparseArrayTest {

    private SparseArray<Object> array;

    @BeforeEach
    void setUp() {
        array = new SparseArray<>();
    }

    @Test
    void handlesInsertionInDescendingOrder() {
        array.put(7, "seven");
        array.checkInvariants();
        array.put(6, "six");
        array.checkInvariants();
        assertEquals("six", array.get(6));
        assertEquals("seven", array.get(7));
    }
}