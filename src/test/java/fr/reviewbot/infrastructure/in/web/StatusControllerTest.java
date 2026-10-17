package fr.reviewbot.infrastructure.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StatusControllerTest {

    @Test
    void statusReportsApplicationUp() {
        StatusResponse response = new StatusController().status();

        assertEquals("ReviewBot", response.application());
        assertEquals("UP", response.status());
        assertFalse(response.javaVersion().isBlank());
    }
}
