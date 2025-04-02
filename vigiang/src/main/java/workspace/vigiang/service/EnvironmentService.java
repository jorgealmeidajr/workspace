package workspace.vigiang.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import workspace.vigiang.dao.*;
import workspace.vigiang.model.Environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EnvironmentService {

    private static final String VIGIANG_PATH_STR = "C:\\Users\\jjunior\\MyDocuments\\COGNYTE\\VIGIANG";

    public static List<Environment> getEnvironments() throws IOException {
        Path path = Paths.get("src/main/java/workspace/vigiang/environments.json");
        String read = Files.readString(path);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(read, new TypeReference<>(){});
    }

    public static VigiaNgDAO getVigiaNgDAO(Environment environment) {
        if (Environment.Database.ORACLE.equals(environment.getDatabase())) return new OracleVigiaNgDAO();
        if (Environment.Database.POSTGRES.equals(environment.getDatabase())) return new PostgresVigiaNgDAO();
        return null;
    }

    public static DbSchemaDAO getDbSchemaDAO(Environment environment) {
        if (Environment.Database.ORACLE.equals(environment.getDatabase())) return new OracleSchemaDAO();
        if (Environment.Database.POSTGRES.equals(environment.getDatabase())) return new PostgresSchemaDAO();
        return null;
    }

    public static Path getVigiaNgPath() {
        Path vigiangPath = Paths.get(VIGIANG_PATH_STR);
        if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
            throw new IllegalArgumentException("o diretorio do vigiang nao existe ou nao eh um diretorio");
        }
        return vigiangPath;
    }

    public static Path getEnvironmentPath(Environment environment) throws IOException {
        Path vigiangPath = getVigiaNgPath();

        Path environmentPath = Paths.get(vigiangPath + "\\environments\\" + environment.getName());
        if (!Files.exists(environmentPath)) {
            Files.createDirectories(environmentPath);
        }
        return environmentPath;
    }

    public static Path getDatabaseDataPath(Environment environment) throws IOException {
        Path environmentPath = EnvironmentService.getEnvironmentPath(environment);
        String database = environment.getDatabase().toString().toLowerCase();

        Path databaseDataPath = Paths.get(environmentPath + "\\" + database + "_data");
        if (!Files.exists(databaseDataPath)) {
            Files.createDirectories(databaseDataPath);
        }
        return databaseDataPath;
    }

    public static Path getEmailTemplatesPath(Environment environment) throws IOException {
        Path environmentPath = EnvironmentService.getEnvironmentPath(environment);

        Path emailTemplatesPath = Paths.get(environmentPath + "\\email_templates");
        if (!Files.exists(emailTemplatesPath)) {
            Files.createDirectories(emailTemplatesPath);
        }
        return emailTemplatesPath;
    }

    public static Path getReportTemplatesPath(Environment environment) throws IOException {
        Path environmentPath = EnvironmentService.getEnvironmentPath(environment);

        Path reportTemplatesPath = Paths.get(environmentPath + "\\report_templates");
        if (!Files.exists(reportTemplatesPath)) {
            Files.createDirectories(reportTemplatesPath);
        }
        return reportTemplatesPath;
    }

}
