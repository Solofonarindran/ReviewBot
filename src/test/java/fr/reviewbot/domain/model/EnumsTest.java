package fr.reviewbot.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Arrays;
import java.util.List;

public class EnumsTest {
    private static List<String> names(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).toList();
    }

    @Test
    void platformValues() {
        assertEquals(List.of("GITHUB", "GITLAB", "BITBUCKET"), names(ModalEnum.Platform.values()));
    }

    @Test
    void reviewStatusValues() {
        assertEquals(List.of("QUEUED", "RUNNING", "DONE", "FAILED", "CANCELLED"), names(ModalEnum.ReviewStatus.values()));
    }

    @Test
    void verdictValues() {
        assertEquals(List.of("APPROVED", "TO_FIX", "BLOCKING"), names(ModalEnum.Verdict.values()));
    }

    @Test
    void jobStateValues() {
        assertEquals(List.of("QUEUED", "RUNNING", "DONE", "FAILED"), names(ModalEnum.JobState.values()));
    }

    @Test
    void toolValues() {
        assertEquals(List.of("JAVADOC", "JUNIT", "CHECKSTYLE", "PMD", "SPOTBUGS", "ERRORPRONE", "JACOCO", "LLM"),
                names(ModalEnum.Tool.values()));
    }

    @Test
    void runStatusValues() {
        assertEquals(List.of("OK", "FAILED", "SKIPPED", "TIMEOUT"), names(ModalEnum.RunStatus.values()));
    }

    @Test
    void categoryValues() {
        assertEquals(List.of("BUG", "STYLE", "SECURITY", "COVERAGE", "DOC", "CODE"), names(ModalEnum.Category.values()));
    }

    @Test
    void severityValues() {
        assertEquals(List.of("BLOCKING", "MAJOR", "MINOR", "INFO"), names(ModalEnum.Severity.values()));
    }

    @Test
    void commentKindValues() {
        assertEquals(List.of("GLOBAL", "INLINE"), names(ModalEnum.CommentKind.values()));
    }

    @Test
    void commentStateValues() {
        assertEquals(List.of("POSTED", "RESOLVED", "DELETED"), names(ModalEnum.CommentState.values()));
    }
}
