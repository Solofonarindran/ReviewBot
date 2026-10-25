package fr.reviewbot.domain.model;

import java.time.Instant;
import java.util.Objects;


public record Repository(Long id,
                         Platform platform,
                         String fullName,
                         String cloneUrl,
                         String apiTokenCrypted,
                         String webhookSecretEncrypted,
                         boolean enabled,
                         Instant created,
                         Instant updatedAt) {

    
    public enum Platform {
        GITHUB,
        GITLAB,
        BITBUCKET
    }

    public Repository {
        Objects.requireNonNull(platform, "platform non null");
        Objects.requireNonNull(fullName, "fullName non null");
        Objects.requireNonNull(apiTokenCrypted, "apiTokenCrypted non null");
        Objects.requireNonNull(apiTokenCrypted, "webhookSecretEncrypted non null");
//        Objects.requireNonNull(created);
//        Objects.requireNonNull(updatedAt);
    }

    public Repository(Platform plateform, String fullName, String cloneUrl, String apiTokenCrypted,
                      String webhookSecretEncrypted) {
        this(null,plateform, fullName, cloneUrl, apiTokenCrypted, webhookSecretEncrypted, true, Instant.now(), Instant.now());
    }


}
