package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.JobQueueStore;
import fr.reviewbot.application.port.out.RepositoryStore;
import fr.reviewbot.application.port.out.ReviewStore;
import fr.reviewbot.domain.model.JobQueueEntry;
import fr.reviewbot.domain.model.ModalEnum;
import fr.reviewbot.domain.model.Review;
import fr.reviewbot.domain.model.Unset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JdbcJobQueueStoreTest {

    @Autowired
    private RepositoryStore repositories;

    @Autowired
    private ReviewStore reviews;

    private Review savedReview(String prNumber) {
        return ReviewFixtures.savedReview(repositories, reviews, prNumber);
    }

    private Review savedReview(String prNumber, String headCommitSha) {
        return ReviewFixtures.savedReview(repositories, reviews, prNumber, headCommitSha);
    }

    private static final Duration LOCK = Duration.ofMinutes(10);
    private static final Duration ALREADY_EXPIRED = Duration.ofMinutes(-10);

    @Autowired
    private JobQueueStore queue;

    @Test
    void enqueueCreatesAQueuedJob() {
        Review review = savedReview("1");

        JobQueueEntry job = queue.enqueue(review.id());

        assertNotEquals(Unset.ID, job.id());
        assertEquals(review.id(), job.reviewId());
        assertEquals(ModalEnum.JobState.QUEUED, job.state());
        assertEquals(0, job.attempts());
        assertTrue(Unset.isSet(job.createdAt()));
        assertFalse(Unset.isSet(job.lockedUntil()));
        assertEquals(Unset.TEXT, job.lockedBy());
    }

    @Test
    void claimNextReturnsTheOldestJobFirst() {
        JobQueueEntry first = queue.enqueue(savedReview("1").id());
        JobQueueEntry second = queue.enqueue(savedReview("2").id());
        JobQueueEntry third = queue.enqueue(savedReview("3").id());

        assertEquals(first.id(), queue.claimNext("w1", LOCK).orElseThrow().id());
        assertEquals(second.id(), queue.claimNext("w2", LOCK).orElseThrow().id());
        assertEquals(third.id(), queue.claimNext("w3", LOCK).orElseThrow().id());
        assertTrue(queue.claimNext("w4", LOCK).isEmpty());
    }

    @Test
    void claimNextLocksTheJobForTheWorker() {
        queue.enqueue(savedReview("1").id());

        JobQueueEntry claimed = queue.claimNext("worker-7", LOCK).orElseThrow();

        assertEquals(ModalEnum.JobState.RUNNING, claimed.state());
        assertEquals(1, claimed.attempts());
        assertEquals("worker-7", claimed.lockedBy());
        assertTrue(Unset.isSet(claimed.lockedUntil()));
        assertTrue(claimed.lockedUntil().isAfter(claimed.updatedAt()));
    }

    @Test
    void aClaimedJobIsNeverGivenTwice() {
        queue.enqueue(savedReview("1").id());

        assertTrue(queue.claimNext("w1", LOCK).isPresent());
        assertTrue(queue.claimNext("w2", LOCK).isEmpty());
    }

    @Test
    void claimNextOnEmptyQueueIsEmpty() {
        assertEquals(Optional.empty(), queue.claimNext("w1", LOCK));
    }

    @Test
    void onlyOneJobRunsAtATimeForTheSamePullRequest() {
        JobQueueEntry older = queue.enqueue(savedReview("1", "sha-a").id());
        JobQueueEntry newer = queue.enqueue(savedReview("1", "sha-b").id());

        assertEquals(older.id(), queue.claimNext("w1", LOCK).orElseThrow().id());
        assertTrue(queue.claimNext("w2", LOCK).isEmpty(), "la 2e tâche de la même PR doit attendre");

        queue.markDone(older.id());

        assertEquals(newer.id(), queue.claimNext("w2", LOCK).orElseThrow().id());
    }

    @Test
    void otherPullRequestsAreNotBlocked() {
        queue.enqueue(savedReview("1", "sha-a").id());
        queue.enqueue(savedReview("1", "sha-b").id());
        JobQueueEntry otherPr = queue.enqueue(savedReview("2").id());

        queue.claimNext("w1", LOCK);

        assertEquals(otherPr.id(), queue.claimNext("w2", LOCK).orElseThrow().id());
    }

    @Test
    void markDoneEndsTheJobAndReleasesTheLock() {
        queue.enqueue(savedReview("1").id());
        JobQueueEntry claimed = queue.claimNext("w1", LOCK).orElseThrow();

        JobQueueEntry done = queue.markDone(claimed.id());

        assertEquals(ModalEnum.JobState.DONE, done.state());
        assertEquals(Unset.TEXT, done.lockedBy());
        assertFalse(Unset.isSet(done.lockedUntil()));
        assertEquals(1, done.attempts());
    }

    @Test
    void markFailedPutsTheJobBackInTheQueueWhileAttemptsRemain() {
        queue.enqueue(savedReview("1").id());
        JobQueueEntry claimed = queue.claimNext("w1", LOCK).orElseThrow();

        JobQueueEntry retried = queue.markFailed(claimed.id(), "git clone a échoué", 3);

        assertEquals(ModalEnum.JobState.QUEUED, retried.state());
        assertEquals("git clone a échoué", retried.lastError());
        assertEquals(1, retried.attempts());
        assertEquals(Unset.TEXT, retried.lockedBy());
        assertEquals(claimed.id(), queue.claimNext("w2", LOCK).orElseThrow().id());
    }

    @Test
    void markFailedEndsTheJobOnceMaxAttemptsIsReached() {
        queue.enqueue(savedReview("1").id());
        JobQueueEntry job = queue.claimNext("w1", LOCK).orElseThrow();
        job = queue.markFailed(job.id(), "e1", 2);
        job = queue.claimNext("w1", LOCK).orElseThrow();
        assertEquals(2, job.attempts());

        JobQueueEntry failed = queue.markFailed(job.id(), "e2", 2);

        assertEquals(ModalEnum.JobState.FAILED, failed.state());
        assertEquals("e2", failed.lastError());
        assertTrue(queue.claimNext("w1", LOCK).isEmpty());
    }

    @Test
    void aRetriedJobKeepsItsPlaceInTheQueue() {
        JobQueueEntry first = queue.enqueue(savedReview("1").id());
        queue.enqueue(savedReview("2").id());
        JobQueueEntry claimed = queue.claimNext("w1", LOCK).orElseThrow();
        queue.markFailed(claimed.id(), "boom", 3);

        assertEquals(first.id(), queue.claimNext("w2", LOCK).orElseThrow().id());
    }

    @Test
    void releaseExpiredPutsBackJobsOfDeadWorkers() {
        queue.enqueue(savedReview("1").id());
        JobQueueEntry crashed = queue.claimNext("dead-worker", ALREADY_EXPIRED).orElseThrow();

        int released = queue.releaseExpired(3);

        assertEquals(1, released);
        JobQueueEntry back = queue.findById(crashed.id()).orElseThrow();
        assertEquals(ModalEnum.JobState.QUEUED, back.state());
        assertEquals(Unset.TEXT, back.lockedBy());
        assertFalse(back.lastError().isEmpty());
    }

    @Test
    void releaseExpiredFailsJobsOutOfAttempts() {
        queue.enqueue(savedReview("1").id());
        JobQueueEntry crashed = queue.claimNext("dead-worker", ALREADY_EXPIRED).orElseThrow();

        int released = queue.releaseExpired(1);

        assertEquals(1, released);
        assertEquals(ModalEnum.JobState.FAILED, queue.findById(crashed.id()).orElseThrow().state());
    }

    @Test
    void releaseExpiredLeavesActiveLocksAlone() {
        queue.enqueue(savedReview("1").id());
        JobQueueEntry running = queue.claimNext("w1", LOCK).orElseThrow();

        assertEquals(0, queue.releaseExpired(3));
        assertEquals(ModalEnum.JobState.RUNNING, queue.findById(running.id()).orElseThrow().state());
    }

    @Test
    void countByStateAndFindByReviewId() {
        Review review = savedReview("1");
        long queuedBefore = queue.countByState(ModalEnum.JobState.QUEUED);
        long runningBefore = queue.countByState(ModalEnum.JobState.RUNNING);
        JobQueueEntry job = queue.enqueue(review.id());
        queue.enqueue(savedReview("2").id());

        assertEquals(queuedBefore + 2, queue.countByState(ModalEnum.JobState.QUEUED));
        queue.claimNext("w1", LOCK);
        assertEquals(runningBefore + 1, queue.countByState(ModalEnum.JobState.RUNNING));

        List<Long> ids = queue.findByReviewId(review.id()).stream().map(JobQueueEntry::id).toList();
        assertEquals(List.of(job.id()), ids);
    }

    @Test
    void operationsOnUnknownJobFail() {
        assertThrows(RuntimeException.class, () -> queue.markDone(999_999L));
        assertThrows(RuntimeException.class, () -> queue.markFailed(999_999L, "x", 3));
        assertTrue(queue.findById(999_999L).isEmpty());
    }

    @Test
    void jobsAreDeletedWithTheirReview() {
        Review review = savedReview("1");
        JobQueueEntry job = queue.enqueue(review.id());

        repositories.deleteById(review.repositoryId());

        assertTrue(queue.findById(job.id()).isEmpty());
    }
}

