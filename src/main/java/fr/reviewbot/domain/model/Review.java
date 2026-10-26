package fr.reviewbot.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
/**
 * Une revue de pull request, pour un commit précis.
 *
 * @param id                   identifiant, null tant que la revue n'est pas enregistrée
 * @param repositoryId         dépôt de la PR
 * @param prNumber             numéro de la PR sur la plateforme
 * @param prTitle              titre de la PR
 * @param prAuthor             auteur de la PR
 * @param sourceBranch         branche source
 * @param targetBranch         branche cible
 * @param headCommitSha        commit analysé
 * @param status               avancement de la revue
 * @param verdict              verdict final, null tant que la revue n'est pas terminée
 * @param blockingCount        nombre de problèmes bloquants
 * @param majorCount           nombre de problèmes majeurs
 * @param minorCount           nombre de problèmes mineurs
 * @param coverageChangedLines couverture JaCoCo des lignes modifiées (en %), null si inconnue
 * @param errorMessage         cause de l'échec, le cas échéant
 * @param configSnapshot       copie JSON du .reviewbot.yml utilisé
 * @param globalCommentRef     identifiant du commentaire global sur la plateforme
 * @param queuedAt             mise en file (renseignée à l'enregistrement si null)
 * @param startedAt            début d'exécution
 * @param finishedAt           fin d'exécution
 */

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

    /** Crée une revue neuve, en attente, sans résultat. */

    public static  Review queued(long repositoryId, String prNumber, String prTitle, String prAuthor,
                                 String sourceBranch, String targetBranch, String headCommitSha) {
        return new Review(null, repositoryId, prNumber, prTitle, prAuthor, sourceBranch,
                         targetBranch,headCommitSha, ModalEnum.ReviewStatus.QUEUED, null,0,0,0,
                null, null,null,null,null  ,null,null);
    }
}
