package fr.reviewbot.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnalysisRunTest {

    private static final Instant START = Instant.parse("2026-11-01T10:00:00Z");
    private static final Instant END = Instant.parse("2026-11-01T10:00:05Z");

    @Test
    void okCreatesASuccessfulRun() {
        AnalysisRun run = AnalysisRun.ok(3, ModalEnum.Tool.CHECKSTYLE, 4, 5_000, START, END);

        assertEquals(Unset.ID, run.id());
        assertEquals(ModalEnum.RunStatus.OK, run.status());
        assertEquals(4, run.findingsCount());
        assertEquals(5_000, run.durationMs());
        assertEquals(Unset.TEXT, run.errorMessage());
    }

    @Test
    void notOkKeepsTheCauseAndHasNoDuration() {
        AnalysisRun run = AnalysisRun.notOk(3, ModalEnum.Tool.PMD, ModalEnum.RunStatus.TIMEOUT, "trop long", START, END);

        assertEquals(ModalEnum.RunStatus.TIMEOUT, run.status());
        assertEquals(0, run.findingsCount());
        assertEquals(Unset.NUMBER, run.durationMs());
        assertEquals("trop long", run.errorMessage());
    }

    @Test
    void negativeDurationBecomesUnset() {
        AnalysisRun run = new AnalysisRun(0, 3, ModalEnum.Tool.JACOCO, ModalEnum.RunStatus.OK, 0, -42, null, null, null);

        assertEquals(Unset.NUMBER, run.durationMs());
        assertEquals(Unset.TEXT, run.errorMessage());
        assertEquals(Unset.INSTANT, run.startedAt());
    }

    @Test
    void toolAndStatusAreRequired() {
        assertThrows(NullPointerException.class,
                () -> new AnalysisRun(0, 3, null, ModalEnum.RunStatus.OK, 0, 0, "", START, END));
        assertThrows(NullPointerException.class,
                () -> new AnalysisRun(0, 3, ModalEnum.Tool.PMD, null, 0, 0, "", START, END));
    }

    @Test
    void findingsCountCannotBeNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> new AnalysisRun(0, 3, ModalEnum.Tool.PMD, ModalEnum.RunStatus.OK, -1, 0, "", START, END));
    }
}

