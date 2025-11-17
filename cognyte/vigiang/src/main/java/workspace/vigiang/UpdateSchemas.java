package workspace.vigiang;

import workspace.commons.dao.DbSchemaDAO;
import workspace.commons.model.Database;
import workspace.commons.model.DbObjectDefinition;
import workspace.commons.model.SchemaResult;
import workspace.vigiang.model.DatabaseCredentialsVigiaNG;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class UpdateSchemas {

    public static void main(String[] args) {
        System.out.println("\n## START checking all database schemas\n");
        try {
            List<DatabaseCredentialsVigiaNG> databasesCredentials = EnvironmentService.getVigiangDatabases();
            execute(databasesCredentials);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("## END checking all database schemas.");
    }

    private static void execute(List<DatabaseCredentialsVigiaNG> databasesCredentials) throws IOException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(databasesCredentials.size());
        List<Callable<SchemaResult>> callableTasks = new ArrayList<>();

        for (DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG : databasesCredentials) {
            Callable<SchemaResult> callableTask = getCallableTask(databaseCredentialsVigiaNG);
            callableTasks.add(callableTask);
        }

        try {
            List<Future<SchemaResult>> futures = executorService.invokeAll(callableTasks);
            for (Future<SchemaResult> future : futures) {
                handleResult(future);
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

    private static void handleResult(Future<SchemaResult> future) throws InterruptedException, ExecutionException, IOException {
        SchemaResult result = future.get();
        DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG = (DatabaseCredentialsVigiaNG) result.getDatabaseCredentials();
        Path databaseSchemaPath = EnvironmentService.getDatabaseSchemaPath(databaseCredentialsVigiaNG);
        System.out.println(databaseCredentialsVigiaNG.getName() + ":");

//        updateLocalSchemaFiles(databaseSchemaPath, "tables", result.getTables());
        updateLocalSchemaFiles(databaseSchemaPath, "views", result.getViews());
//        updateLocalSchemaFiles(databaseSchemaPath, "indexes", result.getIndexes());
        updateLocalSchemaFiles(databaseSchemaPath, "functions", result.getFunctions());

        if (Database.POSTGRES.equals(databaseCredentialsVigiaNG.getDatabase())) {
            updateLocalSchemaFiles(databaseSchemaPath, "procedures", result.getProcedures());
        }

        if (Database.ORACLE.equals(databaseCredentialsVigiaNG.getDatabase())) {
            updateLocalSchemaFiles(databaseSchemaPath, "packageBodies", result.getPackageBodies());
        }

        System.out.println();
    }

    private static void updateLocalSchemaFiles(Path databaseSchemaPath, String fileName, List<DbObjectDefinition> data) throws IOException {
        var finalLines = new ArrayList<String>();
        for (DbObjectDefinition row : data) {
            String rowDefinitionStr = getRowDefinitionStr(row);
            finalLines.add(rowDefinitionStr);
        }

        var newFileContent = String.join(System.lineSeparator(), finalLines);

        Path finalFilePath = Paths.get(databaseSchemaPath + "\\" + fileName + ".sql");

        var initialFileContent = "";
        if (Files.exists(finalFilePath)) {
            initialFileContent = new String(Files.readAllBytes(finalFilePath));
        }

        if (!initialFileContent.equals(newFileContent)) {
            System.out.println("updating file: " + finalFilePath);
            Files.writeString(finalFilePath, newFileContent, StandardCharsets.UTF_8);
        }
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

    private static Callable<SchemaResult> getCallableTask(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) {
        return () -> {
            DbSchemaDAO dao = EnvironmentService.getDbSchemaDAO(databaseCredentialsVigiaNG);

            // TODO: oracle, create tables statements must be simplify
            // TODO: postgres, create tables statements should be in create sql format
            List<DbObjectDefinition> tables = dao.listTables(databaseCredentialsVigiaNG);

            List<DbObjectDefinition> views = dao.listViews(databaseCredentialsVigiaNG);
            List<DbObjectDefinition> functions = dao.listFunctions(databaseCredentialsVigiaNG);
            List<DbObjectDefinition> indexes = dao.listIndexes(databaseCredentialsVigiaNG);
            List<DbObjectDefinition> procedures = dao.listProcedures(databaseCredentialsVigiaNG);
            List<DbObjectDefinition> packageBodies = dao.listPackageBodies(databaseCredentialsVigiaNG);

            return new SchemaResult(databaseCredentialsVigiaNG, tables, views, functions, indexes, procedures, packageBodies);
        };
    }

}
