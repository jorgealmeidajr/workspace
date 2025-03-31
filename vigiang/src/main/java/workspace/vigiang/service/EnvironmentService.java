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

}
