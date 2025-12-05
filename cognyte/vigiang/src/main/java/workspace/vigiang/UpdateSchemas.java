package workspace.vigiang;

import workspace.commons.dao.DbSchemaDAO;
import workspace.commons.model.Database;
import workspace.commons.model.DatabaseCredentials;
import workspace.commons.model.DbObjectDefinition;
import workspace.commons.model.SchemaResult;
import workspace.vigiang.model.DatabaseCredentialsVigiaNG;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static workspace.commons.service.UpdateSchemasService.*;

public class UpdateSchemas {

    public static void main(String[] args) {
        System.out.println("\n## START checking all database schemas\n");
        try {
            List<DatabaseCredentialsVigiaNG> databasesCredentials = EnvironmentService.getVigiangDatabases();
            Request request = getRequest();
            execute(databasesCredentials, request, UpdateSchemas::getCallableTask, UpdateSchemas::handleResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("## END checking all database schemas.");
    }

    private static Request getRequest() {
        final var postgresSchemas = List.of("api", "conf", "dash", "evt", "gen", "itc", "log", "ofc", "prog", "public", "sec", "sync");
        final var oraclePrefixes = List.of("ITC", "CFG", "LOG", "SIT", "SEG", "OFC", "PTB", "QDS", "LOC");
        final var oraclePackagePrefixes = List.of("PITC", "PCFG", "PLOG", "PSIT", "PSEG", "POFC", "PPTB", "PQDS", "PLOC");

        boolean update = true;

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

    // TODO: move to commons
    private static Callable<SchemaResult> getCallableTask(DatabaseCredentials databaseCredentials, Request request) {
        return () -> {
            DbSchemaDAO dao = workspace.commons.service.EnvironmentService.getDbSchemaDAO(databaseCredentials);

            var tablesNames = dao.listTablesNames();

            List<DbObjectDefinition> tables = List.of();
            if (request.isUpdateTablesDefinitions()) {
                var tablesNamesFiltered = tablesNames.stream()
                        .filter(name -> request.getTablesFilter().test(name, databaseCredentials.getDatabase()))
                        .collect(Collectors.toList());

                tables = dao.listTablesDefinitions(tablesNamesFiltered);
            }

            var viewsNames = dao.listViewsNames();

            List<DbObjectDefinition> views = List.of();
            if (request.isUpdateViewsDefinitions()) {
                var viewsNamesFiltered = viewsNames.stream()
                        .filter(name -> request.getViewsFilter().test(name, databaseCredentials.getDatabase()))
                        .collect(Collectors.toList());

                views = dao.listViewsDefinitions(viewsNamesFiltered);
            }

            var functionsNames = dao.listFunctionsNames();

            List<DbObjectDefinition> functions = List.of();
            if (request.isUpdateFunctionsDefinitions()) {
                var functionsNamesFiltered = functionsNames.stream()
                        .filter(name -> request.getFunctionsFilter().test(name, databaseCredentials.getDatabase()))
                        .collect(Collectors.toList());

                functions = dao.listFunctionsDefinitions(functionsNamesFiltered);
            }

            var indexesNames = dao.listIndexesNames();

            List<DbObjectDefinition> indexes = List.of();
            if (request.isUpdateIndexesDefinitions()) {
                var indexesNamesFiltered = indexesNames.stream()
                        .filter(name -> request.getIndexesFilter().test(name, databaseCredentials.getDatabase()))
                        .collect(Collectors.toList());

                indexes = dao.listIndexesDefinitions(indexesNamesFiltered);
            }

            var proceduresNames = dao.listProceduresNames();

            List<DbObjectDefinition> procedures = List.of();
            if (request.isUpdateProceduresDefinitions()) {
                var proceduresNamesFiltered = proceduresNames.stream()
                        .filter(name -> request.getProceduresFilter().test(name, databaseCredentials.getDatabase()))
                        .collect(Collectors.toList());

                procedures = dao.listProceduresDefinitions(proceduresNamesFiltered);
            }

            var packageBodiesNames = dao.listPackageBodiesNames();

            List<DbObjectDefinition> packageBodies = List.of();
            if (request.isUpdatePackageBodiesDefinitions()) {
                var packageBodiesNamesFiltered = packageBodiesNames.stream()
                        .filter(name -> request.getPackageBodiesFilter().test(name, databaseCredentials.getDatabase()))
                        .collect(Collectors.toList());

                packageBodies = dao.listPackageBodiesDefinitions(packageBodiesNamesFiltered);
            }

            return SchemaResult.builder()
                    .databaseCredentials(databaseCredentials)
                    .tablesNames(tablesNames)
                    .tables(tables)
                    .viewsNames(viewsNames)
                    .views(views)
                    .functionsNames(functionsNames)
                    .functions(functions)
                    .indexesNames(indexesNames)
                    .indexes(indexes)
                    .proceduresNames(proceduresNames)
                    .procedures(procedures)
                    .packageBodiesNames(packageBodiesNames)
                    .packageBodies(packageBodies)
                    .build();
        };
    }

}
