package workspace.vigiang.dao;

import workspace.vigiang.model.DbObjectDefinition;
import workspace.vigiang.model.Environment;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PostgresSchemaDAO implements DbSchemaDAO {

    @Override
    public List<String> listTables(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<DbObjectDefinition> listViews(Environment env) throws SQLException {
        String sql =
            "select table_schema, table_name, view_definition\n" +
            "from information_schema.views\n" +
            "where table_schema in ('api', 'conf', 'dash', 'evt', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')\n" +
            "order by table_schema, table_name";

        List<DbObjectDefinition> result = new ArrayList<>();
        try (Connection conn = getConnection(env);
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
    public List<DbObjectDefinition> listFunctions(Environment env) throws SQLException {
        String sql =
            "select routine_schema, routine_name, routine_definition\n" +
            "from information_schema.routines\n" +
            "where routine_type = 'FUNCTION'\n" +
            "  and routine_schema in ('api', 'conf', 'dash', 'evt', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')\n" +
            "order by routine_schema, routine_name";

        List<DbObjectDefinition> result = new ArrayList<>();
        try (Connection conn = getConnection(env);
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
    public List<DbObjectDefinition> listIndexes(Environment env) throws SQLException {
        String sql =
            "select schemaname, tablename, indexname, indexdef\n" +
            "from pg_indexes\n" +
            "where schemaname in ('api', 'conf', 'dash', 'evt', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')\n" +
            "order by schemaname, tablename, indexname";

        List<DbObjectDefinition> result = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("schemaname") + "." + rs.getString("tablename") + " " + rs.getString("indexname");
                result.add(new DbObjectDefinition(name, rs.getString("indexdef")));
            }
        }
        return result;
    }

}
