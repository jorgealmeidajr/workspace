package workspace.vigiang.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import workspace.vigiang.dao.*;
import workspace.vigiang.model.DatabaseCredentials;
import workspace.vigiang.model.Laboratory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class EnvironmentService {

    private static final String VIGIANG_PATH_STR = "C:\\work\\COGNYTE1\\VIGIANG";
    private static final String VIGIANG_LABORATORIES_PATH = "C:\\work\\COGNYTE1\\vigiang_labs";
    private static final String VIGIANG_DATABASES_PATH = "C:\\work\\COGNYTE1\\vigiang_dbs";

    public static List<DatabaseCredentials> getVigiangDatabases() throws IOException {
        Path path = Paths.get(VIGIANG_DATABASES_PATH + "\\databases.json");
        String read = Files.readString(path);

        ObjectMapper mapper = new ObjectMapper();
        List<DatabaseCredentials> databaseCredentials = mapper.readValue(read, new TypeReference<>(){});
        return databaseCredentials.stream()
                .filter(DatabaseCredentials::isActive)
                .collect(Collectors.toList());
    }

    public static List<Laboratory> getVigiangLaboratories() throws IOException {
        Path path = Paths.get(VIGIANG_LABORATORIES_PATH + "\\laboratories.json");
        String read = Files.readString(path);

        ObjectMapper mapper = new ObjectMapper();
        List<Laboratory> laboratories = mapper.readValue(read, new TypeReference<>(){});
        return laboratories.stream()
                .filter(Laboratory::isActive)
                .collect(Collectors.toList());
    }

    public static VigiaNgDAO getVigiaNgDAO(DatabaseCredentials databaseCredentials) {
        if (DatabaseCredentials.Database.ORACLE.equals(databaseCredentials.getDatabase())) return new OracleVigiaNgDAO();
        if (DatabaseCredentials.Database.POSTGRES.equals(databaseCredentials.getDatabase())) return new PostgresVigiaNgDAO();
        return null;
    }

    public static DbSchemaDAO getDbSchemaDAO(DatabaseCredentials databaseCredentials) {
        if (DatabaseCredentials.Database.ORACLE.equals(databaseCredentials.getDatabase())) return new OracleSchemaDAO();
        if (DatabaseCredentials.Database.POSTGRES.equals(databaseCredentials.getDatabase())) return new PostgresSchemaDAO();
        return null;
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

    public static Path getEnvironmentPath(DatabaseCredentials databaseCredentials) throws IOException {
        Path vigiangPath = getVigiaNgPath();

        Path environmentPath = Paths.get(vigiangPath + "\\laboratories\\" + databaseCredentials.getName());
        if (!Files.exists(environmentPath)) {
            Files.createDirectories(environmentPath);
        }
        return environmentPath;
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
        Path environmentPath = EnvironmentService.getEnvironmentPath(databaseCredentials);
        String database = databaseCredentials.getDatabase().toString().toLowerCase();

        Path databaseDataPath = Paths.get(environmentPath + "\\" + database + "_schema");
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

}
