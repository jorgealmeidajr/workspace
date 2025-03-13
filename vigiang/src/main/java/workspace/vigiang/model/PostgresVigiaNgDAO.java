package workspace.vigiang.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresVigiaNgDAO implements VigiaNgDAO {

    @Override
    public List<String[]> listFeatures(Environment env, String[] columns) throws SQLException {
        var credentials = CredentialsOracle.getCredentials(env);
        var selectColumns = String.join(", ", columns);

        String sql =
            "select " + selectColumns + "\n" +
            "from conf.feature\n" +
            "order by feature";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(credentials.get("url"), credentials.get("username"), credentials.get("password"));
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var description = (rs.getString("description") == null) ? "NULL" : rs.getString("description");
                String[] row = new String[] {
                    rs.getString("feature"),
                    rs.getString("status"),
                    description
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listConfigurationValues(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<String[]> listModules(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<String[]> listPrivileges(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<String[]> listProfiles(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<String[]> listFilterQueries(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<String[]> listZoneInterceptions(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<String[]> listValidationRules(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<String[]> listQdsValidationRules(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<String[]> listEmailTemplates(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<String[]> listReports(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<String[]> listConfigurationReports(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<Object[]> listReportTemplates(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<String> listObjects(Environment env, String objectType) throws SQLException {
        return List.of();
    }

    @Override
    public List<String[]> listDdlStatements(Environment env, List<String> objectNames, String objectType) throws SQLException {
        return List.of();
    }

}
