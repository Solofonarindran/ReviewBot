package fr.reviewbot.domain.model;

import java.time.Instant;
import java.util.Objects;


public record Repository(Long id,
                         ModalEnum.Platform platform,
                         String fullName,
                         String cloneUrl,
                         String apiTokenCrypted,
                         String webhookSecretEncrypted,
                         boolean enabled,
                         Instant created,
                         Instant updatedAt) {

    public Repository {
        Objects.requireNonNull(platform, "platform non null");
        Objects.requireNonNull(fullName, "fullName non null");
        Objects.requireNonNull(apiTokenCrypted, "apiTokenCrypted non null");
        Objects.requireNonNull(apiTokenCrypted, "webhookSecretEncrypted non null");
//        Objects.requireNonNull(created);
//        Objects.requireNonNull(updatedAt);
    }

    public Repository(ModalEnum.Platform plateform, String fullName, String cloneUrl, String apiTokenCrypted,
                      String webhookSecretEncrypted) {
        this(null,plateform, fullName, cloneUrl, apiTokenCrypted, webhookSecretEncrypted, true, Instant.now(), Instant.now());
    }
    /** Crée un dépôt neuf, actif, pas encore enregistré. */
    public static Repository create(ModalEnum.Platform platform, String fullName, String cloneUrl,
                                    String apiTokenEncrypted, String webhookSecretEncrypted) {
        return new Repository(null, platform, fullName, cloneUrl,
                apiTokenEncrypted, webhookSecretEncrypted, true, null, null);
    
    }

}
