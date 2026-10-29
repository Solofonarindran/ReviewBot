package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.application.port.out.RepositoryStore;
import fr.reviewbot.domain.model.ModalEnum;
import fr.reviewbot.domain.model.Repository;

import java.sql.ResultSet;
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

            try {
                if(repository.id() == null) {
                    var id = jdbc.insert("INSERT INTO repository (plateform, full_name, clone_url, api_token_encrypted," +
                                    "webhook_secret_encrypted, enable, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?)",
                            ps -> {
                                ps.setString(1, repository.platform().name());
                                ps.setString(2, repository.fullName());
                                ps.setString(3, repository.cloneUrl());
                                ps.setString(4, repository.apiTokenCrypted());
                                ps.setString(5, repository.webhookSecretEncrypted());
                                ps.setBoolean(6, repository.enabled());
                                ps.setTimestamp(7, now);
                                ps.setTimestamp(8, now);

                            });
                    return findById(id).orElseThrow();
                }
                var rows = jdbc.update(
                        "UPDATE repository SET platform=?, full_name=?, clone_url=?, api_token_encrypted=?, "
                                + "webhook_secret_encrypted=?, enabled=?, updated_at=? WHERE id=?",
                        ps -> {
                            ps.setString(1, repository.platform().name());
                            ps.setString(2, repository.fullName());
                            ps.setString(3, repository.cloneUrl());
                            ps.setString(4, repository.apiTokenCrypted());
                            ps.setString(5, repository.webhookSecretEncrypted());
                            ps.setBoolean(6, repository.enabled());
                            ps.setTimestamp(7, now);
                            ps.setLong(8, repository.id());
                        });
                if(rows == 0)  throw new RuntimeException("Dépôt not found : " + repository.id(), null);
                return findById(repository.id()).orElseThrow();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }


    }

    @Override
    public Optional<Repository> findById(long id) {
        try {
            return jdbc.queryOne("SELECT" + COLUMNS + "FROM repository where id = ?",
                    ps -> ps.setLong(1,id),JdbcRepositoryStore::map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static Repository map(ResultSet resultSet) throws  SQLException{
        return new Repository(
                resultSet.getLong("id"),
                Sql.getEnum(resultSet, "platform", ModalEnum.Platform.class),
                resultSet.getString("full_name"),
                resultSet.getString("clone_url"),
                resultSet.getString("api_token_encrypted"),
                resultSet.getString("webhook_secret_encrypted"),
                resultSet.getBoolean("enabled"),
                Sql.getInstant(resultSet, "created_at"),
                Sql.getInstant(resultSet, "updated_at"));
    }

}
