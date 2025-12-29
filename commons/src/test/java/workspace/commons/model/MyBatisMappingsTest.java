package workspace.commons.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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

        @Test
        void shouldReturnEmptyStringIfProjectNameIsEmpty() {
            List<XmlMyBatisMapping> mappings = List.of(
                new XmlMyBatisMapping("", "namespace1", "oracle",
                    new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            List<String> projectKeys = myBatisMappings.getProjectsKeys();

            assertEquals(1, projectKeys.size());
            assertEquals("", projectKeys.get(0));
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

            assertTrue(result.contains(expected));
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

            assertTrue(result.contains(expected));

            int posA = result.indexOf("namespaceA:");
            int posZ = result.indexOf("namespaceZ:");

            assertTrue(posA < posZ, "namespaceA should appear before namespaceZ");
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

            assertTrue(result.contains(expected));

            int pos1 = result.indexOf("call1():");
            int pos2 = result.indexOf("call2():");

            assertTrue(pos1 < pos2, "call1 should appear before call2");
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

            String result = myBatisMappings.getMappersTxt();

            assertTrue(result.contains("_UNDEFINED_"));
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

            // Both projects' data should be present
            assertTrue(result.contains("SELECT FROM A"));
            assertTrue(result.contains("SELECT FROM B"));
        }

        @Test
        void shouldIncludeProperFormatting() {
            List<XmlMyBatisMapping> mappings = List.of(
                    new XmlMyBatisMapping("project1", "namespace1", "oracle",
                            List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                            new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersTxt();

            // Check formatting structure
            assertTrue(result.contains("namespace1:"));
            assertTrue(result.contains("  call1():"));
            assertTrue(result.contains("    oracle:"));
        }

        @Test
        void shouldHandleEmptyProjectName() {
            List<XmlMyBatisMapping> mappings = List.of(
                    new XmlMyBatisMapping("", "namespace1", "oracle",
                            List.of(new XmlCallMapping("namespace1", "call1", "oracle", "SELECT * FROM table", new ArrayList<>())),
                            new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersTxt();

            assertTrue(result.contains("namespace1:"));
            assertTrue(result.contains("call1():"));
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

            String result = myBatisMappings.getMappersTxt();

            assertTrue(result.contains("oracle: SELECT FROM ORACLE1"));
            assertTrue(result.contains("postgres: SELECT FROM POSTGRES1"));
            assertTrue(result.contains("oracle: SELECT FROM ORACLE2"));
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

            String result = myBatisMappings.getMappersTxt();

            assertTrue(result.contains("namespaceX:"));
            assertTrue(result.contains("namespaceY:"));
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

            String result = myBatisMappings.getMappersTxt();

            // Databases should be sorted alphabetically (mysql, oracle, postgres)
            int posMysql = result.indexOf("mysql:");
            int posOracle = result.indexOf("oracle:");
            int posPostgres = result.indexOf("postgres:");

            assertTrue(posMysql < posOracle);
            assertTrue(posOracle < posPostgres);
        }

        @Test
        void shouldHandleCallsWithSpecialCharactersInFunctionCall() {
            List<XmlMyBatisMapping> mappings = List.of(
                    new XmlMyBatisMapping("project1", "namespace1", "oracle",
                            List.of(new XmlCallMapping("namespace1", "call1", "oracle",
                                    "SELECT col1, col2 FROM table WHERE id = ? AND name = ?", new ArrayList<>())),
                            new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
            );
            MyBatisMappings myBatisMappings = new MyBatisMappings(mappings);

            String result = myBatisMappings.getMappersTxt();

            assertTrue(result.contains("SELECT col1, col2 FROM table WHERE id = ? AND name = ?"));
        }
    }

}
