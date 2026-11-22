package fr.reviewbot.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FindingTest {

    static Finding sample() {
        return new Finding(Unset.ID, 1, Unset.ID, Unset.ID, "PMD", "UnusedLocalVariable", ModalEnum.Category.CODE,
                ModalEnum.Severity.MINOR, "src/Main.java", 10, 12, "Variable inutilisée", "x n'est jamais lue",
                "Supprimer x", true, "fp-1", Unset.INSTANT);
    }

    @Test
    void optionalFieldsDefaultToUnsetValues() {
        Finding f = new Finding(0, 1, 0, 0, "LLM", null, ModalEnum.Category.BUG, ModalEnum.Severity.MAJOR, null, -1, -3,
                "Titre", null, null, false, "fp", null);

        assertEquals(Unset.TEXT, f.ruleId());
        assertEquals(Unset.TEXT, f.filePath());
        assertEquals(Unset.NUMBER, f.lineStart());
        assertEquals(Unset.NUMBER, f.lineEnd());
        assertEquals(Unset.TEXT, f.message());
        assertEquals(Unset.TEXT, f.suggestion());
        assertEquals(Unset.INSTANT, f.createdAt());
    }

    @Test
    void requiredFieldsAreChecked() {
        assertThrows(IllegalArgumentException.class, () -> withSource(" "));
        assertThrows(IllegalArgumentException.class, () -> withTitle(""));
        assertThrows(IllegalArgumentException.class, () -> withFingerprint(null));
        assertThrows(NullPointerException.class, () -> new Finding(0, 1, 0, 0, "PMD", "", null,
                ModalEnum.Severity.MINOR, "", 1, 1, "t", "", "", false, "fp", null));
        assertThrows(NullPointerException.class, () -> new Finding(0, 1, 0, 0, "PMD", "", ModalEnum.Category.BUG,
                null, "", 1, 1, "t", "", "", false, "fp", null));
    }

    @Test
    void mergedIntoMarksTheFindingAsDuplicate() {
        Finding finding = sample();
        assertFalse(finding.isMerged());

        Finding merged = finding.mergedInto(99);

        assertTrue(merged.isMerged());
        assertEquals(99, merged.mergedIntoId());
        assertEquals(finding.title(), merged.title());
        assertFalse(finding.isMerged());
    }

    private static Finding withSource(String source) {
        return new Finding(0, 1, 0, 0, source, "", ModalEnum.Category.BUG, ModalEnum.Severity.MINOR, "", 1, 1, "t", "", "", false, "fp", null);
    }

    private static Finding withTitle(String title) {
        return new Finding(0, 1, 0, 0, "PMD", "", ModalEnum.Category.BUG, ModalEnum.Severity.MINOR, "", 1, 1, title, "", "", false, "fp", null);
    }

    private static Finding withFingerprint(String fingerprint) {
        return new Finding(0, 1, 0, 0, "PMD", "", ModalEnum.Category.BUG, ModalEnum.Severity.MINOR, "", 1, 1, "t", "", "", false, fingerprint, null);
    }
}
