package fr.reviewbot.infrastructure.out.persistence;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcExecutor {
    public interface Binder {
        void bind(PreparedStatement statement)throws SQLException;
    }
}
