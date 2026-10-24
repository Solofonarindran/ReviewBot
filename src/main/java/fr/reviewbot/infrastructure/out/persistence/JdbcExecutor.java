package fr.reviewbot.infrastructure.out.persistence;

import org.springframework.jdbc.datasource.DataSourceUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class JdbcExecutor {
    private final  DataSource dataSource;

    @FunctionalInterface
    public interface Binder {
        void bind(PreparedStatement statement)throws SQLException;
    }

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet resultSet) throws SQLException;
    }

    public JdbcExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> List<T> query(String sql, Binder binder,  RowMapper<T> rowMapper) throws Exception {
        var connection = DataSourceUtils.getConnection(dataSource);
        try(var statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try(var resultSet = statement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.map(resultSet));
                }
                return  results;
            }

        } catch (SQLException e) {
            throw new Exception(e.getMessage());
        }finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public  <T> Optional<T> queryOne(String sql, Binder binder, RowMapper<T> mapper) throws Exception {
        return query(sql, binder,mapper).stream().findFirst();
    }

    public int update(String  sql, Binder binder) throws Exception {
        var connection = DataSourceUtils.getConnection(dataSource);
        try(var statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            return statement.executeUpdate();
        }catch (SQLException e) {
            throw new Exception(e.getMessage());
        }finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

}

