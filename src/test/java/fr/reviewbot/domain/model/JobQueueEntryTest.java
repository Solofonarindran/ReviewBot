package fr.reviewbot.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JobQueueEntryTest {

    @Test
    void queuedCreatesAFreshJob() {
        JobQueueEntry job = JobQueueEntry.queued(12);

        assertEquals(Unset.ID, job.id());
        assertEquals(12, job.reviewId());
        assertEquals(ModalEnum.JobState.QUEUED, job.state());
        assertEquals(0, job.attempts());
        assertEquals(Unset.TEXT, job.lastError());
        assertEquals(Unset.TEXT, job.lockedBy());
        assertFalse(Unset.isSet(job.lockedUntil()));
    }

    @Test
    void nullsBecomeUnsetValues() {
        JobQueueEntry job = new JobQueueEntry(1, 2, ModalEnum.JobState.RUNNING, 1, null, null, null, null, null);

        assertEquals(Unset.TEXT, job.lastError());
        assertEquals(Unset.TEXT, job.lockedBy());
        assertEquals(Unset.INSTANT, job.lockedUntil());
        assertEquals(Unset.INSTANT, job.createdAt());
        assertEquals(Unset.INSTANT, job.updatedAt());
    }

    @Test
    void stateIsRequired() {
        assertThrows(NullPointerException.class,
                () -> new JobQueueEntry(1, 2, null, 0, "", "", Instant.EPOCH, Instant.EPOCH, Instant.EPOCH));
    }

    @Test
    void attemptsCannotBeNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> new JobQueueEntry(1, 2, ModalEnum.JobState.QUEUED, -1, "", "", Instant.EPOCH, Instant.EPOCH, Instant.EPOCH));
    }
}

