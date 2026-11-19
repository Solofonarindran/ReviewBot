package fr.reviewbot.domain.model;

import java.time.Instant;

/**
 * Un signalement "faux positif" posé par un humain sur un problème. Aucun champ n'est null : voir {@link Unset}.
 *
 * @param id        identifiant, {@link Unset#ID} tant que le signalement n'est pas enregistré
 * @param findingId problème signalé (un seul signalement par problème)
 * @param flaggedBy personne qui signale (obligatoire)
 * @param reason    raison du signalement ("" si aucune)
 * @param flaggedAt date du signalement ({@link Unset#INSTANT} avant l'enregistrement)
 */
public record FindingFlag(
        long id,
        long findingId,
        String flaggedBy,
        String reason,
        Instant flaggedAt) {

    public FindingFlag {
        if (flaggedBy == null || flaggedBy.isBlank()) {
            throw new IllegalArgumentException("flaggedBy est obligatoire");
        }
        reason = Unset.text(reason);
        flaggedAt = Unset.instant(flaggedAt);
    }

    /** Crée un signalement neuf. */
    public static FindingFlag of(long findingId, String flaggedBy, String reason) {
        return new FindingFlag(Unset.ID, findingId, flaggedBy, reason, Unset.INSTANT);
    }
}
