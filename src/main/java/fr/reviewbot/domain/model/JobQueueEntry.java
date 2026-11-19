package fr.reviewbot.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Une tâche de la file d'attente : "analyser cette revue". Aucun champ n'est null : voir {@link Unset}.
 *
 * @param id          identifiant, {@link Unset#ID} tant que la tâche n'est pas enregistrée
 * @param reviewId    revue à analyser
 * @param state       état de la tâche (obligatoire)
 * @param attempts    nombre de tentatives déjà lancées
 * @param lastError   dernière erreur ("" si aucune)
 * @param lockedBy    nom du worker qui traite la tâche ("" si personne)
 * @param lockedUntil fin du verrou, après quoi la tâche peut être reprise ({@link Unset#INSTANT} si pas verrouillée)
 * @param createdAt   date de mise en file : c'est elle qui fixe l'ordre de passage
 * @param updatedAt   date de dernière modification
 */
public record JobQueueEntry(
        long id,
        long reviewId,
        ModalEnum.JobState state,
        int attempts,
        String lastError,
        String lockedBy,
        Instant lockedUntil,
        Instant createdAt,
        Instant updatedAt) {

    public JobQueueEntry {
        Objects.requireNonNull(state, "state");
        if (attempts < 0) {
            throw new IllegalArgumentException("attempts ne peut pas être négatif");
        }
        lastError = Unset.text(lastError);
        lockedBy = Unset.text(lockedBy);
        lockedUntil = Unset.instant(lockedUntil);
        createdAt = Unset.instant(createdAt);
        updatedAt = Unset.instant(updatedAt);
    }

    /** Crée une tâche neuve, en attente, jamais tentée. */
    public static JobQueueEntry queued(long reviewId) {
        return new JobQueueEntry(Unset.ID, reviewId, ModalEnum.JobState.QUEUED, 0, Unset.TEXT, Unset.TEXT,
                Unset.INSTANT, Unset.INSTANT, Unset.INSTANT);
    }
}
