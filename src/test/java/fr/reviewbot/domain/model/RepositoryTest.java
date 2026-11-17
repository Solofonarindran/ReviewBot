package fr.reviewbot.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryTest {

    @Test
    void createBuildsAnEnabledRepositoryWithoutId() {
        Repository repository = Repository.create(ModalEnum.Platform.GITHUB, "acme/shop", "https://github.com/acme/shop.git", "tok", "sec");

        assertFalse(Unset.isSaved(repository.id()));
        assertEquals(ModalEnum.Platform.GITHUB, repository.platform());
        assertEquals("acme/shop", repository.fullName());
        assertTrue(repository.enabled());
        assertFalse(Unset.isSet(repository.created()));
    }

    @Test
    void nullTextsBecomeEmptyStrings() {
        Repository repository = Repository.create(ModalEnum.Platform.GITLAB, "acme/shop", "https://x", null, null);

        assertEquals("", repository.apiTokenEncrypted());
        assertEquals("", repository.webhookSecretEncrypted());
    }


    @Test
    void platformIsRequired() {
        assertThrows(NullPointerException.class,
                () -> Repository.create(null, "acme/shop", "https://x", null, null));
    }

    @Test
    void fullNameIsRequired() {
        assertThrows(IllegalArgumentException.class,
                () -> Repository.create(ModalEnum.Platform.GITLAB, " ", "https://x", null, null));
    }

    @Test
    void cloneUrlIsRequired() {
        assertThrows(IllegalArgumentException.class,
                () -> Repository.create(ModalEnum.Platform.GITLAB, "acme/shop", null, null, null));
    }
}
