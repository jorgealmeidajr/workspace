package workspace.vigiang.checkers;

import workspace.vigiang.dao.DbSchemaDAO;
import workspace.vigiang.model.DbObjectDefinition;
import workspace.vigiang.model.DatabaseCredentials;
import workspace.vigiang.model.SchemaResult;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CheckSchemas {

    public static void main(String[] args) {
        System.out.println("\n## START checking all database schemas\n");
        try {
            execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("## END checking all database schemas.");
    }

    private static void execute() throws IOException, ExecutionException {
        List<DatabaseCredentials> databasesCredentials = EnvironmentService.getVigiangDatabases();
        ExecutorService executorService = Executors.newFixedThreadPool(databasesCredentials.size());
        List<Callable<SchemaResult>> callableTasks = new ArrayList<>();

        for (DatabaseCredentials databaseCredentials : databasesCredentials) {
            Callable<SchemaResult> callableTask = getCallableTask(databaseCredentials);
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
        }
    }

    private static void handleResult(Future<SchemaResult> future) throws InterruptedException, ExecutionException, IOException {
        SchemaResult result = future.get();
        DatabaseCredentials databaseCredentials = result.getDatabaseCredentials();
        Path databaseSchemaPath = EnvironmentService.getDatabaseSchemaPath(databaseCredentials);
        System.out.println(databaseCredentials.getName() + ":");

//        updateLocalSchemaFiles(databaseSchemaPath, "tables", result.getTables());
        updateLocalSchemaFiles(databaseSchemaPath, "views", result.getViews());
//        updateLocalSchemaFiles(databaseSchemaPath, "indexes", result.getIndexes());
        updateLocalSchemaFiles(databaseSchemaPath, "functions", result.getFunctions());

        if (DatabaseCredentials.Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
            updateLocalSchemaFiles(databaseSchemaPath, "procedures", result.getProcedures());
        }

        if (DatabaseCredentials.Database.ORACLE.equals(databaseCredentials.getDatabase())) {
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

    private static Callable<SchemaResult> getCallableTask(DatabaseCredentials databaseCredentials) {
        return () -> {
            DbSchemaDAO dao = EnvironmentService.getDbSchemaDAO(databaseCredentials);

            // TODO: oracle, tables must be simplify
            // TODO: postgres, tables should be in create sql
            List<DbObjectDefinition> tables = dao.listTables(databaseCredentials);
            List<DbObjectDefinition> views = dao.listViews(databaseCredentials);
            List<DbObjectDefinition> functions = dao.listFunctions(databaseCredentials);
            List<DbObjectDefinition> indexes = dao.listIndexes(databaseCredentials);
            List<DbObjectDefinition> procedures = dao.listProcedures(databaseCredentials);
            List<DbObjectDefinition> packageBodies = dao.listPackageBodies(databaseCredentials);

            return new SchemaResult(databaseCredentials, tables, views, functions, indexes, procedures, packageBodies);
        };
    }

}
