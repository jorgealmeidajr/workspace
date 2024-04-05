package workspace.vigiang.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VigiaNgDAO {

    public List<String[]> listFeatures(Environment env) throws SQLException {
        var credentials = CredentialsOracle.getCredentials(env);

        String sql =
            "select ID_FEATURE, ID_STATUS\n" + // , ID_DESCRICAO
            "from CFG_NG_FEATURE\n" +
            "order by ID_FEATURE";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(credentials.get("url"), credentials.get("username"), credentials.get("password"));
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
//                var descricao = (rs.getString("ID_DESCRICAO") == null) ? "NULL" : rs.getString("ID_DESCRICAO");
                String[] row = new String[] {
                        rs.getString("ID_FEATURE"),
                        rs.getString("ID_STATUS"),
//                        descricao
                };
                data.add(row);
            }
        }
        return data;
    }

    public List<String[]> listConfigurationValues(Environment env) throws SQLException {
        var credentials = CredentialsOracle.getCredentials(env);

        String sql =
            "select ID_PARAMETRO, DE_PARAMETRO, VL_PARAMETRO\n" +
            "from CFG_NG_SITE\n" +
            "order by ID_PARAMETRO";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(credentials.get("url"), credentials.get("username"), credentials.get("password"));
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var valor = (rs.getString("VL_PARAMETRO") == null) ? "NULL" : rs.getString("VL_PARAMETRO");
                String[] row = new String[] {
                        rs.getString("ID_PARAMETRO"),
//                        rs.getString("DE_PARAMETRO"),
//                        valor,
                };
                data.add(row);
            }
        }
        return data;
    }

}
