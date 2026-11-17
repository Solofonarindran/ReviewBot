package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.RepositoryStore;
import fr.reviewbot.domain.model.ModalEnum;
import fr.reviewbot.domain.model.Repository;
import fr.reviewbot.domain.model.Unset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/** Test d'intégration sur une base HSQLDB en mémoire ; chaque test est annulé (rollback) à la fin. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JdbcRepositoryStoreTest {

    @Autowired
    private RepositoryStore store;

    private Repository newRepository(String fullName) {
        return Repository.create(ModalEnum.Platform.GITHUB, fullName, "https://github.com/" + fullName + ".git", "tok", "sec");
    }

    @Test
    void saveInsertsAndReadsBack() {
        Repository saved = store.save(newRepository("acme/shop"));

        assertNotEquals(Unset.ID,saved.id());
        assertEquals(ModalEnum.Platform.GITHUB, saved.platform());
        assertEquals("acme/shop", saved.fullName());
        assertEquals("tok", saved.apiTokenEncrypted());
        assertTrue(saved.enabled());
        assertTrue(Unset.isSet(saved.created()));
        assertTrue(Unset.isSet(saved.updatedAt()));
        assertEquals(saved, store.findById(saved.id()).orElseThrow());
    }

    @Test
    void saveUpdatesAnExistingRepository() {
        Repository saved = store.save(newRepository("acme/shop"));
        Repository changed = new Repository(saved.id(), ModalEnum.Platform.GITLAB, "acme/shop2", "https://gitlab.com/acme/shop2.git",
                null, null, false, saved.created(), saved.updatedAt());

        Repository updated = store.save(changed);

        assertEquals(saved.id(), updated.id());
        assertEquals(ModalEnum.Platform.GITLAB, updated.platform());
        assertEquals("acme/shop2", updated.fullName());
        assertFalse(updated.enabled());
        assertEquals("", updated.apiTokenEncrypted());
    }

    @Test
    void updateOfUnknownRepositoryFails() {
        Repository ghost = new Repository(999_999L, ModalEnum.Platform.GITHUB, "x/y", "https://x", null, null, true, null, null);

        assertThrows(RuntimeException.class, () -> store.save(ghost));
    }

    @Test
    void findByPlatformAndFullName() {
        Repository saved = store.save(newRepository("acme/shop"));

        assertEquals(saved.id(), store.findByPlatformAndFullName(ModalEnum.Platform.GITHUB, "acme/shop").orElseThrow().id());
        assertTrue(store.findByPlatformAndFullName(ModalEnum.Platform.GITLAB, "acme/shop").isEmpty());
    }

    @Test
    void findAllReturnsSavedRepositories() {
        store.save(newRepository("acme/b"));
        store.save(newRepository("acme/a"));

        var names = store.findAll().stream().map(Repository::fullName).toList();

        assertTrue(names.containsAll(java.util.List.of("acme/a", "acme/b")));
        assertTrue(names.indexOf("acme/a") < names.indexOf("acme/b"));
    }

    @Test
    void samePlatformAndNameCannotBeSavedTwice() {
        store.save(newRepository("acme/shop"));

        assertThrows(RuntimeException.class, () -> store.save(newRepository("acme/shop")));
    }

    @Test
    void deleteRemovesTheRepository() {
        Repository saved = store.save(newRepository("acme/shop"));

        store.deleteById(saved.id());

        assertTrue(store.findById(saved.id()).isEmpty());
    }
}
