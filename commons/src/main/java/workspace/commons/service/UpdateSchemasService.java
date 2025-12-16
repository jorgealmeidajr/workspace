package workspace.commons.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import workspace.commons.dao.DbSchemaDAO;
import workspace.commons.model.Database;
import workspace.commons.model.DatabaseCredentials;
import workspace.commons.model.DbObjectDefinition;
import workspace.commons.model.SchemaResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static workspace.commons.service.FileService.writeString;
import static workspace.commons.utils.StringUtils.getValueAfterDot;
import static workspace.commons.utils.StringUtils.getValueBeforeDot;

public class UpdateSchemasService {

    @AllArgsConstructor
    @Getter
    @Builder
    public static class Request {
        boolean updateTablesDefinitions;
        boolean updateViewsDefinitions;
        boolean updateIndexesDefinitions;
        boolean updateFunctionsDefinitions;
        boolean updateProceduresDefinitions;
        boolean updatePackageBodiesDefinitions;

        BiPredicate<String, Database> tablesFilter;
        BiPredicate<String, Database> viewsFilter;
        BiPredicate<String, Database> indexesFilter;
        BiPredicate<String, Database> functionsFilter;
        BiPredicate<String, Database> proceduresFilter;
        BiPredicate<String, Database> packageBodiesFilter;
    }

    public static void execute(
            List<? extends DatabaseCredentials> databasesCredentials,
            Request request,
            BiConsumer<SchemaResult, Request> handleResult)
            throws ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(databasesCredentials.size());
        List<Callable<SchemaResult>> callableTasks = new ArrayList<>();

        for (DatabaseCredentials databaseCredentials : databasesCredentials) {
            Callable<SchemaResult> callableTask = getCallableTask(databaseCredentials, request);
            callableTasks.add(callableTask);
        }

        try {
            List<Future<SchemaResult>> futures = executorService.invokeAll(callableTasks);
            for (Future<SchemaResult> future : futures) {
                SchemaResult result = future.get();
                handleResult.accept(result, request);
            }

            if (!executorService.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        } finally {
            if (!executorService.isShutdown()) {
                executorService.shutdown();
            }
        }
    }

    public static void update(SchemaResult result, Path databaseSchemaPath, Request request) throws IOException {
        Database database = result.getDatabaseCredentials().getDatabase();

        updateSchemaTxt(databaseSchemaPath, "tables", result.getTablesNames(), database);
        updateSchemaTxt(databaseSchemaPath, "views", result.getViewsNames(), database);
        updateSchemaTxt(databaseSchemaPath, "indexes", result.getIndexesNames(), database);
        updateSchemaTxt(databaseSchemaPath, "functions", result.getFunctionsNames(), database);
        updateSchemaTxt(databaseSchemaPath, "procedures", result.getProceduresNames(), database);
        updateSchemaTxt(databaseSchemaPath, "packageBodies", result.getPackageBodiesNames(), database);

        if (request.isUpdateTablesDefinitions()) {
            updateSchemaSql(databaseSchemaPath, "tables", result.getTables());
        }
        if (request.isUpdateViewsDefinitions()) {
            updateSchemaSql(databaseSchemaPath, "views", result.getViews());
        }
        if (request.isUpdateIndexesDefinitions()) {
            updateSchemaSql(databaseSchemaPath, "indexes", result.getIndexes());
        }
        if (request.isUpdateFunctionsDefinitions()) {
            updateSchemaSql(databaseSchemaPath, "functions", result.getFunctions());
        }
        if (request.isUpdateProceduresDefinitions()) {
            updateSchemaSql(databaseSchemaPath, "procedures", result.getProcedures());
        }
        if (request.isUpdatePackageBodiesDefinitions()) {
            updateSchemaSql(databaseSchemaPath, "packageBodies", result.getPackageBodies());
        }
    }

    static void updateSchemaSql(Path path, String fileName, List<DbObjectDefinition> data) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("Path does not exist: " + path);
        }

        if (data.isEmpty()) {
            return;
        }

        var finalLines = new ArrayList<String>();
        for (DbObjectDefinition row : data) {
            String rowDefinitionStr = getRowDefinitionStr(row);
            finalLines.add(rowDefinitionStr);
        }

        var result = String.join(System.lineSeparator(), finalLines);
        Path outputPath = Paths.get(path + "\\" + fileName + ".sql");
        writeString(outputPath, result);
    }

    static void updateSchemaTxt(Path path, String fileName, List<String> names, Database database) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("Path does not exist: " + path);
        }

        if (names.isEmpty()) {
            return;
        }

        var postgresSchemasToSkip = List.of("information_schema", "pg_catalog", "pgagent");

        var finalLines = new ArrayList<String>();
        for (String name : names) {
            if (Database.ORACLE.equals(database)) {
                name = getValueAfterDot(name);
            }

            if (Database.POSTGRES.equals(database)) {
                var schema = getValueBeforeDot(name);
                if (postgresSchemasToSkip.contains(schema)) {
                    continue;
                }
            }

            finalLines.add(name);
        }
        Collections.sort(finalLines);

        var result = String.join(System.lineSeparator(), finalLines);
        Path outputPath = Paths.get(path + "\\" + fileName + ".txt");
        writeString(outputPath, result);
    }

    private static String getRowDefinitionStr(DbObjectDefinition row) {
        String name = row.getName();
        int dotIndex = name.indexOf(".");
        String nameAfterDot = dotIndex >= 0 ? name.substring(dotIndex + 1) : name;

        String rowDefinitionStr = "-- " + "#".repeat(120) + "\n";
        rowDefinitionStr += "-- " + nameAfterDot + "\n";
        rowDefinitionStr += row.getDefinition().trim();
        if (!rowDefinitionStr.trim().endsWith(";")) {
            rowDefinitionStr += ";";
        }
        rowDefinitionStr += "\n\n";
        return rowDefinitionStr;
    }

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
