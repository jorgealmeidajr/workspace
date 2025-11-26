package workspace.commons.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import workspace.commons.dao.DbSchemaDAO;
import workspace.commons.dao.OracleSchemaDAO;
import workspace.commons.dao.PostgresSchemaDAO;
import workspace.commons.model.Database;
import workspace.commons.model.DatabaseCredentials;
import workspace.commons.model.Laboratory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EnvironmentService {

    public static DbSchemaDAO getDbSchemaDAO(DatabaseCredentials databaseCredentials) {
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) return new OracleSchemaDAO();
        if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) return new PostgresSchemaDAO();
        return null;
    }

    public static <T extends Laboratory> List<T> getLaboratories(Path inputPath, Class<T> type) throws IOException {
        if (!Files.exists(inputPath)) {
            throw new IllegalStateException("Resource 'laboratories.json' not found at path: " + inputPath);
        }

        try (InputStream read = Files.newInputStream(inputPath)) {
            ObjectMapper mapper = new ObjectMapper();
            List<T> laboratories = mapper.readValue(read, mapper.getTypeFactory().constructCollectionType(List.class, type));

            validateLaboratories(laboratories);

            return laboratories.stream()
                    .filter(Laboratory::isActive)
                    .collect(Collectors.toList());
        }
    }

    private static void validateLaboratories(List<? extends Laboratory> laboratories) {
        Set<String> names = new HashSet<>();
        Set<String> aliases = new HashSet<>();
        Set<String> hosts = new HashSet<>();

        for (Laboratory lab : laboratories) {
            if (!names.add(lab.getName())) {
                throw new IllegalArgumentException("Duplicate laboratory name found: " + lab.getName());
            }
            if (!aliases.add(lab.getAlias())) {
                throw new IllegalArgumentException("Duplicate laboratory alias found: " + lab.getAlias());
            }
            if (!hosts.add(lab.getSshHost())) {
                throw new IllegalArgumentException("Duplicate laboratory sshHost found: " + lab.getSshHost());
            }
        }
    }

}
