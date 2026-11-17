package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.RepositoryStore;
import fr.reviewbot.application.port.out.ReviewStore;
import fr.reviewbot.domain.model.ModalEnum;
import fr.reviewbot.domain.model.Repository;
import fr.reviewbot.domain.model.Review;
import fr.reviewbot.domain.model.Unset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JdbcReviewStoreTest {

    @Autowired
    private RepositoryStore repositories;

    @Autowired
    private ReviewStore reviews;

    private long repositoryId;

    @BeforeEach
    void createRepository() {
        repositoryId = repositories.save(
                Repository.create(ModalEnum.Platform.GITHUB, "acme/shop", "https://github.com/acme/shop.git", null, null)).id();
    }

    private Review queued(String prNumber) {
        return Review.queued(repositoryId, prNumber, "Titre " + prNumber, "alice", "feature/x", "main", "sha-" + prNumber);
    }

    @Test
    void saveInsertsAQueuedReview() {
        Review saved = reviews.save(queued("214"));

        assertNotNull(saved.id());
        assertEquals(repositoryId, saved.repositoryId());
        assertEquals("214", saved.prNumber());
        assertEquals("Titre 214", saved.prTitle());
        assertEquals(ModalEnum.ReviewStatus.QUEUED, saved.status());
        assertEquals(ModalEnum.Verdict.PENDING, saved.verdict());
        assertFalse(Unset.isKnown(saved.coverageChangedLines()));
        assertTrue(Unset.isSet(saved.queuedAt()));
        assertFalse(Unset.isSet(saved.startedAt()));
    }

    @Test
    void saveUpdatesTheResultOfAReview() {
        Review saved = reviews.save(queued("214"));
        Instant start = Instant.parse("2026-10-19T08:00:00Z");
        Instant end = Instant.parse("2026-10-19T08:03:12Z");
        Review finished = new Review(saved.id(), saved.repositoryId(), saved.prNumber(), saved.prTitle(),
                saved.prAuthor(), saved.sourceBranch(), saved.targetBranch(), saved.headCommitSha(),
                ModalEnum.ReviewStatus.DONE, ModalEnum.Verdict.BLOCKING, 2, 5, 9, new BigDecimal("64.50"), null,
                "{\"checkstyle\":true}", "comment-42", saved.queuedAt(), start, end);

        Review updated = reviews.save(finished);

        assertEquals(ModalEnum.ReviewStatus.DONE, updated.status());
        assertEquals(ModalEnum.Verdict.BLOCKING, updated.verdict());
        assertEquals(2, updated.blockingCount());
        assertEquals(5, updated.majorCount());
        assertEquals(9, updated.minorCount());
        assertEquals(0, new BigDecimal("64.50").compareTo(updated.coverageChangedLines()));
        assertEquals("{\"checkstyle\":true}", updated.configSnapshot());
        assertEquals("comment-42", updated.globalCommentRef());
        assertEquals(start, updated.startedAt());
        assertEquals(end, updated.finishedAt());
        assertEquals(saved.queuedAt(), updated.queuedAt());
    }

    @Test
    void longTextsAreStoredInClobColumns() {
        Review saved = reviews.save(queued("214"));
        String longError = "erreur ".repeat(2_000);
        Review failed = new Review(saved.id(), saved.repositoryId(), saved.prNumber(), saved.prTitle(),
                saved.prAuthor(), saved.sourceBranch(), saved.targetBranch(), saved.headCommitSha(),
                ModalEnum.ReviewStatus.FAILED, ModalEnum.Verdict.PENDING, 0, 0, 0, null, longError, null, null, saved.queuedAt(), null, null);

        Review updated = reviews.save(failed);

        assertEquals(longError, updated.errorMessage());
    }

    @Test
    void updateOfUnknownReviewFails() {
        Review ghost = new Review(999_999L, repositoryId, "1", "t", "a", "s", "t", "sha",
                ModalEnum.ReviewStatus.QUEUED, ModalEnum.Verdict.PENDING, 0, 0, 0, null, null, null, null, null, null, null);

        assertThrows(RuntimeException.class, () -> reviews.save(ghost));
    }

    @Test
    void findByIdOfUnknownReviewIsEmpty() {
        assertTrue(reviews.findById(999_999L).isEmpty());
    }

    @Test
    void findByRepositoryIdReturnsNewestFirst() {
        Review first = reviews.save(queued("1"));
        Review second = reviews.save(queued("2"));
        Review third = reviews.save(queued("3"));

        List<Long> ids = reviews.findByRepositoryId(repositoryId).stream().map(Review::id).toList();

        assertEquals(List.of(third.id(), second.id(), first.id()), ids);
    }

    @Test
    void findPageAndCount() {
        long before = reviews.count();
        reviews.save(queued("1"));
        reviews.save(queued("2"));
        reviews.save(queued("3"));

        assertEquals(before + 3, reviews.count());
        assertEquals(2, reviews.findPage(2, 0).size());
        assertEquals("3", reviews.findPage(1, 0).getFirst().prNumber());
    }

    @Test
    void deletingTheRepositoryDeletesItsReviews() {
        Review saved = reviews.save(queued("214"));

        repositories.deleteById(repositoryId);

        assertTrue(reviews.findById(saved.id()).isEmpty());
    }
}
