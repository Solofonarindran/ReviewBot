package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.RepositoryStore;
import fr.reviewbot.domain.model.Repository;

import java.time.Clock;
import java.util.Objects;

public class JdbcRepositoryStore implements RepositoryStore {

    private  static final  String COLUMN = "id, platform, full_name, clone_url, api_token_encrypted, "
             + "webhook_secret_encrypted, enable, created_at, updated_at";
    private final JdbcExecutor jdbc;
    private final Clock clock;

    public JdbcRepositoryStore(JdbcExecutor jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
        super();
    }

    public Repository save(Repository repository) {
        Objects.requireNonNull(repository);
        var now = 
    }
}
