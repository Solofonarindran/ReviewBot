package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.domain.model.ModalEnum;
import fr.reviewbot.domain.model.Unset;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SqlTest {
    @Test
    void nullInstantGivesNullTimestamp() {
        assertNull(Sql.toTimestamp(null));
        assertNull(Sql.toTimestamp(Unset.INSTANT));
    }

    @Test
    void sqlNullDateReadsAsUnset() {
        assertEquals(Unset.INSTANT, Sql.toInstant(null));
    }

    @Test
    void instantSurvivesRoundTrip() {
        Instant instant = Instant.parse("2026-10-18T20:15:30.123456Z");

        assertEquals(instant, Sql.toInstant(Sql.toTimestamp(instant)));
    }


    @Test
    void unknownPercentIsStoredAsSqlNull() {
        assertNull(Sql.toDecimal(null));
        assertNull(Sql.toDecimal(Unset.PERCENT));
        assertEquals(new BigDecimal("12.5"), Sql.toDecimal(new BigDecimal("12.5")));
    }


    @Test
    void nameOfNullEnumIsNull() {
        assertNull(Sql.name(null));
        assertEquals("BLOCKING", Sql.name(ModalEnum.Verdict.BLOCKING));
    }

}
