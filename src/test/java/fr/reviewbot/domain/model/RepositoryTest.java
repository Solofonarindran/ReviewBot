package fr.reviewbot.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryTest {

    @Test
    void createBuildsAnEnabledRepositoryWithoutId() {
        Repository repository = Repository.create(ModalEnum.Platform.GITHUB, "acme/shop", "https://github.com/acme/shop.git", "tok", "sec");

        assertNull(repository.id());
        assertEquals(ModalEnum.Platform.GITHUB, repository.platform());
        assertEquals("acme/shop", repository.fullName());
        assertTrue(repository.enabled());
        assertNull(repository.created());
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
