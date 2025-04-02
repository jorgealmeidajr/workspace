package workspace.vigiang.checkers;

import workspace.vigiang.service.EnvironmentService;
import workspace.vigiang.service.FileService;
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
        System.out.println("## START checking all report templates\n");
        try {
            for (Environment env : EnvironmentService.getEnvironments()) {
                VigiaNgDAO dao = EnvironmentService.getVigiaNgDAO(env);
                System.out.println(env.getName() + ":");

                List<ReportTemplate> reportTemplates = dao.listReportTemplates(env);
                updateLocalReportFiles(env, reportTemplates);
                updateLocalReportTemplates(env, reportTemplates);

                updateLocalConfigReportFiles(env, dao);

                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("## END checking all report templates.");
    }

    private static void updateLocalReportFiles(Environment env, List<ReportTemplate> reportTemplates) throws IOException {
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

        FileService.updateLocalFiles(env, fileName, columns, data);
    }

    private static void updateLocalReportTemplates(Environment env, List<ReportTemplate> reportTemplates) throws IOException {
        Path reportTemplatesPath = EnvironmentService.getReportTemplatesPath(env);

        for (ReportTemplate reportTemplate : reportTemplates) {
            try {
                writeReportTemplate(reportTemplatesPath, reportTemplate);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeReportTemplate(Path reportTemplatesPath, ReportTemplate reportTemplate) throws IOException {
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
        Path templatePath = Paths.get(reportTemplatesPath + "\\" + templateName);

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

    private static void updateLocalConfigReportFiles(Environment env, VigiaNgDAO dao) throws IOException, SQLException {
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
        FileService.updateLocalFiles(env, fileName, columns, data);
    }

}
