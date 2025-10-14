package workspace.vigiang.checkers;

import workspace.vigiang.service.EnvironmentService;
import workspace.vigiang.service.FileService;
import workspace.vigiang.model.EmailTemplate;
import workspace.vigiang.model.DatabaseCredentials;
import workspace.vigiang.dao.VigiaNgDAO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CheckEmailTemplates {

    public static void main(String[] args) {
        System.out.println("## START checking all email templates\n");
        try {
            for (DatabaseCredentials databaseCredentials : EnvironmentService.getVigiangDatabases()) {
                VigiaNgDAO dao = EnvironmentService.getVigiaNgDAO(databaseCredentials);
                System.out.println(databaseCredentials.getName() + ":");

                List<EmailTemplate> emailTemplates = dao.listEmailTemplates();
                updateLocalEmailTemplatesFiles(databaseCredentials, emailTemplates);
                updateEmailTemplates(databaseCredentials, emailTemplates);

                System.out.println();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        System.out.println("## END checking all email templates.");
    }

    private static void updateLocalEmailTemplatesFiles(DatabaseCredentials databaseCredentials, List<EmailTemplate> emailTemplates) throws IOException {
        String fileName = null;
        String[] columns = null;
        if (DatabaseCredentials.Database.ORACLE.equals(databaseCredentials.getDatabase())) {
            fileName = "CFG_EMAIL_SERVICOS";
            columns = new String[] {
                "CD_OPERADORA", "NM_OPERADORA", "ID_TIPO_SERVICO", "DE_ASSUNTO", "DE_NOME", "DE_NOME_ARQUIVO", "DE_REMETENTE", "DE_DESTINATARIO"
            };
        } else if (DatabaseCredentials.Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
            fileName = "conf.service_email";
            columns = new String[] {
                "carrier_id", "carrier_name", "service_type", "email_subject", "service_name", "attach_name", "email_from", "email_to"
            };
        }

        List<String[]> data = emailTemplates.stream()
                .map(EmailTemplate::toArray)
                .collect(Collectors.toList());

        FileService.updateLocalFiles(databaseCredentials, fileName, columns, data);
    }

    private static void updateEmailTemplates(DatabaseCredentials databaseCredentials, List<EmailTemplate> emailTemplates) {
        for (EmailTemplate emailTemplate : emailTemplates) {
            var newFileContent = emailTemplate.getBody();
            String emailTemplateId = emailTemplate.getId();
            int carrierIdInt = Integer.parseInt(emailTemplate.getCarrierId());
            String carrierId = String.format("%02d", carrierIdInt);
            Path finalFilePath = null;

            try {
                Path emailTemplatesPath = EnvironmentService.getEmailTemplatesPath(databaseCredentials);

                Path emailCarrierPath = Paths.get(emailTemplatesPath + "\\" + carrierId);
                if (!Files.exists(emailCarrierPath)) {
                    Files.createDirectories(emailCarrierPath);
                }

                finalFilePath = Paths.get(emailCarrierPath + "\\" + emailTemplateId + ".html");
                var initialFileContent = "";
                if (Files.exists(finalFilePath)) {
                    initialFileContent = new String(Files.readAllBytes(finalFilePath));
                }

                if (!initialFileContent.equals(newFileContent)) {
                    System.out.println("updating file: " + finalFilePath);
                    Files.writeString(finalFilePath, newFileContent, StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                System.err.println("fail on file: " + finalFilePath);
                e.printStackTrace();
            }
        }
    }

}
