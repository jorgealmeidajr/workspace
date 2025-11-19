package workspace.vigiang.dao;

import workspace.commons.dao.DbSchemaDAO;
import workspace.commons.model.DatabaseCredentials;
import workspace.commons.model.DbObjectDefinition;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static workspace.commons.model.DatabaseCredentials.getConnection;

public class OracleSchemaDAO implements DbSchemaDAO {

    static final int ROWS = 10;

    @Override
    public List<DbObjectDefinition> listTables(DatabaseCredentials databaseCredentials, String filter) throws SQLException {
        // TODO: oracle, create tables statements must be simplify
        List<String> objects = listOracleObjects(databaseCredentials, "TABLE", filter, ROWS);
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
        List<String> objects = listOracleObjects(databaseCredentials, "INDEX", filter, ROWS);
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

        try (Connection conn = getConnection(databaseCredentials)) {
            // TODO: split the tasks to multiple threads
            for (String object : objects) {
                String objectName = object.substring(object.indexOf(".") + 1);
                String sql = "SELECT DBMS_METADATA.GET_DDL('" + objectType + "', '" + objectName + "') as DDL FROM DUAL";

                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    if (rs.next()) {
                        String definition = rs.getString("DDL");
                        DbObjectDefinition dbObjectDefinition = new DbObjectDefinition(object, definition);
                        result.add(dbObjectDefinition);
                    }
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return result;
    }

}
