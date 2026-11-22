package fr.reviewbot.application.port.out;

import fr.reviewbot.domain.model.ModalEnum;
import fr.reviewbot.domain.model.PostedComment;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Port sortant : commentaires publiés sur les PR. */
public interface PostedCommentStore {

    /** Insère le commentaire s'il n'est pas encore enregistré (id = 0), sinon le met à jour. */
    PostedComment save(PostedComment comment) throws Exception;

    Optional<PostedComment> findById(long id) throws Exception;

    /** Commentaires d'une revue, dans l'ordre de publication. */
    List<PostedComment> findByReviewId(long reviewId) throws SQLException;

    /** Commentaires d'une revue d'un genre donné (par exemple le commentaire global à mettre à jour). */
    List<PostedComment> findByReviewIdAndKind(long reviewId, ModalEnum.CommentKind kind) throws SQLException;
}
