package workspace.vigiang.checkers;

import workspace.vigiang.service.FilesService;
import workspace.vigiang.model.Environment;
import workspace.vigiang.model.ReportTemplate;
import workspace.vigiang.dao.VigiaNgDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CheckReportTemplates {

    public static void main(String[] args) {
        var vigiangPathStr = "C:\\Users\\jjunior\\MyDocuments\\COGNYTE\\VIGIANG";
        Path vigiangPath = Paths.get(vigiangPathStr);

        if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
            throw new IllegalArgumentException("o diretorio do vigiang nao existe ou nao eh um diretorio");
        }

        System.out.println("#".repeat(3 * 2));
        System.out.println("## START checking all report templates\n");

        execute(vigiangPath);

        System.out.println("## END checking all report templates.");
        System.out.println("#".repeat(3 * 2));
    }

    private static void execute(Path vigiangPath) {
        for (Environment env : Environment.values()) {
            VigiaNgDAO dao = env.getVigiaNgDAO();
            System.out.println(env + ":");

            try {
                List<ReportTemplate> reportTemplates = dao.listReportTemplates(env);
                updateLocalReportFiles(vigiangPath, env, reportTemplates);
                updateLocalReportTemplates(vigiangPath, env, reportTemplates);

                updateLocalConfigReportFiles(vigiangPath, env, dao);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

            System.out.println();
        }
    }

    private static void updateLocalReportFiles(Path vigiangPath, Environment env, List<ReportTemplate> reportTemplates) throws IOException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_RELATORIO";
            columns = new String[] { "CD_RELATORIO", "ID_RELATORIO", "TP_RELATORIO", "CD_OPERADORA", "NM_OPERADORA" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.report";
            columns = new String[] { "id", "report_id", "report_type", "carrier_id", "carrier_name" };
        }

        List<String[]> data = reportTemplates.stream()
                .map(ReportTemplate::toArray)
                .collect(Collectors.toList());

        FilesService.updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalReportTemplates(Path vigiangPath, Environment env, List<ReportTemplate> reportTemplates) throws IOException {
        Path reportPath = Paths.get(vigiangPath + "\\envs\\" + env + "\\DEV\\report_templates");
        if (!Files.exists(reportPath)) {
            Files.createDirectories(reportPath);
        }

        for (ReportTemplate reportTemplate : reportTemplates) {
            try {
                writeReportTemplate(reportPath, reportTemplate);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeReportTemplate(Path reportPath, ReportTemplate reportTemplate) throws IOException {
        int code = Integer.parseInt(reportTemplate.getCarrierCode());
        String carrierCode = String.format("%02d", code);

        code = Integer.parseInt(reportTemplate.getReportCode());
        String reportCode = String.format("%02d", code);
        var id = reportTemplate.getReportId();

        var extension = reportTemplate.getReportType();
        var templateExtension = "";
        if ("pdf".equals(extension)) {
            templateExtension = "odt";
        } else if ("xls".equals(extension)) {
            templateExtension = "xlsx";
        }

        var bytes = reportTemplate.getTemplate();
        var templateName = carrierCode + "_" + reportCode + "_" + id + "." + templateExtension;
        Path templatePath = Paths.get(reportPath + "\\" + templateName);

        if (Files.exists(templatePath)) {
            byte[] fileContent = Files.readAllBytes(templatePath);
            if (!Arrays.equals(fileContent, bytes)) {
                System.out.println("updating file: " + templatePath);
                Files.write(templatePath, bytes);
            }
        } else {
            System.out.println("writing new file: " + templatePath);
            Files.write(templatePath, bytes);
        }
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
        FilesService.updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

}
