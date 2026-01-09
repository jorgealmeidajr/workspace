package workspace.commons.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class XmlMyBatisMappingTest {

    @Nested
    class Constructor {
        @Test
        void shouldCreateValidInstance() {
            XmlMyBatisMapping mapping = new XmlMyBatisMapping(
                    "project",
                    "namespace",
                    "database",
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of()
            );

            assertEquals("project", mapping.project());
            assertEquals("namespace", mapping.namespace());
            assertEquals("database", mapping.database());
        }

        @Test
        void shouldThrowExceptionWhenProjectIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new XmlMyBatisMapping(
                            null,
                            "namespace",
                            "database",
                            List.of(),
                            List.of(),
                            List.of(),
                            List.of()
                    ),
                    "project cannot be null or empty"
            );
        }

        @Test
        void shouldThrowExceptionWhenProjectIsBlank() {
            assertThrows(IllegalArgumentException.class, () ->
                    new XmlMyBatisMapping(
                            "   ",
                            "namespace",
                            "database",
                            List.of(),
                            List.of(),
                            List.of(),
                            List.of()
                    ),
                    "project cannot be null or empty"
            );
        }

        @Test
        void shouldThrowExceptionWhenProjectIsEmpty() {
            assertThrows(IllegalArgumentException.class, () ->
                    new XmlMyBatisMapping(
                            "",
                            "namespace",
                            "database",
                            List.of(),
                            List.of(),
                            List.of(),
                            List.of()
                    ),
                    "project cannot be null or empty"
            );
        }

        @Test
        void shouldThrowExceptionWhenNamespaceIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new XmlMyBatisMapping(
                            "project",
                            null,
                            "database",
                            List.of(),
                            List.of(),
                            List.of(),
                            List.of()
                    ),
                    "namespace cannot be null or empty"
            );
        }

        @Test
        void shouldThrowExceptionWhenNamespaceIsBlank() {
            assertThrows(IllegalArgumentException.class, () ->
                    new XmlMyBatisMapping(
                            "project",
                            "   ",
                            "database",
                            List.of(),
                            List.of(),
                            List.of(),
                            List.of()
                    ),
                    "namespace cannot be null or empty"
            );
        }

        @Test
        void shouldThrowExceptionWhenNamespaceIsEmpty() {
            assertThrows(IllegalArgumentException.class, () ->
                    new XmlMyBatisMapping(
                            "project",
                            "",
                            "database",
                            List.of(),
                            List.of(),
                            List.of(),
                            List.of()
                    ),
                    "namespace cannot be null or empty"
            );
        }

        @Test
        void shouldThrowExceptionWhenDatabaseIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    new XmlMyBatisMapping(
                            "project",
                            "namespace",
                            null,
                            List.of(),
                            List.of(),
                            List.of(),
                            List.of()
                    ),
                    "database cannot be null or empty"
            );
        }

        @Test
        void shouldThrowExceptionWhenDatabaseIsBlank() {
            assertThrows(IllegalArgumentException.class, () ->
                    new XmlMyBatisMapping(
                            "project",
                            "namespace",
                            "   ",
                            List.of(),
                            List.of(),
                            List.of(),
                            List.of()
                    ),
                    "database cannot be null or empty"
            );
        }

        @Test
        void shouldThrowExceptionWhenDatabaseIsEmpty() {
            assertThrows(IllegalArgumentException.class, () ->
                    new XmlMyBatisMapping(
                            "project",
                            "namespace",
                            "",
                            List.of(),
                            List.of(),
                            List.of(),
                            List.of()
                    ),
                    "database cannot be null or empty"
            );
        }
    }

}
