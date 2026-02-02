package workspace.commons.dao;

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

    private final DatabaseCredentials databaseCredentials;

    public PostgresSchemaDAO(DatabaseCredentials databaseCredentials) {
        this.databaseCredentials = databaseCredentials;
    }

    @Override
    public List<String> listTablesNames() throws SQLException {
        String sql = """
            select table_schema, table_name
            from information_schema.tables
            where table_type in ('BASE TABLE')
            order by table_schema, table_name
            """;

        System.out.println("Executing POSTGRES sql:\n" + sql + "\n");

        List<String> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var tableSchema = rs.getString("table_schema");
                var tableName = rs.getString("table_name");
                var name = tableSchema + "." + tableName;
                result.add(name);
            }
        }
        return result;
    }

    @Override
    public List<DbObjectDefinition> listTablesDefinitions(List<String> names) throws SQLException {
        String sql = """
            select table_schema, table_name
            from information_schema.tables
            where table_type in ('BASE TABLE')
            order by table_schema, table_name
            """;

        List<DbObjectDefinition> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var tableSchema = rs.getString("table_schema");
                var tableName = rs.getString("table_name");
                var name = tableSchema + "." + tableName;

                if (names.contains(name)) {
                    var definition = getTableDefinition(conn, tableSchema, tableName);
                    result.add(new DbObjectDefinition(name, definition));
                }
            }
        }
        return result;
    }

    @Override
    public List<String> listViewsNames() throws SQLException {
        String sql = """
            select table_schema, table_name
            from information_schema.views
            order by table_schema, table_name
            """;

        System.out.println("Executing POSTGRES sql:\n" + sql + "\n");

        List<String> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("table_schema") + "." + rs.getString("table_name");
                result.add(name);
            }
        }
        return result;
    }

    private static String getTableDefinition(Connection conn, String tableSchema, String tableName) throws SQLException {
        String sql = """
            SELECT
                n.nspname AS table_schema,
                c.relname AS table_name,
                a.attnum,
                a.attname || ' ' ||
                pg_catalog.format_type(a.atttypid, a.atttypmod) ||
                CASE WHEN a.attnotnull THEN ' NOT NULL' ELSE '' END ||
                CASE WHEN a.atthasdef THEN ' DEFAULT ' || pg_get_expr(d.adbin, d.adrelid) ELSE '' END\s
                AS column_def
            FROM pg_class c
            JOIN pg_namespace n ON n.oid = c.relnamespace
            JOIN pg_attribute a ON a.attrelid = c.oid
            LEFT JOIN pg_attrdef d ON d.adrelid = c.oid AND d.adnum = a.attnum
            WHERE c.relkind = 'r'
              and a.attnum > 0
              and not a.attisdropped
              and n.nspname = '%s'
              and c.relname = '%s'
            order by a.attnum
            """.formatted(tableSchema, tableName);

        List<String> columns = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                columns.add(rs.getString("column_def"));
            }
        }

        return "CREATE TABLE " + tableSchema + "." + tableName + " (\n    "
                + String.join(",\n    ", columns) + "\n);";
    }

    @Override
    public List<DbObjectDefinition> listViewsDefinitions(List<String> names) throws SQLException {
        String sql = """
            select table_schema, table_name, view_definition
            from information_schema.views
            order by table_schema, table_name
            """;

        List<DbObjectDefinition> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("table_schema") + "." + rs.getString("table_name");

                if (names.contains(name)) {
                    result.add(new DbObjectDefinition(name, rs.getString("view_definition")));
                }
            }
        }
        return result;
    }

    @Override
    public List<String> listFunctionsNames() throws SQLException {
        String sql = """
            select routine_schema, routine_name
            from information_schema.routines
            where routine_type = 'FUNCTION'
            order by routine_schema, routine_name
            """;

        List<String> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("routine_schema") + "." + rs.getString("routine_name");
                result.add(name);
            }
        }
        return result;
    }

    @Override
    public List<DbObjectDefinition> listFunctionsDefinitions(List<String> names) throws SQLException {
        String sql = """
            select routine_schema, routine_name, routine_definition
            from information_schema.routines
            where routine_type = 'FUNCTION'
            order by routine_schema, routine_name
            """;

        List<DbObjectDefinition> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("routine_schema") + "." + rs.getString("routine_name");

                if (names.contains(name)) {
                    result.add(new DbObjectDefinition(name, rs.getString("routine_definition")));
                }
            }
        }
        return result;
    }

    @Override
    public List<String> listIndexesNames() throws SQLException {
        String sql = """
            select schemaname, tablename, indexname
            from pg_indexes
            order by schemaname, tablename, indexname
            """;

        List<String> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("schemaname") + "." + rs.getString("tablename") + " " + rs.getString("indexname");
                result.add(name);
            }
        }
        return result;
    }

    @Override
    public List<DbObjectDefinition> listIndexesDefinitions(List<String> names) throws SQLException {
        String sql = """
            select schemaname, tablename, indexname, indexdef
            from pg_indexes
            order by schemaname, tablename, indexname
            """;

        List<DbObjectDefinition> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("schemaname") + "." + rs.getString("tablename") + " " + rs.getString("indexname");

                if (names.contains(name)) {
                    result.add(new DbObjectDefinition(name, rs.getString("indexdef")));
                }
            }
        }
        return result;
    }

    @Override
    public List<String> listProceduresNames() throws SQLException {
        String sql = """
            select routine_schema as schema_name, routine_name as procedure_name
            from information_schema.routines
            where routine_type = 'PROCEDURE'
            order by schema_name, procedure_name
            """;

        List<String> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("schema_name") + "." + rs.getString("procedure_name");
                result.add(name);
            }
        }
        return result;
    }

    @Override
    public List<DbObjectDefinition> listProceduresDefinitions(List<String> names) throws SQLException {
        String sql = """
            select routine_schema as schema_name, routine_name as procedure_name, routine_definition
            from information_schema.routines
            where routine_type = 'PROCEDURE'
            order by schema_name, procedure_name
            """;

        List<DbObjectDefinition> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("schema_name") + "." + rs.getString("procedure_name");

                if (names.contains(name)) {
                    result.add(new DbObjectDefinition(name, rs.getString("routine_definition")));
                }
            }
        }
        return result;
    }

    @Override
    public List<String> listPackageBodiesNames() throws SQLException {
        return List.of();
    }

    @Override
    public List<DbObjectDefinition> listPackageBodiesDefinitions(List<String> names) throws SQLException {
        return List.of();
    }

    @Override
    public List<String> listFunctionsSignatures() throws SQLException {
        String sql = """            
            SELECT
              n.nspname AS routine_schema,
              p.proname AS routine_name,
              p.proname || '(' || pg_get_function_identity_arguments(p.oid) || ')' AS full_signature
            FROM pg_proc p
            JOIN pg_namespace n ON (p.pronamespace = n.oid)
            WHERE p.prokind IN ('f')
            ORDER BY p.prokind, n.nspname, p.proname
            """;

        List<String> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("routine_schema") + "." + rs.getString("full_signature");
                result.add(name);
            }
        }
        return result;
    }

    @Override
    public List<String> listProceduresSignatures() throws SQLException {
        String sql = """            
            SELECT
              n.nspname AS routine_schema,
              p.proname AS routine_name,
              p.proname || '(' || pg_get_function_identity_arguments(p.oid) || ')' AS full_signature
            FROM pg_proc p
            JOIN pg_namespace n ON (p.pronamespace = n.oid)
            WHERE p.prokind IN ('p')
            ORDER BY p.prokind, n.nspname, p.proname
            """;

        List<String> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("routine_schema") + "." + rs.getString("full_signature");
                result.add(name);
            }
        }
        return result;
    }

}
