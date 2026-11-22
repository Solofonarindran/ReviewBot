package fr.reviewbot.domain.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FindingFilterTest {

    @Test
    void nullSetsMeanNoFilter() {
        FindingFilter filter = new FindingFilter(1, null, null, false, true);
        assertTrue(filter.categories().isEmpty());
        assertTrue(filter.severities().isEmpty());
    }

    @Test
    void visibleOfHidesMergedFindings() {
        FindingFilter filter = FindingFilter.visibleOf(9);

        assertEquals(9, filter.reviewId());
        assertFalse(filter.includeMerged());
        assertFalse(filter.inDiffOnly());
    }

    @Test
    void setsAreCopied() {
        Set<ModalEnum.Severity> severities = new HashSet<>(Set.of(ModalEnum.Severity.BLOCKING));
        FindingFilter filter = new FindingFilter(1, Set.of(), severities, false, false);
        severities.add(ModalEnum.Severity.INFO);

        assertEquals(Set.of(ModalEnum.Severity.BLOCKING), filter.severities());
    }
}