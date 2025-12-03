package workspace.commons.dao;

import workspace.commons.model.DatabaseCredentials;
import workspace.commons.model.DbObjectDefinition;
import workspace.commons.model.SchemaResult;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

import static workspace.commons.model.DatabaseCredentials.getConnection;

public class OracleSchemaDAO implements DbSchemaDAO {

    @Override
    public List<DbObjectDefinition> listTables(DatabaseCredentials databaseCredentials, String filter) throws SQLException {
        // TODO: oracle, create tables statements must be simplify
        List<String> objects = listOracleObjects(databaseCredentials, "TABLE", filter, null);
        return listObjectDefinitions(databaseCredentials, objects, "TABLE");
    }

    @Override
    public List<DbObjectDefinition> listViews(DatabaseCredentials databaseCredentials, String filter) throws SQLException {
        List<String> objects = listOracleObjects(databaseCredentials, "VIEW", filter, null);
        return listObjectDefinitions(databaseCredentials, objects, "VIEW");
    }

    @Override
    public List<DbObjectDefinition> listFunctions(DatabaseCredentials databaseCredentials, String filter) throws SQLException {
        List<String> objects = listOracleObjects(databaseCredentials, "FUNCTION", filter, null);
        return listObjectDefinitions(databaseCredentials, objects, "FUNCTION");
    }

    @Override
    public List<DbObjectDefinition> listIndexes(DatabaseCredentials databaseCredentials, String filter) throws SQLException {
        // TODO: the sql should be simplified
        List<String> objects = listOracleObjects(databaseCredentials, "INDEX", filter, null);
        return listObjectDefinitions(databaseCredentials, objects, "INDEX");
    }

    @Override
    public List<DbObjectDefinition> listProcedures(DatabaseCredentials databaseCredentials, String filter) throws SQLException {
        return List.of();
    }

    @Override
    public List<DbObjectDefinition> listPackageBodies(DatabaseCredentials databaseCredentials, String filter) throws SQLException {
        List<String> objects = listOracleObjects(databaseCredentials, "PACKAGE BODY", filter, null);
        return listObjectDefinitions(databaseCredentials, objects, "PACKAGE_BODY");
    }

    private List<String> listOracleObjects(DatabaseCredentials databaseCredentials, String objectType, String where, Integer rows) throws SQLException {
        String filter = (where != null) ? "  " + where + "\n" : "";

        String sql =
            "select ao.owner, ao.object_name\n" +
            "from all_objects ao\n" +
            "where ao.owner like '" + databaseCredentials.getUsername() + "'\n" +
            "  and ao.object_type = '" + objectType + "'\n" +
            filter +
            "order by ao.owner, ao.object_name";

        if (rows != null) {
            sql += "\nfetch first " + rows + " rows only";
        }

        System.out.println("Executing ORACLE sql:\n" + sql + "\n");

        List<String> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentials);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("owner") + "." + rs.getString("object_name");
                result.add(name);
            }
        }
        return result;
    }

    private List<DbObjectDefinition> listObjectDefinitions(DatabaseCredentials databaseCredentials, List<String> objects, String objectType) throws SQLException {
        List<DbObjectDefinition> result = new ArrayList<>();

        int threads = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);

        List<List<String>> partitions = new ArrayList<>();
        int partitionSize = (int) Math.ceil((double) objects.size() / threads);
        for (int i = 0; i < objects.size(); i += partitionSize) {
            partitions.add(objects.subList(i, Math.min(i + partitionSize, objects.size())));
        }

        try (Connection conn = getConnection(databaseCredentials)) {
            List<Callable<List<DbObjectDefinition>>> tasks = getTasks(objectType, partitions, conn);

            try {
                List<Future<List<DbObjectDefinition>>> futures = executorService.invokeAll(tasks);
                for (Future<List<DbObjectDefinition>> future : futures) {
                    List<DbObjectDefinition> definitions = future.get();
                    result.addAll(definitions);
                }

                if (!executorService.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (Exception e) {
                executorService.shutdownNow();
            } finally {
                if (!executorService.isShutdown()) {
                    executorService.shutdown();
                }
            }
        }

        result.sort(Comparator.comparing(DbObjectDefinition::getName));
        return result;
    }

    private static List<Callable<List<DbObjectDefinition>>> getTasks(String objectType, List<List<String>> partitions, Connection conn) {
        List<Callable<List<DbObjectDefinition>>> tasks = new ArrayList<>();
        for (List<String> partition : partitions) {
            tasks.add(() -> {
                List<DbObjectDefinition> definitions = new ArrayList<>();
                for (String object : partition) {
                    String objectName = object.substring(object.indexOf(".") + 1);
                    String sql = "SELECT DBMS_METADATA.GET_DDL('" + objectType + "', '" + objectName + "') as DDL FROM DUAL";

                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(sql)) {
                        if (rs.next()) {
                            String definition = rs.getString("DDL");
                            DbObjectDefinition dbObjectDefinition = new DbObjectDefinition(object, definition);
                            definitions.add(dbObjectDefinition);
                        }
                    }

                    try {
                        TimeUnit.MILLISECONDS.sleep(50);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                return definitions;
            });
        }
        return tasks;
    }

}
