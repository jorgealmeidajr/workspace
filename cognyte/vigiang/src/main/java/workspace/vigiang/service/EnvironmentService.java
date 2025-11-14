package workspace.vigiang.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import workspace.commons.model.Database;
import workspace.vigiang.dao.*;
import workspace.vigiang.model.DatabaseCredentials;
import workspace.vigiang.model.Laboratory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class EnvironmentService {

    private static final String WORK_PATH_STR = "C:\\work";
    private static final String COGNYTE_PATH_STR = WORK_PATH_STR + "\\COGNYTE1";
    private static final String VIGIANG_PATH_STR = COGNYTE_PATH_STR + "\\VIGIANG";
    private static final String VIGIANG_LABORATORIES_PATH = COGNYTE_PATH_STR + "\\vigiang_labs";
    private static final String VIGIANG_DATABASES_PATH = COGNYTE_PATH_STR + "\\vigiang_dbs";

    public static List<String> getVersions() {
        return List.of("1.5", "1.7", "2.1", "2.2");
    }

    public static List<DatabaseCredentials> getVigiangDatabases() throws IOException {
        try (InputStream read = EnvironmentService.class.getResourceAsStream("/databases.json")) {
            if (read == null) {
                throw new IllegalStateException("Resource `/databases.json` not found on classpath");
            }
            return getDatabaseCredentials(read);
        }
    }

    private static List<DatabaseCredentials> getDatabaseCredentials(InputStream read) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<DatabaseCredentials> databaseCredentials = mapper.readValue(read, new TypeReference<>(){});
        return databaseCredentials.stream()
                .filter(DatabaseCredentials::isActive)
                .collect(Collectors.toList());
    }

    public static List<Laboratory> getVigiangLaboratories() throws IOException {
        try (InputStream read = EnvironmentService.class.getResourceAsStream("/laboratories.json")) {
            if (read == null) {
                throw new IllegalStateException("Resource `/laboratories.json` not found on classpath");
            }
            return getLaboratories(read);
        }
    }

    private static List<Laboratory> getLaboratories(InputStream read) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Laboratory> laboratories = mapper.readValue(read, new TypeReference<>(){});
        return laboratories.stream()
                .filter(Laboratory::isActive)
                .collect(Collectors.toList());
    }

    public static VigiaNgDAO getVigiaNgDAO(DatabaseCredentials databaseCredentials) {
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) return new OracleVigiaNgDAO(databaseCredentials);
        if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) return new PostgresVigiaNgDAO(databaseCredentials);
        return null;
    }

    public static DbSchemaDAO getDbSchemaDAO(DatabaseCredentials databaseCredentials) {
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) return new OracleSchemaDAO();
        if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) return new PostgresSchemaDAO();
        return null;
    }

    public static Path getCognytePath() {
        Path vigiangPath = Paths.get(COGNYTE_PATH_STR);
        if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
            throw new IllegalArgumentException("o diretorio do cognyte nao existe ou nao eh um diretorio");
        }
        return vigiangPath;
    }

    public static Path getVigiaNgPath() {
        Path vigiangPath = Paths.get(VIGIANG_PATH_STR);
        if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
            throw new IllegalArgumentException("o diretorio do vigiang nao existe ou nao eh um diretorio");
        }
        return vigiangPath;
    }

    public static Path getVigiaNgLaboratoriesPath() {
        Path vigiangPath = Paths.get(VIGIANG_LABORATORIES_PATH);
        if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
            throw new IllegalArgumentException("o diretorio do vigiang laboratories nao existe ou nao eh um diretorio");
        }
        return vigiangPath;
    }

    public static Path getLaboratoryPath(Laboratory laboratory) throws IOException {
        Path vigiaNgLaboratoriesPath = getVigiaNgLaboratoriesPath();

        Path laboratoryPath;
        if (laboratory.getCarrier() == null) {
            laboratoryPath = Paths.get(vigiaNgLaboratoriesPath + "\\" + laboratory.getName());
        } else {
            laboratoryPath = Paths.get(vigiaNgLaboratoriesPath + "\\" + laboratory.getCarrier() + "\\" + laboratory.getName());
        }

        if (!Files.exists(laboratoryPath)) {
            Files.createDirectories(laboratoryPath);
        }
        return laboratoryPath;
    }

    public static Path getDatabaseDataPath(DatabaseCredentials databaseCredentials) throws IOException {
        Path environmentPath = Paths.get(VIGIANG_DATABASES_PATH);
        String database = databaseCredentials.getDatabase().toString();

        Path databaseDataPath = Paths.get(environmentPath + "\\" + database + "\\" + databaseCredentials.getName() + "\\data");
        if (!Files.exists(databaseDataPath)) {
            Files.createDirectories(databaseDataPath);
        }
        return databaseDataPath;
    }

    public static Path getDatabaseSchemaPath(DatabaseCredentials databaseCredentials) throws IOException {
        Path environmentPath = getDatabasePath(databaseCredentials);
        Path databaseDataPath = Paths.get(environmentPath + "\\schema");
        if (!Files.exists(databaseDataPath)) {
            Files.createDirectories(databaseDataPath);
        }
        return databaseDataPath;
    }

    public static Path getDatabasePath(DatabaseCredentials databaseCredentials) throws IOException {
        Path environmentPath = Paths.get(VIGIANG_DATABASES_PATH);
        String database = databaseCredentials.getDatabase().toString();

        Path databaseDataPath = Paths.get(environmentPath + "\\" + database + "\\" + databaseCredentials.getName());
        if (!Files.exists(databaseDataPath)) {
            Files.createDirectories(databaseDataPath);
        }
        return databaseDataPath;
    }

    public static Path getEmailTemplatesPath(DatabaseCredentials databaseCredentials) throws IOException {
        Path environmentPath = getDatabasePath(databaseCredentials);

        Path emailTemplatesPath = Paths.get(environmentPath + "\\email_templates");
        if (!Files.exists(emailTemplatesPath)) {
            Files.createDirectories(emailTemplatesPath);
        }
        return emailTemplatesPath;
    }

    public static Path getReportTemplatesPath(DatabaseCredentials databaseCredentials) throws IOException {
        Path environmentPath = getDatabasePath(databaseCredentials);

        Path reportTemplatesPath = Paths.get(environmentPath + "\\report_templates");
        if (!Files.exists(reportTemplatesPath)) {
            Files.createDirectories(reportTemplatesPath);
        }
        return reportTemplatesPath;
    }

    public static DatabaseCredentials getDatabaseCredentials(String databaseName) throws IOException {
        return getVigiangDatabases().stream()
                .filter((credentials) -> credentials.getName().equals(databaseName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Database credentials not found by name=" + databaseName));
    }

}
