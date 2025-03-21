package workspace.vigiang;

import workspace.vigiang.model.Environment;
import workspace.vigiang.model.VigiaNgDAO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CheckDatabases {

    public static void main(String[] args) {
        var vigiangPathStr = "C:\\Users\\jjunior\\MyDocuments\\COGNYTE\\VIGIANG";
        Path vigiangPath = Paths.get(vigiangPathStr);

        if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
            throw new IllegalArgumentException("o diretorio do vigiang nao existe ou nao eh um diretorio");
        }

        System.out.println("#".repeat(3 * 2));
        System.out.println("## START checking all environment databases\n");

        for (Environment env : Environment.values()) {
            VigiaNgDAO dao = env.getVigiaNgDAO();
            System.out.println(env + ":");

            try {
                updateLocalFeatureFiles(vigiangPath, env, dao);
                updateLocalConfigurationFiles(vigiangPath, env, dao);

                updateLocalModuleFiles(vigiangPath, env, dao);
                updateLocalPrivilegeFiles(vigiangPath, env, dao);
                updateLocalProfileFiles(vigiangPath, env, dao);

                updateLocalFilterQueryFiles(vigiangPath, env, dao);
                updateLocalZoneInterceptionFiles(vigiangPath, env, dao);
                updateLocalValidationRuleFiles(vigiangPath, env, dao);
                updateLocalQdsValidationRuleFiles(vigiangPath, env, dao);
                updateLocalCarriersFiles(vigiangPath, env, dao);
                updateLocalZonesFiles(vigiangPath, env, dao);

                updateLocalEmailTemplatesFiles(vigiangPath, env, dao);
//                updateEmailTemplates(vigiangPath, env, dao);

                updateLocalReportFiles(vigiangPath, env, dao);
                updateLocalConfigReportFiles(vigiangPath, env, dao);
                // TODO: report template should be written in a local file
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

            System.out.println();
        }

        System.out.println("## END checking all environment databases.");
        System.out.println("#".repeat(3 * 2));
    }

    private static void updateEmailTemplates(Path vigiangPath, Environment env, VigiaNgDAO dao) throws SQLException, IOException {
        List<String[]> data = dao.listEmailTemplates(env);

        for (String[] row : data) {
            var carrierId = row[0];
            var nameId = row[1];
            var newFileContent = row[row.length - 1];
            var fileName = carrierId + "_" + nameId;

            Path emailTemplatesPath = Paths.get(vigiangPath + "\\envs\\" + env + "\\DEV\\email_templates");
            if (!Files.exists(emailTemplatesPath)) {
                Files.createDirectories(emailTemplatesPath);
            }

            Path finalFilePath = Paths.get(emailTemplatesPath + "\\" + fileName + ".html");
            var initialFileContent = "";
            if (Files.exists(finalFilePath)) {
                initialFileContent = new String(Files.readAllBytes(finalFilePath));
            }

            if (!initialFileContent.equals(newFileContent)) {
                System.out.println("updating file: " + finalFilePath);
                Files.writeString(finalFilePath, newFileContent, StandardCharsets.UTF_8);
            }
        }
    }

    private static void updateLocalFeatureFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws SQLException, IOException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_NG_FEATURE";
            columns = new String[] { "ID_FEATURE", "ID_STATUS", "ID_DESCRICAO" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.feature";
            columns = new String[] { "feature", "status", "description" };
        }

        List<String[]> data = dao.listFeatures(env);
        updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalConfigurationFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws SQLException, IOException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_NG_SITE";
            columns = new String[] { "CD_OPERADORA", "ID_PARAMETRO", "DE_PARAMETRO", "VL_PARAMETRO" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.site";
            columns = new String[] { "carrier_id", "parameter_id", "parameter_description", "value" };
        }

        List<String[]> data = dao.listConfigurationValues(env);
        updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalModuleFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_MODULO";
            columns = new String[] { "ID_CHAVE", "ID_STATUS", "ID_TIPO" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            return;
        }

        List<String[]> data = dao.listModules(env);
        updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalPrivilegeFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "SEG_PRIVILEGIO";
            columns = new String[] { "NM_MODULO", "STATUS_MODULO", "NM_PRIVILEGIO" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "sec.privilege";
            columns = new String[] { "module_id", "name" };
        }

        List<String[]> data = dao.listPrivileges(env);
        updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalProfileFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "SEG_PERFIL_PRIVILEGIO";
            columns = new String[] { "CD_OPERADORA", "NM_PERFIL", "NM_PRIVILEGIO", "CD_MODULO", "NM_MODULO" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "sec.profile_privilege";
            columns = new String[] { "carrier_id", "profile_name", "privilege_name", "module_id" };
        }

        List<String[]> data = dao.listProfiles(env);
        updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalFilterQueryFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_NG_FILTERQUERY";
            columns = new String[] { "MODULE", "LABEL", "VALUE" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.filterquery";
            columns = new String[] { "module", "label", "value" };
        }

        List<String[]> data = dao.listFilterQueries(env);
        updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalZoneInterceptionFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_TP_ZONA_TP_VL_ITC";
            columns = new String[] {
                "CD_OPERADORA", "NM_OPERADORA",
                "NM_ZONA_MONIT", "NM_TIPO_VALOR_INTERCEPTADO",
                "SN_VISIVEL_CAD_ITC", "SN_VISIVEL_LOTE", "NM_REGRAS"
            };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.tp_zone_tp_vl_itc";
            columns = new String[] {
                "carrier_id", "carrier_name",
                "network_element_type_name", "target_type_name",
                "itc_form_visible", "visible", "rules"
            };
        }

        try {
            List<String[]> data = dao.listZoneInterceptions(env);
            updateLocalFiles(vigiangPath, env, fileName, columns, data);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalValidationRuleFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_NG_VALIDATRULES";
            columns = new String[] { "CD_OPERADORA", "NM_OPERADORA", "MODULO", "VALID_RULES" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.validatrules";
            columns = new String[] { "carrier_id", "carrier_name", "module", "valid_rules" };
        }

        try {
            List<String[]> data = dao.listValidationRules(env);
            updateLocalFiles(vigiangPath, env, fileName, columns, data);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalQdsValidationRuleFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_TIPO_NUMERO_QDS";
            columns = new String[] { "ID_TIPO_NUMERO_QDS", "NM_CHAVE", "TP_CONSULTA", "SN_VOUCHER_DATE", "VALID_RULES" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            return;
        }

        try {
            List<String[]> data = dao.listQdsValidationRules(env);
            updateLocalFiles(vigiangPath, env, fileName, columns, data);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalEmailTemplatesFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_EMAIL_SERVICOS";
            columns = new String[] { "CD_OPERADORA", "ID_TIPO_SERVICO", "DE_ASSUNTO", "DE_NOME", "DE_NOME_ARQUIVO", "DE_REMETENTE", "DE_DESTINATARIO", "DE_TEXTO" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.service_email";
            columns = new String[] { "carrier_id", "service_type", "email_subject", "service_name", "attach_name", "email_from", "email_to", "email_body" };
        }

        List<String[]> data = dao.listEmailTemplates(env);
        updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalReportFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_RELATORIO";
            columns = new String[] { "CD_RELATORIO", "ID_RELATORIO", "TP_RELATORIO", "CD_OPERADORA" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.report";
            columns = new String[] { "id", "report_id", "report_type", "carrier_id" };
        }

        List<String[]> data = dao.listReports(env);
        updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalConfigReportFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_SITE_RELATORIO";
            columns = new String[] { "CD_OPERADORA", "ID_PARAMETRO", "DE_PARAMETRO", "VL_PARAMETRO", "CD_RELATORIO", "ID_RELATORIO" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.site_report";
            columns = new String[] { "carrier_id", "parameter_id", "parameter_description", "value", "id", "report_id" };
        }

        List<String[]> data = dao.listConfigurationReports(env);
        updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalCarriersFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws SQLException, IOException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_OPERADORA";
            columns = new String[] {
                "CD_OPERADORA", "NM_OPERADORA",
                "ID_SMTP_HOST", "ID_SMTP_USER", "ID_SMPT_PASSWORD", "ID_SMPT_ACCOUNT", "ID_SMTP_PORT",
                "DE_COMENTARIO", "SN_MULTIOPERADORA", "ID_REGEX", "DS_LOCAL_IMAGENS",  "DS_REGEX", "ID_API_KEY_MAPS", "SN_TOKEN",
                "IM_LOGO", "IM_LOGO_FOOTER"
            };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.carrier";
            columns = new String[] {
                "id", "name",
                "smtp_host", "smtp_user", "smpt_password", "smpt_account", "smtp_port",
                "comments", "muti_carrier", "regex", "local_images", "ds_regex", "api_key_maps", "token",
                "im_logo", "im_logo_footer"
            };
        }

        List<String[]> data = dao.listCarriers(env);
        updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalZonesFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws SQLException, IOException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_ZONA_MONIT";
            columns = new String[] {
                "CD_OPERADORA", "NM_OPERADORA",
                "CD_ZONA_MONIT", "NM_ZONA_MONIT", "DE_COMENTARIOS", "IN_ATIVO"
            };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.zone_monit";
            columns = new String[] {
                "carrier_id", "carrier_name",
                "zone_monit_id", "zone_monit_name", "comments", "active"
            };
        }

        List<String[]> data = dao.listZones(env);
        updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalFiles(Path vigiangPath, Environment env, String fileName, String[] columns, List<String[]> data) throws IOException {
        var finalLines = new ArrayList<String>();
        int columnWidth = calculateColumnWidth(columns);

        for (String[] row : data) {
            String line = "";
            for (int i = 0; i < columns.length; i++) {
                var column = rightPad(columns[i], columnWidth, " ");
                line += column + ": " + row[i] + "\n";
            }
            finalLines.add(line);
        }

        var newFileContent =
            "# " + env + " | " + fileName + "\n" +
            "```\n" +
            String.join(System.lineSeparator(), finalLines) +
            "```\n";

        Path finalFilePath = Paths.get(vigiangPath + "\\envs\\" + env + "\\DEV\\database\\" + fileName + ".md");

        var initialFileContent = "";
        if (Files.exists(finalFilePath)) {
            initialFileContent = new String(Files.readAllBytes(finalFilePath));
        }

        if (!initialFileContent.equals(newFileContent)) {
            System.out.println("updating file: " + finalFilePath);
            Files.writeString(finalFilePath, newFileContent, StandardCharsets.UTF_8);
        }
    }

    private static int calculateColumnWidth(String[] headers) {
        int maxValue = headers[0].length();
        for (String header : headers) {
            int headerLength = header.length();
            if (headerLength > maxValue) {
                maxValue = headerLength;
            }
        }
        return maxValue;
    }

    public static String rightPad(String input, int length, String padStr) {
        if(input == null || padStr == null){
            return null;
        }

        if(input.length() >= length){
            return input;
        }

        int padLength = length - input.length();

        StringBuilder paddedString = new StringBuilder();
        paddedString.append(input);
        paddedString.append(padStr.repeat(padLength));

        return paddedString.toString();
    }

}
