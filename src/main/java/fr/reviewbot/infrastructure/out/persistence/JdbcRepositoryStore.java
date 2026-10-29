package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.RepositoryStore;
import fr.reviewbot.domain.model.Repository;

import java.sql.SQLException;
import java.time.Clock;
import java.util.Objects;
import java.util.Optional;

public class JdbcRepositoryStore implements RepositoryStore {

    private  static final  String COLUMNS = "id, platform, full_name, clone_url, api_token_encrypted, "
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
        var now = Sql.toTimestamp(clock.instant());
        if(repository.id() == null) {
            try {
                var id = jdbc.insert("INSERT INTO repository (plateform, full_name, clone_url, api_token_encrypted," +
                                "webhook_secret_encrypted, enable, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?)",
                        ps-> {
                            ps.setString(1, repository.platform().name());
                            ps.setString(2, repository.fullName());
                            ps.setString(3, repository.cloneUrl());
                            ps.setString(4, repository.apiTokenCrypted());
                            ps.setString(5, repository.webhookSecretEncrypted());
                            ps.setBoolean(6,repository.enabled());
                            ps.setTimestamp(7,now);
                            ps.setTimestamp(8,now);

                        });
                return findById(id).orElseThrow();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public Optional<Repository> findById(long id) {
        return jdbc.queryOne("SELECT" + COLUMNS + "FROM repository where id = ?",
                ps -> ps.setLong(1,id),JdbcRepositoryStore::map);
    }
}
