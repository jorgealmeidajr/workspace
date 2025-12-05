package workspace.commons.dao;

import workspace.commons.model.DatabaseCredentials;
import workspace.commons.model.DbObjectDefinition;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

import static workspace.commons.model.DatabaseCredentials.getConnection;

public class OracleSchemaDAO implements DbSchemaDAO {

    private final DatabaseCredentials databaseCredentials;

    public OracleSchemaDAO(DatabaseCredentials databaseCredentials) {
        this.databaseCredentials = databaseCredentials;
    }

    @Override
    public List<String> listTablesNames() throws SQLException {
        return listOracleObjects("TABLE", null);
    }

    @Override
    public List<DbObjectDefinition> listTablesDefinitions(List<String> names) throws SQLException {
        // TODO: oracle, create tables statements must be simplify
        return listObjectDefinitions(names, "TABLE");
    }

    @Override
    public List<String> listViewsNames() throws SQLException {
        return listOracleObjects("VIEW", null);
    }

    @Override
    public List<DbObjectDefinition> listViewsDefinitions(List<String> names) throws SQLException {
        return listObjectDefinitions(names, "VIEW");
    }

    @Override
    public List<String> listFunctionsNames() throws SQLException {
        return listOracleObjects("FUNCTION", null);
    }

    @Override
    public List<DbObjectDefinition> listFunctionsDefinitions(List<String> names) throws SQLException {
        return listObjectDefinitions(names, "FUNCTION");
    }

    @Override
    public List<String> listIndexesNames() throws SQLException {
        return listOracleObjects("INDEX", null);
    }

    @Override
    public List<DbObjectDefinition> listIndexesDefinitions(List<String> names) throws SQLException {
        // TODO: the sql should be simplified
        return listObjectDefinitions(names, "INDEX");
    }

    @Override
    public List<String> listProceduresNames() throws SQLException {
        return List.of();
    }

    @Override
    public List<DbObjectDefinition> listProcedures(String filter) throws SQLException {
        return List.of();
    }

    @Override
    public List<String> listPackageBodiesNames() throws SQLException {
        return listOracleObjects("PACKAGE BODY", null);
    }

    @Override
    public List<DbObjectDefinition> listPackageBodies(String filter) throws SQLException {
        List<String> objects = listOracleObjects("PACKAGE BODY", filter);
        return listObjectDefinitions(objects, "PACKAGE_BODY");
    }

    private List<String> listOracleObjects(String objectType, String where) throws SQLException {
        return listOracleObjects(objectType, where, null);
    }

    private List<String> listOracleObjects(String objectType, String where, Integer rows) throws SQLException {
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
                String owner = rs.getString("owner");
                var name = owner + "." + rs.getString("object_name");
                result.add(name);
            }
        }
        return result;
    }

    private List<DbObjectDefinition> listObjectDefinitions(List<String> objects, String objectType) throws SQLException {
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
