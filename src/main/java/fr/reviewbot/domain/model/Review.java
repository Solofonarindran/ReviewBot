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


    public Review  {
        if (prNumber == null || prNumber.isBlank()) {
            throw new IllegalArgumentException("prNumber est obligatoire");
        }
        if (headCommitSha == null || headCommitSha.isBlank()) {
            throw new IllegalArgumentException("headCommitSha est obligatoire");
        }
        Objects.requireNonNull(status, "status");
        verdict = (verdict == null) ? ModalEnum.Verdict.PENDING : verdict;
        prTitle = Unset.text(prTitle);
        prAuthor = Unset.text(prAuthor);
        sourceBranch = Unset.text(sourceBranch);
        targetBranch = Unset.text(targetBranch);
        errorMessage = Unset.text(errorMessage);
        configSnapshot = Unset.text(configSnapshot);
        globalCommentRef = Unset.text(globalCommentRef);
        coverageChangedLines = Unset.percent(coverageChangedLines);
        queuedAt = Unset.instant(queuedAt);
        startedAt = Unset.instant(startedAt);
        finishedAt = Unset.instant(finishedAt);
    }
    /** Crée une revue neuve, en attente, sans résultat. */

    public static  Review queued(long repositoryId, String prNumber, String prTitle, String prAuthor,
                                 String sourceBranch, String targetBranch, String headCommitSha) {
        return new Review(Unset.ID, repositoryId, prNumber, prTitle, prAuthor, sourceBranch,
                         targetBranch,headCommitSha, ModalEnum.ReviewStatus.QUEUED, ModalEnum.Verdict.PENDING,0,0,0,
                Unset.PERCENT, Unset.TEXT,Unset.TEXT,Unset.TEXT,Unset.INSTANT,Unset.INSTANT,Unset.INSTANT);
    }
}
