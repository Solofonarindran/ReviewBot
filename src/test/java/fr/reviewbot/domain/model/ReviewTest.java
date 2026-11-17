package fr.reviewbot.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewTest {
    @Test
    void queuedBuildsAReviewWaitingWithoutResult() {
        Review review = Review.queued(7L, "214", "Ajout paiement", "alice", "feature/pay", "main", "a1b2c3d");
        assertFalse(Unset.isSaved(review.id()));
        assertEquals(7L, review.repositoryId());
        assertEquals(ModalEnum.ReviewStatus.QUEUED, review.status());
        assertEquals(ModalEnum.Verdict.PENDING, review.verdict());
        assertEquals(0, review.blockingCount());
        assertEquals(0, review.majorCount());
        assertEquals(0, review.minorCount());
        assertFalse(Unset.isKnown(review.coverageChangedLines()));
        assertFalse(Unset.isSet(review.queuedAt()));
        assertFalse(Unset.isSet(review.startedAt()));
        assertFalse(Unset.isSet(review.finishedAt()));
    }

    @Test
    void nullValuesAreReplacedByInitialValues() {
        Review review = new Review(Unset.ID, 1L, "1", null, null, null, null, "sha",
                ModalEnum.ReviewStatus.QUEUED, null, 0, 0, 0, null, null, null, null, null, null, null);

        assertEquals(ModalEnum.Verdict.PENDING, review.verdict());
        assertEquals("", review.prTitle());
        assertEquals("", review.errorMessage());
        assertEquals("", review.configSnapshot());
        assertEquals(Unset.PERCENT, review.coverageChangedLines());
        assertEquals(Unset.INSTANT, review.queuedAt());
    }

    @Test
    void prNumberIsRequired() {
        assertThrows(IllegalArgumentException.class,
                () -> Review.queued(1L, "", "t", "a", "s", "t", "sha"));
    }

    @Test
    void headCommitShaIsRequired() {
        assertThrows(IllegalArgumentException.class,
                () -> Review.queued(1L, "1", "t", "a", "s", "t", null));
    }

    @Test
    void statusIsRequired() {
        assertThrows(NullPointerException.class, () -> new Review(null, 1L, "1", "t", "a", "s", "t", "sha",
                null, null, 0, 0, 0, null, null, null, null, null, null, null));
    }
}
