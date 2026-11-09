package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.ReviewStore;
import fr.reviewbot.domain.model.Review;

import java.time.Clock;

public class JdbcReviewStore implements ReviewStore {
    private static final String COLUMNS = "id, repository_id, pr_number, pr_title, pr_author, source_branch, "
            + "target_branch, head_commit_sha, status, verdict, blocking_count, major_count, minor_count, "
            + "coverage_changed_lines, error_message, config_snapshot, global_comment_ref, "
            + "queued_at, started_at, finished_at";
    private static final String ORDER_NEWEST_FIRST = " ORDER BY queued_at DESC, id DESC";
    private final JdbcExecutor jdbc;
    private final Clock clock;

    public JdbcReviewStore(JdbcExecutor jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }
    @Override
    public Review save(Review r) {
        if (r.id() == null) {
            var queuedAt = Sql.toTimestamp(r.queuedAt() != null ? r.queuedAt() : clock.instant());
            try {
                long id = jdbc.insert(
                        "INSERT INTO review (repository_id, pr_number, pr_title, pr_author, source_branch, "
                                + "target_branch, head_commit_sha, status, verdict, blocking_count, major_count, "
                                + "minor_count, coverage_changed_lines, error_message, config_snapshot, "
                                + "global_comment_ref, queued_at, started_at, finished_at) "
                                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        ps -> {
                            ps.setLong(1, r.repositoryId());
                            ps.setString(2, r.prNumber());
                            ps.setString(3, r.prTitle());
                            ps.setString(4, r.prAuthor());
                            ps.setString(5, r.sourceBranch());
                            ps.setString(6, r.targetBranch());
                            ps.setString(7, r.headCommitSha());
                            ps.setString(8, r.status().name());
                            ps.setString(9, Sql.name(r.verdict()));
                            ps.setInt(10, r.blockingCount());
                            ps.setInt(11, r.majorCount());
                            ps.setInt(12, r.minorCount());
                            ps.setBigDecimal(13, r.coverageChangedLines());
                            ps.setString(14, r.errorMessage());
                            ps.setString(15, r.configSnapshot());
                            ps.setString(16, r.globalCommentRef());
                            ps.setTimestamp(17, queuedAt);
                            ps.setTimestamp(18, Sql.toTimestamp(r.startedAt()));
                            ps.setTimestamp(19, Sql.toTimestamp(r.finishedAt()));
                        });
                return findById(id).orElseThrow();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        try {
            int rows = jdbc.update(
                    "UPDATE review SET pr_title=?, pr_author=?, source_branch=?, target_branch=?, head_commit_sha=?, "
                            + "status=?, verdict=?, blocking_count=?, major_count=?, minor_count=?, "
                            + "coverage_changed_lines=?, error_message=?, config_snapshot=?, global_comment_ref=?, "
                            + "started_at=?, finished_at=? WHERE id=?",
                    ps -> {
                        ps.setString(1, r.prTitle());
                        ps.setString(2, r.prAuthor());
                        ps.setString(3, r.sourceBranch());
                        ps.setString(4, r.targetBranch());
                        ps.setString(5, r.headCommitSha());
                        ps.setString(6, r.status().name());
                        ps.setString(7, Sql.name(r.verdict()));
                        ps.setInt(8, r.blockingCount());
                        ps.setInt(9, r.majorCount());
                        ps.setInt(10, r.minorCount());
                        ps.setBigDecimal(11, r.coverageChangedLines());
                        ps.setString(12, r.errorMessage());
                        ps.setString(13, r.configSnapshot());
                        ps.setString(14, r.globalCommentRef());
                        ps.setTimestamp(15, Sql.toTimestamp(r.startedAt()));
                        ps.setTimestamp(16, Sql.toTimestamp(r.finishedAt()));
                        ps.setLong(17, r.id());
                    });
            if (rows == 0) {
                throw new RuntimeException("Revue introuvable : " + r.id(), null);
            }
            return findById(r.id()).orElseThrow();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
