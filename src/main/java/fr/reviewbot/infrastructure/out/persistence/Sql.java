package fr.reviewbot.infrastructure.out.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

/** Petites conversions entre types Java et colonnes SQL, tolérantes aux valeurs null. */
public record Sql() {
    static Timestamp toTimestamp(Instant instant) {
        Objects.requireNonNull(instant);
        return Timestamp.from(instant);
    }

    static Instant toInstant(Timestamp timestamp){
        Objects.requireNonNull(timestamp);
        return timestamp.toInstant();
    }

    static  Instant getTimestamp(ResultSet resultset, String column) throws SQLException {
        Objects.requireNonNull(resultset);
        Objects.requireNonNull(column);
        return  toInstant(resultset.getTimestamp(column));
    }
}
