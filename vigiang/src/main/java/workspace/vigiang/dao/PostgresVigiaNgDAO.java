package workspace.vigiang.dao;

import workspace.vigiang.model.EmailTemplate;
import workspace.vigiang.model.Environment;
import workspace.vigiang.model.ReportTemplate;

import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class PostgresVigiaNgDAO implements VigiaNgDAO {

    @Override
    public List<String[]> listFeatures(Environment env) throws SQLException {
        String sql =
            "select feature, status, description\n" +
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
            "select carrier_id, parameter_id, parameter_description, value\n" +
            "from conf.site\n" +
            "order by carrier_id, parameter_id";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var value = (rs.getString("value") == null) ? "NULL" : rs.getString("value");
                String[] row = new String[] {
                    rs.getString("carrier_id"),
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
        String sql =
            "select t3.carrier_id, t3.\"name\" as \"profile_name\", t2.\"name\" as \"privilege_name\", t2.module_id\n" +
            "from sec.profile_privilege t1\n" +
            "join sec.privilege t2 on (t1.privilege_id = t2.id)\n" +
            "join sec.profile t3 on (t1.profile_id = t3.id)\n" +
            "where LOWER(t3.\"name\") in ('administrador', 'admin', 'autoridades')\n" +
            "order by t3.carrier_id, t3.\"name\", t2.\"name\"";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("carrier_id"),
                    rs.getString("profile_name"),
                    rs.getString("privilege_name"),
                    (rs.getString("module_id") == null) ? "NULL" : rs.getString("module_id")
                };
                data.add(row);
            }
        }
        return data;
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
        try (Connection conn = getConnection(env);
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
    public List<String[]> listValidationRules(Environment env) throws SQLException {
        String sql =
            "select t1.carrier_id, t2.\"name\" as \"carrier_name\", t1.\"module\", t1.valid_rules\n" +
            "from conf.validatrules t1\n" +
            "left join conf.carrier t2 on (t1.carrier_id = t2.id)\n" +
            "order by t1.carrier_id, t1.\"module\"";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
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
    public List<String[]> listQdsValidationRules(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<EmailTemplate> listEmailTemplates(Environment env) throws SQLException {
        String sql =
            "select carrier_id, service_type, email_subject, service_name, attach_name, email_to, email_from, email_body, t2.\"name\" as \"carrier_name\"\n" +
            "from conf.service_email t1\n" +
            "left join conf.carrier t2 on (t1.carrier_id = t2.id)\n" +
            "order by carrier_id, service_type";

        List<EmailTemplate> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
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
    public List<ReportTemplate> listReportTemplates(Environment env) throws SQLException {
        String sql =
            "select t1.id, report_id, report_type, carrier_id, t2.\"name\" as \"carrier_name\", file\n" +
            "from conf.report t1\n" +
            "left join conf.carrier t2 on (t1.carrier_id = t2.id)\n" +
            "order by carrier_id, report_id";

        List<ReportTemplate> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
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
    public List<String[]> listCarriers(Environment env) throws SQLException {
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
        try (Connection conn = getConnection(env);
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
    public List<String[]> listZones(Environment env) throws SQLException {
        String sql =
            "select\n" +
            "  t2.id as \"carrier_id\", t2.\"name\" as \"carrier_name\",\n" +
            "  t1.id as \"zone_monit_id\", t1.\"name\" as \"zone_monit_name\", t1.\"comments\", t1.active\n" +
            "from conf.zone_monit t1\n" +
            "left join conf.carrier t2 on (t1.carrier_id = t2.id)\n" +
            "where lower(t1.\"name\") not like '%test%'\n" +
            "order by t1.carrier_id, t1.id";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
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

}
