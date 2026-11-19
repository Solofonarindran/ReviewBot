package fr.reviewbot.domain.model;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

/**
 * Un problème trouvé dans le code d'une PR, par un outil ou par le LLM. Aucun champ n'est null : voir {@link Unset}.
 *
 * @param id            identifiant, {@link Unset#ID} tant que le problème n'est pas enregistré
 * @param reviewId      revue concernée
 * @param analysisRunId exécution qui l'a produit, {@link Unset#ID} si aucune
 * @param mergedIntoId  problème "principal" dans lequel celui-ci a été fusionné (doublon), {@link Unset#ID} sinon
 * @param source        qui l'a trouvé : nom de l'outil ou "LLM" (obligatoire)
 * @param ruleId        règle de l'outil ("" si aucune)
 * @param category      catégorie (obligatoire)
 * @param severity      gravité (obligatoire)
 * @param filePath      fichier concerné ("" si le problème n'est pas lié à un fichier)
 * @param lineStart     première ligne, {@link Unset#NUMBER} si inconnue
 * @param lineEnd       dernière ligne, {@link Unset#NUMBER} si inconnue
 * @param title         titre court (obligatoire)
 * @param message       explication détaillée
 * @param suggestion    correction proposée
 * @param inDiff        vrai si la ligne fait partie des lignes modifiées par la PR
 * @param fingerprint   empreinte stable servant à détecter les doublons (obligatoire)
 * @param createdAt     date de création ({@link Unset#INSTANT} avant l'enregistrement)
 */

@Component
public record Finding(
        long id,
        long reviewId,
        long analysisRunId,
        long mergedIntoId,
        String source,
        String ruleId,
        ModalEnum.Category category,
        ModalEnum.Severity severity,
        String filePath,
        int lineStart,
        int lineEnd,
        String title,
        String message,
        String suggestion,
        boolean inDiff,
        String fingerprint,
        Instant createdAt) {

    public Finding {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source est obligatoire");
        }
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(severity, "severity");
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title est obligatoire");
        }
        if (fingerprint == null || fingerprint.isBlank()) {
            throw new IllegalArgumentException("fingerprint est obligatoire");
        }
        ruleId = Unset.text(ruleId);
        filePath = Unset.text(filePath);
        lineStart = Unset.number(lineStart);
        lineEnd = Unset.number(lineEnd);
        message = Unset.text(message);
        suggestion = Unset.text(suggestion);
        createdAt = Unset.instant(createdAt);
    }

    /** Vrai si ce problème a été fusionné dans un autre : on ne l'affiche plus. */
    public boolean isMerged() {
        return Unset.isSaved(mergedIntoId);
    }

    /** Copie de ce problème, marqué comme doublon du problème {@code principalId}. */
    public Finding mergedInto(long principalId) {
        return new Finding(id, reviewId, analysisRunId, principalId, source, ruleId, category, severity,
                filePath, lineStart, lineEnd, title, message, suggestion, inDiff, fingerprint, createdAt);
    }
}

