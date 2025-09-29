package workspace.vigiang.scripts.templates;

import workspace.vigiang.dao.VigiaNgDAO;
import workspace.vigiang.model.Environment;
import workspace.vigiang.service.EnvironmentService;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class UpdateReports {

    public static void main(String[] args) {
        String ENVIRONMENT_NAME = "?"; // TODO: this name should be in environments
        Integer CARRIER_ID = 0; // this id is from database

        try {
            Environment environment = EnvironmentService.getVigiangDatabases().stream()
                    .filter(env -> env.getName().equals(ENVIRONMENT_NAME))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Environment not found: " + ENVIRONMENT_NAME));
            VigiaNgDAO dao = EnvironmentService.getVigiaNgDAO(environment);

            Path reportTemplatesPath = EnvironmentService.getReportTemplatesPath(environment);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(reportTemplatesPath)) {
                for (Path entry : stream) {
                    String fullFileName = entry.getFileName().toString();
                    String[] split = fullFileName.split("_");
                    String carrierId = split[0];
                    String reportId = split[1];

                    if (!CARRIER_ID.equals(Integer.parseInt(carrierId))) continue;

                    int firstUnderscore = fullFileName.indexOf('_');
                    int secondUnderscore = fullFileName.indexOf('_', firstUnderscore + 1);
                    int lastDot = fullFileName.lastIndexOf('.');
                    String reportName = fullFileName.substring(secondUnderscore + 1, lastDot);

                    byte[] fileBytes = Files.readAllBytes(entry);
                    dao.updateTemplateReport(environment, carrierId, reportId, reportName, fileBytes);
                }
            }

            System.out.println("execution finished...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
