package workspace.vigiang.dao;

import workspace.vigiang.model.DbObjectDefinition;
import workspace.vigiang.model.DatabaseCredentials;
import workspace.vigiang.model.TablePrinter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PostgresSchemaDAO implements DbSchemaDAO {

    @Override
    public List<DbObjectDefinition> listTables(DatabaseCredentials databaseCredentials) throws SQLException {
        String sql =
            "select table_schema, table_name\n" +
            "from information_schema.tables \n" +
            "where table_type in ('BASE TABLE') \n" +
            "  and table_schema in ('api', 'conf', 'dash', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')\n" +
            "order by table_schema, table_name";

        List<DbObjectDefinition> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var tableSchema = rs.getString("table_schema");
                var tableName = rs.getString("table_name");
                var name = tableSchema + "." + tableName;
                var definition = getTableDefinition(conn, tableSchema, tableName);
                result.add(new DbObjectDefinition(name, definition));
            }
        }
        return result;
    }

    private static String getTableDefinition(Connection conn, String tableSchema, String tableName) throws SQLException {
        String result = "";
        String sql =
            "select \n" +
            "  table_schema, table_name, column_name, column_default, is_nullable, data_type, \n" +
            "  character_maximum_length, udt_name\n" +
            "from information_schema.columns\n" +
            "where table_schema in ('api', 'conf', 'dash', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')\n" +
            "  and table_schema = '" + tableSchema + "'\n" +
            "  and table_name = '" + tableName + "'\n" +
            "order by table_schema, table_name, ordinal_position";

        String[] headers = new String[] {
            "column_name", "column_default", "is_nullable", "data_type", "character_maximum_length",
        };
        List<String[]> data = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("column_name"),
                    (rs.getString("column_default") == null) ? "null" : rs.getString("column_default"),
                    rs.getString("is_nullable"),
                    rs.getString("data_type"),
                    (rs.getString("character_maximum_length") == null) ? "null" : rs.getString("character_maximum_length"),
                };
                data.add(row);
            }
        }

        int[] widths = TablePrinter.calculateColumnWidths(headers, data);

        result += TablePrinter.printHorizontalLine(widths) + "\n";
        result += TablePrinter.printRow(headers, widths) + "\n";
        result += TablePrinter.printHorizontalLine(widths) + "\n";

        for (String[] row : data) {
            result += TablePrinter.printRow(row, widths) + "\n";
        }
        result += TablePrinter.printHorizontalLine(widths);

        return result;
    }

    @Override
    public List<DbObjectDefinition> listViews(DatabaseCredentials databaseCredentials) throws SQLException {
        String sql =
            "select table_schema, table_name, view_definition\n" +
            "from information_schema.views\n" +
            "where table_schema in ('api', 'conf', 'dash', 'evt', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')\n" +
            "order by table_schema, table_name";

        List<DbObjectDefinition> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("table_schema") + "." + rs.getString("table_name");
                result.add(new DbObjectDefinition(name, rs.getString("view_definition")));
            }
        }
        return result;
    }

    @Override
    public List<DbObjectDefinition> listFunctions(DatabaseCredentials databaseCredentials) throws SQLException {
        String sql =
            "select routine_schema, routine_name, routine_definition\n" +
            "from information_schema.routines\n" +
            "where routine_type = 'FUNCTION'\n" +
            "  and routine_schema in ('api', 'conf', 'dash', 'evt', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')\n" +
            "order by routine_schema, routine_name";

        List<DbObjectDefinition> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("routine_schema") + "." + rs.getString("routine_name");
                result.add(new DbObjectDefinition(name, rs.getString("routine_definition")));
            }
        }
        return result;
    }

    @Override
    public List<DbObjectDefinition> listIndexes(DatabaseCredentials databaseCredentials) throws SQLException {
        String sql =
            "select schemaname, tablename, indexname, indexdef\n" +
            "from pg_indexes\n" +
            "where schemaname in ('api', 'conf', 'dash', 'evt', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')\n" +
            "order by schemaname, tablename, indexname";

        List<DbObjectDefinition> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("schemaname") + "." + rs.getString("tablename") + " " + rs.getString("indexname");
                result.add(new DbObjectDefinition(name, rs.getString("indexdef")));
            }
        }
        return result;
    }

    @Override
    public List<DbObjectDefinition> listProcedures(DatabaseCredentials databaseCredentials) throws SQLException {
        String sql =
            "select routine_schema as schema_name, routine_name as procedure_name, routine_definition\n" +
            "from information_schema.routines\n" +
            "where routine_type = 'PROCEDURE'\n" +
            "  and routine_schema in ('api', 'conf', 'dash', 'evt', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')\n" +
            "order by schema_name, procedure_name";

        List<DbObjectDefinition> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("schema_name") + "." + rs.getString("procedure_name");
                result.add(new DbObjectDefinition(name, rs.getString("routine_definition")));
            }
        }
        return result;
    }

    @Override
    public List<DbObjectDefinition> listPackageBodies(DatabaseCredentials databaseCredentials) throws SQLException {
        return List.of();
    }

}
