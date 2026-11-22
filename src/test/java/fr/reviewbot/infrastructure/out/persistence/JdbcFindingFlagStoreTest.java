package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.FindingFlagStore;
import fr.reviewbot.application.port.out.FindingStore;
import fr.reviewbot.application.port.out.RepositoryStore;
import fr.reviewbot.application.port.out.ReviewStore;
import fr.reviewbot.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional

class JdbcFindingFlagStoreTest {

    @Autowired
    private RepositoryStore repositories;

    @Autowired
    private ReviewStore reviews;

    @Autowired
    private FindingStore findings;

    @Autowired
    private FindingFlagStore flags;

    private Review savedReview(String prNumber) {
        return ReviewFixtures.savedReview(repositories, reviews, prNumber);
    }

    private Review savedReview(String prNumber, String headCommitSha) {
        return ReviewFixtures.savedReview(repositories, reviews, prNumber, headCommitSha);
    }

    private Finding savedFinding(Review review, String fingerprint) {
        return findings.save(new Finding(Unset.ID, review.id(), Unset.ID, Unset.ID, "PMD", "R", Category.BUG,
                ModalEnum.Severity.MAJOR, "A.java", 1, 1, "t", "", "", true, fingerprint, null));
    }

    @Test
    void saveInsertsAFlag() {
        Finding finding = savedFinding(savedReview("1"), "fp");

        FindingFlag saved = flags.save(FindingFlag.of(finding.id(), "alice", "voulu"));

        assertNotEquals(Unset.ID, saved.id());
        assertEquals(finding.id(), saved.findingId());
        assertEquals("alice", saved.flaggedBy());
        assertEquals("voulu", saved.reason());
        assertTrue(Unset.isSet(saved.flaggedAt()));
    }

    @Test
    void emptyReasonIsAllowed() {
        Finding finding = savedFinding(savedReview("1"), "fp");

        assertEquals(Unset.TEXT, flags.save(FindingFlag.of(finding.id(), "alice", null)).reason());
    }

    @Test
    void aFindingCanOnlyBeFlaggedOnce() {
        Finding finding = savedFinding(savedReview("1"), "fp");
        flags.save(FindingFlag.of(finding.id(), "alice", "a"));

        assertThrows(RuntimeException.class, () -> flags.save(FindingFlag.of(finding.id(), "bob", "b")));
    }

    @Test
    void saveUpdatesReasonAndAuthor() {
        Finding finding = savedFinding(savedReview("1"), "fp");
        FindingFlag saved = flags.save(FindingFlag.of(finding.id(), "alice", "a"));

        FindingFlag updated = flags.save(new FindingFlag(saved.id(), saved.findingId(), "bob", "b", saved.flaggedAt()));

        assertEquals("bob", updated.flaggedBy());
        assertEquals("b", updated.reason());
        assertEquals(saved.flaggedAt(), updated.flaggedAt());
    }

    @Test
    void updateOfUnknownFlagFails() {
        Finding finding = savedFinding(savedReview("1"), "fp");

        assertThrows(RuntimeException.class,
                () -> flags.save(new FindingFlag(999_999L, finding.id(), "alice", "", Unset.INSTANT)));
    }

    @Test
    void findByFindingId() {
        Finding finding = savedFinding(savedReview("1"), "fp");
        Finding other = savedFinding(savedReview("2"), "fp2");
        flags.save(FindingFlag.of(finding.id(), "alice", "a"));

        assertEquals("alice", flags.findByFindingId(finding.id()).orElseThrow().flaggedBy());
        assertTrue(flags.findByFindingId(other.id()).isEmpty());
    }

    @Test
    void findByReviewIdReturnsOnlyFlagsOfThatReview() {
        Review review = savedReview("1");
        Review other = savedReview("2");
        Finding f1 = savedFinding(review, "a");
        Finding f2 = savedFinding(review, "b");
        Finding f3 = savedFinding(other, "c");
        flags.save(FindingFlag.of(f1.id(), "alice", ""));
        flags.save(FindingFlag.of(f2.id(), "bob", ""));
        flags.save(FindingFlag.of(f3.id(), "carol", ""));

        var result = flags.findByReviewId(review.id());

        assertEquals(2, result.size());
        assertEquals("alice", result.get(0).flaggedBy());
        assertEquals("bob", result.get(1).flaggedBy());
    }

    @Test
    void deleteByFindingIdRemovesTheFlag() {
        Finding finding = savedFinding(savedReview("1"), "fp");
        flags.save(FindingFlag.of(finding.id(), "alice", "a"));

        flags.deleteByFindingId(finding.id());
        flags.deleteByFindingId(finding.id());

        assertTrue(flags.findByFindingId(finding.id()).isEmpty());
    }

    @Test
    void flagsAreDeletedWithTheirFinding() {
        Review review = savedReview("1");
        Finding finding = savedFinding(review, "fp");
        flags.save(FindingFlag.of(finding.id(), "alice", "a"));

        repositories.deleteById(review.repositoryId());

        assertTrue(flags.findByFindingId(finding.id()).isEmpty());
    }
}
