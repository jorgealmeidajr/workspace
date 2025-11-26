package workspace.vigiang;

import workspace.commons.dao.DbSchemaDAO;
import workspace.commons.model.Database;
import workspace.commons.model.DatabaseCredentials;
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
import java.util.function.Consumer;
import java.util.function.Function;

import static workspace.commons.service.FileService.writeString;

public class UpdateSchemas {

    public static void main(String[] args) {
        System.out.println("\n## START checking all database schemas\n");
        try {
            List<DatabaseCredentialsVigiaNG> databasesCredentials = EnvironmentService.getVigiangDatabases();
            execute(databasesCredentials, UpdateSchemas::getCallableTask, UpdateSchemas::handleResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("## END checking all database schemas.");
    }

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

    private static void handleResult(SchemaResult result) {
        DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG = (DatabaseCredentialsVigiaNG) result.getDatabaseCredentials();

        try {
            Path databaseSchemaPath = EnvironmentService.getDatabaseSchemaPath(databaseCredentialsVigiaNG);
            System.out.println(databaseCredentialsVigiaNG.getName() + ":");

//            updateLocalSchemaFiles(databaseSchemaPath, "tables", result.getTables());
            updateLocalSchemaFiles(databaseSchemaPath, "views", result.getViews());
//            updateLocalSchemaFiles(databaseSchemaPath, "indexes", result.getIndexes());
            updateLocalSchemaFiles(databaseSchemaPath, "functions", result.getFunctions());

            if (Database.POSTGRES.equals(databaseCredentialsVigiaNG.getDatabase())) {
                updateLocalSchemaFiles(databaseSchemaPath, "procedures", result.getProcedures());
            }

            if (Database.ORACLE.equals(databaseCredentialsVigiaNG.getDatabase())) {
                updateLocalSchemaFiles(databaseSchemaPath, "packageBodies", result.getPackageBodies());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println();
    }

    private static void updateLocalSchemaFiles(Path databaseSchemaPath, String fileName, List<DbObjectDefinition> data) throws IOException {
        if (data.isEmpty()) {
            return;
        }

        var finalLines = new ArrayList<String>();
        for (DbObjectDefinition row : data) {
            String rowDefinitionStr = getRowDefinitionStr(row);
            finalLines.add(rowDefinitionStr);
        }

        var result = String.join(System.lineSeparator(), finalLines);
        Path outputPath = Paths.get(databaseSchemaPath + "\\" + fileName + ".sql");
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

    private static Callable<SchemaResult> getCallableTask(DatabaseCredentials databaseCredentials) {
        return () -> {
            DbSchemaDAO dao = workspace.commons.service.EnvironmentService.getDbSchemaDAO(databaseCredentials);

            String filter = null;
            if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
                filter = "and SUBSTR(ao.object_name, 0, 3) in ('ITC', 'CFG', 'LOG', 'SIT', 'SEG', 'OFC', 'PTB', 'QDS', 'LOC')";
            } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
                filter = "and table_schema in ('api', 'conf', 'dash', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')";
            }
            List<DbObjectDefinition> tables = dao.listTables(databaseCredentials, filter);
//            List<DbObjectDefinition> tables = List.of();

            if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
                filter = "and ao.object_name like 'VW_NG_%'";
            } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
                filter = "table_schema in ('api', 'conf', 'dash', 'evt', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')";
            }
            List<DbObjectDefinition> views = dao.listViews(databaseCredentials, filter);

            if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
                filter = "and ao.object_name like 'FN_NG_%'";
            } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
                filter = "and routine_schema in ('api', 'conf', 'dash', 'evt', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')";
            }
            List<DbObjectDefinition> functions = dao.listFunctions(databaseCredentials, filter);
//            List<DbObjectDefinition> functions = List.of();

            if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
                filter = "and SUBSTR(ao.object_name, 0, 3) in ('ITC', 'CFG', 'LOG', 'SIT', 'SEG', 'OFC', 'PTB', 'QDS', 'LOC')";
            } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
                filter = "where schemaname in ('api', 'conf', 'dash', 'evt', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')";
            }
            List<DbObjectDefinition> indexes = dao.listIndexes(databaseCredentials, filter);
//            List<DbObjectDefinition> indexes = List.of();

            if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
                filter = "  and routine_schema in ('api', 'conf', 'dash', 'evt', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')";
            }
            List<DbObjectDefinition> procedures = dao.listProcedures(databaseCredentials, filter);
//            List<DbObjectDefinition> procedures = List.of();

            if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
                filter = "and SUBSTR(ao.object_name, 0, 4) in ('PITC', 'PCFG', 'PLOG', 'PSIT', 'PSEG', 'POFC', 'PPTB', 'PQDS', 'PLOC')";
            }
            List<DbObjectDefinition> packageBodies = dao.listPackageBodies(databaseCredentials, filter);
//            List<DbObjectDefinition> packageBodies = List.of();

            return new SchemaResult(databaseCredentials, tables, views, functions, indexes, procedures, packageBodies);
        };
    }

}
