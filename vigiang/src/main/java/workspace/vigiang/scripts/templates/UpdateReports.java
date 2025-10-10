package workspace.vigiang.scripts.templates;

import workspace.vigiang.dao.VigiaNgDAO;
import workspace.vigiang.model.Carrier;
import workspace.vigiang.model.DatabaseCredentials;
import workspace.vigiang.service.EnvironmentService;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

public class UpdateReports {

    public static void main(String[] args) {
        // the parameters bellow must match in file databases.json
        String DATABASE_NAME = "?";
        Carrier CARRIER = null;
        DatabaseCredentials.Database DATABASE = null;

        Integer CARRIER_ID = 1; // this id is from database

        try {
            Predicate<DatabaseCredentials> databaseCredentialsPredicate = (credentials) ->
                    credentials.getName().equals(DATABASE_NAME)
                        && credentials.getCarrier().equals(CARRIER)
                        && credentials.getDatabase().equals(DATABASE);
            DatabaseCredentials databaseCredentials = EnvironmentService.getVigiangDatabases().stream()
                    .filter(databaseCredentialsPredicate)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Database credentials not found, check the parameters..."));
            VigiaNgDAO dao = EnvironmentService.getVigiaNgDAO(databaseCredentials);

            Path reportTemplatesPath = EnvironmentService.getReportTemplatesPath(databaseCredentials);
            String carrierCode = String.format("%02d", CARRIER_ID);
            Path carrierReportTemplatesPath = Paths.get(reportTemplatesPath + "\\" + carrierCode);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(carrierReportTemplatesPath)) {
                for (Path entry : stream) {
                    String fullFileName = entry.getFileName().toString();
                    String[] split = fullFileName.split("_");
                    String reportId = split[0];

                    int firstUnderscore = fullFileName.indexOf('_');
                    int lastDot = fullFileName.lastIndexOf('.');
                    String reportName = fullFileName.substring(firstUnderscore + 1, lastDot);

                    byte[] fileBytes = Files.readAllBytes(entry);
                    dao.updateTemplateReport(databaseCredentials, String.valueOf(CARRIER_ID), reportId, reportName, fileBytes);
                }
            }

            System.out.println("execution finished...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
