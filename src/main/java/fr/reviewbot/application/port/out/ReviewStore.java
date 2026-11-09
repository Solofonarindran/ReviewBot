package fr.reviewbot.application.port.out;

import fr.reviewbot.domain.model.Review;

import java.util.List;
import java.util.Optional;

/** Port sortant : enregistrement des revues. */
public interface ReviewStore {
    /** Insère la revue si son id est null, sinon la met à jour. Renvoie la revue telle qu'enregistrée. */
    Review save(Review review);

    Optional<Review> findById(long id);

    /** Revues d'un dépôt, de la plus récente à la plus ancienne. */
    List<Review> findByRepositoryId(long repositoryId);

    /** Page de revues, de la plus récente à la plus ancienne. */
    List<Review> findPage(int limit, int offset);

    long count();

}
