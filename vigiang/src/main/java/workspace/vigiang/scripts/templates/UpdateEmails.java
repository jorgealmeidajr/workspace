package workspace.vigiang.scripts.templates;

import workspace.vigiang.dao.VigiaNgDAO;
import workspace.vigiang.model.DatabaseCredentials;
import workspace.vigiang.service.EnvironmentService;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UpdateEmails {

    public static void main(String[] args) {
        String DATABASE_NAME = "ENTEL?"; // the name must match in file databases.json
        Integer CARRIER_ID = -1;         // this id is from the database

        try {
            DatabaseCredentials databaseCredentials = EnvironmentService.getDatabaseCredentials(DATABASE_NAME);
            VigiaNgDAO dao = EnvironmentService.getVigiaNgDAO(databaseCredentials);

            Path emailTemplatesPath = EnvironmentService.getEmailTemplatesPath(databaseCredentials);

            String carrierCode = String.format("%02d", CARRIER_ID);
            Path carrierReportTemplatesPath = Paths.get(emailTemplatesPath + "\\" + carrierCode);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(carrierReportTemplatesPath)) {
                for (Path entry : stream) {
                    String fullFileName = entry.getFileName().toString();
                    int lastDot = fullFileName.lastIndexOf('.');
                    String reportName = fullFileName.substring(0, lastDot);

                    byte[] fileBytes = Files.readAllBytes(entry);
                    System.out.println("Updating email template: " + reportName);
                    dao.updateTemplateEmail(String.valueOf(CARRIER_ID), reportName, fileBytes);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
