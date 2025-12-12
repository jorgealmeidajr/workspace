package workspace.vigiang.checkers;

import workspace.commons.service.FileService;
import workspace.vigiang.model.FileConfigRegistry;
import workspace.vigiang.service.EnvironmentService;
import workspace.vigiang.model.DatabaseCredentialsVigiaNG;
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
            for (DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG : EnvironmentService.getDatabasesVigiaNg()) {
                VigiaNgDAO dao = EnvironmentService.getVigiaNgDAO(databaseCredentialsVigiaNG);
                System.out.println(databaseCredentialsVigiaNG.getName() + ":");

                List<ReportTemplate> reportTemplates = dao.listReportTemplates();
                updateLocalReportFiles(databaseCredentialsVigiaNG, reportTemplates);
                updateLocalReportTemplates(databaseCredentialsVigiaNG, reportTemplates);

                updateLocalConfigReportFiles(databaseCredentialsVigiaNG, dao);

                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("## END checking all report templates.");
    }

    private static void updateLocalReportFiles(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, List<ReportTemplate> reportTemplates) throws IOException {
        var fileConfig = FileConfigRegistry.getConfig("report", databaseCredentialsVigiaNG.getDatabase());

        List<String[]> data = reportTemplates.stream()
                .map(ReportTemplate::toArray)
                .collect(Collectors.toList());

        Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
        FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileConfig.getFileName(), fileConfig.getColumns(), data, databaseDataPath);
    }

    private static void updateLocalReportTemplates(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, List<ReportTemplate> reportTemplates) throws IOException {
        Path reportTemplatesPath = EnvironmentService.getReportTemplatesPath(databaseCredentialsVigiaNG);

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

    private static void updateLocalConfigReportFiles(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, VigiaNgDAO dao) throws IOException, SQLException {
        var fileConfig = FileConfigRegistry.getConfig("reportConfig", databaseCredentialsVigiaNG.getDatabase());

        List<String[]> data = dao.listConfigurationReports();
        Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
        FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileConfig.getFileName(), fileConfig.getColumns(), data, databaseDataPath);
    }

}
