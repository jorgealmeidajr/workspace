package workspace.vigiang.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VigiaNgDAO {

    public List<String[]> listFeatures(Environment env, String[] columns) throws SQLException {
        var credentials = CredentialsOracle.getCredentials(env);
        var selectColumns = String.join(", ", columns);

        String sql =
            "select " + selectColumns + "\n" +
            "from CFG_NG_FEATURE\n" +
            "order by ID_FEATURE";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(credentials.get("url"), credentials.get("username"), credentials.get("password"));
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var descricao = (rs.getString("ID_DESCRICAO") == null) ? "NULL" : rs.getString("ID_DESCRICAO");
                String[] row = new String[] {
                    rs.getString("ID_FEATURE"),
                    rs.getString("ID_STATUS"),
                    descricao
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
                    rs.getString("DE_PARAMETRO"),
                    valor,
                };
                data.add(row);
            }
        }
        return data;
    }

    public List<String[]> listModules(Environment env) throws SQLException {
        var credentials = CredentialsOracle.getCredentials(env);

        String sql =
            "select ID_CHAVE, ID_STATUS, ID_TIPO\n" +
            "from CFG_MODULO\n" +
            "order by ID_CHAVE";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(credentials.get("url"), credentials.get("username"), credentials.get("password"));
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("ID_CHAVE"),
                    rs.getString("ID_STATUS"),
                    rs.getString("ID_TIPO"),
                };
                data.add(row);
            }
        }
        return data;
    }

    public List<String[]> listPrivileges(Environment env) throws SQLException {
        var credentials = CredentialsOracle.getCredentials(env);

        String sql =
            "select \n" +
            "  ID_CHAVE as NM_MODULO, ID_STATUS as STATUS_MODULO, NM_PRIVILEGIO\n" +
            "from SEG_PRIVILEGIO t1\n" +
            "left join CFG_MODULO t2 on (t1.CD_MODULO = t2.CD_MODULO)\n" +
            "order by NM_MODULO, NM_PRIVILEGIO";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(credentials.get("url"), credentials.get("username"), credentials.get("password"));
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                        (rs.getString("NM_MODULO") == null) ? "NULL" : rs.getString("NM_MODULO"),
                        (rs.getString("STATUS_MODULO") == null) ? "NULL" : rs.getString("STATUS_MODULO"),
                        rs.getString("NM_PRIVILEGIO"),
                };
                data.add(row);
            }
        }
        return data;
    }

    public List<String[]> listProfiles(Environment env) throws SQLException {
        var credentials = CredentialsOracle.getCredentials(env);

        String sql =
            "select t3.NM_PERFIL, t2.NM_PRIVILEGIO, t4.ID_CHAVE as NM_MODULO\n" +
            "from SEG_PERFIL_PRIVILEGIO t1\n" +
            "join SEG_PRIVILEGIO t2 on (t1.CD_PRIVILEGIO = t2.CD_PRIVILEGIO)\n" +
            "join SEG_PERFIL t3 on (t1.CD_PERFIL = t3.CD_PERFIL)\n" +
            "left join CFG_MODULO t4 on (t2.CD_MODULO = t4.CD_MODULO)\n" +
            "where CD_OPERADORA = 1\n" +
            "  and t3.NM_PERFIL in ('Administrador', 'Admin', 'ADMIN')\n" +
            "order by t3.NM_PERFIL, t2.NM_PRIVILEGIO";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(credentials.get("url"), credentials.get("username"), credentials.get("password"));
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                        rs.getString("NM_PERFIL"),
                        rs.getString("NM_PRIVILEGIO"),
                        (rs.getString("NM_MODULO") == null) ? "NULL" : rs.getString("NM_MODULO"),
                };
                data.add(row);
            }
        }
        return data;
    }

    public List<String[]> listFilterQueries(Environment env) throws SQLException {
        var credentials = CredentialsOracle.getCredentials(env);

        String sql =
            "select * from CFG_NG_FILTERQUERY\n" +
            "order by MODULE, LABEL";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(credentials.get("url"), credentials.get("username"), credentials.get("password"));
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("MODULE"),
                    rs.getString("LABEL"),
                    rs.getString("VALUE")
                };
                data.add(row);
            }
        }
        return data;
    }

}
