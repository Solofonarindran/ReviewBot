package fr.reviewbot.application.port.out;

import fr.reviewbot.domain.model.FindingFlag;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Port sortant : signalements "faux positif". */
public interface FindingFlagStore {

    /** Enregistre le signalement. Un problème ne peut être signalé qu'une fois. */
    FindingFlag save(FindingFlag flag) throws Exception;

    Optional<FindingFlag> findByFindingId(long findingId) throws Exception;

    /** Signalements portant sur les problèmes d'une revue. */
    List<FindingFlag> findByReviewId(long reviewId) throws SQLException;

    /** Retire le signalement d'un problème (sans effet s'il n'existe pas). */
    void deleteByFindingId(long findingId) throws Exception;
}
