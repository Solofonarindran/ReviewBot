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


}
