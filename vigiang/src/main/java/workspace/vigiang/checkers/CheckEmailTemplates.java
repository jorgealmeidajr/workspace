package workspace.vigiang.checkers;

import workspace.vigiang.FilesService;
import workspace.vigiang.model.EmailTemplate;
import workspace.vigiang.model.Environment;
import workspace.vigiang.model.VigiaNgDAO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CheckEmailTemplates {

    public static void main(String[] args) {
        var vigiangPathStr = "C:\\Users\\jjunior\\MyDocuments\\COGNYTE\\VIGIANG";
        Path vigiangPath = Paths.get(vigiangPathStr);

        if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
            throw new IllegalArgumentException("o diretorio do vigiang nao existe ou nao eh um diretorio");
        }

        System.out.println("#".repeat(3 * 2));
        System.out.println("## START checking all email templates\n");

        for (Environment env : Environment.values()) {
            VigiaNgDAO dao = env.getVigiaNgDAO();
            System.out.println(env + ":");

            try {
                List<EmailTemplate> emailTemplates = dao.listEmailTemplates(env);
                updateLocalEmailTemplatesFiles(vigiangPath, env, emailTemplates);
                updateEmailTemplates(vigiangPath, env, emailTemplates);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

            System.out.println();
        }

        System.out.println("## END checking all email templates.");
        System.out.println("#".repeat(3 * 2));
    }

    private static void updateLocalEmailTemplatesFiles(Path vigiangPath, Environment env, List<EmailTemplate> emailTemplates) throws IOException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_EMAIL_SERVICOS";
            columns = new String[] {
                "CD_OPERADORA", "NM_OPERADORA", "ID_TIPO_SERVICO", "DE_ASSUNTO", "DE_NOME", "DE_NOME_ARQUIVO", "DE_REMETENTE", "DE_DESTINATARIO"
            };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.service_email";
            columns = new String[] {
                "carrier_id", "carrier_name", "service_type", "email_subject", "service_name", "attach_name", "email_from", "email_to"
            };
        }

        List<String[]> data = emailTemplates.stream()
                .map(EmailTemplate::toArray)
                .collect(Collectors.toList());

        FilesService.updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateEmailTemplates(Path vigiangPath, Environment env, List<EmailTemplate> emailTemplates) {
        for (EmailTemplate emailTemplate : emailTemplates) {
            var newFileContent = emailTemplate.getBody();
            var fileName = emailTemplate.getCarrierId() + "_" + emailTemplate.getId();

            try {
                Path emailTemplatesPath = Paths.get(vigiangPath + "\\envs\\" + env + "\\DEV\\email_templates");
                if (!Files.exists(emailTemplatesPath)) {
                    Files.createDirectories(emailTemplatesPath);
                }

                Path finalFilePath = Paths.get(emailTemplatesPath + "\\" + fileName + ".html");
                var initialFileContent = "";
                if (Files.exists(finalFilePath)) {
                    initialFileContent = new String(Files.readAllBytes(finalFilePath));
                }

                if (!initialFileContent.equals(newFileContent)) {
                    System.out.println("updating file: " + finalFilePath);
                    Files.writeString(finalFilePath, newFileContent, StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                System.err.println("fail on file: " + fileName);
                e.printStackTrace();
            }
        }
    }

}
