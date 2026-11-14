package fr.reviewbot.domain.model;

import java.time.Instant;
import java.util.Objects;


public record Repository(Long id,
                         ModalEnum.Platform platform,
                         String fullName,
                         String cloneUrl,
                         String apiTokenEncrypted,
                         String webhookSecretEncrypted,
                         boolean enabled,
                         Instant created,
                         Instant updatedAt) {
    public Repository {
        Objects.requireNonNull(platform, "platform");
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName est obligatoire");
        }
        if (cloneUrl == null || cloneUrl.isBlank()) {
            throw new IllegalArgumentException("cloneUrl est obligatoire");
        }

        // conversion si certains valeurs seront nulls
        apiTokenEncrypted = Unset.text(apiTokenEncrypted);
        webhookSecretEncrypted = Unset.text(webhookSecretEncrypted);
        created = Unset.instant(created);
        updatedAt = Unset.instant(updatedAt);
    }

    public Repository(ModalEnum.Platform plateform, String fullName, String cloneUrl, String apiTokenCrypted,
                      String webhookSecretEncrypted) {
        this(Unset.ID,plateform, fullName, cloneUrl, apiTokenCrypted, webhookSecretEncrypted, true, Instant.now(), Instant.now());
    }
    /** Crée un dépôt neuf, actif, pas encore enregistré. */
    public static Repository create(ModalEnum.Platform platform, String fullName, String cloneUrl,
                                    String apiTokenEncrypted, String webhookSecretEncrypted) {
        return new Repository(Unset.ID, platform, fullName, cloneUrl,
                apiTokenEncrypted, webhookSecretEncrypted, true, Unset.INSTANT, Unset.INSTANT);

    }

}
