package workspace.commons.service;

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
import java.util.function.Consumer;
import java.util.function.Function;

import static workspace.commons.service.FileService.writeString;

public class UpdateSchemasService {

    public static void execute(
            List<? extends DatabaseCredentials> databasesCredentials,
            Function<DatabaseCredentials, Callable<SchemaResult>> getCallableTask,
            Consumer<SchemaResult> handleResult)
            throws ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(databasesCredentials.size());
        List<Callable<SchemaResult>> callableTasks = new ArrayList<>();

        for (DatabaseCredentials databaseCredentials : databasesCredentials) {
            Callable<SchemaResult> callableTask = getCallableTask.apply(databaseCredentials);
            callableTasks.add(callableTask);
        }

        try {
            List<Future<SchemaResult>> futures = executorService.invokeAll(callableTasks);
            for (Future<SchemaResult> future : futures) {
                SchemaResult result = future.get();
                handleResult.accept(result);
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

    // TODO: write a simple txt with list names of all objects instead of full definitions
    public static void update(SchemaResult result, Path databaseSchemaPath) throws IOException {
//        updateLocalSchemaFiles(databaseSchemaPath, "tables", result.getTables());

        updateSchemaSql(databaseSchemaPath, "views", result.getViews());
        updateSchemaTxt(databaseSchemaPath, "views", result.getViews());

//        updateLocalSchemaFiles(databaseSchemaPath, "indexes", result.getIndexes());

        updateSchemaSql(databaseSchemaPath, "functions", result.getFunctions());
        updateSchemaTxt(databaseSchemaPath, "functions", result.getFunctions());

        updateSchemaSql(databaseSchemaPath, "procedures", result.getProcedures());

        updateSchemaSql(databaseSchemaPath, "packageBodies", result.getPackageBodies());
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

    static void updateSchemaTxt(Path path, String fileName, List<DbObjectDefinition> data) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("Path does not exist: " + path);
        }

        if (data.isEmpty()) {
            return;
        }

        var finalLines = new ArrayList<String>();
        for (DbObjectDefinition row : data) {
            String name = getValueAfterDot(row.getName());
            finalLines.add(name);
        }
        Collections.sort(finalLines);

        var result = String.join(System.lineSeparator(), finalLines);
        Path outputPath = Paths.get(path + "\\" + fileName + ".txt");
        writeString(outputPath, result);
    }

    private static String getValueAfterDot(String name) {
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
