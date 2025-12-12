package workspace.vigiang.checkers;

import workspace.commons.service.FileService;
import workspace.vigiang.model.FileConfigRegistry;
import workspace.vigiang.service.EnvironmentService;
import workspace.vigiang.model.EmailTemplate;
import workspace.vigiang.model.DatabaseCredentialsVigiaNG;
import workspace.vigiang.dao.VigiaNgDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static workspace.commons.service.FileService.writeString;

public class CheckEmailTemplates {

    public static void main(String[] args) {
        System.out.println("## START checking all email templates\n");
        try {
            for (DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG : EnvironmentService.getDatabasesVigiaNg()) {
                VigiaNgDAO dao = EnvironmentService.getVigiaNgDAO(databaseCredentialsVigiaNG);
                System.out.println(databaseCredentialsVigiaNG.getName() + ":");

                List<EmailTemplate> emailTemplates = dao.listEmailTemplates();
                updateLocalEmailTemplatesFiles(databaseCredentialsVigiaNG, emailTemplates);
                updateEmailTemplates(databaseCredentialsVigiaNG, emailTemplates);

                System.out.println();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        System.out.println("## END checking all email templates.");
    }

    private static void updateLocalEmailTemplatesFiles(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, List<EmailTemplate> emailTemplates) throws IOException {
        var fileConfig = FileConfigRegistry.getConfig("emailTemplate", databaseCredentialsVigiaNG.getDatabase());

        List<String[]> data = emailTemplates.stream()
                .map(EmailTemplate::toArray)
                .collect(Collectors.toList());

        Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
        FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileConfig.getFileName(), fileConfig.getColumns(), data, databaseDataPath);
    }

    private static void updateEmailTemplates(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, List<EmailTemplate> emailTemplates) {
        for (EmailTemplate emailTemplate : emailTemplates) {
            var result = emailTemplate.getBody();
            String emailTemplateId = emailTemplate.getId();
            int carrierIdInt = Integer.parseInt(emailTemplate.getCarrierId());
            String carrierId = String.format("%02d", carrierIdInt);

            Path outputPath = null;

            try {
                Path emailTemplatesPath = EnvironmentService.getEmailTemplatesPath(databaseCredentialsVigiaNG);

                Path emailCarrierPath = Paths.get(emailTemplatesPath + "\\" + carrierId);
                if (!Files.exists(emailCarrierPath)) {
                    Files.createDirectories(emailCarrierPath);
                }

                outputPath = Paths.get(emailCarrierPath + "\\" + emailTemplateId + ".html");

                writeString(outputPath, result);
            } catch (Exception e) {
                System.err.println("fail on file: " + outputPath);
                e.printStackTrace();
            }
        }
    }

}
