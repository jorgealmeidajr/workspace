package workspace.vigiang.checkers;

import workspace.commons.model.Database;
import workspace.commons.service.FileService;
import workspace.vigiang.service.EnvironmentService;
import workspace.vigiang.model.EmailTemplate;
import workspace.vigiang.model.DatabaseCredentialsVigiaNG;
import workspace.vigiang.dao.VigiaNgDAO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
            for (DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG : EnvironmentService.getVigiangDatabases()) {
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
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentialsVigiaNG.getDatabase())) {
            fileName = "CFG_EMAIL_SERVICOS";
            columns = new String[] {
                "CD_OPERADORA", "NM_OPERADORA", "ID_TIPO_SERVICO", "DE_ASSUNTO", "DE_NOME", "DE_NOME_ARQUIVO", "DE_REMETENTE", "DE_DESTINATARIO"
            };
        } else if (Database.POSTGRES.equals(databaseCredentialsVigiaNG.getDatabase())) {
            fileName = "conf.service_email";
            columns = new String[] {
                "carrier_id", "carrier_name", "service_type", "email_subject", "service_name", "attach_name", "email_from", "email_to"
            };
        }

        List<String[]> data = emailTemplates.stream()
                .map(EmailTemplate::toArray)
                .collect(Collectors.toList());

        Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
        FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileName, columns, data, databaseDataPath);
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
