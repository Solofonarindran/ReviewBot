package fr.reviewbot.domain.model;

public class ModalEnum {
    public static  enum Platform {
        GITHUB,
        GITLAB,
        BITBUCKET
    }

    public static  enum Category {
        BUG,
        STYLE,
        SECURITY,
        COVERAGE,
        DOC,
        CODE
    }
    public static enum CommentKind {
        GLOBAL,
        INLINE
    }
    public static enum CommentState {
        POSTED,
        RESOLVED,
        DELETED
    }

    public static enum JobState {
        QUEUED,
        RUNNING,
        DONE,
        FAILED
    }

    public static  enum ReviewStatus {
        QUEUED,
        RUNNING,
        DONE,
        FAILED,
        CANCELLED
    }

    public static enum RunStatus {
        OK,
        FAILED,
        SKIPPED,
        TIMEOUT
    }

    public static enum Severity {
        BLOCKING,
        MAJOR,
        MINOR,
        INFO
    }

    public  static  enum Tool {
        JAVADOC,
        JUNIT,
        CHECKSTYLE,
        PMD,
        SPOTBUGS,
        ERRORPRONE,
        JACOCO,
        LLM
    }

    public static enum Verdict{
        APPROVED,
        TO_FIX,
        BLOCKING
    }

}
