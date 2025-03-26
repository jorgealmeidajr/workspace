package workspace.vigiang.model;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OracleVigiaNgDAO implements VigiaNgDAO {

    @Override
    public List<String[]> listFeatures(Environment env) throws SQLException {
        String sql =
            "select ID_FEATURE, ID_STATUS, ID_DESCRICAO\n" +
            "from CFG_NG_FEATURE\n" +
            "order by ID_FEATURE";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
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

    @Override
    public List<String[]> listConfigurationValues(Environment env) throws SQLException {
        String sql =
            "select CD_OPERADORA, ID_PARAMETRO, DE_PARAMETRO, VL_PARAMETRO\n" +
            "from CFG_NG_SITE\n" +
            "order by CD_OPERADORA, ID_PARAMETRO";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var value = (rs.getString("VL_PARAMETRO") == null) ? "NULL" : rs.getString("VL_PARAMETRO");
                String[] row = new String[] {
                    rs.getString("CD_OPERADORA"),
                    rs.getString("ID_PARAMETRO"),
                    rs.getString("DE_PARAMETRO"),
                    value,
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listModules(Environment env) throws SQLException {
        String sql =
            "select ID_CHAVE, ID_STATUS, ID_TIPO\n" +
            "from CFG_MODULO\n" +
            "order by ID_CHAVE";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
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

    @Override
    public List<String[]> listPrivileges(Environment env) throws SQLException {
        String sql =
            "select ID_CHAVE as NM_MODULO, ID_STATUS as STATUS_MODULO, t1.NM_PRIVILEGIO\n" +
            "from SEG_PRIVILEGIO t1\n" +
            "left join CFG_MODULO t2 on (t1.CD_MODULO = t2.CD_MODULO)\n" +
            "order by NM_MODULO, t1.NM_PRIVILEGIO";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
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

    @Override
    public List<String[]> listProfiles(Environment env) throws SQLException {
        String sql =
            "select t3.CD_OPERADORA, t3.NM_PERFIL, t2.NM_PRIVILEGIO, t2.CD_MODULO, t4.ID_CHAVE as NM_MODULO\n" +
            "from SEG_PERFIL_PRIVILEGIO t1\n" +
            "join SEG_PRIVILEGIO t2 on (t1.CD_PRIVILEGIO = t2.CD_PRIVILEGIO)\n" +
            "join SEG_PERFIL t3 on (t1.CD_PERFIL = t3.CD_PERFIL)\n" +
            "left join CFG_MODULO t4 on (t2.CD_MODULO = t4.CD_MODULO)\n" +
            "where LOWER(t3.NM_PERFIL) in ('administrador', 'admin', 'autoridades')\n" +
            "order by t3.CD_OPERADORA, t3.NM_PERFIL, t2.NM_PRIVILEGIO";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("CD_OPERADORA"),
                    rs.getString("NM_PERFIL"),
                    rs.getString("NM_PRIVILEGIO"),
                    (rs.getString("CD_MODULO") == null) ? "NULL" : rs.getString("CD_MODULO"),
                    (rs.getString("NM_MODULO") == null) ? "NULL" : rs.getString("NM_MODULO"),
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listFilterQueries(Environment env) throws SQLException {
        String sql =
            "select * from CFG_NG_FILTERQUERY\n" +
            "order by MODULE, LABEL";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
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

    @Override
    public List<String[]> listZoneInterceptions(Environment env) throws SQLException {
        String sql =
            "select\n" +
            "  t4.CD_OPERADORA, t4.NM_OPERADORA,\n" +
            "  t3.NM_ZONA_MONIT, t2.NM_TIPO_VALOR_INTERCEPTADO,\n" +
            "  t1.SN_VISIVEL_CAD_ITC, t1.SN_VISIVEL_LOTE, t1.NM_REGRAS\n" +
            "from CFG_TP_ZONA_TP_VL_ITC t1\n" +
            "join CFG_TIPO_VALOR_INTERCEPTADO t2 on (t1.CD_TIPO_VALOR_INTERCEPTADO = t2.CD_TIPO_VALOR_INTERCEPTADO)\n" +
            "join CFG_ZONA_MONIT t3 on (t1.CD_TIPO_CENTRAL = t3.CD_TIPO_CENTRAL)\n" +
            "left join CFG_OPERADORA t4 on (t1.CD_OPERADORA = t4.CD_OPERADORA)\n" +
            "order by t4.CD_OPERADORA, t3.NM_ZONA_MONIT, t2.NM_TIPO_VALOR_INTERCEPTADO";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("CD_OPERADORA"),
                    rs.getString("NM_OPERADORA"),
                    rs.getString("NM_ZONA_MONIT"),
                    rs.getString("NM_TIPO_VALOR_INTERCEPTADO"),
                    rs.getString("SN_VISIVEL_CAD_ITC"),
                    rs.getString("SN_VISIVEL_LOTE"),
                    rs.getString("NM_REGRAS"),
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listValidationRules(Environment env) throws SQLException {
        String sql =
            "select t1.CD_OPERADORA, t2.NM_OPERADORA, t1.MODULO, t1.VALID_RULES\n" +
            "from CFG_NG_VALIDATRULES t1\n" +
            "left join CFG_OPERADORA t2 on (t1.CD_OPERADORA = t2.CD_OPERADORA)\n" +
            "order by t1.CD_OPERADORA, t1.MODULO";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("CD_OPERADORA"),
                    rs.getString("NM_OPERADORA"),
                    rs.getString("MODULO"),
                    rs.getString("VALID_RULES"),
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listQdsValidationRules(Environment env) throws SQLException {
        String sql =
            "select ID_TIPO_NUMERO_QDS, NM_CHAVE, TP_CONSULTA, SN_VOUCHER_DATE, VALID_RULES\n" +
            "from CFG_TIPO_NUMERO_QDS\n" +
            "order by ID_TIPO_NUMERO_QDS";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("ID_TIPO_NUMERO_QDS"),
                    rs.getString("NM_CHAVE"),
                    rs.getString("TP_CONSULTA"),
                    rs.getString("SN_VOUCHER_DATE"),
                    rs.getString("VALID_RULES"),
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<EmailTemplate> listEmailTemplates(Environment env) throws SQLException {
        String sql =
            "select CD_OPERADORA, ID_TIPO_SERVICO, DE_ASSUNTO, DE_NOME, DE_NOME_ARQUIVO, DE_REMETENTE, DE_DESTINATARIO, DE_TEXTO\n" +
            "from CFG_EMAIL_SERVICOS\n" +
            "order by CD_OPERADORA, ID_TIPO_SERVICO";

        List<EmailTemplate> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                EmailTemplate row = new EmailTemplate(
                    rs.getString("CD_OPERADORA"),
                    rs.getString("ID_TIPO_SERVICO"),
                    rs.getString("DE_ASSUNTO"),
                    rs.getString("DE_NOME"),
                    rs.getString("DE_NOME_ARQUIVO"),
                    rs.getString("DE_REMETENTE"),
                    rs.getString("DE_DESTINATARIO"),
                    rs.getString("DE_TEXTO"));
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listReports(Environment env) throws SQLException {
        String sql =
            "select CD_RELATORIO, ID_RELATORIO, TP_RELATORIO, CD_OPERADORA\n" +
            "from CFG_RELATORIO\n" +
            "order by CD_OPERADORA, ID_RELATORIO";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("CD_RELATORIO"),
                    rs.getString("ID_RELATORIO"),
                    rs.getString("TP_RELATORIO"),
                    rs.getString("CD_OPERADORA"),
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<String[]> listConfigurationReports(Environment env) throws SQLException {
        String sql =
            "select cfg.CD_OPERADORA, cfg.ID_PARAMETRO, cfg.DE_PARAMETRO, cfg.VL_PARAMETRO, rel.CD_RELATORIO, rel.ID_RELATORIO\n" +
            "from CFG_NG_SITE cfg\n" +
            "left join CFG_RELATORIO rel on (cfg.VL_PARAMETRO = rel.CD_RELATORIO)\n" +
            "where cfg.ID_PARAMETRO like '%report.id'\n" +
            "order by cfg.CD_OPERADORA, cfg.ID_PARAMETRO";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("CD_OPERADORA"),
                    rs.getString("ID_PARAMETRO"),
                    rs.getString("DE_PARAMETRO"),
                    rs.getString("VL_PARAMETRO"),
                    rs.getString("CD_RELATORIO"),
                    rs.getString("ID_RELATORIO"),
                };
                data.add(row);
            }
        }
        return data;
    }

    @Override
    public List<Object[]> listReportTemplates(Environment env) throws SQLException {
        String sql =
            "select * from CFG_RELATORIO\n" +
            "where CD_OPERADORA = 1\n" +
            "  and TP_RELATORIO in ('xls', 'pdf')\n" +
            "order by CD_RELATORIO";

        List<Object[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var blob = rs.getBlob("DC_RELATORIO");

                if (blob != null) {
                    var bytes = blob.getBytes(1l, (int)blob.length());

                    Object[] row = new Object[] {
                        rs.getString("CD_RELATORIO"),
                        rs.getString("ID_RELATORIO"),
                        rs.getString("TP_RELATORIO"),
                        bytes,
                    };
                    data.add(row);
                }
            }
        }
        return data;
    }

    @Override
    public List<String> listObjects(Environment env, String objectType) throws SQLException {
        String sql =
            "select ao.owner, ao.object_type, ao.object_name, ao.status\n" +
            "from all_objects ao\n" +
            "where ao.owner like 'VIGIANG_" + env + "'\n" +
            "  and ao.object_type = '" + objectType + "'\n" +
            "order by ao.owner, ao.object_type, ao.object_name";

        List<String> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                data.add(rs.getString("OBJECT_NAME"));
            }
        }
        return data;
    }

    @Override
    public List<String[]> listDdlStatements(Environment env, List<String> objectNames, String objectType) throws SQLException {
        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env)) {
            int count = 0;
            for (String objectName : objectNames) {
                String sql = "SELECT DBMS_METADATA.GET_DDL('" + objectType + "', '" + objectName + "') as DDL FROM DUAL";

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while(rs.next()) {
                        String[] row = new String[] {
                            objectName,
                            rs.getString("DDL")
                        };
                        data.add(row);
                    }
                }
                count++;
                if (count > 10) break; // todo: this routine takes time, in a large database
            }
        }

        return data;
    }

    @Override
    public List<String[]> listCarriers(Environment env) throws SQLException {
        String sql =
            "select\n" +
            "  CD_OPERADORA, NM_OPERADORA,\n" +
            "  ID_SMTP_HOST, ID_SMTP_USER, ID_SMPT_PASSWORD, ID_SMPT_ACCOUNT, ID_SMTP_PORT,\n" +
            "  DE_COMENTARIO, SN_MULTIOPERADORA, ID_REGEX, DS_LOCAL_IMAGENS, DS_REGEX, ID_API_KEY_MAPS, SN_TOKEN,\n" +
            "  IM_LOGO, IM_LOGO_FOOTER\n" +
            "from CFG_OPERADORA\n" +
            "where (lower(NM_OPERADORA) not like '%test%' and lower(NM_OPERADORA) not like '%robot%')\n" +
            "order by CD_OPERADORA";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("CD_OPERADORA"),
                    rs.getString("NM_OPERADORA"),
                    rs.getString("ID_SMTP_HOST"),
                    rs.getString("ID_SMTP_USER"),
                    rs.getString("ID_SMPT_PASSWORD"),
                    rs.getString("ID_SMPT_ACCOUNT"),
                    rs.getString("ID_SMTP_PORT"),
                    rs.getString("DE_COMENTARIO"),
                    rs.getString("SN_MULTIOPERADORA"),
                    rs.getString("ID_REGEX"),
                    rs.getString("DS_LOCAL_IMAGENS"),
                    rs.getString("DS_REGEX"),
                    rs.getString("ID_API_KEY_MAPS"),
                    rs.getString("SN_TOKEN"),
                    getBlobAsString(rs.getBlob("IM_LOGO")),
                    getBlobAsString(rs.getBlob("IM_LOGO_FOOTER"))
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
            "  t2.CD_OPERADORA, t2.NM_OPERADORA,\n" +
            "  t1.CD_ZONA_MONIT, t1.NM_ZONA_MONIT, t1.DE_COMENTARIOS, t1.IN_ATIVO\n" +
            "from CFG_ZONA_MONIT t1\n" +
            "left join CFG_OPERADORA t2 on (t1.CD_OPERADORA = t2.CD_OPERADORA)\n" +
            "where lower(t1.NM_ZONA_MONIT) not like '%test%'\n" +
            "order by t1.CD_OPERADORA, t1.CD_ZONA_MONIT";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                String[] row = new String[] {
                    rs.getString("CD_OPERADORA"),
                    rs.getString("NM_OPERADORA"),
                    rs.getString("CD_ZONA_MONIT"),
                    rs.getString("NM_ZONA_MONIT"),
                    rs.getString("DE_COMENTARIOS"),
                    rs.getString("IN_ATIVO")
                };
                data.add(row);
            }
        }
        return data;
    }

    private String getBlobAsString(Blob blob) {
        StringBuffer result = new StringBuffer();

        if (blob != null) {
            int read = 0;
            Reader reader = null;
            char[] buffer = new char[1024];

            try {
                reader = new InputStreamReader(blob.getBinaryStream(), StandardCharsets.UTF_8);

                while ((read = reader.read(buffer)) != -1) {
                    result.append(buffer, 0, read);
                }
            } catch (SQLException | IOException ex) {
                throw new RuntimeException("Unable to read blob data.", ex);
            } finally {
                try {
                    if (reader != null) reader.close();
                } catch (Exception ex) { }
            }
        }

        return result.toString();
    }

}
