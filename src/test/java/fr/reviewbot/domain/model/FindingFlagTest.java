package fr.reviewbot.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FindingFlagTest {

    @Test
    void ofCreatesAFreshFlag() {
        FindingFlag flag = FindingFlag.of(7, "alice", "faux positif, c'est voulu");

        assertEquals(Unset.ID, flag.id());
        assertEquals(7, flag.findingId());
        assertEquals("alice", flag.flaggedBy());
        assertEquals(Unset.INSTANT, flag.flaggedAt());
    }

    @Test
    void nullReasonBecomesEmptyText() {
        assertEquals(Unset.TEXT, FindingFlag.of(7, "alice", null).reason());
    }

    @Test
    void flaggedByIsRequired() {
        assertThrows(IllegalArgumentException.class, () -> FindingFlag.of(7, null, "x"));
        assertThrows(IllegalArgumentException.class, () -> FindingFlag.of(7, "  ", "x"));
    }
}