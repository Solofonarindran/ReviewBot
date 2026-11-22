package fr.reviewbot.infrastructure.out.persistence;

import fr.reviewbot.domain.model.Unset;

import java.math.BigDecimal;
import java.sql.*;
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
    /** Référence vers une autre ligne : "pas de référence" (id 0) devient NULL. */
    static void setId(PreparedStatement ps, int index, long id) throws SQLException {
        if (Unset.isSaved(id)) {
            ps.setLong(index, id);
        } else {
            ps.setNull(index, Types.BIGINT);
        }
    }

    /** Référence SQL vers domaine : NULL devient "pas de référence" (id 0). */
    static long getId(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? Unset.ID : value;
    }

    /** Pourcentage du domaine vers SQL : "inconnu" devient NULL. */
    static BigDecimal toDecimal(BigDecimal percent) {
        return Unset.isKnown(percent) ? percent : null;
    }

    /** Entier optionnel du domaine vers SQL : "inconnu" devient NULL. */
    static void setNumber(PreparedStatement ps, int index, int value) throws SQLException {
        if (Unset.isKnown(new BigDecimal(value))) {
            ps.setInt(index, value);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }

    /** Pourcentage SQL vers domaine : NULL devient "inconnu". */
    static BigDecimal getPercent(ResultSet rs, String column) throws SQLException {
        return Unset.percent(rs.getBigDecimal(column));
    }

    /** Entier optionnel SQL vers domaine : NULL devient "inconnu". */
    static int getNumber(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? Unset.NUMBER : value;
    }

    static <E extends  Enum<E>> E getEnum(ResultSet resultset, String column, Class<E> enumClass) throws SQLException {
//        Objects.requireNonNull(resultset);
//        Objects.requireNonNull(column);
//        Objects.requireNonNull(enumClass);

        var value = resultset.getString(column);
        return value == null ? null : Enum.valueOf(enumClass, value);
    }
    static  String name(Enum<?> value) {
        return value.name();
    }
}
