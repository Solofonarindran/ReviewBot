package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.PostedCommentStore;
import fr.reviewbot.domain.model.ModalEnum;
import fr.reviewbot.domain.model.PostedComment;
import fr.reviewbot.domain.model.Unset;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

/** Adaptateur JDBC du port PostedCommentStore (table posted_comment). */
@Component
public class JdbcPostedCommentStore implements PostedCommentStore {

    private static final String COLUMNS = "id, review_id, finding_id, kind, platform_comment_id, file_path, "
            + "line, state, posted_at, updated_at";

    private final JdbcExecutor jdbc;
    private final Clock clock;

    public JdbcPostedCommentStore(JdbcExecutor jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public PostedComment save(PostedComment c) throws Exception {
        var now = clock.instant();
        if (!Unset.isSaved(c.id())) {
            var postedAt = Sql.toTimestamp(Unset.isSet(c.postedAt()) ? c.postedAt() : now);
            long id = jdbc.insert(
                    "INSERT INTO posted_comment (review_id, finding_id, kind, platform_comment_id, file_path, "
                            + "line, state, posted_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?)",
                    ps -> {
                        ps.setLong(1, c.reviewId());
                        Sql.setId(ps, 2, c.findingId());
                        ps.setString(3, c.kind().name());
                        ps.setString(4, c.platformCommentId());
                        ps.setString(5, c.filePath());
                        Sql.setNumber(ps, 6, c.line());
                        ps.setString(7, c.state().name());
                        ps.setTimestamp(8, postedAt);
                        ps.setTimestamp(9, postedAt);
                    });
            return findById(id).orElseThrow();
        }
        int rows = jdbc.update(
                "UPDATE posted_comment SET finding_id=?, kind=?, platform_comment_id=?, file_path=?, line=?, "
                        + "state=?, updated_at=? WHERE id=?",
                ps -> {
                    Sql.setId(ps, 1, c.findingId());
                    ps.setString(2, c.kind().name());
                    ps.setString(3, c.platformCommentId());
                    ps.setString(4, c.filePath());
                    Sql.setNumber(ps, 5, c.line());
                    ps.setString(6, c.state().name());
                    ps.setTimestamp(7, Sql.toTimestamp(now));
                    ps.setLong(8, c.id());
                });
        if (rows == 0) {
            throw new RuntimeException("Commentaire introuvable : " + c.id(), null);
        }
        return findById(c.id()).orElseThrow();
    }

    @Override
    public Optional<PostedComment> findById(long id) throws Exception {
        return jdbc.queryOne("SELECT " + COLUMNS + " FROM posted_comment WHERE id = ?",
                ps -> ps.setLong(1, id), JdbcPostedCommentStore::map);
    }

    @Override
    public List<PostedComment> findByReviewId(long reviewId) throws SQLException {
        return jdbc.query("SELECT " + COLUMNS + " FROM posted_comment WHERE review_id = ? ORDER BY id",
                ps -> ps.setLong(1, reviewId), JdbcPostedCommentStore::map);
    }

    @Override
    public List<PostedComment> findByReviewIdAndKind(long reviewId, ModalEnum.CommentKind kind) throws SQLException {
        return jdbc.query("SELECT " + COLUMNS + " FROM posted_comment WHERE review_id = ? AND kind = ? ORDER BY id",
                ps -> {
                    ps.setLong(1, reviewId);
                    ps.setString(2, kind.name());
                }, JdbcPostedCommentStore::map);
    }

    private static PostedComment map(ResultSet rs) throws SQLException {
        return new PostedComment(
                rs.getLong("id"),
                rs.getLong("review_id"),
                Sql.getId(rs, "finding_id"),
                Sql.getEnum(rs, "kind", ModalEnum.CommentKind.class),
                rs.getString("platform_comment_id"),
                rs.getString("file_path"),
                Sql.getNumber(rs, "line"),
                Sql.getEnum(rs, "state", ModalEnum.CommentState.class),
                Sql.getInstant(rs, "posted_at"),
                Sql.getInstant(rs, "updated_at"));
    }
}

