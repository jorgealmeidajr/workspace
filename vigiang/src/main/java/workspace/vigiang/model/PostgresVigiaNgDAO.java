package workspace.vigiang.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostgresVigiaNgDAO implements VigiaNgDAO {

    @Override
    public List<String[]> listFeatures(Environment env, String[] columns) throws SQLException {
        var selectColumns = String.join(", ", columns);

        String sql =
            "select " + selectColumns + "\n" +
            "from conf.feature\n" +
            "order by feature";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
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
        String sql =
            "select parameter_id, parameter_description, value\n" +
            "from conf.site\n" +
            "order by parameter_id";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var value = (rs.getString("value") == null) ? "NULL" : rs.getString("value");
                String[] row = new String[] {
                    rs.getString("parameter_id"),
                    rs.getString("parameter_description"),
                    value,
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listModules(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<String[]> listPrivileges(Environment env) throws SQLException {
        String sql =
            "select module_id, \"name\"\n" +
            "from sec.privilege\n" +
            "order by name";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    (rs.getString("module_id") == null) ? "NULL" : rs.getString("module_id"),
                    rs.getString("name"),
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listProfiles(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<String[]> listFilterQueries(Environment env) throws SQLException {
        String sql =
            "select module, label, value\n" +
            "from conf.filterquery\n" +
            "order by module, label";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("module"),
                    rs.getString("label"),
                    rs.getString("value")
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listZoneInterceptions(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<String[]> listValidationRules(Environment env) throws SQLException {
        String sql =
            "select module, valid_rules\n" +
            "from conf.validatrules\n" +
            "order by module";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("module"),
                    rs.getString("valid_rules"),
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listQdsValidationRules(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<String[]> listEmailTemplates(Environment env) throws SQLException {
        String sql =
            "select carrier_id, service_type, email_subject, service_name, attach_name, email_from, email_to, email_body\n" +
            "from conf.service_email\n" +
            "order by carrier_id, service_type";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("carrier_id"),
                    rs.getString("service_type"),
                    rs.getString("email_subject"),
                    rs.getString("service_name"),
                    rs.getString("attach_name"),
                    rs.getString("email_from"),
                    rs.getString("email_to"),
                    rs.getString("email_body"),
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listReports(Environment env) throws SQLException {
        String sql =
            "select id, report_id, report_type, carrier_id\n" +
            "from conf.report\n" +
            "order by carrier_id, report_id";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("id"),
                    rs.getString("report_id"),
                    rs.getString("report_type"),
                    rs.getString("carrier_id"),
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listConfigurationReports(Environment env) throws SQLException {
        String sql =
            "select cfg.carrier_id, cfg.parameter_id, cfg.parameter_description, cfg.value, rel.id as id, rel.report_id\n" +
            "from conf.site cfg\n" +
            "left join conf.report rel on (CAST(nullif(cfg.value, '') AS integer) = rel.id)\n" +
            "where cfg.parameter_id like '%report.id'\n" +
            "order by cfg.carrier_id, cfg.parameter_id";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("carrier_id"),
                    rs.getString("parameter_id"),
                    rs.getString("parameter_description"),
                    rs.getString("value"),
                    rs.getString("id"),
                    rs.getString("report_id"),
                };
                data.add(row);
            }
        }
        return data;
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
