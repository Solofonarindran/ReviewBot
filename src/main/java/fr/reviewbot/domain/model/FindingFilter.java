package fr.reviewbot.domain.model;

import java.util.Set;

/**
 * Critères de recherche des problèmes d'une revue. Un ensemble vide veut dire "pas de filtre".
 *
 * @param reviewId     revue concernée
 * @param categories   catégories voulues (vide = toutes)
 * @param severities   gravités voulues (vide = toutes)
 * @param inDiffOnly   vrai pour ne garder que les problèmes sur des lignes modifiées par la PR
 * @param includeMerged faux pour cacher les doublons fusionnés
 */
public record FindingFilter(
        long reviewId,
        Set<ModalEnum.Category> categories,
        Set<ModalEnum.Severity> severities,
        boolean inDiffOnly,
        boolean includeMerged) {

    public FindingFilter {
        categories = categories == null ? Set.of() : Set.copyOf(categories);
        severities = severities == null ? Set.of() : Set.copyOf(severities);
    }

    /** Tous les problèmes visibles d'une revue (doublons cachés). */
    public static FindingFilter visibleOf(long reviewId) {
        return new FindingFilter(reviewId, Set.of(), Set.of(), false, false);
    }
}
