package fr.reviewbot.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UnsetNumberTest {

    @Test
    void negativeNumbersBecomeUnset() {
        assertEquals(Unset.NUMBER, Unset.number(-5));
        assertEquals(Unset.NUMBER, Unset.number(Unset.NUMBER));
    }

    @Test
    void validNumbersAreKept() {
        assertEquals(0, Unset.number(0));
        assertEquals(42, Unset.number(42));
    }

    @Test
    void isKnownDistinguishesUnsetFromRealValues() {
        assertFalse(Unset.isKnown(BigDecimal.valueOf(Unset.NUMBER)));
        assertTrue(Unset.isKnown(BigDecimal.valueOf(0)));
        assertTrue(Unset.isKnown(BigDecimal.valueOf(7)));
    }
}