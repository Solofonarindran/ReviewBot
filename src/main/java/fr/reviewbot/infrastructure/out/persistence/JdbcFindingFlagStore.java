package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.FindingFlagStore;
import fr.reviewbot.domain.model.FindingFlag;
import fr.reviewbot.domain.model.Unset;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

/** Adaptateur JDBC du port FindingFlagStore (table finding_flag). */
@Component
public class JdbcFindingFlagStore implements FindingFlagStore {

    private static final String COLUMNS = "id, finding_id, flagged_by, reason, flagged_at";

    private final JdbcExecutor jdbc;
    private final Clock clock;

    public JdbcFindingFlagStore(JdbcExecutor jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public FindingFlag save(FindingFlag flag) throws Exception {
        if (!Unset.isSaved(flag.id())) {
            var flaggedAt = Sql.toTimestamp(Unset.isSet(flag.flaggedAt()) ? flag.flaggedAt() : clock.instant());
            long id = jdbc.insert(
                    "INSERT INTO finding_flag (finding_id, flagged_by, reason, flagged_at) VALUES (?,?,?,?)",
                    ps -> {
                        ps.setLong(1, flag.findingId());
                        ps.setString(2, flag.flaggedBy());
                        ps.setString(3, flag.reason());
                        ps.setTimestamp(4, flaggedAt);
                    });
            return findById(id).orElseThrow();
        }
        int rows = jdbc.update("UPDATE finding_flag SET flagged_by=?, reason=? WHERE id=?",
                ps -> {
                    ps.setString(1, flag.flaggedBy());
                    ps.setString(2, flag.reason());
                    ps.setLong(3, flag.id());
                });
        if (rows == 0) {
            throw new RuntimeException("Signalement introuvable : " + flag.id(), null);
        }
        return findById(flag.id()).orElseThrow();
    }

    @Override
    public Optional<FindingFlag> findByFindingId(long findingId) throws Exception {
        return jdbc.queryOne("SELECT " + COLUMNS + " FROM finding_flag WHERE finding_id = ?",
                ps -> ps.setLong(1, findingId), JdbcFindingFlagStore::map);
    }

    @Override
    public List<FindingFlag> findByReviewId(long reviewId) throws SQLException {
        return jdbc.query("SELECT f.id, f.finding_id, f.flagged_by, f.reason, f.flagged_at "
                        + "FROM finding_flag f JOIN finding x ON x.id = f.finding_id "
                        + "WHERE x.review_id = ? ORDER BY f.id",
                ps -> ps.setLong(1, reviewId), JdbcFindingFlagStore::map);
    }

    @Override
    public void deleteByFindingId(long findingId) throws Exception {
        jdbc.update("DELETE FROM finding_flag WHERE finding_id = ?", ps -> ps.setLong(1, findingId));
    }

    private Optional<FindingFlag> findById(long id) throws Exception {
        return jdbc.queryOne("SELECT " + COLUMNS + " FROM finding_flag WHERE id = ?",
                ps -> ps.setLong(1, id), JdbcFindingFlagStore::map);
    }

    private static FindingFlag map(ResultSet rs) throws SQLException {
        return new FindingFlag(
                rs.getLong("id"),
                rs.getLong("finding_id"),
                rs.getString("flagged_by"),
                rs.getString("reason"),
                Sql.getInstant(rs, "flagged_at"));
    }
}

