package fr.reviewbot.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UnsetTest {

    @Test
    void nullBecomesTheInitialValue() {
        assertEquals("", Unset.text(null));
        assertEquals(Unset.INSTANT, Unset.instant(null));
        assertSame(Unset.PERCENT, Unset.percent(null));
    }

    @Test
    void realValuesAreKept() {
        Instant now = Instant.parse("2026-11-13T10:00:00Z");

        assertEquals("abc", Unset.text("abc"));
        assertEquals(now, Unset.instant(now));
        assertEquals(new BigDecimal("42.5"), Unset.percent(new BigDecimal("42.5")));
    }

    @Test
    void isSavedDependsOnTheId() {
        assertFalse(Unset.isSaved(Unset.ID));
        assertTrue(Unset.isSaved(12L));
    }

    @Test
    void isSetDetectsRealDates() {

        assertFalse(Unset.isSet(Unset.INSTANT));
        assertTrue(Unset.isSet(Instant.parse("2026-11-13T10:00:00Z")));
    }

    @Test
    void isKnownDetectsRealPercentages() {
        assertFalse(Unset.isKnown(Unset.PERCENT));
        assertFalse(Unset.isKnown(new BigDecimal("-1.00")));
        assertTrue(Unset.isKnown(BigDecimal.ZERO));
    }
}
