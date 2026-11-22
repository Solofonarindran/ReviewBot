package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.AnalysisRunStore;
import fr.reviewbot.domain.model.AnalysisRun;
import fr.reviewbot.domain.model.ModalEnum;
import fr.reviewbot.domain.model.Unset;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Adaptateur JDBC du port AnalysisRunStore (table analysis_run). */
@Component
public class JdbcAnalysisRunStore implements AnalysisRunStore {

    private static final String COLUMNS = "id, review_id, tool, status, findings_count, duration_ms, "
            + "error_message, started_at, finished_at";

    private final JdbcExecutor jdbc;

    public JdbcAnalysisRunStore(JdbcExecutor jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public AnalysisRun save(AnalysisRun run) throws Exception {
        if (!Unset.isSaved(run.id())) {
            long id = jdbc.insert(
                    "INSERT INTO analysis_run (review_id, tool, status, findings_count, duration_ms, "
                            + "error_message, started_at, finished_at) VALUES (?,?,?,?,?,?,?,?)",
                    ps -> {
                        ps.setLong(1, run.reviewId());
                        ps.setString(2, run.tool().name());
                        ps.setString(3, run.status().name());
                        ps.setInt(4, run.findingsCount());
                        Sql.setNumber(ps, 5, run.durationMs());
                        ps.setString(6, run.errorMessage());
                        ps.setTimestamp(7, Sql.toTimestamp(run.startedAt()));
                        ps.setTimestamp(8, Sql.toTimestamp(run.finishedAt()));
                    });
            return findById(id).orElseThrow();
        }
        int rows = jdbc.update(
                "UPDATE analysis_run SET tool=?, status=?, findings_count=?, duration_ms=?, error_message=?, "
                        + "started_at=?, finished_at=? WHERE id=?",
                ps -> {
                    ps.setString(1, run.tool().name());
                    ps.setString(2, run.status().name());
                    ps.setInt(3, run.findingsCount());
                    Sql.setNumber(ps, 4, run.durationMs());
                    ps.setString(5, run.errorMessage());
                    ps.setTimestamp(6, Sql.toTimestamp(run.startedAt()));
                    ps.setTimestamp(7, Sql.toTimestamp(run.finishedAt()));
                    ps.setLong(8, run.id());
                });
        if (rows == 0) {
            throw new RuntimeException("Exécution introuvable : " + run.id(), null);
        }
        return findById(run.id()).orElseThrow();
    }

    @Override
    public Optional<AnalysisRun> findById(long id) throws Exception {
        return jdbc.queryOne("SELECT " + COLUMNS + " FROM analysis_run WHERE id = ?",
                ps -> ps.setLong(1, id), JdbcAnalysisRunStore::map);
    }

    @Override
    public List<AnalysisRun> findByReviewId(long reviewId) throws SQLException {
        return jdbc.query("SELECT " + COLUMNS + " FROM analysis_run WHERE review_id = ? ORDER BY id",
                ps -> ps.setLong(1, reviewId), JdbcAnalysisRunStore::map);
    }

    private static AnalysisRun map(ResultSet rs) throws SQLException {
        return new AnalysisRun(
                rs.getLong("id"),
                rs.getLong("review_id"),
                Sql.getEnum(rs, "tool", ModalEnum.Tool.class),
                Sql.getEnum(rs, "status", ModalEnum.RunStatus.class),
                rs.getInt("findings_count"),
                Sql.getNumber(rs, "duration_ms"),
                rs.getString("error_message"),
                Sql.getInstant(rs, "started_at"),
                Sql.getInstant(rs, "finished_at"));
    }
}
