package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.RepositoryStore;
import fr.reviewbot.application.port.out.ReviewStore;
import fr.reviewbot.domain.model.ModalEnum;
import fr.reviewbot.domain.model.Repository;
import fr.reviewbot.domain.model.Review;

/**
 * Données de test partagées (composition) : un dépôt de test et des revues rattachées.
 * Le dépôt est créé à la première utilisation puis retrouvé.
 */
final class ReviewFixtures {

    private ReviewFixtures() {
    }

    static Review savedReview(RepositoryStore repositories, ReviewStore reviews, String prNumber) {
        return savedReview(repositories, reviews, prNumber, "sha-" + prNumber);
    }

    static Review savedReview(RepositoryStore repositories, ReviewStore reviews, String prNumber,
                              String headCommitSha) {
        Repository repository = repositories.findByPlatformAndFullName(ModalEnum.Platform.GITHUB, "acme/shop")
                .orElseGet(() -> repositories.save(Repository.create(ModalEnum.Platform.GITHUB, "acme/shop",
                        "https://github.com/acme/shop.git", null, null)));
        return reviews.save(Review.queued(repository.id(), prNumber, "Titre " + prNumber, "alice",
                "feature/x", "main", headCommitSha));
    }
}