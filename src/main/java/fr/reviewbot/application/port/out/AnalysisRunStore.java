package fr.reviewbot.application.port.out;

import fr.reviewbot.domain.model.AnalysisRun;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Port sortant : enregistrement des exécutions d'outils d'analyse. */
public interface AnalysisRunStore {

    /** Insère l'exécution si elle n'est pas encore enregistrée (id = 0), sinon la met à jour. */
    AnalysisRun save(AnalysisRun run) throws Exception;

    Optional<AnalysisRun> findById(long id) throws Exception;

    /** Exécutions d'une revue, dans l'ordre de création. */
    List<AnalysisRun> findByReviewId(long reviewId) throws SQLException;
}
