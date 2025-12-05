package workspace.commons.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
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
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import static workspace.commons.service.FileService.writeString;

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
    }

    public static void execute(
            List<? extends DatabaseCredentials> databasesCredentials,
            Request request,
            BiFunction<DatabaseCredentials, Request, Callable<SchemaResult>> getCallableTask,
            BiConsumer<SchemaResult, Request> handleResult)
            throws ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(databasesCredentials.size());
        List<Callable<SchemaResult>> callableTasks = new ArrayList<>();

        for (DatabaseCredentials databaseCredentials : databasesCredentials) {
            Callable<SchemaResult> callableTask = getCallableTask.apply(databaseCredentials, request);
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

        var finalLines = new ArrayList<String>();
        for (String name : names) {
            if (Database.ORACLE.equals(database)) {
                name = getValueAfterDot(name);
            }

            finalLines.add(name);
        }
        Collections.sort(finalLines);

        var result = String.join(System.lineSeparator(), finalLines);
        Path outputPath = Paths.get(path + "\\" + fileName + ".txt");
        writeString(outputPath, result);
    }

    public static String getValueAfterDot(String name) {
        int dotIndex = name.indexOf(".");
        return dotIndex >= 0 ? name.substring(dotIndex + 1) : name;
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

}
