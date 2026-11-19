package fr.reviewbot.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Un commentaire publié par ReviewBot sur la PR. Aucun champ n'est null : voir {@link Unset}.
 *
 * @param id                identifiant, {@link Unset#ID} tant que le commentaire n'est pas enregistré
 * @param reviewId          revue concernée
 * @param findingId         problème commenté, {@link Unset#ID} pour le commentaire global
 * @param kind              global ou en ligne (obligatoire)
 * @param platformCommentId identifiant du commentaire chez GitHub/GitLab/Bitbucket ("" tant que pas publié)
 * @param filePath          fichier commenté ("" pour le commentaire global)
 * @param line              ligne commentée, {@link Unset#NUMBER} pour le commentaire global
 * @param state             état du commentaire (obligatoire)
 * @param postedAt          date de publication ({@link Unset#INSTANT} avant l'enregistrement)
 * @param updatedAt         date de dernière modification
 */
public record PostedComment(
        long id,
        long reviewId,
        long findingId,
        ModalEnum.CommentKind kind,
        String platformCommentId,
        String filePath,
        int line,
        ModalEnum.CommentState state,
        Instant postedAt,
        Instant updatedAt) {

    public PostedComment {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(state, "state");
        platformCommentId = Unset.text(platformCommentId);
        filePath = Unset.text(filePath);
        line = Unset.number(line);
        postedAt = Unset.instant(postedAt);
        updatedAt = Unset.instant(updatedAt);
    }

    /** Crée le commentaire global d'une revue. */
    public static PostedComment global(long reviewId, String platformCommentId) {
        return new PostedComment(Unset.ID, reviewId, Unset.ID, ModalEnum.CommentKind.GLOBAL, platformCommentId,
                Unset.TEXT, Unset.NUMBER, ModalEnum.CommentState.POSTED, Unset.INSTANT, Unset.INSTANT);
    }

    /** Crée un commentaire en ligne rattaché à un problème. */
    public static PostedComment inline(long reviewId, long findingId, String platformCommentId,
                                       String filePath, int line) {
        return new PostedComment(Unset.ID, reviewId, findingId, ModalEnum.CommentKind.INLINE, platformCommentId,
                filePath, line, ModalEnum.CommentState.POSTED, Unset.INSTANT, Unset.INSTANT);
    }
}
