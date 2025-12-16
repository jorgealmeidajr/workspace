package workspace.vigiang;

import workspace.commons.model.Database;
import workspace.commons.model.SchemaResult;
import workspace.vigiang.model.DatabaseCredentialsVigiaNG;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static workspace.commons.service.UpdateSchemasService.*;
import static workspace.commons.utils.StringUtils.getValueAfterDot;

public class UpdateSchemas {

    public static void main(String[] args) {
        System.out.println("\n## START checking all database schemas\n");
        try {
            List<DatabaseCredentialsVigiaNG> databasesCredentials = EnvironmentService.getDatabasesVigiaNg();
            Request request = getRequest();
            execute(databasesCredentials, request, UpdateSchemas::handleResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("## END checking all database schemas.");
    }

    private static Request getRequest() {
        final var postgresSchemas = List.of("api", "conf", "dash", "evt", "gen", "itc", "log", "ofc", "prog", "public", "sec", "sync");
        final var oraclePrefixes = List.of("ITC", "CFG", "LOG", "SIT", "SEG", "OFC", "PTB", "QDS", "LOC");
        final var oraclePackagePrefixes = List.of("PITC", "PCFG", "PLOG", "PSIT", "PSEG", "POFC", "PPTB", "PQDS", "PLOC");

        boolean update = false;

        return Request.builder()
                .updateTablesDefinitions(false)
                .updateViewsDefinitions(update)
                .updateIndexesDefinitions(false)
                .updateFunctionsDefinitions(update)
                .updateProceduresDefinitions(update)
                .updatePackageBodiesDefinitions(update)
                .tablesFilter((String name, Database database) -> {
                    if (Database.ORACLE.equals(database)) {
                        name = getValueAfterDot(name);
                        String prefix = name.contains("_") ? name.substring(0, name.indexOf("_")) : name;
                        return oraclePrefixes.contains(prefix);
                    } else if (Database.POSTGRES.equals(database)) {
                        if (!name.contains(".")) return false;
                        String schema = name.contains(".") ? name.substring(0, name.indexOf(".")) : name;
                        return postgresSchemas.contains(schema);
                    }
                    return false;
                })
                .viewsFilter((String name, Database database) -> {
                    if (Database.ORACLE.equals(database)) {
                        name = getValueAfterDot(name);
                        return name.startsWith("VW_NG_");
                    } else if (Database.POSTGRES.equals(database)) {
                        if (!name.contains(".")) return false;
                        String schema = name.substring(0, name.indexOf("."));
                        return postgresSchemas.contains(schema);
                    }
                    return false;
                })
                .functionsFilter((String name, Database database) -> {
                    if (Database.ORACLE.equals(database)) {
                        name = getValueAfterDot(name);
                        return name.startsWith("FN_NG_");
                    } else if (Database.POSTGRES.equals(database)) {
                        if (!name.contains(".")) return false;
                        String schema = name.substring(0, name.indexOf("."));
                        return postgresSchemas.contains(schema);
                    }
                    return false;
                })
                .indexesFilter((String name, Database database) -> {
                    if (Database.ORACLE.equals(database)) {
                        name = getValueAfterDot(name);
                        String prefix = name.contains("_") ? name.substring(0, name.indexOf("_")) : name;
                        return oraclePrefixes.contains(prefix);
                    } else if (Database.POSTGRES.equals(database)) {
                        if (!name.contains(".")) return false;
                        String schema = name.substring(0, name.indexOf("."));
                        return postgresSchemas.contains(schema);
                    }
                    return false;
                })
                .proceduresFilter((String name, Database database) -> {
                    if (Database.POSTGRES.equals(database)) {
                        if (!name.contains(".")) return false;
                        String schema = name.substring(0, name.indexOf("."));
                        return postgresSchemas.contains(schema);
                    }
                    return false;
                })
                .packageBodiesFilter((String name, Database database) -> {
                    if (Database.ORACLE.equals(database)) {
                        name = getValueAfterDot(name);
                        String prefix = name.contains("_") ? name.substring(0, name.indexOf("_")) : name;
                        return oraclePackagePrefixes.contains(prefix);
                    }
                    return false;
                })
                .build();
    }

    private static void handleResult(SchemaResult result, Request request) {
        DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG = (DatabaseCredentialsVigiaNG) result.getDatabaseCredentials();

        try {
            Path databaseSchemaPath = EnvironmentService.getDatabaseSchemaPath(databaseCredentialsVigiaNG);
            System.out.println(databaseCredentialsVigiaNG.getName() + ":");

            update(result, databaseSchemaPath, request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println();
    }

}
