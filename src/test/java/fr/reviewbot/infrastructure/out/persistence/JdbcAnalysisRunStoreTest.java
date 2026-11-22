package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.AnalysisRunStore;
import fr.reviewbot.application.port.out.RepositoryStore;
import fr.reviewbot.application.port.out.ReviewStore;
import fr.reviewbot.domain.model.AnalysisRun;
import fr.reviewbot.domain.model.ModalEnum;
import fr.reviewbot.domain.model.Review;
import fr.reviewbot.domain.model.Unset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@ActiveProfiles("test")
@Transactional

class JdbcAnalysisRunStoreTest {

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

    private static final Instant START = Instant.parse("2026-11-01T10:00:00Z");
    private static final Instant END = Instant.parse("2026-11-01T10:00:05Z");

    @Autowired
    private AnalysisRunStore runs;

    @Test
    void saveInsertsASuccessfulRun() throws Exception {
        Review review = savedReview("1");

        AnalysisRun saved = runs.save(AnalysisRun.ok(review.id(), ModalEnum.Tool.CHECKSTYLE, 4, 5_000, START, END));

        assertNotEquals(Unset.ID, saved.id());
        assertEquals(review.id(), saved.reviewId());
        assertEquals(ModalEnum.Tool.CHECKSTYLE, saved.tool());
        assertEquals(ModalEnum.RunStatus.OK, saved.status());
        assertEquals(4, saved.findingsCount());
        assertEquals(5_000, saved.durationMs());
        assertEquals(START, saved.startedAt());
        assertEquals(END, saved.finishedAt());
    }

    @Test
    void unknownDurationAndDatesComeBackAsUnset() throws Exception {
        Review review = savedReview("1");

        AnalysisRun saved = runs.save(AnalysisRun.notOk(review.id(), ModalEnum.Tool.PMD, ModalEnum.RunStatus.SKIPPED,
                "pas de sources", Unset.INSTANT, Unset.INSTANT));

        assertEquals(Unset.NUMBER, saved.durationMs());
        assertEquals(Unset.INSTANT, saved.startedAt());
        assertEquals(Unset.INSTANT, saved.finishedAt());
        assertEquals("pas de sources", saved.errorMessage());
    }

    @Test
    void zeroDurationIsKeptAsZero() throws Exception {
        Review review = savedReview("1");

        AnalysisRun saved = runs.save(AnalysisRun.ok(review.id(), ModalEnum.Tool.JAVADOC, 0, 0, START, END));

        assertEquals(0, saved.durationMs());
    }

    @Test
    void saveUpdatesAnExistingRun() throws Exception {
        Review review = savedReview("1");
        AnalysisRun saved = runs.save(new AnalysisRun(Unset.ID, review.id(), ModalEnum.Tool.SPOTBUGS, ModalEnum.RunStatus.OK,
                0, Unset.NUMBER, null, START, Unset.INSTANT));

        AnalysisRun updated = runs.save(new AnalysisRun(saved.id(), review.id(), ModalEnum.Tool.SPOTBUGS, ModalEnum.RunStatus.FAILED,
                0, 1_500, "erreur ".repeat(2_000), START, END));

        assertEquals(ModalEnum.RunStatus.FAILED, updated.status());
        assertEquals(1_500, updated.durationMs());
        assertEquals("erreur ".repeat(2_000), updated.errorMessage());
        assertEquals(END, updated.finishedAt());
    }

    @Test
    void updateOfUnknownRunFails() {
        Review review = savedReview("1");
        AnalysisRun ghost = new AnalysisRun(999_999L, review.id(), ModalEnum.Tool.PMD, ModalEnum.RunStatus.OK, 0, 0, null, null, null);

        assertThrows(RuntimeException.class, () -> runs.save(ghost));
    }

    @Test
    void findByReviewIdReturnsRunsInCreationOrder() throws Exception {
        Review review = savedReview("1");
        Review other = savedReview("2");
        AnalysisRun first = runs.save(AnalysisRun.ok(review.id(), ModalEnum.Tool.JAVADOC, 0, 10, START, END));
        AnalysisRun second = runs.save(AnalysisRun.ok(review.id(), ModalEnum.Tool.PMD, 2, 20, START, END));
        runs.save(AnalysisRun.ok(other.id(), ModalEnum.Tool.PMD, 1, 30, START, END));

        List<Long> ids = runs.findByReviewId(review.id()).stream().map(AnalysisRun::id).toList();

        assertEquals(List.of(first.id(), second.id()), ids);
    }

    @Test
    void findByIdOfUnknownRunIsEmpty() throws Exception {
        assertTrue(runs.findById(999_999L).isEmpty());
    }

    @Test
    void runsAreDeletedWithTheirReview() throws Exception {
        Review review = savedReview("1");
        AnalysisRun saved = runs.save(AnalysisRun.ok(review.id(), ModalEnum.Tool.PMD, 0, 1, START, END));

        repositories.deleteById(review.repositoryId());

        assertTrue(runs.findById(saved.id()).isEmpty());
    }
}
