package workspace.commons.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import workspace.commons.model.DbObjectDefinition;

import static org.junit.jupiter.api.Assertions.*;

public class UpdateSchemasServiceTest {

    @Nested
    class GetRowDefinitionStr {
        @Test
        void testSimpleNameWithoutDot() {
            DbObjectDefinition row = new DbObjectDefinition("table_name", "CREATE TABLE table_name (id INT);");

            String result = UpdateSchemasService.getRowDefinitionStr(row);

            String expected =
                "-- " + "#".repeat(120) + "\n" +
                "-- table_name\n" +
                "CREATE TABLE table_name (id INT);\n\n";
            assertEquals(expected, result);
        }

        @Test
        void testNameWithDot() {
            DbObjectDefinition row = new DbObjectDefinition("schema.table_name", "CREATE TABLE table_name (id INT);");

            String result = UpdateSchemasService.getRowDefinitionStr(row);

            String expected =
                "-- " + "#".repeat(120) + "\n" +
                "-- table_name\n" +
                "CREATE TABLE table_name (id INT);\n\n";
            assertEquals(expected, result);
        }

        @Test
        void testMultipleDots() {
            DbObjectDefinition row = new DbObjectDefinition("schema.sub.table_name", "CREATE TABLE table_name (id INT);");

            String result = UpdateSchemasService.getRowDefinitionStr(row);

            String expected =
                "-- " + "#".repeat(120) + "\n" +
                "-- sub.table_name\n" +
                "CREATE TABLE table_name (id INT);\n\n";
            assertEquals(expected, result);
        }

        @Test
        void testDefinitionWithoutSemicolon() {
            DbObjectDefinition row = new DbObjectDefinition("table1", "CREATE TABLE table1 (id INT)");

            String result = UpdateSchemasService.getRowDefinitionStr(row);

            String expected =
                "-- " + "#".repeat(120) + "\n" +
                "-- table1\n" +
                "CREATE TABLE table1 (id INT);\n\n";
            assertEquals(expected, result);
        }

        @Test
        void testDefinitionWithSemicolon() {
            DbObjectDefinition row = new DbObjectDefinition("table1", "CREATE TABLE table1 (id INT);");

            String result = UpdateSchemasService.getRowDefinitionStr(row);

            String expected =
                "-- " + "#".repeat(120) + "\n" +
                "-- table1\n" +
                "CREATE TABLE table1 (id INT);\n\n";
            assertEquals(expected, result);
        }

        @Test
        void testDefinitionWithWhitespace() {
            DbObjectDefinition row = new DbObjectDefinition("table1", "  CREATE TABLE table1 (id INT);  ");

            String result = UpdateSchemasService.getRowDefinitionStr(row);

            String expected =
                "-- " + "#".repeat(120) + "\n" +
                "-- table1\n" +
                "CREATE TABLE table1 (id INT);\n\n";
            assertEquals(expected, result);
        }

        @Test
        void testMultilineDefinition() {
            String definition =
                "CREATE TABLE table1 (\n" +
                "  id INT,\n" +
                "  name VARCHAR(100)\n" +
                ");";
            DbObjectDefinition row = new DbObjectDefinition("table1", definition);

            String result = UpdateSchemasService.getRowDefinitionStr(row);

            String expected =
                "-- " + "#".repeat(120) + "\n" +
                "-- table1\n" +
                "CREATE TABLE table1 (\n" +
                "  id INT,\n" +
                "  name VARCHAR(100)\n" +
                ");\n\n";
            assertEquals(expected, result);
        }

        @Test
        void testHeaderFormat() {
            DbObjectDefinition row = new DbObjectDefinition("my_table", "CREATE TABLE my_table (id INT);");

            String result = UpdateSchemasService.getRowDefinitionStr(row);

            String expected =
                "-- " + "#".repeat(120) + "\n" +
                "-- my_table\n" +
                "CREATE TABLE my_table (id INT);\n\n";
            assertEquals(expected, result);
        }

        @Test
        void testDefinitionWithLeadingTrailingWhitespace() {
            DbObjectDefinition row = new DbObjectDefinition("table1", "   \nCREATE TABLE table1 (id INT);\n   ");

            String result = UpdateSchemasService.getRowDefinitionStr(row);

            String expected =
                "-- " + "#".repeat(120) + "\n" +
                "-- table1\n" +
                "CREATE TABLE table1 (id INT);\n\n";
            assertEquals(expected, result);
        }

        @Test
        void testEmptyDefinition() {
            DbObjectDefinition row = new DbObjectDefinition("table1", "");

            String result = UpdateSchemasService.getRowDefinitionStr(row);

            String expected =
                "-- " + "#".repeat(120) + "\n" +
                "-- table1\n" +
                ";\n\n";
            assertEquals(expected, result);
        }
    }

}
