package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.JobQueueStore;
import fr.reviewbot.domain.model.JobQueueEntry;
import fr.reviewbot.domain.model.ModalEnum;
import fr.reviewbot.domain.model.Unset;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Adaptateur JDBC du port JobQueueStore (table job_queue).
 * Ordre de passage : created_at puis id. Une tâche qui repasse en file garde sa date d'origine,
 * donc sa place dans la file.
 */
@Component
public class JdbcJobQueueStore implements JobQueueStore {

    private static final String COLUMNS = "id, review_id, state, attempts, last_error, locked_by, "
            + "locked_until, created_at, updated_at";

    /** Une tâche QUEUED est prête si aucune autre tâche de la même PR n'est en cours. */
    private static final String NEXT_READY_JOB = "SELECT j.id FROM job_queue j "
            + "JOIN review r ON r.id = j.review_id "
            + "WHERE j.state = 'QUEUED' AND NOT EXISTS ("
            + "SELECT 1 FROM job_queue j2 JOIN review r2 ON r2.id = j2.review_id "
            + "WHERE j2.state = 'RUNNING' AND r2.repository_id = r.repository_id AND r2.pr_number = r.pr_number) "
            + "ORDER BY j.created_at, j.id LIMIT 1";

    private static final int MAX_CLAIM_TRIES = 3;

    private final JdbcExecutor jdbc;
    private final Clock clock;

    public JdbcJobQueueStore(JdbcExecutor jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public JobQueueEntry enqueue(long reviewId) throws Exception {
        var now = Sql.toTimestamp(clock.instant());
        long id = jdbc.insert(
                "INSERT INTO job_queue (review_id, state, attempts, created_at, updated_at) VALUES (?,?,?,?,?)",
                ps -> {
                    ps.setLong(1, reviewId);
                    ps.setString(2, ModalEnum.JobState.QUEUED.name());
                    ps.setInt(3, 0);
                    ps.setTimestamp(4, now);
                    ps.setTimestamp(5, now);
                });
        return findById(id).orElseThrow();
    }

    /**
     * "synchronized" : la recherche de la tâche et sa réservation forment un seul bloc pour ce processus
     * (la base HSQLDB en fichier n'est utilisée que par une seule application). La réservation SQL reste
     * conditionnelle (state = 'QUEUED') par sécurité.
     */
    @Override
    public synchronized Optional<JobQueueEntry> claimNext(String workerName, Duration lockDuration) throws Exception {
        for (int attempt = 0; attempt < MAX_CLAIM_TRIES; attempt++) {
            Optional<Long> candidate = jdbc.queryOne(NEXT_READY_JOB, ps -> { }, rs -> rs.getLong(1));
            if (candidate.isEmpty()) {
                return Optional.empty();
            }
            long jobId = candidate.get();
            var now = clock.instant();
            int rows = jdbc.update(
                    "UPDATE job_queue SET state = 'RUNNING', attempts = attempts + 1, locked_by = ?, "
                            + "locked_until = ?, updated_at = ? WHERE id = ? AND state = 'QUEUED'",
                    ps -> {
                        ps.setString(1, workerName);
                        ps.setTimestamp(2, Sql.toTimestamp(now.plus(lockDuration)));
                        ps.setTimestamp(3, Sql.toTimestamp(now));
                        ps.setLong(4, jobId);
                    });
            if (rows == 1) {
                return findById(jobId);
            }
        }
        return Optional.empty();
    }

    @Override
    public JobQueueEntry markDone(long jobId) throws Exception {
        int rows = jdbc.update(
                "UPDATE job_queue SET state = 'DONE', locked_by = NULL, locked_until = NULL, updated_at = ? "
                        + "WHERE id = ?",
                ps -> {
                    ps.setTimestamp(1, Sql.toTimestamp(clock.instant()));
                    ps.setLong(2, jobId);
                });
        return afterUpdate(rows, jobId);
    }

    @Override
    public JobQueueEntry markFailed(long jobId, String error, int maxAttempts) throws Exception {
        JobQueueEntry job = findById(jobId)
                .orElseThrow(() -> new RuntimeException("Tâche introuvable : " + jobId, null));
        var next = job.attempts() >= maxAttempts ? ModalEnum.JobState.FAILED : ModalEnum.JobState.QUEUED;
        int rows = jdbc.update(
                "UPDATE job_queue SET state = ?, last_error = ?, locked_by = NULL, locked_until = NULL, "
                        + "updated_at = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, next.name());
                    ps.setString(2, Unset.text(error));
                    ps.setTimestamp(3, Sql.toTimestamp(clock.instant()));
                    ps.setLong(4, jobId);
                });
        return afterUpdate(rows, jobId);
    }

    @Override
    public int releaseExpired(int maxAttempts) throws Exception {
        var now = Sql.toTimestamp(clock.instant());
        String message = "Verrou expiré : le worker s'est arrêté avant la fin de la tâche";
        int failed = releaseExpiredTo(ModalEnum.JobState.FAILED, "attempts >= ?", maxAttempts, message, now);
        int retried = releaseExpiredTo(ModalEnum.JobState.QUEUED, "attempts < ?", maxAttempts, message, now);
        return failed + retried;
    }

    private int releaseExpiredTo(ModalEnum.JobState next, String attemptsCondition, int maxAttempts,
                                 String message, java.sql.Timestamp now) throws Exception {
        return jdbc.update(
                "UPDATE job_queue SET state = ?, last_error = ?, locked_by = NULL, locked_until = NULL, "
                        + "updated_at = ? WHERE state = 'RUNNING' AND locked_until < ? AND " + attemptsCondition,
                ps -> {
                    ps.setString(1, next.name());
                    ps.setString(2, message);
                    ps.setTimestamp(3, now);
                    ps.setTimestamp(4, now);
                    ps.setInt(5, maxAttempts);
                });
    }

    @Override
    public Optional<JobQueueEntry> findById(long jobId) throws Exception {
        return jdbc.queryOne("SELECT " + COLUMNS + " FROM job_queue WHERE id = ?",
                ps -> ps.setLong(1, jobId), JdbcJobQueueStore::map);
    }

    @Override
    public List<JobQueueEntry> findByReviewId(long reviewId) throws SQLException {
        return jdbc.query("SELECT " + COLUMNS + " FROM job_queue WHERE review_id = ? ORDER BY id",
                ps -> ps.setLong(1, reviewId), JdbcJobQueueStore::map);
    }

    @Override
    public long countByState(ModalEnum.JobState state) throws Exception {
        return jdbc.queryOne("SELECT COUNT(*) FROM job_queue WHERE state = ?",
                ps -> ps.setString(1, state.name()), rs -> rs.getLong(1)).orElse(0L);
    }

    private JobQueueEntry afterUpdate(int rows, long jobId) throws Exception {
        if (rows == 0) {
            throw new RuntimeException("Tâche introuvable : " + jobId, null);
        }
        return findById(jobId).orElseThrow();
    }

    private static JobQueueEntry map(ResultSet rs) throws SQLException {
        return new JobQueueEntry(
                rs.getLong("id"),
                rs.getLong("review_id"),
                Sql.getEnum(rs, "state", ModalEnum.JobState.class),
                rs.getInt("attempts"),
                rs.getString("last_error"),
                rs.getString("locked_by"),
                Sql.getInstant(rs, "locked_until"),
                Sql.getInstant(rs, "created_at"),
                Sql.getInstant(rs, "updated_at"));
    }
}
