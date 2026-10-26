package fr.reviewbot.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record Review(
        Long id,
        long repositoryId,
        String prNumber,
        String prTitle,
        String prAuthor,
        String sourceBranch,
        String targetBranch,
        String headCommitSha,
        ModalEnum.ReviewStatus status,
        ModalEnum.Verdict verdict,
        int blockingCount,
        int majorCount,
        int minorCount,
        BigDecimal coverageChangedLines,
        String errorMessage,
        String configSnapshot,
        String globalCommentRef,
        Instant queuedAt,
        Instant startedAt,
        Instant finishedAt
) {
    public Review {
        Objects.requireNonNull(prNumber);
        Objects.requireNonNull(prTitle);
        Objects.requireNonNull(prAuthor);
        Objects.requireNonNull(sourceBranch);
        Objects.requireNonNull(targetBranch);
        Objects.requireNonNull(headCommitSha);
        Objects.requireNonNull(status);
    }

    public static  Review queued(long repositoryId, String prNumber, String prTitle, String prAuthor,
                                 String sourceBranch, String targetBranch, String headCommitSha) {
        return new Review(null, repositoryId, prNumber, prTitle, prAuthor, sourceBranch,
                         targetBranch,headCommitSha, ModalEnum.ReviewStatus.QUEUED, null,0,0,0,
                null, null,null,null,null  ,null,null);
    }
}
