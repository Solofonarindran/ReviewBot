package fr.reviewbot.infrastructure.out.persistence;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Vérifie que schema.sql crée bien les 7 tables du diagramme de classes. */
@SpringBootTest
class DatabaseSchemaTest {

    private static final Set<String> EXPECTED = Set.of(
            "REPOSITORY", "REVIEW", "JOB_QUEUE", "ANALYSIS_RUN", "FINDING", "FINDING_FLAG", "POSTED_COMMENT");

    @Autowired
    private DataSource dataSource;

    @Test
    void allTablesExist() throws SQLException {
        Set<String> found = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
             ResultSet tables = connection.getMetaData().getTables(null, "PUBLIC", null, new String[] {"TABLE"})) {
            while (tables.next()) {
                found.add(tables.getString("TABLE_NAME"));
            }
        }
        assertTrue(found.containsAll(EXPECTED), "Tables trouvées : " + found);
    }
}
