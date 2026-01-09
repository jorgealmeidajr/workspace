package workspace.commons.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MyBatisMappingsTest {

    @Nested
    class ValidateUniqueMappings {
        @Test
        void shouldAcceptEmptyMappingsList() {
            // Should not throw an exception
            assertDoesNotThrow(() -> new MyBatisMappings(new ArrayList<>()));
        }

        @Test
        void shouldAcceptSingleMapping() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            assertDoesNotThrow(() -> new MyBatisMappings(mappings));
        }

        @Test
        void shouldAcceptMultipleMappingsWithDifferentProjects() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project2", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            assertDoesNotThrow(() -> new MyBatisMappings(mappings));
        }

        @Test
        void shouldAcceptMultipleMappingsWithDifferentNamespaces() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace2", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            assertDoesNotThrow(() -> new MyBatisMappings(mappings));
        }

        @Test
        void shouldAcceptMultipleMappingsWithDifferentDatabases() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "postgres",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            assertDoesNotThrow(() -> new MyBatisMappings(mappings));
        }

        @Test
        void shouldRejectDuplicateMappingsWithSameProjectNamespaceDatabase() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new MyBatisMappings(mappings));

            assertTrue(exception.getMessage().contains("Duplicate mapping found"));
            assertTrue(exception.getMessage().contains("project=project1"));
            assertTrue(exception.getMessage().contains("namespace=namespace1"));
            assertTrue(exception.getMessage().contains("database=oracle"));
        }

        @Test
        void shouldRejectDuplicateMappingsAmongMultipleMappings() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace2", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new MyBatisMappings(mappings));

            assertTrue(exception.getMessage().contains("Duplicate mapping found"));
        }

        @Test
        void shouldRejectDuplicatesOnlyProjectAndNamespace() {
            // Same project and namespace but different database should NOT throw
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "postgres",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            assertDoesNotThrow(() -> new MyBatisMappings(mappings));
        }

        @Test
        void shouldRejectDuplicatesOnlyProjectAndDatabase() {
            // Same project and database but different namespace should NOT throw
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace2", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            assertDoesNotThrow(() -> new MyBatisMappings(mappings));
        }

        @Test
        void shouldRejectDuplicatesOnlyNamespaceAndDatabase() {
            // Same namespace and database but different project should NOT throw
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project2", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            assertDoesNotThrow(() -> new MyBatisMappings(mappings));
        }

    }

    @Nested
    class ValidateUniqueCalls {
        @Test
        void shouldAcceptEmptyCallsList() {
            // Should not throw an exception
            assertDoesNotThrow(() -> new MyBatisMappings(new ArrayList<>()));
        }

        @Test
        void shouldAcceptSingleCall() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            assertDoesNotThrow(() -> new MyBatisMappings(mappings));
        }

        @Test
        void shouldAcceptMultipleCallsWithDifferentNamespaces() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace2", "oracle",
                    List.of(new XmlCallMapping("namespace2", "call1", "oracle", "SELECT * FROM table2", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            assertDoesNotThrow(() -> new MyBatisMappings(mappings));
        }

        @Test
        void shouldAcceptMultipleCallsWithDifferentIds() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(
                        new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>()),
                        new XmlCallMapping("namespace1", "call2", "oracle", "SELECT * FROM table2", new ArrayList<>())
                    ),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            assertDoesNotThrow(() -> new MyBatisMappings(mappings));
        }

        @Test
        void shouldAcceptMultipleCallsWithDifferentDatabases() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "postgres",
                    List.of(new XmlCallMapping("namespace1", "call1", "postgres", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            assertDoesNotThrow(() -> new MyBatisMappings(mappings));
        }

        @Test
        void shouldRejectDuplicateCallsWithSameNamespaceIdDatabase() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(
                        new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>()),
                        new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())
                    ),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new MyBatisMappings(mappings));

            assertTrue(exception.getMessage().contains("Duplicate call found"));
            assertTrue(exception.getMessage().contains("namespace=namespace1"));
            assertTrue(exception.getMessage().contains("id=call1"));
            assertTrue(exception.getMessage().contains("database=oracle"));
        }

        @Test
        void shouldRejectDuplicateCallsAcrossDifferentMappings() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new MyBatisMappings(mappings));

            // This should fail at validateUniqueMappings, not validateUniqueCalls
            assertTrue(exception.getMessage().contains("Duplicate"));
        }

        @Test
        void shouldAcceptCallsWithDifferentNamespaceButSameIdDatabase() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace2", "oracle",
                    List.of(new XmlCallMapping("namespace2", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            assertDoesNotThrow(() -> new MyBatisMappings(mappings));
        }

        @Test
        void shouldAcceptCallsWithDifferentIdButSameNamespaceDatabase() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(
                        new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>()),
                        new XmlCallMapping("namespace1", "call2", "oracle", "SELECT * FROM table2", new ArrayList<>())
                    ),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            assertDoesNotThrow(() -> new MyBatisMappings(mappings));
        }

        @Test
        void shouldAcceptCallsWithDifferentDatabaseButSameNamespaceId() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "postgres",
                    List.of(new XmlCallMapping("namespace1", "call1", "postgres", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            assertDoesNotThrow(() -> new MyBatisMappings(mappings));
        }

        @Test
        void shouldRejectDuplicateCallsInInserts() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(),
                    List.of(
                        new XmlCallMapping("namespace1", "insert1", "oracle", "INSERT INTO table", new ArrayList<>()),
                        new XmlCallMapping("namespace1", "insert1", "oracle", "INSERT INTO table", new ArrayList<>())
                    ),
                    new ArrayList<>(), new ArrayList<>())
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new MyBatisMappings(mappings));

            assertTrue(exception.getMessage().contains("Duplicate call found"));
        }

        @Test
        void shouldRejectDuplicateCallsInUpdates() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(),
                    List.of(
                        new XmlCallMapping("namespace1", "update1", "oracle", "UPDATE table", new ArrayList<>()),
                        new XmlCallMapping("namespace1", "update1", "oracle", "UPDATE table", new ArrayList<>())
                    ),
                    new ArrayList<>())
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new MyBatisMappings(mappings));

            assertTrue(exception.getMessage().contains("Duplicate call found"));
        }

        @Test
        void shouldRejectDuplicateCallsAcrossSelectsAndInserts() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "INSERT INTO table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>())
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new MyBatisMappings(mappings));

            assertTrue(exception.getMessage().contains("Duplicate call found"));
        }
    }

    @Nested
    class Sort {
        @Test
        void shouldReturnEmptyListWhenInputIsEmpty() {
            List<XmlMyBatisMapping> mappings = new ArrayList<>();

            List<XmlMyBatisMapping> result = MyBatisMappings.sort(mappings);

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldReturnSingleMappingUnchanged() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            List<XmlMyBatisMapping> result = MyBatisMappings.sort(mappings);

            assertEquals(1, result.size());
            assertEquals("project1", result.get(0).project());
        }

        @Test
        void shouldNotModifyOriginalList() {
            List<XmlMyBatisMapping> mappings = new ArrayList<>(List.of(
                new XmlMyBatisMapping("project2", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            ));

            List<XmlMyBatisMapping> result = MyBatisMappings.sort(mappings);

            // Original list should still have project2 first
            assertEquals("project2", mappings.get(0).project());
            // Sorted result should have project1 first
            assertEquals("project1", result.get(0).project());
        }

        @Test
        void shouldSortByProjectAscending() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("projectZ", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectA", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectM", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            List<XmlMyBatisMapping> result = MyBatisMappings.sort(mappings);

            assertEquals("projectA", result.get(0).project());
            assertEquals("projectM", result.get(1).project());
            assertEquals("projectZ", result.get(2).project());
        }

        @Test
        void shouldSortByNamespaceWhenProjectIsEqual() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespaceZ", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespaceA", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespaceM", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            List<XmlMyBatisMapping> result = MyBatisMappings.sort(mappings);

            assertEquals("namespaceA", result.get(0).namespace());
            assertEquals("namespaceM", result.get(1).namespace());
            assertEquals("namespaceZ", result.get(2).namespace());
        }

        @Test
        void shouldSortByDatabaseWhenProjectAndNamespaceAreEqual() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "postgres",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "mysql",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            List<XmlMyBatisMapping> result = MyBatisMappings.sort(mappings);

            assertEquals("mysql", result.get(0).database());
            assertEquals("oracle", result.get(1).database());
            assertEquals("postgres", result.get(2).database());
        }

        @Test
        void shouldSortWithMultipleProjects() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("projectB", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectA", "namespace2", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectB", "namespace2", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectA", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            List<XmlMyBatisMapping> result = MyBatisMappings.sort(mappings);

            // Should be sorted by project first, then namespace
            assertEquals("projectA", result.get(0).project());
            assertEquals("namespace1", result.get(0).namespace());
            assertEquals("projectA", result.get(1).project());
            assertEquals("namespace2", result.get(1).namespace());
            assertEquals("projectB", result.get(2).project());
            assertEquals("namespace1", result.get(2).namespace());
            assertEquals("projectB", result.get(3).project());
            assertEquals("namespace2", result.get(3).namespace());
        }

        @Test
        void shouldSortComplexScenario() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("projectB", "namespaceA", "postgres",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectA", "namespaceB", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectB", "namespaceA", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectA", "namespaceA", "postgres",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectA", "namespaceA", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            List<XmlMyBatisMapping> result = MyBatisMappings.sort(mappings);

            // Verify ordering: project1->namespace1->oracle, project1->namespace1->postgres, etc.
            assertEquals("projectA", result.get(0).project());
            assertEquals("namespaceA", result.get(0).namespace());
            assertEquals("oracle", result.get(0).database());

            assertEquals("projectA", result.get(1).project());
            assertEquals("namespaceA", result.get(1).namespace());
            assertEquals("postgres", result.get(1).database());

            assertEquals("projectA", result.get(2).project());
            assertEquals("namespaceB", result.get(2).namespace());

            assertEquals("projectB", result.get(3).project());
            assertEquals("namespaceA", result.get(3).namespace());
            assertEquals("oracle", result.get(3).database());

            assertEquals("projectB", result.get(4).project());
            assertEquals("namespaceA", result.get(4).namespace());
            assertEquals("postgres", result.get(4).database());
        }

        @Test
        void shouldPreserveListSize() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project2", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project3", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            List<XmlMyBatisMapping> result = MyBatisMappings.sort(mappings);

            assertEquals(mappings.size(), result.size());
        }
    }

    @Nested
    class ValidateCalls {
        @Test
        void shouldAcceptEmptyCallsList() {
            // Should not throw an exception
            assertDoesNotThrow(() -> {
                List<XmlCallMapping> calls = new ArrayList<>();
                MyBatisMappings.validateCalls(calls);
            });
        }

        @Test
        void shouldAcceptSingleValidCall() {
            List<XmlCallMapping> calls = List.of(
                new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())
            );

            assertDoesNotThrow(() -> MyBatisMappings.validateCalls(calls));
        }

        @Test
        void shouldAcceptMultipleValidCalls() {
            List<XmlCallMapping> calls = List.of(
                new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table1", new ArrayList<>()),
                new XmlCallMapping("namespace1", "call2", "oracle", "SELECT * FROM table2", new ArrayList<>()),
                new XmlCallMapping("namespace1", "call3", "postgres", "SELECT * FROM table3", new ArrayList<>())
            );

            assertDoesNotThrow(() -> MyBatisMappings.validateCalls(calls));
        }

        @Test
        void shouldRejectCallWithEmptyFunctionCall() {
            List<XmlCallMapping> calls = List.of(
                new XmlCallMapping("namespace1", "call1", "oracle", "", new ArrayList<>())
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> MyBatisMappings.validateCalls(calls));

            assertTrue(exception.getMessage().contains("Function call or ID cannot be empty"));
            assertTrue(exception.getMessage().contains("namespace=namespace1"));
            assertTrue(exception.getMessage().contains("id=call1"));
            assertTrue(exception.getMessage().contains("database=oracle"));
        }

        @Test
        void shouldRejectCallWithEmptyId() {
            List<XmlCallMapping> calls = List.of(
                new XmlCallMapping("namespace1", "", "oracle", "SELECT * FROM table", new ArrayList<>())
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> MyBatisMappings.validateCalls(calls));

            assertTrue(exception.getMessage().contains("Function call or ID cannot be empty"));
        }

        @Test
        void shouldRejectCallWithOnlyWhitespaceId() {
            List<XmlCallMapping> calls = List.of(
                new XmlCallMapping("namespace1", "   ", "oracle", "SELECT * FROM table", new ArrayList<>())
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> MyBatisMappings.validateCalls(calls));

            assertTrue(exception.getMessage().contains("Function call or ID cannot be empty"));
        }

        @Test
        void shouldRejectCallWithTabsInId() {
            List<XmlCallMapping> calls = List.of(
                new XmlCallMapping("namespace1", "\t\t", "oracle", "SELECT * FROM table", new ArrayList<>())
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> MyBatisMappings.validateCalls(calls));

            assertTrue(exception.getMessage().contains("Function call or ID cannot be empty"));
        }

        @Test
        void shouldRejectCallWithNewlineInId() {
            List<XmlCallMapping> calls = List.of(
                new XmlCallMapping("namespace1", "\n", "oracle", "SELECT * FROM table", new ArrayList<>())
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> MyBatisMappings.validateCalls(calls));

            assertTrue(exception.getMessage().contains("Function call or ID cannot be empty"));
        }

        @Test
        void shouldRejectFirstInvalidCallWhenMultipleInvalid() {
            List<XmlCallMapping> calls = List.of(
                new XmlCallMapping("namespace1", "call1", "oracle", "", new ArrayList<>()),
                new XmlCallMapping("namespace1", "call2", "oracle", "SELECT * FROM table", new ArrayList<>())
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> MyBatisMappings.validateCalls(calls));

            assertTrue(exception.getMessage().contains("Function call or ID cannot be empty"));
            assertTrue(exception.getMessage().contains("id=call1"));
        }

        @Test
        void shouldRejectInvalidCallInMiddleOfList() {
            List<XmlCallMapping> calls = List.of(
                new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table1", new ArrayList<>()),
                new XmlCallMapping("namespace1", "call2", "oracle", "", new ArrayList<>()),
                new XmlCallMapping("namespace1", "call3", "oracle", "SELECT * FROM table3", new ArrayList<>())
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> MyBatisMappings.validateCalls(calls));

            assertTrue(exception.getMessage().contains("Function call or ID cannot be empty"));
            assertTrue(exception.getMessage().contains("id=call2"));
        }

        @Test
        void shouldAcceptCallWithValidIdContainingSpaces() {
            List<XmlCallMapping> calls = List.of(
                new XmlCallMapping("namespace1", "call with spaces", "oracle", "SELECT * FROM table", new ArrayList<>())
            );

            assertDoesNotThrow(() -> MyBatisMappings.validateCalls(calls));
        }

        @Test
        void shouldAcceptCallWithValidIdContainingNumbers() {
            List<XmlCallMapping> calls = List.of(
                new XmlCallMapping("namespace1", "call123", "oracle", "SELECT * FROM table", new ArrayList<>())
            );

            assertDoesNotThrow(() -> MyBatisMappings.validateCalls(calls));
        }

        @Test
        void shouldAcceptCallWithValidIdContainingSpecialCharacters() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call_with-special.chars", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );

            assertDoesNotThrow(() -> new MyBatisMappings(mappings));
        }

        @Test
        void shouldAcceptCallWithFunctionCallContainingNewlines() {
            List<XmlCallMapping> calls = List.of(
                new XmlCallMapping("namespace1", "call1", "oracle",
                    "SELECT col1, col2\nFROM table\nWHERE id = ?", new ArrayList<>())
            );

            assertDoesNotThrow(() -> MyBatisMappings.validateCalls(calls));
        }

        @Test
        void shouldRejectBothEmptyFunctionCallAndId() {
            List<XmlCallMapping> calls = List.of(
                new XmlCallMapping("namespace1", "", "oracle", "", new ArrayList<>())
            );

            // Should fail on ID check first (comes second in the condition)
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> MyBatisMappings.validateCalls(calls));

            assertTrue(exception.getMessage().contains("Function call or ID cannot be empty"));
        }

        @Test
        void shouldRejectInsertWithEmptyFunctionCall() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(),
                    List.of(new XmlCallMapping("namespace1", "insert1", "oracle", "", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>())
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new MyBatisMappings(mappings));

            assertTrue(exception.getMessage().contains("Function call or ID cannot be empty"));
        }

        @Test
        void shouldRejectUpdateWithEmptyFunctionCall() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(),
                    List.of(new XmlCallMapping("namespace1", "update1", "oracle", "", new ArrayList<>())),
                    new ArrayList<>())
            );

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new MyBatisMappings(mappings));

            assertTrue(exception.getMessage().contains("Function call or ID cannot be empty"));
        }

        @Test
        void shouldAcceptCallWithOnlyOneValidIdCharacter() {
            List<XmlCallMapping> calls = List.of(
                new XmlCallMapping("namespace1", "c", "oracle", "SELECT * FROM table", new ArrayList<>())
            );

            assertDoesNotThrow(() -> MyBatisMappings.validateCalls(calls));
        }
    }

    @Nested
    class GetProjectsKeys {
        @Test
        void shouldReturnEmptyListWhenNoProjects() {
            MyBatisMappings mappings = new MyBatisMappings(new ArrayList<>());

            List<String> projectKeys = mappings.getProjectsKeys();

            assertTrue(projectKeys.isEmpty());
        }

        @Test
        void shouldReturnSingleProjectKey() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            List<String> projectKeys = myBatisMappings.getProjectsKeys();

            assertEquals(1, projectKeys.size());
            assertEquals("project1", projectKeys.get(0));
        }

        @Test
        void shouldReturnMultipleProjectKeys() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("projectA", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectB", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectC", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            List<String> projectKeys = myBatisMappings.getProjectsKeys();

            assertEquals(3, projectKeys.size());
            assertTrue(projectKeys.contains("projectA"));
            assertTrue(projectKeys.contains("projectB"));
            assertTrue(projectKeys.contains("projectC"));
        }

        @Test
        void shouldReturnProjectKeysInSortedOrder() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("projectZ", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectA", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectM", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            List<String> projectKeys = myBatisMappings.getProjectsKeys();

            assertEquals("projectA", projectKeys.get(0));
            assertEquals("projectM", projectKeys.get(1));
            assertEquals("projectZ", projectKeys.get(2));
        }

        @Test
        void shouldReturnAlphabeticalSortedProjectKeys() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("zebra", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("apple", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("banana", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("cherry", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            List<String> projectKeys = myBatisMappings.getProjectsKeys();

            assertEquals(4, projectKeys.size());
            assertEquals("apple", projectKeys.get(0));
            assertEquals("banana", projectKeys.get(1));
            assertEquals("cherry", projectKeys.get(2));
            assertEquals("zebra", projectKeys.get(3));
        }

        @Test
        void shouldReturnProjectKeysWithCaseSensitiveOrdering() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("Project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "postgres",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("PROJECT1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            List<String> projectKeys = myBatisMappings.getProjectsKeys();

            assertEquals(3, projectKeys.size());
            // Uppercase letters come before lowercase in ASCII
            assertTrue(projectKeys.indexOf("PROJECT1") < projectKeys.indexOf("Project1"));
            assertTrue(projectKeys.indexOf("Project1") < projectKeys.indexOf("project1"));
        }

        @Test
        void shouldReturnUniqueProjectKeys() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace2", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "postgres",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            List<String> projectKeys = myBatisMappings.getProjectsKeys();

            assertEquals(1, projectKeys.size());
            assertEquals("project1", projectKeys.get(0));
        }

        @Test
        void shouldNotModifyReturnedListOnSubsequentCalls() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("projectA", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectB", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            List<String> projectKeys1 = myBatisMappings.getProjectsKeys();
            projectKeys1.add("projectC");

            List<String> projectKeys2 = myBatisMappings.getProjectsKeys();

            assertEquals(2, projectKeys2.size());
            assertFalse(projectKeys2.contains("projectC"));
        }

        @Test
        void shouldHandleProjectNamesWithSpecialCharacters() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project-1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project_2", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project.3", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            List<String> projectKeys = myBatisMappings.getProjectsKeys();

            assertEquals(3, projectKeys.size());
            assertTrue(projectKeys.contains("project-1"));
            assertTrue(projectKeys.contains("project_2"));
            assertTrue(projectKeys.contains("project.3"));
        }

        @Test
        void shouldReturnProjectKeysInCorrectOrderWithNumbers() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project10", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project2", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            List<String> projectKeys = myBatisMappings.getProjectsKeys();

            // String comparison: "project1" < "project10" < "project2"
            assertEquals("project1", projectKeys.get(0));
            assertEquals("project10", projectKeys.get(1));
            assertEquals("project2", projectKeys.get(2));
        }

        @Test
        void shouldReturnProjectKeysWithMultipleMappingsPerProject() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("projectA", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectA", "namespace2", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectA", "namespace3", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectB", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            List<String> projectKeys = myBatisMappings.getProjectsKeys();

            assertEquals(2, projectKeys.size());
            assertEquals("projectA", projectKeys.get(0));
            assertEquals("projectB", projectKeys.get(1));
        }
    }

    @Nested
    class GetMappersTxt {
        @Test
        void shouldReturnEmptyStringForEmptyMappings() {
            List<XmlMyBatisMapping> mappings = new ArrayList<>();
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersTxt();

            assertEquals("", result);
        }

        @Test
        void shouldReturnSingleCallMapping() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "call1", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersTxt();

            String expected = """
                namespace1:
                  call1():
                    oracle: call1
                """;

            assertEquals(expected, result);
        }

        @Test
        void shouldSortNamespacesByAlphabeticalOrder() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespaceZ", "oracle",
                    List.of(new XmlCallMapping("namespaceZ", "call1", "oracle", "SELECT * FROM Z", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespaceA", "oracle",
                    List.of(new XmlCallMapping("namespaceA", "call1", "oracle", "SELECT * FROM A", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersTxt();

            String expected = """
                namespaceA:
                  call1():
                    oracle: SELECT * FROM A
                
                namespaceZ:
                  call1():
                    oracle: SELECT * FROM Z
                """;

            assertEquals(expected, result);
        }

        @Test
        void shouldSortCallIdsByAlphabeticalOrder() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(
                        new XmlCallMapping("namespace1", "call2", "oracle", "SELECT * FROM table2", new ArrayList<>()),
                        new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table1", new ArrayList<>())
                    ),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersTxt();

            String expected = """
                namespace1:
                  call1():
                    oracle: SELECT * FROM table1
                  call2():
                    oracle: SELECT * FROM table2
                """;

            assertEquals(expected, result);
        }

        @Test
        void shouldHandleMultipleDatabases() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT FROM ORACLE", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "postgres",
                    List.of(new XmlCallMapping("namespace1", "call1", "postgres", "SELECT FROM POSTGRES", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersTxt();

            String expected = """
                namespace1:
                  call1():
                    oracle: SELECT FROM ORACLE
                    postgres: SELECT FROM POSTGRES
                """;

            assertEquals(expected, result);
        }

        @Test
        void shouldShowUndefinedForMissingDatabaseMapping() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT FROM ORACLE", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "postgres",
                    List.of(),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersTxt();

            String expected = """
                namespace1:
                  call1():
                    oracle: SELECT FROM ORACLE
                    postgres: _UNDEFINED_
                """;

            assertEquals(expected, result);
        }

        @Test
        void shouldHandleMultipleProjectsInSortedOrder() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("projectB", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT FROM B", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectA", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT FROM A", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersTxt();

            String expected = """
                namespace1:
                  call1():
                    oracle: SELECT FROM A
                
                namespace1:
                  call1():
                    oracle: SELECT FROM B
                """;

            assertEquals(expected, result);
        }

        @Test
        void shouldHandleMultipleCallsPerNamespace() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(
                        new XmlCallMapping("namespace1", "call1", "oracle", "SELECT 1", new ArrayList<>()),
                        new XmlCallMapping("namespace1", "call2", "oracle", "SELECT 2", new ArrayList<>()),
                        new XmlCallMapping("namespace1", "call3", "oracle", "SELECT 3", new ArrayList<>())
                    ),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersTxt();

            String expected = """
                namespace1:
                  call1():
                    oracle: SELECT 1
                  call2():
                    oracle: SELECT 2
                  call3():
                    oracle: SELECT 3
                """;

            assertEquals(expected, result);
        }

        @Test
        void shouldHandleCallsWithDifferentDatabasesPerNamespace() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(
                        new XmlCallMapping("namespace1", "call1", "oracle", "SELECT FROM ORACLE1", new ArrayList<>()),
                        new XmlCallMapping("namespace1", "call2", "oracle", "SELECT FROM ORACLE2", new ArrayList<>())
                    ),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "postgres",
                    List.of(
                        new XmlCallMapping("namespace1", "call1", "postgres", "SELECT FROM POSTGRES1", new ArrayList<>()),
                        new XmlCallMapping("namespace1", "call2", "postgres", "SELECT FROM POSTGRES2", new ArrayList<>())
                    ),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersTxt();

            String expected = """
                namespace1:
                  call1():
                    oracle: SELECT FROM ORACLE1
                    postgres: SELECT FROM POSTGRES1
                  call2():
                    oracle: SELECT FROM ORACLE2
                    postgres: SELECT FROM POSTGRES2
                """;

            assertEquals(expected, result);
        }

        @Test
        void shouldHandleComplexMultiProjectScenario() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("projectA", "namespaceX", "oracle",
                    List.of(new XmlCallMapping("namespaceX", "methodA", "oracle", "SELECT A_ORACLE", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectA", "namespaceY", "postgres",
                    List.of(new XmlCallMapping("namespaceY", "methodB", "postgres", "SELECT A_POSTGRES", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectB", "namespaceX", "oracle",
                    List.of(new XmlCallMapping("namespaceX", "methodA", "oracle", "SELECT B_ORACLE", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersTxt();

            String expected = """
                namespaceX:
                  methodA():
                    oracle: SELECT A_ORACLE
                    postgres: _UNDEFINED_

                namespaceY:
                  methodB():
                    oracle: _UNDEFINED_
                    postgres: SELECT A_POSTGRES

                namespaceX:
                  methodA():
                    oracle: SELECT B_ORACLE
                    postgres: _UNDEFINED_
                """;

            assertEquals(expected, result);
        }

        @Test
        void shouldSortDatabasesConsistently() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "postgres",
                    List.of(new XmlCallMapping("namespace1", "call1", "postgres", "POSTGRES_QUERY", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "mysql",
                    List.of(new XmlCallMapping("namespace1", "call1", "mysql", "MYSQL_QUERY", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "ORACLE_QUERY", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersTxt();

            String expected = """
                namespace1:
                  call1():
                    mysql: MYSQL_QUERY
                    oracle: ORACLE_QUERY
                    postgres: POSTGRES_QUERY
                """;

            assertEquals(expected, result);
        }
    }

    @Nested
    class GetResultMaps {
        @Test
        void shouldReturnEmptyStringWhenResultMapsIsNull() {
            Map<String, List<XmlResultMap>> resultsByNamespace = new HashMap<>();

            String result = MyBatisMappings.getResultMaps("namespace1", resultsByNamespace);

            assertEquals("", result);
        }

        @Test
        void shouldReturnEmptyStringWhenResultMapsIsEmpty() {
            Map<String, List<XmlResultMap>> resultsByNamespace = new HashMap<>();
            resultsByNamespace.put("namespace1", new ArrayList<>());

            String result = MyBatisMappings.getResultMaps("namespace1", resultsByNamespace);

            assertEquals("", result);
        }

        @Test
        void shouldReturnResultMapsWithSingleResult() {
            List<XmlResultMap.XmlResult> results = List.of(
                new XmlResultMap.XmlResult("property1", "column1")
            );
            List<XmlResultMap> resultMaps = List.of(
                new XmlResultMap("namespace1", "resultMap1", "oracle", results)
            );
            Map<String, List<XmlResultMap>> resultsByNamespace = new HashMap<>();
            resultsByNamespace.put("namespace1", resultMaps);

            String result = MyBatisMappings.getResultMaps("namespace1", resultsByNamespace);

            assertTrue(result.contains("RESULT_MAPS:"));
            assertTrue(result.contains("oracle: resultMap1"));
            assertTrue(result.contains("property: property1, column: column1"));
        }

        @Test
        void shouldReturnResultMapsWithMultipleResults() {
            List<XmlResultMap.XmlResult> results = List.of(
                new XmlResultMap.XmlResult("property1", "column1"),
                new XmlResultMap.XmlResult("property2", "column2"),
                new XmlResultMap.XmlResult("property3", "column3")
            );
            List<XmlResultMap> resultMaps = List.of(
                new XmlResultMap("namespace1", "resultMap1", "oracle", results)
            );
            Map<String, List<XmlResultMap>> resultsByNamespace = new HashMap<>();
            resultsByNamespace.put("namespace1", resultMaps);

            String result = MyBatisMappings.getResultMaps("namespace1", resultsByNamespace);

            assertTrue(result.contains("property: property1, column: column1"));
            assertTrue(result.contains("property: property2, column: column2"));
            assertTrue(result.contains("property: property3, column: column3"));
        }

        @Test
        void shouldReturnResultMapsForMultipleDatabases() {
            List<XmlResultMap.XmlResult> results1 = List.of(
                new XmlResultMap.XmlResult("prop1", "col1")
            );
            List<XmlResultMap.XmlResult> results2 = List.of(
                new XmlResultMap.XmlResult("prop2", "col2")
            );
            List<XmlResultMap> resultMaps = List.of(
                new XmlResultMap("namespace1", "resultMap1", "oracle", results1),
                new XmlResultMap("namespace1", "resultMap2", "postgres", results2)
            );
            Map<String, List<XmlResultMap>> resultsByNamespace = new HashMap<>();
            resultsByNamespace.put("namespace1", resultMaps);

            String result = MyBatisMappings.getResultMaps("namespace1", resultsByNamespace);

            assertTrue(result.contains("oracle: resultMap1"));
            assertTrue(result.contains("postgres: resultMap2"));
        }

        @Test
        void shouldSortResultMapsByIdNullsLast() {
            List<XmlResultMap.XmlResult> emptyResults = new ArrayList<>();
            List<XmlResultMap> resultMaps = List.of(
                new XmlResultMap("namespace1", "zebra", "oracle", emptyResults),
                new XmlResultMap("namespace1", null, "oracle", emptyResults),
                new XmlResultMap("namespace1", "apple", "oracle", emptyResults)
            );
            Map<String, List<XmlResultMap>> resultsByNamespace = new HashMap<>();
            resultsByNamespace.put("namespace1", resultMaps);

            String result = MyBatisMappings.getResultMaps("namespace1", resultsByNamespace);

            // Verify sorting: apple, zebra, then null
            int appleIndex = result.indexOf("apple");
            int zebraIndex = result.indexOf("zebra");
            assertTrue(appleIndex < zebraIndex, "apple should come before zebra");
        }

        @Test
        void shouldHandleResultMapWithNullId() {
            List<XmlResultMap.XmlResult> results = List.of(
                new XmlResultMap.XmlResult("property1", "column1")
            );
            List<XmlResultMap> resultMaps = List.of(
                new XmlResultMap("namespace1", null, "oracle", results)
            );
            Map<String, List<XmlResultMap>> resultsByNamespace = new HashMap<>();
            resultsByNamespace.put("namespace1", resultMaps);

            String result = MyBatisMappings.getResultMaps("namespace1", resultsByNamespace);

            assertTrue(result.contains("RESULT_MAPS:"));
            assertTrue(result.contains("oracle: null"));
        }

        @Test
        void shouldHandleMultipleResultMapsWithMixedIds() {
            List<XmlResultMap.XmlResult> emptyResults = new ArrayList<>();
            List<XmlResultMap> resultMaps = List.of(
                new XmlResultMap("namespace1", "map1", "oracle", emptyResults),
                new XmlResultMap("namespace1", null, "postgres", emptyResults),
                new XmlResultMap("namespace1", "map2", "mysql", emptyResults)
            );
            Map<String, List<XmlResultMap>> resultsByNamespace = new HashMap<>();
            resultsByNamespace.put("namespace1", resultMaps);

            String result = MyBatisMappings.getResultMaps("namespace1", resultsByNamespace);

            assertTrue(result.contains("map1"));
            assertTrue(result.contains("map2"));
            assertTrue(result.contains("null"));
        }

        @Test
        void shouldFormatResultMapsCorrectly() {
            List<XmlResultMap.XmlResult> results = List.of(
                new XmlResultMap.XmlResult("userId", "user_id"),
                new XmlResultMap.XmlResult("userName", "user_name")
            );
            List<XmlResultMap> resultMaps = List.of(
                new XmlResultMap("namespace1", "userResultMap", "oracle", results)
            );
            Map<String, List<XmlResultMap>> resultsByNamespace = new HashMap<>();
            resultsByNamespace.put("namespace1", resultMaps);

            String result = MyBatisMappings.getResultMaps("namespace1", resultsByNamespace);

            String expected = """
                RESULT_MAPS:
                  oracle: userResultMap
                    - property: userId, column: user_id
                    - property: userName, column: user_name

                """;
            assertEquals(expected, result);
        }

        @Test
        void shouldReturnEmptyStringForNonExistentNamespace() {
            Map<String, List<XmlResultMap>> resultsByNamespace = new HashMap<>();
            resultsByNamespace.put("namespace1", new ArrayList<>());

            String result = MyBatisMappings.getResultMaps("namespace2", resultsByNamespace);

            assertEquals("", result);
        }

        @Test
        void shouldHandleEmptyResultsListInResultMap() {
            List<XmlResultMap> resultMaps = List.of(
                new XmlResultMap("namespace1", "resultMap1", "oracle", new ArrayList<>())
            );
            Map<String, List<XmlResultMap>> resultsByNamespace = new HashMap<>();
            resultsByNamespace.put("namespace1", resultMaps);

            String result = MyBatisMappings.getResultMaps("namespace1", resultsByNamespace);

            assertTrue(result.contains("RESULT_MAPS:"));
            assertTrue(result.contains("oracle: resultMap1"));
            assertTrue(result.endsWith("\n"));
        }
    }

    @Nested
    class GetCallMd {
        @Test
        void shouldReturnCallWithDatabaseAndFunctionWhenCallExists() {
            XmlCallMapping call = new XmlCallMapping("namespace1", "id1", "oracle", "SELECT * FROM table", new ArrayList<>());
            List<XmlCallMapping> byIdList = List.of(call);

            String result = MyBatisMappings.getCallMd(call, "oracle", byIdList);

            String expected = "  oracle: SELECT * FROM table\n";
            assertEquals(expected, result);
        }

        @Test
        void shouldReturnUndefinedWhenCallDoesNotExist() {
            XmlCallMapping xmlCallMapping = new XmlCallMapping("namespace1", "id1", "oracle", "SELECT * FROM table", new ArrayList<>());
            List<XmlCallMapping> byIdList = new ArrayList<>();

            String result = MyBatisMappings.getCallMd(xmlCallMapping, "oracle", byIdList);

            String expected = "  oracle: _UNDEFINED_\n\n";
            assertEquals(expected, result);
        }

        @Test
        void shouldReturnUndefinedWhenDatabaseNotInList() {
            XmlCallMapping oracleCall = new XmlCallMapping("namespace1", "id1", "oracle", "SELECT * FROM oracle_table", new ArrayList<>());
            List<XmlCallMapping> byIdList = List.of(oracleCall);

            String result = MyBatisMappings.getCallMd(oracleCall, "postgres", byIdList);

            assertTrue(result.contains("postgres: _UNDEFINED_"));
        }

        @Test
        void shouldIncludeParamsWhenFunctionParamsArePresent() {
            List<String> params = List.of("param1", "param2", "param3");
            XmlCallMapping call = new XmlCallMapping("namespace1", "id1", "oracle", "SELECT * FROM table WHERE id = ?", params);
            List<XmlCallMapping> byIdList = List.of(call);

            String result = MyBatisMappings.getCallMd(call, "oracle", byIdList);

            assertTrue(result.contains("params:"));
            assertTrue(result.contains("- param1"));
            assertTrue(result.contains("- param2"));
            assertTrue(result.contains("- param3"));
        }

        @Test
        void shouldNotIncludeParamsWhenFunctionParamsAreEmpty() {
            XmlCallMapping call = new XmlCallMapping("namespace1", "id1", "oracle", "SELECT * FROM table", new ArrayList<>());
            List<XmlCallMapping> byIdList = List.of(call);

            String result = MyBatisMappings.getCallMd(call, "oracle", byIdList);

            assertFalse(result.contains("params:"));
        }

        @Test
        void shouldFormatParamsCorrectly() {
            List<String> params = List.of("userId", "status");
            XmlCallMapping call = new XmlCallMapping("namespace1", "id1", "oracle", "SELECT * FROM users", params);
            List<XmlCallMapping> byIdList = List.of(call);

            String result = MyBatisMappings.getCallMd(call, "oracle", byIdList);

            String expected = "  oracle: SELECT * FROM users\n" +
                            "    params:\n" +
                            "      - userId\n" +
                            "      - status\n" +
                            "\n";
            assertEquals(expected, result);
        }

        @Test
        void shouldFindCorrectCallFromMultipleCalls() {
            XmlCallMapping oracleCall = new XmlCallMapping("namespace1", "id1", "oracle", "CALL oracle_func()", List.of());
            XmlCallMapping postgresCall = new XmlCallMapping("namespace1", "id1", "postgres", "CALL postgres_func()", List.of());
            XmlCallMapping mysqlCall = new XmlCallMapping("namespace1", "id1", "mysql", "CALL mysql_func()", List.of());
            List<XmlCallMapping> byIdList = List.of(oracleCall, postgresCall, mysqlCall);

            String result = MyBatisMappings.getCallMd(oracleCall, "postgres", byIdList);

            assertTrue(result.contains("postgres: CALL postgres_func()"));
            assertFalse(result.contains("oracle_func"));
            assertFalse(result.contains("mysql_func"));
        }
    }

    @Nested
    class GetMappersMd {
        @Test
        void shouldReturnEmptyStringForEmptyMappings() {
            List<XmlMyBatisMapping> mappings = new ArrayList<>();
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            assertEquals("", result);
        }

        @Test
        void shouldReturnSingleCallMappingInMarkdownFormat() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            assertTrue(result.contains("# namespace1:"));
            assertTrue(result.contains("```"));
            assertTrue(result.contains("call1():"));
            assertTrue(result.contains("oracle: SELECT * FROM table"));
        }

        @Test
        void shouldFormatNamespacesWithMarkdownHeaders() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT 1", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            assertTrue(result.startsWith("# namespace1:"));
        }

        @Test
        void shouldEncloseCallsInCodeBlock() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            assertTrue(result.contains("```\n"));
            assertTrue(result.contains("\n```"));
        }

        @Test
        void shouldSortNamespacesByAlphabeticalOrder() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespaceZ", "oracle",
                    List.of(new XmlCallMapping("namespaceZ", "call1", "oracle", "SELECT * FROM Z", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespaceA", "oracle",
                    List.of(new XmlCallMapping("namespaceA", "call1", "oracle", "SELECT * FROM A", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            int indexA = result.indexOf("# namespaceA:");
            int indexZ = result.indexOf("# namespaceZ:");

            assertTrue(indexA > -1);
            assertTrue(indexZ > -1);
            assertTrue(indexA < indexZ, "namespaceA should appear before namespaceZ");
        }

        @Test
        void shouldSortCallIdsByAlphabeticalOrder() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(
                        new XmlCallMapping("namespace1", "call2", "oracle", "SELECT * FROM table2", new ArrayList<>()),
                        new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table1", new ArrayList<>())
                    ),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            int indexCall1 = result.indexOf("call1():");
            int indexCall2 = result.indexOf("call2():");

            assertTrue(indexCall1 > -1);
            assertTrue(indexCall2 > -1);
            assertTrue(indexCall1 < indexCall2, "call1 should appear before call2");
        }

        @Test
        void shouldHandleMultipleDatabases() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT FROM ORACLE", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "postgres",
                    List.of(new XmlCallMapping("namespace1", "call1", "postgres", "SELECT FROM POSTGRES", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            assertTrue(result.contains("oracle: SELECT FROM ORACLE"));
            assertTrue(result.contains("postgres: SELECT FROM POSTGRES"));
        }

        @Test
        void shouldShowUndefinedForMissingDatabaseMapping() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT FROM ORACLE", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "postgres",
                    List.of(),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            assertTrue(result.contains("postgres: _UNDEFINED_"));
        }

        @Test
        void shouldHandleMultipleProjectsInSortedOrder() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("projectB", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT FROM B", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectA", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT FROM A", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            int indexA = result.indexOf("SELECT FROM A");
            int indexB = result.indexOf("SELECT FROM B");

            assertTrue(indexA > -1);
            assertTrue(indexB > -1);
            assertTrue(indexA < indexB, "projectA should appear before projectB");
        }

        @Test
        void shouldHandleMultipleCallsPerNamespace() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(
                        new XmlCallMapping("namespace1", "call1", "oracle", "SELECT 1", new ArrayList<>()),
                        new XmlCallMapping("namespace1", "call2", "oracle", "SELECT 2", new ArrayList<>()),
                        new XmlCallMapping("namespace1", "call3", "oracle", "SELECT 3", new ArrayList<>())
                    ),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            assertTrue(result.contains("call1():"));
            assertTrue(result.contains("call2():"));
            assertTrue(result.contains("call3():"));
            assertTrue(result.contains("SELECT 1"));
            assertTrue(result.contains("SELECT 2"));
            assertTrue(result.contains("SELECT 3"));
        }

        @Test
        void shouldHandleCallsWithDifferentDatabasesPerNamespace() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(
                        new XmlCallMapping("namespace1", "call1", "oracle", "SELECT FROM ORACLE1", new ArrayList<>()),
                        new XmlCallMapping("namespace1", "call2", "oracle", "SELECT FROM ORACLE2", new ArrayList<>())
                    ),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "postgres",
                    List.of(
                        new XmlCallMapping("namespace1", "call1", "postgres", "SELECT FROM POSTGRES1", new ArrayList<>()),
                        new XmlCallMapping("namespace1", "call2", "postgres", "SELECT FROM POSTGRES2", new ArrayList<>())
                    ),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            assertTrue(result.contains("call1():"));
            assertTrue(result.contains("call2():"));
            assertTrue(result.contains("oracle: SELECT FROM ORACLE1"));
            assertTrue(result.contains("oracle: SELECT FROM ORACLE2"));
            assertTrue(result.contains("postgres: SELECT FROM POSTGRES1"));
            assertTrue(result.contains("postgres: SELECT FROM POSTGRES2"));
        }

        @Test
        void shouldHandleComplexMultiProjectScenario() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("projectA", "namespaceX", "oracle",
                    List.of(new XmlCallMapping("namespaceX", "methodA", "oracle", "SELECT A_ORACLE", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectA", "namespaceY", "postgres",
                    List.of(new XmlCallMapping("namespaceY", "methodB", "postgres", "SELECT A_POSTGRES", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("projectB", "namespaceX", "oracle",
                    List.of(new XmlCallMapping("namespaceX", "methodA", "oracle", "SELECT B_ORACLE", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            assertTrue(result.contains("# namespaceX:"));
            assertTrue(result.contains("# namespaceY:"));
            assertTrue(result.contains("methodA():"));
            assertTrue(result.contains("methodB():"));
            assertTrue(result.contains("SELECT A_ORACLE"));
            assertTrue(result.contains("SELECT A_POSTGRES"));
            assertTrue(result.contains("SELECT B_ORACLE"));
        }

        @Test
        void shouldSortDatabasesConsistently() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "postgres",
                    List.of(new XmlCallMapping("namespace1", "call1", "postgres", "POSTGRES_QUERY", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "mysql",
                    List.of(new XmlCallMapping("namespace1", "call1", "mysql", "MYSQL_QUERY", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "ORACLE_QUERY", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            int indexMysql = result.indexOf("mysql: MYSQL_QUERY");
            int indexOracle = result.indexOf("oracle: ORACLE_QUERY");
            int indexPostgres = result.indexOf("postgres: POSTGRES_QUERY");

            assertTrue(indexMysql > -1);
            assertTrue(indexOracle > -1);
            assertTrue(indexPostgres > -1);
            assertTrue(indexMysql < indexOracle, "mysql should appear before oracle");
            assertTrue(indexOracle < indexPostgres, "oracle should appear before postgres");
        }

        @Test
        void shouldIncludeIndentationForDatabaseCalls() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            assertTrue(result.contains("  oracle:"), "Database calls should be indented");
        }

        @Test
        void shouldEndWithNewline() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            assertTrue(result.endsWith("\n"));
        }

        @Test
        void shouldHandleCallsWithParameters() {
            List<String> params = List.of("param1", "param2");
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table WHERE id = ?", params)),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            assertTrue(result.contains("params:"));
            assertTrue(result.contains("- param1"));
            assertTrue(result.contains("- param2"));
        }

        @Test
        void shouldNotIncludeParamsWhenEmpty() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            assertFalse(result.contains("params:"));
        }

        @Test
        void shouldHandleResultMaps() {
            List<XmlResultMap.XmlResult> xmlResults = List.of(
                new XmlResultMap.XmlResult("property1", "column1")
            );
            XmlResultMap resultMap = new XmlResultMap("namespace1", "resultMapId", "oracle", xmlResults);

            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), List.of(resultMap))
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            assertTrue(result.contains("RESULT_MAPS:") || result.contains("result_maps:"));
            assertTrue(result.contains("resultMapId"));
            assertTrue(result.contains("property1"));
            assertTrue(result.contains("column1"));
        }

        @Test
        void shouldHaveCodeBlockBoundaries() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("project1", "namespace1", "oracle",
                    List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersMd();

            int openingCount = 0;
            int lastIndex = 0;
            while ((lastIndex = result.indexOf("```", lastIndex)) != -1) {
                openingCount++;
                lastIndex += 3;
            }

            assertTrue(openingCount >= 2, "Should have at least opening and closing code blocks");
        }
    }

}
