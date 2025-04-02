package workspace.vigiang.dao;

import workspace.vigiang.model.Environment;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OracleSchemaDAO implements DbSchemaDAO {

    @Override
    public List<String> listTables(Environment env) throws SQLException {
        return listOracleObjects(env, "TABLE");
    }

    private List<String> listOracleObjects(Environment env, String objectType) throws SQLException {
        String sql =
            "select ao.owner, ao.object_name\n" +
            "from all_objects ao\n" +
            "where ao.owner like 'VIGIANG_" + env.getCarrier().toString() + "'\n" +
            "  and ao.object_type = '" + objectType + "'\n" +
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
    public List<String> listViews(Environment env) throws SQLException {
        return listOracleObjects(env, "VIEW");
    }

}
