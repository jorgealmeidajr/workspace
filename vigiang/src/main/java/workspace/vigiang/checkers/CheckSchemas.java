package workspace.vigiang.checkers;

import workspace.vigiang.dao.DbSchemaDAO;
import workspace.vigiang.model.DbObjectDefinition;
import workspace.vigiang.model.Environment;
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
        List<Environment> environments = EnvironmentService.getEnvironments();
        ExecutorService executorService = Executors.newFixedThreadPool(environments.size());
        List<Callable<SchemaResult>> callableTasks = new ArrayList<>();

        for (Environment env : environments) {
            Callable<SchemaResult> callableTask = getCallableTask(env);
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
        Environment env = result.getEnvironment();
        Path databaseSchemaPath = EnvironmentService.getDatabaseSchemaPath(env);

        System.out.println(env.getName() + ":");
        updateLocalSchemaFiles(databaseSchemaPath, "tables", result.getTables());
        updateLocalSchemaFiles(databaseSchemaPath, "views", result.getViews());
        updateLocalSchemaFiles(databaseSchemaPath, "indexes", result.getIndexes());
        updateLocalSchemaFiles(databaseSchemaPath, "functions", result.getFunctions());

        if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            updateLocalSchemaFiles(databaseSchemaPath, "procedures", result.getProcedures());
        }

        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            updateLocalSchemaFiles(databaseSchemaPath, "packageBodies", result.getPackageBodies());
        }

        System.out.println();
    }

    private static void updateLocalSchemaFiles(Path databaseSchemaPath, String fileName, List<DbObjectDefinition> data) throws IOException {
        var finalLines = new ArrayList<String>();
        for (DbObjectDefinition row : data) {
            String line = "## " + row.getName() + "\n```\n" + row.getDefinition().trim() + "\n```\n";
            finalLines.add(line);
        }

        var newFileContent = String.join(System.lineSeparator(), finalLines);

        Path finalFilePath = Paths.get(databaseSchemaPath + "\\" + fileName + ".md");

        var initialFileContent = "";
        if (Files.exists(finalFilePath)) {
            initialFileContent = new String(Files.readAllBytes(finalFilePath));
        }

        if (!initialFileContent.equals(newFileContent)) {
            System.out.println("updating file: " + finalFilePath);
            Files.writeString(finalFilePath, newFileContent, StandardCharsets.UTF_8);
        }
    }

    private static Callable<SchemaResult> getCallableTask(Environment env) {
        return () -> {
            DbSchemaDAO dao = EnvironmentService.getDbSchemaDAO(env);

            List<DbObjectDefinition> tables = dao.listTables(env);
            List<DbObjectDefinition> views = dao.listViews(env);
            List<DbObjectDefinition> functions = dao.listFunctions(env);
            List<DbObjectDefinition> indexes = dao.listIndexes(env);
            List<DbObjectDefinition> procedures = dao.listProcedures(env);
            List<DbObjectDefinition> packageBodies = dao.listPackageBodies(env);

            return new SchemaResult(env, tables, views, functions, indexes, procedures, packageBodies);
        };
    }

}
