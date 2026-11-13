package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.domain.model.ModalEnum;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SqlTest {
//    @Test
//    void nullInstantGivesNullTimestamp() {
//        assertNull(Sql.toTimestamp(null));
//        assertNull(Sql.toInstant(null));
//    }

    @Test
    void instantSurvivesRoundTrip() {
        Instant instant = Instant.parse("2026-10-18T20:15:30.123456Z");

        assertEquals(instant, Sql.toInstant(Sql.toTimestamp(instant)));
    }

//    @Test
//    void nameOfNullEnumIsNull() {
//        assertNull(Sql.name(null));
//        assertEquals("BLOCKING", Sql.name(ModalEnum.Verdict.BLOCKING));
//    }

}
