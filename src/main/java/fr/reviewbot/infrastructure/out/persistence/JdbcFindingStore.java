package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.FindingStore;
import fr.reviewbot.domain.model.Finding;
import fr.reviewbot.domain.model.FindingFilter;
import fr.reviewbot.domain.model.ModalEnum;
import fr.reviewbot.domain.model.Unset;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.*;

/** Adaptateur JDBC du port FindingStore (table finding). */
@Component
public class JdbcFindingStore implements FindingStore {

    private static final String COLUMNS = "id, review_id, analysis_run_id, merged_into_id, source, rule_id, "
            + "category, severity, file_path, line_start, line_end, title, message, suggestion, in_diff, "
            + "fingerprint, created_at";

    private static final String MOST_SEVERE_FIRST = " ORDER BY CASE severity WHEN 'BLOCKING' THEN 0 "
            + "WHEN 'MAJOR' THEN 1 WHEN 'MINOR' THEN 2 ELSE 3 END, file_path, line_start, id";

    private final JdbcExecutor jdbc;
    private final Clock clock;

    public JdbcFindingStore(JdbcExecutor jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public Finding save(Finding f) throws Exception {
        if (!Unset.isSaved(f.id())) {
            var createdAt = Sql.toTimestamp(Unset.isSet(f.createdAt()) ? f.createdAt() : clock.instant());
            long id = jdbc.insert(
                    "INSERT INTO finding (review_id, analysis_run_id, merged_into_id, source, rule_id, category, "
                            + "severity, file_path, line_start, line_end, title, message, suggestion, in_diff, "
                            + "fingerprint, created_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    ps -> {
                        ps.setLong(1, f.reviewId());
                        Sql.setId(ps, 2, f.analysisRunId());
                        Sql.setId(ps, 3, f.mergedIntoId());
                        ps.setString(4, f.source());
                        ps.setString(5, f.ruleId());
                        ps.setString(6, f.category().name());
                        ps.setString(7, f.severity().name());
                        ps.setString(8, f.filePath());
                        Sql.setNumber(ps, 9, f.lineStart());
                        Sql.setNumber(ps, 10, f.lineEnd());
                        ps.setString(11, f.title());
                        ps.setString(12, f.message());
                        ps.setString(13, f.suggestion());
                        ps.setBoolean(14, f.inDiff());
                        ps.setString(15, f.fingerprint());
                        ps.setTimestamp(16, createdAt);
                    });
            return findById(id).orElseThrow();
        }
        int rows = jdbc.update(
                "UPDATE finding SET analysis_run_id=?, merged_into_id=?, source=?, rule_id=?, category=?, "
                        + "severity=?, file_path=?, line_start=?, line_end=?, title=?, message=?, suggestion=?, "
                        + "in_diff=?, fingerprint=? WHERE id=?",
                ps -> {
                    Sql.setId(ps, 1, f.analysisRunId());
                    Sql.setId(ps, 2, f.mergedIntoId());
                    ps.setString(3, f.source());
                    ps.setString(4, f.ruleId());
                    ps.setString(5, f.category().name());
                    ps.setString(6, f.severity().name());
                    ps.setString(7, f.filePath());
                    Sql.setNumber(ps, 8, f.lineStart());
                    Sql.setNumber(ps, 9, f.lineEnd());
                    ps.setString(10, f.title());
                    ps.setString(11, f.message());
                    ps.setString(12, f.suggestion());
                    ps.setBoolean(13, f.inDiff());
                    ps.setString(14, f.fingerprint());
                    ps.setLong(15, f.id());
                });
        if (rows == 0) {
            throw new RuntimeException("Problème introuvable : " + f.id(), null);
        }
        return findById(f.id()).orElseThrow();
    }

    @Override
    @Transactional
    public List<Finding> saveAll(List<Finding> findings) throws Exception {
        List<Finding> saved = new ArrayList<>(findings.size());
        for (Finding finding : findings) {
            saved.add(save(finding));
        }
        return saved;
    }

    @Override
    public Optional<Finding> findById(long id) throws Exception {
        return jdbc.queryOne("SELECT " + COLUMNS + " FROM finding WHERE id = ?",
                ps -> ps.setLong(1, id), JdbcFindingStore::map);
    }

    @Override
    public List<Finding> findByReviewId(long reviewId) throws SQLException {
        return jdbc.query("SELECT " + COLUMNS + " FROM finding WHERE review_id = ? ORDER BY id",
                ps -> ps.setLong(1, reviewId), JdbcFindingStore::map);
    }

    @Override
    public List<Finding> search(FindingFilter filter) throws SQLException {
        List<ModalEnum.Category> categories = List.copyOf(filter.categories());
        List<ModalEnum.Severity> severities = List.copyOf(filter.severities());
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM finding WHERE review_id = ?");
        appendIn(sql, "category", categories.size());
        appendIn(sql, "severity", severities.size());
        if (filter.inDiffOnly()) {
            sql.append(" AND in_diff = TRUE");
        }
        if (!filter.includeMerged()) {
            sql.append(" AND merged_into_id IS NULL");
        }
        sql.append(MOST_SEVERE_FIRST);
        return jdbc.query(sql.toString(), ps -> {
            int index = 1;
            ps.setLong(index++, filter.reviewId());
            for (var category : categories) {
                ps.setString(index++, category.name());
            }
            for (var severity : severities) {
                ps.setString(index++, severity.name());
            }
        }, JdbcFindingStore::map);
    }

    @Override
    public List<Finding> findByReviewIdAndFingerprint(long reviewId, String fingerprint) throws SQLException {
        return jdbc.query("SELECT " + COLUMNS + " FROM finding WHERE review_id = ? AND fingerprint = ? ORDER BY id",
                ps -> {
                    ps.setLong(1, reviewId);
                    ps.setString(2, fingerprint);
                }, JdbcFindingStore::map);
    }

    @Override
    public Map<ModalEnum.Severity, Long> countVisibleBySeverity(long reviewId) throws SQLException {
        Map<ModalEnum.Severity, Long> counts = new EnumMap<>(ModalEnum.Severity.class);
        for (ModalEnum.Severity severity : ModalEnum.Severity.values()) {
            counts.put(severity, 0L);
        }
        jdbc.query("SELECT severity, COUNT(*) AS total FROM finding "
                        + "WHERE review_id = ? AND merged_into_id IS NULL GROUP BY severity",
                ps -> ps.setLong(1, reviewId),
                rs -> {
                    counts.put(ModalEnum.Severity.valueOf(rs.getString("severity")), rs.getLong("total"));
                    return rs.getLong("total");
                });
        return counts;
    }

    private static void appendIn(StringBuilder sql, String column, int count) {
        if (count == 0) {
            return;
        }
        sql.append(" AND ").append(column).append(" IN (");
        sql.append("?,".repeat(count), 0, count * 2 - 1);
        sql.append(')');
    }

    private static Finding map(ResultSet rs) throws SQLException {
        return new Finding(
                rs.getLong("id"),
                rs.getLong("review_id"),
                Sql.getId(rs, "analysis_run_id"),
                Sql.getId(rs, "merged_into_id"),
                rs.getString("source"),
                rs.getString("rule_id"),
                Sql.getEnum(rs, "category", ModalEnum.Category.class),
                Sql.getEnum(rs, "severity", ModalEnum.Severity.class),
                rs.getString("file_path"),
                Sql.getNumber(rs, "line_start"),
                Sql.getNumber(rs, "line_end"),
                rs.getString("title"),
                rs.getString("message"),
                rs.getString("suggestion"),
                rs.getBoolean("in_diff"),
                rs.getString("fingerprint"),
                Sql.getInstant(rs, "created_at"));
    }
}

