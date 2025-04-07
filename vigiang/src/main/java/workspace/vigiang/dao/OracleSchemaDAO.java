package workspace.vigiang.dao;

import workspace.vigiang.model.DbObjectDefinition;
import workspace.vigiang.model.Environment;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OracleSchemaDAO implements DbSchemaDAO {

    @Override
    public List<DbObjectDefinition> listTables(Environment env) throws SQLException {
        List<String> objects = listOracleObjects(env, "TABLE", "  and SUBSTR(ao.object_name, 0, 3) in ('ITC', 'CFG', 'LOG', 'SIT', 'SEG', 'OFC', 'PTB', 'QDS', 'LOC')");
        return objects.stream()
                .map((result) -> new DbObjectDefinition(result, ""))
                .collect(Collectors.toList());
    }

    private List<String> listOracleObjects(Environment env, String objectType, String where) throws SQLException {
        String sql =
            "select ao.owner, ao.object_name\n" +
            "from all_objects ao\n" +
            "where ao.owner like 'VIGIANG_" + env.getCarrier().toString() + "'\n" +
            "  and ao.object_type = '" + objectType + "'\n" +
            where + "\n" +
            "order by ao.owner, ao.object_name";

        List<String> result = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("owner") + "." + rs.getString("object_name");
                result.add(name);
            }
        }
        return result;
    }

    @Override
    public List<DbObjectDefinition> listViews(Environment env) throws SQLException {
        List<String> objects = listOracleObjects(env, "VIEW", "  and ao.object_name like 'VW_NG_%'");;
        return objects.stream()
                .map((result) -> new DbObjectDefinition(result, ""))
                .collect(Collectors.toList());
    }

    @Override
    public List<DbObjectDefinition> listFunctions(Environment env) throws SQLException {
        List<String> objects = listOracleObjects(env, "FUNCTION", "  and ao.object_name like 'FN_NG_%'");
        return objects.stream()
                .map((result) -> new DbObjectDefinition(result, ""))
                .collect(Collectors.toList());
    }

    @Override
    public List<DbObjectDefinition> listIndexes(Environment env) throws SQLException {
        List<String> objects = listOracleObjects(env, "INDEX", "  and SUBSTR(ao.object_name, 0, 3) in ('ITC', 'CFG', 'LOG', 'SIT', 'SEG', 'OFC', 'PTB', 'QDS', 'LOC')");
        return objects.stream()
                .map((result) -> new DbObjectDefinition(result, ""))
                .collect(Collectors.toList());
    }

    @Override
    public List<DbObjectDefinition> listProcedures(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<DbObjectDefinition> listPackageBodies(Environment env) throws SQLException {
        return List.of();
    }

}
