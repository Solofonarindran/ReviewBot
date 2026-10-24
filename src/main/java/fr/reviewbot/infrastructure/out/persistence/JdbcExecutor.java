package fr.reviewbot.infrastructure.out.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcExecutor {
    
    @FunctionalInterface
    public interface Binder {
        void bind(PreparedStatement statement)throws SQLException;
    }

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet resultSet) throws SQLException;
    }

}
