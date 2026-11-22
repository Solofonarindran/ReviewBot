package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.AnalysisRunStore;
import fr.reviewbot.application.port.out.FindingStore;
import fr.reviewbot.application.port.out.RepositoryStore;
import fr.reviewbot.application.port.out.ReviewStore;
import fr.reviewbot.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JdbcFindingStoreTest {

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

    @Autowired
    private FindingStore findings;

    @Autowired
    private AnalysisRunStore runs;

    private Finding finding(long reviewId, ModalEnum.Severity severity, ModalEnum.Category category, String file, int line,
                            boolean inDiff, String fingerprint) {
        return new Finding(Unset.ID, reviewId, Unset.ID, Unset.ID, "PMD", "Rule", category, severity, file,
                line, line, "Titre " + fingerprint, "message", "suggestion", inDiff, fingerprint, Unset.INSTANT);
    }

    @Test
    void saveInsertsAFinding() throws Exception {
        Review review = savedReview("1");

        Finding saved = findings.save(finding(review.id(), ModalEnum.Severity.MAJOR, ModalEnum.Category.BUG, "A.java", 10, true, "fp1"));

        assertNotEquals(Unset.ID, saved.id());
        assertEquals(review.id(), saved.reviewId());
        assertEquals(ModalEnum.Severity.MAJOR, saved.severity());
        assertEquals(ModalEnum.Category.BUG, saved.category());
        assertEquals("A.java", saved.filePath());
        assertEquals(10, saved.lineStart());
        assertTrue(saved.inDiff());
        assertEquals("fp1", saved.fingerprint());
        assertTrue(Unset.isSet(saved.createdAt()));
        assertFalse(saved.isMerged());
        assertEquals(Unset.ID, saved.analysisRunId());
    }

    @Test
    void findingWithoutFileNorLineKeepsUnsetValues() throws Exception {
        Review review = savedReview("1");
        Finding global = new Finding(Unset.ID, review.id(), Unset.ID, Unset.ID, "LLM", null, ModalEnum.Category.CODE,
                ModalEnum.Severity.INFO, null, Unset.NUMBER, Unset.NUMBER, "Remarque générale", null, null, false, "fp", null);

        Finding saved = findings.save(global);

        assertEquals(Unset.TEXT, saved.filePath());
        assertEquals(Unset.NUMBER, saved.lineStart());
        assertEquals(Unset.NUMBER, saved.lineEnd());
        assertEquals(Unset.TEXT, saved.ruleId());
    }

    @Test
    void findingKeepsItsAnalysisRun() throws Exception {
        Review review = savedReview("1");
        Instant now = Instant.parse("2026-11-01T10:00:00Z");
        AnalysisRun run = runs.save(AnalysisRun.ok(review.id(), ModalEnum.Tool.PMD, 1, 100, now, now));
        Finding withRun = new Finding(Unset.ID, review.id(), run.id(), Unset.ID, "PMD", "R", ModalEnum.Category.STYLE,
                ModalEnum.Severity.MINOR, "A.java", 1, 1, "t", "", "", false, "fp", null);

        Finding saved = findings.save(withRun);

        assertEquals(run.id(), saved.analysisRunId());
    }

    @Test
    void longMessagesAreStored() throws Exception {
        Review review = savedReview("1");
        String longText = "explication ".repeat(3_000);
        Finding big = new Finding(Unset.ID, review.id(), Unset.ID, Unset.ID, "LLM", "", ModalEnum.Category.BUG,
                ModalEnum.Severity.MAJOR, "A.java", 1, 1, "t", longText, longText, false, "fp", null);

        Finding saved = findings.save(big);

        assertEquals(longText, saved.message());
        assertEquals(longText, saved.suggestion());
    }

    @Test
    void saveUpdatesAFinding() throws Exception {
        Review review = savedReview("1");
        Finding saved = findings.save(finding(review.id(), ModalEnum.Severity.MINOR, ModalEnum.Category.STYLE, "A.java", 5, false, "fp"));

        Finding changed = new Finding(saved.id(), saved.reviewId(), saved.analysisRunId(), saved.mergedIntoId(),
                saved.source(), saved.ruleId(), ModalEnum.Category.SECURITY, ModalEnum.Severity.BLOCKING, "B.java", 7, 9,
                "Nouveau titre", "nouveau", "autre", true, "fp", saved.createdAt());

        Finding updated = findings.save(changed);

        assertEquals(saved.id(), updated.id());
        assertEquals(ModalEnum.Severity.BLOCKING, updated.severity());
        assertEquals(ModalEnum.Category.SECURITY, updated.category());
        assertEquals("B.java", updated.filePath());
        assertEquals(9, updated.lineEnd());
        assertTrue(updated.inDiff());
        assertEquals(saved.createdAt(), updated.createdAt());
    }

    @Test
    void updateOfUnknownFindingFails() {
        Review review = savedReview("1");
        Finding ghost = new Finding(999_999L, review.id(), Unset.ID, Unset.ID, "PMD", "", ModalEnum.Category.BUG,
                ModalEnum.Severity.MAJOR, "", 1, 1, "t", "", "", false, "fp", null);

        assertThrows(RuntimeException.class, () -> findings.save(ghost));
    }

    @Test
    void mergedFindingPointsToItsMainFinding() throws Exception {
        Review review = savedReview("1");
        Finding main = findings.save(finding(review.id(), ModalEnum.Severity.MAJOR, ModalEnum.Category.BUG, "A.java", 10, true, "fp"));
        Finding duplicate = findings.save(finding(review.id(), ModalEnum.Severity.MAJOR, ModalEnum.Category.BUG, "A.java", 10, true, "fp"));

        Finding merged = findings.save(duplicate.mergedInto(main.id()));

        assertTrue(merged.isMerged());
        assertEquals(main.id(), merged.mergedIntoId());
    }

    @Test
    void saveAllReturnsSavedFindingsInOrder() throws Exception {
        Review review = savedReview("1");

        List<Finding> saved = findings.saveAll(List.of(
                finding(review.id(), ModalEnum.Severity.MINOR, ModalEnum.Category.STYLE, "A.java", 1, false, "a"),
                finding(review.id(), ModalEnum.Severity.MAJOR, ModalEnum.Category.BUG, "B.java", 2, true, "b")));

        assertEquals(2, saved.size());
        assertEquals("a", saved.get(0).fingerprint());
        assertEquals("b", saved.get(1).fingerprint());
        assertNotEquals(saved.get(0).id(), saved.get(1).id());
    }

    @Test
    void findByReviewIdOnlyReturnsThatReview() throws Exception {
        Review review = savedReview("1");
        Review other = savedReview("2");
        findings.save(finding(review.id(), ModalEnum.Severity.MINOR, ModalEnum.Category.STYLE, "A.java", 1, false, "a"));
        findings.save(finding(other.id(), ModalEnum.Severity.MINOR, ModalEnum.Category.STYLE, "A.java", 1, false, "b"));

        assertEquals(1, findings.findByReviewId(review.id()).size());
    }

    @Test
    void searchSortsMostSevereFirstThenByFileAndLine() throws Exception {
        Review review = savedReview("1");
        findings.save(finding(review.id(), ModalEnum.Severity.INFO, ModalEnum.Category.DOC, "A.java", 1, false, "info"));
        findings.save(finding(review.id(), ModalEnum.Severity.MINOR, ModalEnum.Category.STYLE, "B.java", 5, false, "minor-b5"));
        findings.save(finding(review.id(), ModalEnum.Severity.BLOCKING, ModalEnum.Category.SECURITY, "Z.java", 1, true, "blocking"));
        findings.save(finding(review.id(), ModalEnum.Severity.MINOR, ModalEnum.Category.STYLE, "A.java", 9, false, "minor-a9"));
        findings.save(finding(review.id(), ModalEnum.Severity.MAJOR, ModalEnum.Category.BUG, "A.java", 3, true, "major"));
        findings.save(finding(review.id(), ModalEnum.Severity.MINOR, ModalEnum.Category.STYLE, "A.java", 2, false, "minor-a2"));

        List<String> order = findings.search(FindingFilter.visibleOf(review.id())).stream()
                .map(Finding::fingerprint).toList();

        assertEquals(List.of("blocking", "major", "minor-a2", "minor-a9", "minor-b5", "info"), order);
    }

    @Test
    void searchFiltersByCategoryAndSeverity() throws Exception {
        Review review = savedReview("1");
        findings.save(finding(review.id(), ModalEnum.Severity.BLOCKING, ModalEnum.Category.SECURITY, "A.java", 1, true, "sec"));
        findings.save(finding(review.id(), ModalEnum.Severity.MAJOR, ModalEnum.Category.BUG, "A.java", 2, true, "bug"));
        findings.save(finding(review.id(), ModalEnum.Severity.MINOR, ModalEnum.Category.STYLE, "A.java", 3, true, "style"));

        List<Finding> bugsAndStyle = findings.search(
                new FindingFilter(review.id(), Set.of(ModalEnum.Category.BUG, ModalEnum.Category.STYLE), Set.of(), false, false));
        List<Finding> blocking = findings.search(
                new FindingFilter(review.id(), Set.of(), Set.of(ModalEnum.Severity.BLOCKING, ModalEnum.Severity.MAJOR), false, false));
        List<Finding> both = findings.search(
                new FindingFilter(review.id(), Set.of(ModalEnum.Category.BUG), Set.of(ModalEnum.Severity.BLOCKING), false, false));

        assertEquals(2, bugsAndStyle.size());
        assertEquals(2, blocking.size());
        assertTrue(both.isEmpty());
    }

    @Test
    void searchCanKeepOnlyChangedLines() throws Exception {
        Review review = savedReview("1");
        findings.save(finding(review.id(), ModalEnum.Severity.MAJOR, ModalEnum.Category.BUG, "A.java", 1, true, "in"));
        findings.save(finding(review.id(), ModalEnum.Severity.MAJOR, ModalEnum.Category.BUG, "A.java", 2, false, "out"));

        List<Finding> result = findings.search(new FindingFilter(review.id(), Set.of(), Set.of(), true, false));

        assertEquals(List.of("in"), result.stream().map(Finding::fingerprint).toList());
    }

    @Test
    void searchHidesMergedFindingsUnlessAsked() throws Exception {
        Review review = savedReview("1");
        Finding main = findings.save(finding(review.id(), ModalEnum.Severity.MAJOR, ModalEnum.Category.BUG, "A.java", 1, true, "main"));
        Finding duplicate = findings.save(finding(review.id(), ModalEnum.Severity.MAJOR, ModalEnum.Category.BUG, "A.java", 1, true, "dup"));
        findings.save(duplicate.mergedInto(main.id()));

        assertEquals(1, findings.search(FindingFilter.visibleOf(review.id())).size());
        assertEquals(2, findings.search(new FindingFilter(review.id(), Set.of(), Set.of(), false, true)).size());
    }

    @Test
    void findByFingerprintIsScopedToTheReview() throws Exception {
        Review review = savedReview("1");
        Review other = savedReview("2");
        findings.save(finding(review.id(), ModalEnum.Severity.MAJOR, ModalEnum.Category.BUG, "A.java", 1, true, "same"));
        findings.save(finding(review.id(), ModalEnum.Severity.MAJOR, ModalEnum.Category.BUG, "A.java", 1, true, "same"));
        findings.save(finding(other.id(), ModalEnum.Severity.MAJOR, ModalEnum.Category.BUG, "A.java", 1, true, "same"));

        assertEquals(2, findings.findByReviewIdAndFingerprint(review.id(), "same").size());
        assertTrue(findings.findByReviewIdAndFingerprint(review.id(), "unknown").isEmpty());
    }

    @Test
    void countVisibleBySeverityIgnoresMergedAndFillsMissingWithZero() throws Exception {
        Review review = savedReview("1");
        Finding main = findings.save(finding(review.id(), ModalEnum.Severity.BLOCKING, ModalEnum.Category.BUG, "A.java", 1, true, "m"));
        findings.save(finding(review.id(), ModalEnum.Severity.MINOR, ModalEnum.Category.STYLE, "A.java", 2, true, "n1"));
        findings.save(finding(review.id(), ModalEnum.Severity.MINOR, ModalEnum.Category.STYLE, "A.java", 3, true, "n2"));
        Finding duplicate = findings.save(finding(review.id(), ModalEnum.Severity.BLOCKING, ModalEnum.Category.BUG, "A.java", 1, true, "d"));
        findings.save(duplicate.mergedInto(main.id()));

        Map<ModalEnum.Severity, Long> counts = findings.countVisibleBySeverity(review.id());

        assertEquals(1L, counts.get(ModalEnum.Severity.BLOCKING).longValue());
        assertEquals(0L, counts.get(ModalEnum.Severity.MAJOR).longValue());
        assertEquals(2L, counts.get(ModalEnum.Severity.MINOR).longValue());
        assertEquals(0L, counts.get(ModalEnum.Severity.INFO).longValue());
    }

    @Test
    void findByIdOfUnknownFindingIsEmpty() throws Exception {
        assertTrue(findings.findById(999_999L).isEmpty());
    }

    @Test
    void findingsAreDeletedWithTheirReview() throws Exception {
        Review review = savedReview("1");
        Finding saved = findings.save(finding(review.id(), ModalEnum.Severity.MAJOR, ModalEnum.Category.BUG, "A.java", 1, true, "fp"));

        repositories.deleteById(review.repositoryId());

        assertTrue(findings.findById(saved.id()).isEmpty());
    }
}
