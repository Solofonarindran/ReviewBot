package fr.reviewbot.application.port.out;

import fr.reviewbot.domain.model.Finding;
import fr.reviewbot.domain.model.FindingFilter;
import fr.reviewbot.domain.model.ModalEnum;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Port sortant : enregistrement des problèmes trouvés. */
public interface FindingStore {

    /**
     * Insère le problème s'il n'est pas encore enregistré (id = 0), sinon le met à jour.
     */
    Finding save(Finding finding);

    /**
     * Enregistre plusieurs problèmes d'un coup (même règle que {@link #save}).
     */
    List<Finding> saveAll(List<Finding> findings);

    Optional<Finding> findById(long id);

    /**
     * Tous les problèmes d'une revue, doublons fusionnés compris, dans l'ordre de création.
     */
    List<Finding> findByReviewId(long reviewId);

    /**
     * Problèmes d'une revue selon des critères, les plus graves d'abord, puis par fichier et par ligne.
     */
    List<Finding> search(FindingFilter filter);

    /**
     * Problèmes d'une revue ayant cette empreinte (détection des doublons).
     */
    List<Finding> findByReviewIdAndFingerprint(long reviewId, String fingerprint);

    /**
     * Nombre de problèmes visibles (non fusionnés) par gravité ; les gravités absentes valent 0.
     */
    Map<ModalEnum.Severity, Long> countVisibleBySeverity(long reviewId);
}