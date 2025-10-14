package workspace.vigiang.dao;

import workspace.vigiang.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class PostgresVigiaNgDAO implements VigiaNgDAO {

    private final DatabaseCredentials databaseCredentials;

    public PostgresVigiaNgDAO(DatabaseCredentials databaseCredentials) {
        this.databaseCredentials = databaseCredentials;
    }

    @Override
    public List<Feature> listFeatures() throws SQLException {
        String sql =
            "select id, feature, status, description\n" +
            "from conf.feature\n" +
            "order by feature";

        List<Feature> data = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var description = (rs.getString("description") == null) ? "NULL" : rs.getString("description");

                var feature = new Feature(
                    rs.getInt("id"),
                    rs.getString("feature"),
                    rs.getString("status"),
                    description
                );

                data.add(feature);
            }
        }
        return data;
    }

    @Override
    public List<Configuration> listConfigurationValues() throws SQLException {
        String sql =
            "select id, carrier_id, parameter_id, parameter_description, value\n" +
            "from conf.site\n" +
            "order by carrier_id, parameter_id";

        List<Configuration> data = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var value = (rs.getString("value") == null) ? "NULL" : rs.getString("value");
                var row = new Configuration(
                    rs.getInt("id"),
                    rs.getString("carrier_id"),
                    rs.getString("parameter_id"),
                    rs.getString("parameter_description"),
                    value
                );
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listPrivileges() throws SQLException {
        String sql =
            "select t1.id, t1.privilegeid, t1.\"name\"\n" +
            "from sec.privilege t1\n" +
            "order by t1.id desc";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                Integer id = rs.getInt("id");
                String[] row = new String[] {
                    id.toString(),
                    rs.getString("privilegeid"),
                    rs.getString("name"),
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listProfiles(DatabaseCredentials databaseCredentials) throws SQLException {
        String sql =
            "select t3.carrier_id, t3.\"name\" as \"profile_name\", t2.\"name\" as \"privilege_name\"\n" +
            "from sec.profile_privilege t1\n" +
            "join sec.privilege t2 on (t1.privilege_id = t2.id)\n" +
            "join sec.profile t3 on (t1.profile_id = t3.id)\n" +
            "where LOWER(t3.\"name\") in ('administrador', 'admin', 'autoridades')\n" +
            "order by t3.carrier_id, t3.\"name\", t2.\"name\"";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("carrier_id"),
                    rs.getString("profile_name"),
                    rs.getString("privilege_name")
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listFilterQueries(DatabaseCredentials databaseCredentials) throws SQLException {
        String sql =
            "select module, label, value\n" +
            "from conf.filterquery\n" +
            "order by module, label";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
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
    public List<String[]> listZoneInterceptions(DatabaseCredentials databaseCredentials) throws SQLException {
        String sql =
            "select\n" +
            "  t4.id as \"carrier_id\", t4.\"name\" as \"carrier_name\",\n" +
            "  t3.\"name\" as \"network_element_type_name\", t2.\"name\" as \"target_type_name\",\n" +
            "  t1.itc_form_visible, t1.visible, t1.rules\n" +
            "from conf.tp_zone_tp_vl_itc t1\n" +
            "join conf.target_type t2 on (t1.targettype_id = t2.id)\n" +
            "join conf.network_element_type t3 on (t1.networkelementtype_id  = t3.id)\n" +
            "left join conf.carrier t4 on (t1.carrier_id = t4.id)\n" +
            "order by t4.id, t3.\"name\", t2.\"name\"";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("carrier_id"),
                    rs.getString("carrier_name"),
                    rs.getString("network_element_type_name"),
                    rs.getString("target_type_name"),
                    rs.getString("itc_form_visible"),
                    rs.getString("visible"),
                    rs.getString("rules")
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listValidationRules(DatabaseCredentials databaseCredentials) throws SQLException {
        String sql =
            "select t1.carrier_id, t2.\"name\" as \"carrier_name\", t1.\"module\", t1.valid_rules\n" +
            "from conf.validatrules t1\n" +
            "left join conf.carrier t2 on (t1.carrier_id = t2.id)\n" +
            "order by t1.carrier_id, t1.\"module\"";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("carrier_id"),
                    rs.getString("carrier_name"),
                    rs.getString("module"),
                    rs.getString("valid_rules"),
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listQdsValidationRules(DatabaseCredentials databaseCredentials) throws SQLException {
        return List.of();
    }

    @Override
    public List<EmailTemplate> listEmailTemplates(DatabaseCredentials databaseCredentials) throws SQLException {
        String sql =
            "select carrier_id, service_type, email_subject, service_name, attach_name, email_to, email_from, email_body, t2.\"name\" as \"carrier_name\"\n" +
            "from conf.service_email t1\n" +
            "left join conf.carrier t2 on (t1.carrier_id = t2.id)\n" +
            "order by carrier_id, service_type";

        List<EmailTemplate> data = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                EmailTemplate row = new EmailTemplate(
                    rs.getString("carrier_id"),
                    rs.getString("service_type"),
                    rs.getString("email_subject"),
                    rs.getString("service_name"),
                    rs.getString("attach_name"),
                    rs.getString("email_from"),
                    rs.getString("email_to"),
                    rs.getString("email_body"),
                    rs.getString("carrier_name"));
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<ReportTemplate> listReportTemplates(DatabaseCredentials databaseCredentials) throws SQLException {
        String sql =
            "select t1.id, report_id, report_type, carrier_id, t2.\"name\" as \"carrier_name\", file\n" +
            "from conf.report t1\n" +
            "left join conf.carrier t2 on (t1.carrier_id = t2.id)\n" +
            "order by carrier_id, report_id";

        List<ReportTemplate> data = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var blob = rs.getString("file");
                byte[] template = new byte[] {};
                if (blob != null) {
                    template = Base64.getDecoder().decode(blob);
                }

                ReportTemplate row = new ReportTemplate(
                    rs.getString("id"),
                    rs.getString("report_id"),
                    rs.getString("report_type"),
                    rs.getString("carrier_id"),
                    rs.getString("carrier_name"),
                    template
                );
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listConfigurationReports(DatabaseCredentials databaseCredentials) throws SQLException {
        String sql =
            "select cfg.carrier_id, cfg.parameter_id, cfg.parameter_description, cfg.value, rel.id as id, rel.report_id\n" +
            "from conf.site cfg\n" +
            "left join conf.report rel on (CAST(nullif(cfg.value, '') AS integer) = rel.id)\n" +
            "where cfg.parameter_id like '%report.id'\n" +
            "order by cfg.carrier_id, cfg.parameter_id";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
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
    public List<String[]> listCarriers(DatabaseCredentials databaseCredentials) throws SQLException {
        String sql =
            "select\n" +
            "  id, \"name\",\n" +
            "  smtp_host, smtp_user, smpt_password, smpt_account, smtp_port,\n" +
            "  \"comments\", muti_carrier, regex, local_images, ds_regex, api_key_maps, \"token\",\n" +
            "  im_logo, im_logo_footer\n" +
            "from conf.carrier\n" +
            "where (lower(\"name\") not like '%test%' and lower(\"name\") not like '%robot%')\n" +
            "order by id";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("smtp_host"),
                    rs.getString("smtp_user"),
                    rs.getString("smpt_password"),
                    rs.getString("smpt_account"),
                    rs.getString("smtp_port"),
                    rs.getString("comments"),
                    rs.getString("muti_carrier"),
                    rs.getString("regex"),
                    rs.getString("local_images"),
                    rs.getString("ds_regex"),
                    rs.getString("api_key_maps"),
                    rs.getString("token"),
                    "", "" // TODO: convert bytea to string
//                    rs.getBinaryStream("im_logo"),
//                    rs.getBinaryStream("im_logo_footer")
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listZones(DatabaseCredentials databaseCredentials) throws SQLException {
        String sql =
            "select\n" +
            "  t2.id as \"carrier_id\", t2.\"name\" as \"carrier_name\",\n" +
            "  t1.id as \"zone_monit_id\", t1.\"name\" as \"zone_monit_name\", t1.\"comments\", t1.active\n" +
            "from conf.zone_monit t1\n" +
            "left join conf.carrier t2 on (t1.carrier_id = t2.id)\n" +
            "where lower(t1.\"name\") not like '%test%'\n" +
            "order by t1.carrier_id, t1.id";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("carrier_id"),
                    rs.getString("carrier_name"),
                    rs.getString("zone_monit_id"),
                    rs.getString("zone_monit_name"),
                    rs.getString("comments"),
                    rs.getString("active")
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public void updateTemplateReport(DatabaseCredentials databaseCredentials, String carrierId, String reportId, String reportName, byte[] fileBytes) throws SQLException {
        String originalContent = getTemplateReportContent(databaseCredentials, carrierId, reportId, reportName);
        String base64Content = Base64.getEncoder().encodeToString(fileBytes);
        if (originalContent.equals(base64Content)) return;

        String sql =
            "update conf.report set file = ?\n" +
            "where carrier_id = ?\n" +
            "  and id = ?\n" +
            "  and report_id = ?";

        try (Connection conn = getConnection(databaseCredentials);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, base64Content);
            stmt.setInt(2, Integer.parseInt(carrierId));
            stmt.setInt(3, Integer.parseInt(reportId));
            stmt.setString(4, reportName);
            int updated = stmt.executeUpdate();
            System.out.println("Updated conf.report: carrierId=" + carrierId + ", reportId=" + reportId + ", reportName=" + reportName + ", rows=" + updated);
        }
    }

    @Override
    public void updateConfigurationValue(DatabaseCredentials databaseCredentials, Configuration configuration, String newValue) throws SQLException {
        String sql =
            "update conf.site set value = ?\n" +
            "where id = ?\n" +
            "  and parameter_id = ?\n" +
            "  and carrier_id = ?";

        try (Connection conn = getConnection(databaseCredentials);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newValue);
            stmt.setInt(2, configuration.getCode());
            stmt.setString(3, configuration.getId());
            stmt.setInt(4, Integer.parseInt(configuration.getCarrierId()));
            int updated = stmt.executeUpdate();
            System.out.println("Updated conf.site, rows=" + updated);
        }
    }

    @Override
    public void insertPrivileges(DatabaseCredentials databaseCredentials, List<String> privilegeIds) throws SQLException {
        // TODO: missing implementation
    }

    @Override
    public void associatePrivileges(DatabaseCredentials targetDb, int targetPrivilegeId) throws SQLException {
        // TODO: missing implementation
    }

    private String getTemplateReportContent(DatabaseCredentials databaseCredentials, String carrierId, String reportId, String reportName) throws SQLException {
        String sql =
            "select file from conf.report\n" +
            "where carrier_id = ?\n" +
            "  and id = ?\n" +
            "  and report_id = ?";

        try (Connection conn = getConnection(databaseCredentials);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(carrierId));
            stmt.setInt(2, Integer.parseInt(reportId));
            stmt.setString(3, reportName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("file");
                }
            }
        }
        return "";
    }

}
