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

import static workspace.commons.service.UpdateSchemasService.*;

public class UpdateSchemas {

    public static void main(String[] args) {
        System.out.println("\n## START checking all database schemas\n");
        try {
            List<DatabaseCredentialsVigiaNG> databasesCredentials = EnvironmentService.getVigiangDatabases();

            boolean update = true;
            Request request = Request.builder()
                    .updateTxt(true)
                    .updateSql(true)
                    .updateTables(update)
                    .updateViews(update)
                    .updateIndexes(update)
                    .updateFunctions(update)
                    .updateProcedures(update)
                    .updatePackageBodies(update)
                    .build();

            execute(databasesCredentials, request, UpdateSchemas::getCallableTask, UpdateSchemas::handleResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("## END checking all database schemas.");
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

    private static Callable<SchemaResult> getCallableTask(DatabaseCredentials databaseCredentials, Request request) {
        return () -> {
            DbSchemaDAO dao = workspace.commons.service.EnvironmentService.getDbSchemaDAO(databaseCredentials);

            List<DbObjectDefinition> tables = List.of();
            if (request.isUpdateTables()) {
                String filter = null;
                if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
                    filter = "and SUBSTR(ao.object_name, 0, 3) in ('ITC', 'CFG', 'LOG', 'SIT', 'SEG', 'OFC', 'PTB', 'QDS', 'LOC')";
                } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
                    filter = "and table_schema in ('api', 'conf', 'dash', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')";
                }
                tables = dao.listTables(databaseCredentials, filter);
            }

            List<DbObjectDefinition> views = List.of();
            if (request.isUpdateViews()) {
                String filter = null;
                if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
                    filter = "and ao.object_name like 'VW_NG_%'";
                } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
                    filter = "table_schema in ('api', 'conf', 'dash', 'evt', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')";
                }
                views = dao.listViews(databaseCredentials, filter);
            }

            List<DbObjectDefinition> functions = List.of();
            if (request.isUpdateFunctions()) {
                String filter = null;
                if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
                    filter = "and ao.object_name like 'FN_NG_%'";
                } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
                    filter = "and routine_schema in ('api', 'conf', 'dash', 'evt', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')";
                }
                functions = dao.listFunctions(databaseCredentials, filter);
            }

            List<DbObjectDefinition> indexes = List.of();
            if (request.isUpdateIndexes()) {
                String filter = null;
                if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
                    filter = "and SUBSTR(ao.object_name, 0, 3) in ('ITC', 'CFG', 'LOG', 'SIT', 'SEG', 'OFC', 'PTB', 'QDS', 'LOC')";
                } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
                    filter = "where schemaname in ('api', 'conf', 'dash', 'evt', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')";
                }
                indexes = dao.listIndexes(databaseCredentials, filter);
            }

            List<DbObjectDefinition> procedures = List.of();
            if (request.isUpdateProcedures()) {
                String filter = null;
                if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
                    filter = "  and routine_schema in ('api', 'conf', 'dash', 'evt', 'gen', 'itc', 'log', 'ofc', 'prog', 'public', 'sec', 'sync')";
                }
                procedures = dao.listProcedures(databaseCredentials, filter);
            }

            List<DbObjectDefinition> packageBodies = List.of();
            if (request.isUpdatePackageBodies()) {
                String filter = null;
                if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
                    filter = "and SUBSTR(ao.object_name, 0, 4) in ('PITC', 'PCFG', 'PLOG', 'PSIT', 'PSEG', 'POFC', 'PPTB', 'PQDS', 'PLOC')";
                }
                packageBodies = dao.listPackageBodies(databaseCredentials, filter);
            }

            return new SchemaResult(databaseCredentials, tables, views, functions, indexes, procedures, packageBodies);
        };
    }

}
