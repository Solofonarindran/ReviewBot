package fr.reviewbot.application.port.out;

import fr.reviewbot.domain.model.JobQueueEntry;
import fr.reviewbot.domain.model.ModalEnum;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Port sortant : la file d'attente des analyses.
 * Ordre de passage : la tâche la plus ancienne d'abord (date de mise en file, puis identifiant).
 * Une seule tâche tourne à la fois pour une même PR.
 */
public interface JobQueueStore {

    /** Met une revue en file d'attente. */
    JobQueueEntry enqueue(long reviewId) throws Exception;

    /**
     * Réserve la prochaine tâche à traiter pour le worker {@code workerName} : elle passe en RUNNING,
     * son nombre de tentatives augmente de 1 et elle est verrouillée pendant {@code lockDuration}.
     * Deux workers ne peuvent jamais obtenir la même tâche. Vide si rien n'est prêt.
     */
    Optional<JobQueueEntry> claimNext(String workerName, Duration lockDuration) throws Exception;

    /** Tâche terminée avec succès. */
    JobQueueEntry markDone(long jobId) throws Exception;

    /**
     * Tâche en erreur : elle repasse en file (QUEUED) si elle n'a pas atteint {@code maxAttempts}
     * tentatives, sinon elle devient FAILED définitivement.
     */
    JobQueueEntry markFailed(long jobId, String error, int maxAttempts) throws Exception;

    /**
     * Reprend les tâches RUNNING dont le verrou a expiré (worker mort) : même règle que {@link #markFailed}.
     * Renvoie le nombre de tâches reprises.
     */
    int releaseExpired(int maxAttempts) throws Exception;

    Optional<JobQueueEntry> findById(long jobId) throws Exception;

    List<JobQueueEntry> findByReviewId(long reviewId) throws SQLException;

    long countByState(ModalEnum.JobState state) throws Exception;
}