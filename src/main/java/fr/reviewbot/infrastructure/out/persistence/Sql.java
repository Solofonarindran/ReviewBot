package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.domain.model.Unset;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;


/**
 * Conversions entre le domaine (qui n'utilise jamais null, voir {@link Unset}) et la base (qui utilise NULL).
 * C'est le seul endroit où cette traduction est faite.
 */
final class  Sql {
    private Sql (){}

    /** Date du domaine vers SQL : "non renseignée" devient NULL. */
    static Timestamp toTimestamp(Instant instant) {
        return Unset.isSet(instant) ? Timestamp.from(instant) : null;
    }
    /** Date SQL vers domaine : NULL devient "non renseignée". */
    static Instant toInstant(Timestamp timestamp){
        return timestamp == null ? Unset.INSTANT : timestamp.toInstant();
    }

    static  Instant getInstant(ResultSet resultset, String column) throws SQLException {
        return toInstant(resultset.getTimestamp(column));
    }

    /** Pourcentage du domaine vers SQL : "inconnu" devient NULL. */
    static BigDecimal toDecimal(BigDecimal percent) {
        return Unset.isKnown(percent) ? percent : null;
    }

    /** Pourcentage SQL vers domaine : NULL devient "inconnu". */
    static BigDecimal getPercent(ResultSet rs, String column) throws SQLException {
        return Unset.percent(rs.getBigDecimal(column));
    }

    static <E extends  Enum<E>> E getEnum(ResultSet resultset, String column, Class<E> enumClass) throws SQLException {
//        Objects.requireNonNull(resultset);
//        Objects.requireNonNull(column);
//        Objects.requireNonNull(enumClass);

        var value = resultset.getString(column);
        return value == null ? null : Enum.valueOf(enumClass, value);
    }
    static  String name(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
