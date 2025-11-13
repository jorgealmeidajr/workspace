package workspace.vigiang.checkers;

import workspace.vigiang.model.Database;
import workspace.vigiang.service.EnvironmentService;
import workspace.vigiang.service.FileService;
import workspace.vigiang.model.DatabaseCredentials;
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
            for (DatabaseCredentials databaseCredentials : EnvironmentService.getVigiangDatabases()) {
                VigiaNgDAO dao = EnvironmentService.getVigiaNgDAO(databaseCredentials);
                System.out.println(databaseCredentials.getName() + ":");

                List<ReportTemplate> reportTemplates = dao.listReportTemplates();
                updateLocalReportFiles(databaseCredentials, reportTemplates);
                updateLocalReportTemplates(databaseCredentials, reportTemplates);

                updateLocalConfigReportFiles(databaseCredentials, dao);

                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("## END checking all report templates.");
    }

    private static void updateLocalReportFiles(DatabaseCredentials databaseCredentials, List<ReportTemplate> reportTemplates) throws IOException {
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
            fileName = "CFG_RELATORIO";
            columns = new String[] { "CD_RELATORIO", "ID_RELATORIO", "TP_RELATORIO", "CD_OPERADORA", "NM_OPERADORA" };

        } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
            fileName = "conf.report";
            columns = new String[] { "id", "report_id", "report_type", "carrier_id", "carrier_name" };
        }

        List<String[]> data = reportTemplates.stream()
                .map(ReportTemplate::toArray)
                .collect(Collectors.toList());

        FileService.updateLocalFiles(databaseCredentials, fileName, columns, data);
    }

    private static void updateLocalReportTemplates(DatabaseCredentials databaseCredentials, List<ReportTemplate> reportTemplates) throws IOException {
        Path reportTemplatesPath = EnvironmentService.getReportTemplatesPath(databaseCredentials);

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

        Path reportCarrierPath = Paths.get(reportTemplatesPath + "\\" + carrierCode);
        if (!Files.exists(reportCarrierPath)) {
            Files.createDirectories(reportCarrierPath);
        }

        var bytes = reportTemplate.getTemplate();
        var templateName = reportCode + "_" + id + "." + templateExtension;
        Path templatePath = Paths.get(reportCarrierPath + "\\" + templateName);

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

    private static void updateLocalConfigReportFiles(DatabaseCredentials databaseCredentials, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
            fileName = "CFG_SITE_RELATORIO";
            columns = new String[] { "CD_OPERADORA", "ID_PARAMETRO", "DE_PARAMETRO", "VL_PARAMETRO", "CD_RELATORIO", "ID_RELATORIO" };

        } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
            fileName = "conf.site_report";
            columns = new String[] { "carrier_id", "parameter_id", "parameter_description", "value", "id", "report_id" };
        }

        List<String[]> data = dao.listConfigurationReports();
        FileService.updateLocalFiles(databaseCredentials, fileName, columns, data);
    }

}
