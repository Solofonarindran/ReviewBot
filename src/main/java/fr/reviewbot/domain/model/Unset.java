package fr.reviewbot.domain.model;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;

/**
 * Valeurs « non renseigné » du domaine : les objets du domaine ne contiennent jamais null.
 * Un texte absent vaut "", une date absente Instant.EPOCH, un pourcentage inconnu -1, un id non enregistré 0.
 */
public final class Unset {

    public static final long ID = 0L;
    public static final String TEXT = "";
    public static final Instant INSTANT = Instant.EPOCH;
    public static final BigDecimal PERCENT = new BigDecimal("-1");
    /** Entier non renseigné (durée inconnue, pas de numéro de ligne...). */
    public static final int NUMBER = -1;

    private Unset() {
    }

    public static String text(String value) {
        return value == null ? TEXT : value;
    }

    public static Instant instant(Instant value) {
        return value == null ? INSTANT : value;
    }

    public static BigDecimal percent(BigDecimal value) {
        return value == null ? PERCENT : value;
    }

    /** Vrai si l'objet a déjà été enregistré (id attribué par la base). */
    public static boolean isSaved(long id) {
        return id != ID;
    }

    /** Entier valide : toute valeur négative devient {@link #NUMBER}. */
    public static int number(int value) {
        return value < 0 ? NUMBER : value;
    }

    /** Vrai si la date a une vraie valeur. */
    public static boolean isSet(Instant value) {
        return value != null && !value.equals(INSTANT);
    }

    /** Vrai si le pourcentage est connu. */
    public static boolean isKnown(BigDecimal value) {
        return value != null && value.compareTo(PERCENT) != 0;
    }


}
