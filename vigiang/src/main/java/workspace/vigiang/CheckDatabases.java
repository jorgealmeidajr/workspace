package workspace.vigiang;

import workspace.vigiang.model.Environment;
import workspace.vigiang.model.OracleVigiaNgDAO;
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

    static final VigiaNgDAO VIGIA_NG_DAO = new OracleVigiaNgDAO();

    public static void main(String[] args) {
        var vigiangPathStr = "C:\\Users\\jjunior\\MyDocuments\\COGNYTE\\VIGIANG";
        Path vigiangPath = Paths.get(vigiangPathStr);

        if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
            throw new IllegalArgumentException("o diretorio do vigiang nao existe ou nao eh um diretorio");
        }

        System.out.println("#".repeat(3 * 2));
        System.out.println("## START checking all environment databases\n");

        for (Environment env : Environment.values()) {
            System.out.println("#".repeat(3 * 1));
            System.out.println(env);

            try {
                VigiaNgDAO dao = env.getVigiaNgDAO();
                updateLocalFeatureFiles(vigiangPath, env, dao);
                updateLocalConfigurationFiles(vigiangPath, env, dao);
                updateLocalModuleFiles(vigiangPath, env, dao);
                updateLocalPrivilegeFiles(vigiangPath, env, dao);
//                updateLocalProfileFiles(vigiangPath, env);
//                updateLocalFilterQueryFiles(vigiangPath, env);
//                updateLocalZoneInterceptionFiles(vigiangPath, env);
//                updateLocalValidationRuleFiles(vigiangPath, env);
//                updateLocalQdsValidationRuleFiles(vigiangPath, env);
//                updateLocalEmailTemplatesFiles(vigiangPath, env); // TODO: email text template should be written in a file, it is large
//                updateLocalReportFiles(vigiangPath, env); // TODO: report template should be written in a local file
//                updateLocalConfigReportFiles(vigiangPath, env);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

            System.out.println("#".repeat(3 * 1) + "\n");
        }

        System.out.println("## END checking all environment databases.");
        System.out.println("#".repeat(3 * 2));
    }

    private static void updateLocalFeatureFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws SQLException, IOException {
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            var fileName = "CFG_NG_FEATURE";
            String[] columns = new String[] { "ID_FEATURE", "ID_STATUS", "ID_DESCRICAO" };
            List<String[]> data = dao.listFeatures(env, columns);
            updateLocalFiles(vigiangPath, env, fileName, columns, data);

        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            var fileName = "conf.feature";
            String[] columns = new String[] { "feature", "status", "description" };
            List<String[]> data = dao.listFeatures(env, columns);
            updateLocalFiles(vigiangPath, env, fileName, columns, data);
        }
    }

    private static void updateLocalConfigurationFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws SQLException, IOException {
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            var fileName = "CFG_NG_SITE";
            String[] columns = new String[] { "ID_PARAMETRO", "DE_PARAMETRO", "VL_PARAMETRO" };
            List<String[]> data = dao.listConfigurationValues(env);
            updateLocalFiles(vigiangPath, env, fileName, columns, data);

        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            var fileName = "conf.site";
            String[] columns = new String[] { "parameter_id", "parameter_description", "value" };
            List<String[]> data = dao.listConfigurationValues(env);
            updateLocalFiles(vigiangPath, env, fileName, columns, data);
        }
    }

    private static void updateLocalModuleFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws IOException, SQLException {
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            var fileName = "CFG_MODULO";
            String[] columns = new String[] { "ID_CHAVE", "ID_STATUS", "ID_TIPO" };
            List<String[]> data = dao.listModules(env);
            updateLocalFiles(vigiangPath, env, fileName, columns, data);

        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {

        }
    }

    private static void updateLocalPrivilegeFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws IOException, SQLException {
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            var fileName = "SEG_PRIVILEGIO";
            String[] columns = new String[] { "NM_MODULO", "STATUS_MODULO", "NM_PRIVILEGIO" };
            List<String[]> data = dao.listPrivileges(env);
            updateLocalFiles(vigiangPath, env, fileName, columns, data);

        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            var fileName = "sec.privilege";
            String[] columns = new String[] { "module_id", "name" };
            List<String[]> data = dao.listPrivileges(env);
            updateLocalFiles(vigiangPath, env, fileName, columns, data);
        }

        if (Environment.Database.ORACLE.equals(env.getDatabase())) {

        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {

        }
    }

    private static void updateLocalProfileFiles(Path vigiangPath, Environment env) throws IOException, SQLException {
        var fileName = "SEG_PERFIL_PRIVILEGIO";
        String[] columns = new String[] { "NM_PERFIL", "NM_PRIVILEGIO", "NM_MODULO" };
        List<String[]> data = VIGIA_NG_DAO.listProfiles(env);
        updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalFilterQueryFiles(Path vigiangPath, Environment env) throws IOException, SQLException {
        var fileName = "CFG_NG_FILTERQUERY";
        String[] columns = new String[] { "MODULE", "LABEL", "VALUE" };
        List<String[]> data = VIGIA_NG_DAO.listFilterQueries(env);
        updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalZoneInterceptionFiles(Path vigiangPath, Environment env) {
        var fileName = "CFG_TP_ZONA_TP_VL_ITC";
        String[] columns = new String[] { "NM_ZONA_MONIT", "NM_TIPO_VALOR_INTERCEPTADO", "SN_VISIVEL_CAD_ITC", "SN_VISIVEL_LOTE", "NM_REGRAS" };
        try {
            List<String[]> data = VIGIA_NG_DAO.listZoneInterceptions(env);
            updateLocalFiles(vigiangPath, env, fileName, columns, data);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalValidationRuleFiles(Path vigiangPath, Environment env) {
        var fileName = "CFG_NG_VALIDATRULES";
        String[] columns = new String[] { "MODULO", "VALID_RULES" };
        try {
            List<String[]> data = VIGIA_NG_DAO.listValidationRules(env);
            updateLocalFiles(vigiangPath, env, fileName, columns, data);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalQdsValidationRuleFiles(Path vigiangPath, Environment env) {
        var fileName = "CFG_TIPO_NUMERO_QDS";
        String[] columns = new String[] { "ID_TIPO_NUMERO_QDS", "NM_CHAVE", "TP_CONSULTA", "SN_VOUCHER_DATE", "VALID_RULES" };
        try {
            List<String[]> data = VIGIA_NG_DAO.listQdsValidationRules(env);
            updateLocalFiles(vigiangPath, env, fileName, columns, data);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalEmailTemplatesFiles(Path vigiangPath, Environment env) throws IOException, SQLException {
        var fileName = "CFG_EMAIL_SERVICOS";
        String[] columns = new String[] {
            "CD_OPERADORA", "ID_TIPO_SERVICO", "DE_ASSUNTO", "DE_NOME", "DE_NOME_ARQUIVO", "DE_REMETENTE", "DE_DESTINATARIO", "DE_TEXTO"
        };
        List<String[]> data = VIGIA_NG_DAO.listEmailTemplates(env);
        updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalReportFiles(Path vigiangPath, Environment env) throws IOException, SQLException {
        var fileName = "CFG_RELATORIO";
        String[] columns = new String[] { "CD_RELATORIO", "ID_RELATORIO", "TP_RELATORIO", "CD_OPERADORA" };
        List<String[]> data = VIGIA_NG_DAO.listReports(env);
        updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalConfigReportFiles(Path vigiangPath, Environment env) throws IOException, SQLException {
        var fileName = "CFG_SITE_RELATORIO";
        String[] columns = new String[] { "ID_PARAMETRO", "DE_PARAMETRO", "VL_PARAMETRO", "CD_RELATORIO", "ID_RELATORIO" };
        List<String[]> data = VIGIA_NG_DAO.listConfigurationReports(env);
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
        for (int i = 0; i < headers.length; i++) {
            int headerLength = headers[i].length();
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
