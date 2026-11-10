package fr.reviewbot.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Reviewtest {
    @Test
    void queuedBuildsAReviewWaitingWithoutResult() {
        Review review = Review.queued(7L, "214", "Ajout paiement", "alice", "feature/pay", "main", "a1b2c3d");
        assertNull(review.id());
        assertEquals(7L, review.repositoryId());
        assertEquals(ModalEnum.ReviewStatus.QUEUED, review.status());
        assertNull(review.verdict());
        assertEquals(0, review.blockingCount());
        assertEquals(0, review.majorCount());
        assertEquals(0, review.minorCount());
        assertNull(review.queuedAt());
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
