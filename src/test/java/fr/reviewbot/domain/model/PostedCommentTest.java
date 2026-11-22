package fr.reviewbot.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostedCommentTest {

    @Test
    void globalCommentHasNoFindingNoFileNoLine() {
        PostedComment comment = PostedComment.global(5, "c-100");

        assertEquals(ModalEnum.CommentKind.GLOBAL, comment.kind());
        assertEquals(ModalEnum.CommentState.POSTED, comment.state());
        assertEquals(Unset.ID, comment.findingId());
        assertEquals(Unset.TEXT, comment.filePath());
        assertEquals(Unset.NUMBER, comment.line());
        assertEquals("c-100", comment.platformCommentId());
    }

    @Test
    void inlineCommentPointsToAFindingAndALine() {
        PostedComment comment = PostedComment.inline(5, 8, "c-101", "src/A.java", 33);

        assertEquals(ModalEnum.CommentKind.INLINE, comment.kind());
        assertEquals(8, comment.findingId());
        assertEquals("src/A.java", comment.filePath());
        assertEquals(33, comment.line());
    }

    @Test
    void nullsBecomeUnsetValues() {
        PostedComment comment = new PostedComment(0, 5, 0, ModalEnum.CommentKind.GLOBAL, null, null, -9,
                ModalEnum.CommentState.POSTED, null, null);

        assertEquals(Unset.TEXT, comment.platformCommentId());
        assertEquals(Unset.TEXT, comment.filePath());
        assertEquals(Unset.NUMBER, comment.line());
        assertEquals(Unset.INSTANT, comment.postedAt());
    }

    @Test
    void kindAndStateAreRequired() {
        assertThrows(NullPointerException.class,
                () -> new PostedComment(0, 5, 0, null, "", "", 1, ModalEnum.CommentState.POSTED, null, null));
        assertThrows(NullPointerException.class,
                () -> new PostedComment(0, 5, 0, ModalEnum.CommentKind.GLOBAL, "", "", 1, null, null, null));
    }
}