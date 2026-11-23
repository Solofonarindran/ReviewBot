package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.FindingStore;
import fr.reviewbot.application.port.out.PostedCommentStore;
import fr.reviewbot.application.port.out.RepositoryStore;
import fr.reviewbot.application.port.out.ReviewStore;
import fr.reviewbot.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JdbcPostedCommentStoreTest {

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
    private PostedCommentStore comments;

    @Autowired
    private JdbcExecutor executor;

    private Finding savedFinding(Review review) throws Exception {
        return findings.save(new Finding(Unset.ID, review.id(), Unset.ID, Unset.ID, "PMD", "R", ModalEnum.Category.BUG,
                ModalEnum.Severity.MAJOR, "A.java", 12, 12, "t", "", "", true, "fp", null));
    }

    @Test
    void saveInsertsTheGlobalComment() throws Exception {
        Review review = savedReview("1");

        PostedComment saved = comments.save(PostedComment.global(review.id(), "gh-1"));

        assertNotEquals(Unset.ID, saved.id());
        assertEquals(ModalEnum.CommentKind.GLOBAL, saved.kind());
        assertEquals(ModalEnum.CommentState.POSTED, saved.state());
        assertEquals(Unset.ID, saved.findingId());
        assertEquals(Unset.NUMBER, saved.line());
        assertEquals(Unset.TEXT, saved.filePath());
        assertEquals("gh-1", saved.platformCommentId());
        assertTrue(Unset.isSet(saved.postedAt()));
    }

    @Test
    void saveInsertsAnInlineComment() throws Exception {
        Review review = savedReview("1");
        Finding finding = savedFinding(review);

        PostedComment saved = comments.save(PostedComment.inline(review.id(), finding.id(), "gh-2", "A.java", 12));

        assertEquals(ModalEnum.CommentKind.INLINE, saved.kind());
        assertEquals(finding.id(), saved.findingId());
        assertEquals("A.java", saved.filePath());
        assertEquals(12, saved.line());
    }

    @Test
    void saveUpdatesStateAndPlatformId() throws Exception {
        Review review = savedReview("1");
        PostedComment saved = comments.save(PostedComment.global(review.id(), ""));

        PostedComment updated = comments.save(new PostedComment(saved.id(), saved.reviewId(), saved.findingId(),
                saved.kind(), "gh-99", saved.filePath(), saved.line(), ModalEnum.CommentState.RESOLVED,
                saved.postedAt(), saved.updatedAt()));

        assertEquals("gh-99", updated.platformCommentId());
        assertEquals(ModalEnum.CommentState.RESOLVED, updated.state());
        assertEquals(saved.postedAt(), updated.postedAt());
        assertFalse(updated.updatedAt().isBefore(saved.updatedAt()));
    }

    @Test
    void updateOfUnknownCommentFails() {
        Review review = savedReview("1");
        PostedComment ghost = new PostedComment(999_999L, review.id(), Unset.ID, ModalEnum.CommentKind.GLOBAL, "", "",
                Unset.NUMBER, ModalEnum.CommentState.POSTED, Unset.INSTANT, Unset.INSTANT);

        assertThrows(RuntimeException.class, () -> comments.save(ghost));
    }

    @Test
    void findByReviewIdAndKind() throws Exception {
        Review review = savedReview("1");
        Finding finding = savedFinding(review);
        PostedComment global = comments.save(PostedComment.global(review.id(), "g"));
        PostedComment inline = comments.save(PostedComment.inline(review.id(), finding.id(), "i", "A.java", 12));
        comments.save(PostedComment.global(savedReview("2").id(), "other"));

        assertEquals(List.of(global.id(), inline.id()),
                comments.findByReviewId(review.id()).stream().map(PostedComment::id).toList());
        assertEquals(List.of(global.id()),
                comments.findByReviewIdAndKind(review.id(), ModalEnum.CommentKind.GLOBAL).stream().map(PostedComment::id).toList());
        assertEquals(List.of(inline.id()),
                comments.findByReviewIdAndKind(review.id(), ModalEnum.CommentKind.INLINE).stream().map(PostedComment::id).toList());
    }

    @Test
    void findByIdOfUnknownCommentIsEmpty() throws Exception {
        assertTrue(comments.findById(999_999L).isEmpty());
    }

    @Test
    void deletingTheFindingKeepsTheCommentWithoutReference() throws Exception {
        Review review = savedReview("1");
        Finding finding = savedFinding(review);
        PostedComment inline = comments.save(PostedComment.inline(review.id(), finding.id(), "i", "A.java", 12));

        // ON DELETE SET NULL : la suppression du problème ne supprime pas le commentaire déjà publié
        deleteFinding(finding.id());

        assertEquals(Unset.ID, comments.findById(inline.id()).orElseThrow().findingId());
    }

    @Test
    void commentsAreDeletedWithTheirReview() throws Exception {
        Review review = savedReview("1");
        PostedComment saved = comments.save(PostedComment.global(review.id(), "g"));

        repositories.deleteById(review.repositoryId());

        assertTrue(comments.findById(saved.id()).isEmpty());
    }

    private void deleteFinding(long id) {
        // pas de méthode de suppression dans le port : on passe par JdbcExecutor, comme le ferait une purge
        try {
            executor.update("DELETE FROM finding WHERE id = ?", ps -> ps.setLong(1, id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
