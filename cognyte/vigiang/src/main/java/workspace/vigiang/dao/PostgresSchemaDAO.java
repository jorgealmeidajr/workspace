package workspace.vigiang.dao;

import workspace.commons.dao.DbSchemaDAO;
import workspace.commons.model.DatabaseCredentials;
import workspace.commons.model.DbObjectDefinition;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static workspace.commons.model.DatabaseCredentials.getConnection;

public class PostgresSchemaDAO implements DbSchemaDAO {

    @Override
    public List<DbObjectDefinition> listTables(DatabaseCredentials databaseCredentials, String filter) throws SQLException {
        // TODO: postgres, create tables statements should be in create sql format
        String sql =
            "select table_schema, table_name\n" +
            "from information_schema.tables \n" +
            "where table_type in ('BASE TABLE') \n" +
            (filter != null ? filter : "") + "\n" +
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

    @Deprecated
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

        return result;
    }

    @Override
    public List<DbObjectDefinition> listViews(DatabaseCredentials databaseCredentials, String filter) throws SQLException {
        var where = (filter != null) ? "where " + filter + "\n" : "";

        String sql =
            "select table_schema, table_name, view_definition\n" +
            "from information_schema.views\n" +
            where +
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
    public List<DbObjectDefinition> listFunctions(DatabaseCredentials databaseCredentials, String filter) throws SQLException {
        filter = (filter != null) ? "  " + filter + "\n" : "";

        String sql =
            "select routine_schema, routine_name, routine_definition\n" +
            "from information_schema.routines\n" +
            "where routine_type = 'FUNCTION'\n" +
            filter +
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
    public List<DbObjectDefinition> listIndexes(DatabaseCredentials databaseCredentials, String filter) throws SQLException {
        String sql =
            "select schemaname, tablename, indexname, indexdef\n" +
            "from pg_indexes\n" +
            filter + "\n" +
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
    public List<DbObjectDefinition> listProcedures(DatabaseCredentials databaseCredentials, String filter) throws SQLException {
        String sql =
            "select routine_schema as schema_name, routine_name as procedure_name, routine_definition\n" +
            "from information_schema.routines\n" +
            "where routine_type = 'PROCEDURE'\n" +
            filter + "\n" +
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
    public List<DbObjectDefinition> listPackageBodies(DatabaseCredentials databaseCredentials, String filter) throws SQLException {
        return List.of();
    }

}
