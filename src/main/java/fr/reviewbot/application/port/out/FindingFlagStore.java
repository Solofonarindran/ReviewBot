package fr.reviewbot.application.port.out;

import fr.reviewbot.domain.model.FindingFlag;

import java.util.List;
import java.util.Optional;

/** Port sortant : signalements "faux positif". */
public interface FindingFlagStore {

    /** Enregistre le signalement. Un problème ne peut être signalé qu'une fois. */
    FindingFlag save(FindingFlag flag);

    Optional<FindingFlag> findByFindingId(long findingId);

    /** Signalements portant sur les problèmes d'une revue. */
    List<FindingFlag> findByReviewId(long reviewId);

    /** Retire le signalement d'un problème (sans effet s'il n'existe pas). */
    void deleteByFindingId(long findingId);
}
