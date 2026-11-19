package fr.reviewbot.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * L'exécution d'un outil d'analyse (Checkstyle, PMD...) pendant une revue. Aucun champ n'est null : voir {@link Unset}.
 *
 * @param id            identifiant, {@link Unset#ID} tant que l'exécution n'est pas enregistrée
 * @param reviewId      revue concernée
 * @param tool          outil lancé (obligatoire)
 * @param status        résultat de l'exécution (obligatoire)
 * @param findingsCount nombre de problèmes trouvés (jamais négatif)
 * @param durationMs    durée en millisecondes, {@link Unset#NUMBER} si inconnue
 * @param errorMessage  cause de l'échec ("" si aucune)
 * @param startedAt     début ({@link Unset#INSTANT} si pas commencée)
 * @param finishedAt    fin ({@link Unset#INSTANT} si pas terminée)
 */
public record AnalysisRun(
        long id,
        long reviewId,
        ModalEnum.Tool tool,
        ModalEnum.RunStatus status,
        int findingsCount,
        int durationMs,
        String errorMessage,
        Instant startedAt,
        Instant finishedAt) {

    public AnalysisRun {
        Objects.requireNonNull(tool, "tool");
        Objects.requireNonNull(status, "status");
        if (findingsCount < 0) {
            throw new IllegalArgumentException("findingsCount ne peut pas être négatif");
        }
        durationMs = Unset.number(durationMs);
        errorMessage = Unset.text(errorMessage);
        startedAt = Unset.instant(startedAt);
        finishedAt = Unset.instant(finishedAt);
    }

    /** Crée une exécution terminée avec succès. */
    public static AnalysisRun ok(long reviewId, ModalEnum.Tool tool, int findingsCount, int durationMs,
                                 Instant startedAt, Instant finishedAt) {
        return new AnalysisRun(Unset.ID, reviewId, tool, ModalEnum.RunStatus.OK, findingsCount, durationMs,
                Unset.TEXT, startedAt, finishedAt);
    }

    /** Crée une exécution en échec (ou expirée, ou ignorée) avec sa cause. */
    public static AnalysisRun notOk(long reviewId, ModalEnum.Tool tool, ModalEnum.RunStatus status, String errorMessage,
                                    Instant startedAt, Instant finishedAt) {
        return new AnalysisRun(Unset.ID, reviewId, tool, status, 0, Unset.NUMBER, errorMessage,
                startedAt, finishedAt);
    }
}
