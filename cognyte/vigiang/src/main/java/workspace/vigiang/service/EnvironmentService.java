package workspace.vigiang.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import workspace.commons.model.Database;
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

    private static final String WORK_PATH_STR = "C:\\work";
    private static final String COGNYTE_PATH_STR = WORK_PATH_STR + "\\COGNYTE";
    private static final String VIGIANG_PATH_STR = COGNYTE_PATH_STR + "\\VIGIANG";
    private static final String VIGIANG_LABORATORIES_PATH = VIGIANG_PATH_STR + "\\laboratories";
    private static final String VIGIANG_DATABASES_PATH = VIGIANG_PATH_STR + "\\databases";

    public static List<String> getVersions() {
        return List.of("1.5", "1.7", "2.0", "2.1", "2.2", "3.0");
    }

    public static List<DatabaseCredentialsVigiaNG> getDatabasesVigiaNg() throws IOException {
        Path inputPath = Paths.get(getVigiaNgPath() + "\\databases.json");
        try (InputStream read = Files.newInputStream(inputPath)) {
            return getDatabaseCredentials(read);
        }
    }

    private static List<DatabaseCredentialsVigiaNG> getDatabaseCredentials(InputStream read) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<DatabaseCredentialsVigiaNG> databaseCredentialVigiaNGS = mapper.readValue(read, new TypeReference<>(){});
        return databaseCredentialVigiaNGS.stream()
                .filter(DatabaseCredentialsVigiaNG::isActive)
                .collect(Collectors.toList());
    }

    public static List<LaboratoryVigiaNg> getLaboratoriesVigiaNg() throws IOException {
        Path inputPath = Paths.get(getVigiaNgPath() + "\\laboratories.json");
        return getLaboratories(inputPath, LaboratoryVigiaNg.class);
    }

    public static VigiaNgDAO getVigiaNgDAO(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) {
        if (Database.ORACLE.equals(databaseCredentialsVigiaNG.getDatabase())) return new OracleVigiaNgDAO(databaseCredentialsVigiaNG);
        if (Database.POSTGRES.equals(databaseCredentialsVigiaNG.getDatabase())) return new PostgresVigiaNgDAO(databaseCredentialsVigiaNG);
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
        Path environmentPath = Paths.get(VIGIANG_DATABASES_PATH);
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
        Path environmentPath = Paths.get(VIGIANG_DATABASES_PATH);
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
        return getDatabasesVigiaNg().stream()
                .filter((credentials) -> credentials.getName().equals(databaseName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Database credentials not found by name=" + databaseName));
    }

}
