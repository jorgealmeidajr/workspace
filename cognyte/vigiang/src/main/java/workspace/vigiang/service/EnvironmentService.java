package workspace.vigiang.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import workspace.commons.model.Database;
import workspace.commons.model.Laboratory;
import workspace.vigiang.dao.OracleVigiaNgDAO;
import workspace.vigiang.dao.PostgresVigiaNgDAO;
import workspace.vigiang.dao.VigiaNgDAO;
import workspace.vigiang.model.DatabaseCredentialsVigiaNG;
import workspace.vigiang.model.LaboratoryVigiaNg;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static workspace.commons.service.EnvironmentService.getLaboratories;

public class EnvironmentService {

    private static String getWorkPathStr() {
        return ConfigService.getWorkPath();
    }

    private static String getCognytePathStr() {
        return getWorkPathStr() + "\\COGNYTE";
    }

    private static String getVigiaNgPathStr() {
        return getCognytePathStr() + "\\VIGIANG";
    }

    private static String getVigiaNgLaboratoriesPathStr() {
        return getVigiaNgPathStr() + "\\laboratories";
    }

    private static String getVigiaNgDatabasesPathStr() {
        return getVigiaNgPathStr() + "\\databases";
    }

    public static List<String> getVersions() {
        // todo: add 3.2
        return List.of(
            "2.2",
            "2.3",
            "3.1"
        );
    }

    public static List<DatabaseCredentialsVigiaNG> getDatabasesVigiaNgActive() throws IOException {
        Path inputPath = Paths.get(getVigiaNgPath() + "\\databases.json");
        try (InputStream read = Files.newInputStream(inputPath)) {
            return getDatabaseCredentials(read).stream()
                    .filter(DatabaseCredentialsVigiaNG::isActive)
                    .collect(Collectors.toList());
        }
    }

    private static List<DatabaseCredentialsVigiaNG> getDatabaseCredentials(InputStream read) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(read, new TypeReference<>(){});
    }

    public static List<DatabaseCredentialsVigiaNG> getDatabasesVigiaNgInactive() throws IOException {
        Path inputPath = Paths.get(getVigiaNgPath() + "\\databases.json");
        try (InputStream read = Files.newInputStream(inputPath)) {
            return getDatabaseCredentials(read).stream()
                    .filter(credentials -> !credentials.isActive())
                    .collect(Collectors.toList());
        }
    }

    public static List<LaboratoryVigiaNg> getLaboratoriesVigiaNgActive() throws IOException {
        Path inputPath = Paths.get(getVigiaNgPath() + "\\laboratories.json");
        return getLaboratories(inputPath, LaboratoryVigiaNg.class).stream()
                .filter(Laboratory::isActive)
                .collect(Collectors.toList());
    }

    public static List<LaboratoryVigiaNg> getLaboratoriesVigiaNgInactive() throws IOException {
        Path inputPath = Paths.get(getVigiaNgPath() + "\\laboratories.json");
        return getLaboratories(inputPath, LaboratoryVigiaNg.class).stream()
                .filter(laboratory -> !laboratory.isActive())
                .collect(Collectors.toList());
    }

    public static VigiaNgDAO getVigiaNgDAO(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) {
        if (Database.ORACLE.equals(databaseCredentialsVigiaNG.getDatabase())) return new OracleVigiaNgDAO(databaseCredentialsVigiaNG);
        if (Database.POSTGRES.equals(databaseCredentialsVigiaNG.getDatabase())) return new PostgresVigiaNgDAO(databaseCredentialsVigiaNG);
        return null;
    }

    public static Path getCognytePath() {
        Path vigiangPath = Paths.get(getCognytePathStr());
        if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
            throw new IllegalArgumentException("o diretorio do cognyte nao existe ou nao eh um diretorio");
        }
        return vigiangPath;
    }

    public static Path getVigiaNgPath() {
        Path vigiangPath = Paths.get(getVigiaNgPathStr());
        if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
            throw new IllegalArgumentException("o diretorio do vigiang nao existe ou nao eh um diretorio");
        }
        return vigiangPath;
    }

    public static Path getVigiaNgLaboratoriesPath() {
        Path vigiangPath = Paths.get(getVigiaNgLaboratoriesPathStr());
        if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
            throw new IllegalArgumentException("o diretorio do vigiang laboratories nao existe ou nao eh um diretorio");
        }
        return vigiangPath;
    }

    public static Path getLaboratoryPath(LaboratoryVigiaNg laboratoryVigiaNg) throws IOException {
        Path vigiaNgLaboratoriesPath = getVigiaNgLaboratoriesPath();

        Path laboratoryPath;
        if (laboratoryVigiaNg.getCarrier() == null) {
            laboratoryPath = Paths.get(vigiaNgLaboratoriesPath + "\\" + laboratoryVigiaNg.getName());
        } else {
            laboratoryPath = Paths.get(vigiaNgLaboratoriesPath + "\\" + laboratoryVigiaNg.getCarrier() + "\\" + laboratoryVigiaNg.getName());
        }

        if (!Files.exists(laboratoryPath)) {
            Files.createDirectories(laboratoryPath);
        }
        return laboratoryPath;
    }

    public static Path getDatabaseDataPath(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws IOException {
        Path environmentPath = Paths.get(getVigiaNgDatabasesPathStr());
        String database = databaseCredentialsVigiaNG.getDatabase().toString();

        Path databaseDataPath = Paths.get(environmentPath + "\\" + database + "\\" + databaseCredentialsVigiaNG.getName() + "\\data");
        if (!Files.exists(databaseDataPath)) {
            Files.createDirectories(databaseDataPath);
        }
        return databaseDataPath;
    }

    public static Path getDatabaseSchemaPath(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws IOException {
        Path environmentPath = getDatabasePath(databaseCredentialsVigiaNG);
        Path databaseDataPath = Paths.get(environmentPath + "\\schema");
        if (!Files.exists(databaseDataPath)) {
            Files.createDirectories(databaseDataPath);
        }
        return databaseDataPath;
    }

    public static Path getDatabasePath(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws IOException {
        Path environmentPath = Paths.get(getVigiaNgDatabasesPathStr());
        String database = databaseCredentialsVigiaNG.getDatabase().toString();

        Path databaseDataPath = Paths.get(environmentPath + "\\" + database + "\\" + databaseCredentialsVigiaNG.getName());
        if (!Files.exists(databaseDataPath)) {
            Files.createDirectories(databaseDataPath);
        }
        return databaseDataPath;
    }

    public static Path getEmailTemplatesPath(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws IOException {
        Path environmentPath = getDatabasePath(databaseCredentialsVigiaNG);

        Path emailTemplatesPath = Paths.get(environmentPath + "\\email_templates");
        if (!Files.exists(emailTemplatesPath)) {
            Files.createDirectories(emailTemplatesPath);
        }
        return emailTemplatesPath;
    }

    public static Path getReportTemplatesPath(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws IOException {
        Path environmentPath = getDatabasePath(databaseCredentialsVigiaNG);

        Path reportTemplatesPath = Paths.get(environmentPath + "\\report_templates");
        if (!Files.exists(reportTemplatesPath)) {
            Files.createDirectories(reportTemplatesPath);
        }
        return reportTemplatesPath;
    }

    public static DatabaseCredentialsVigiaNG getDatabaseCredentials(String databaseName) throws IOException {
        return getDatabasesVigiaNgActive().stream()
                .filter((credentials) -> credentials.getName().equals(databaseName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Database credentials not found by name=" + databaseName));
    }

    public static void validateProjectDirectories(String workDir, String version) {
        Path backendPath = Paths.get(workDir + "\\" + version + "\\back-" + version);
        if (!Files.exists(backendPath) || !Files.isDirectory(backendPath)) {
            throw new IllegalArgumentException("o diretorio backendPath nao existe ou nao eh um diretorio");
        }

        Path frontendPath = Paths.get(workDir + "\\" + version + "\\front-" + version);
        if (!Files.exists(frontendPath) || !Files.isDirectory(frontendPath)) {
            throw new IllegalArgumentException("o diretorio frontendPath nao existe ou nao eh um diretorio");
        }

        Path versionPath = Paths.get(EnvironmentService.getVigiaNgPath() + "\\versions\\" + version);
        if (!Files.exists(versionPath) || !Files.isDirectory(versionPath)) {
            throw new IllegalArgumentException("o diretorio versionPath nao existe ou nao eh um diretorio");
        }
    }

    public static String getWorkVigiaDir() {
        return getWorkPathStr() + "\\vigiang";
    }

}
