package workspace.vigiang.dao;

import workspace.commons.dao.DbSchemaDAO;
import workspace.commons.model.DatabaseCredentials;
import workspace.commons.model.DbObjectDefinition;
import workspace.vigiang.model.DatabaseCredentialsVigiaNG;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OracleSchemaDAO implements DbSchemaDAO {

    static final int ROWS = 10;

    @Override
    public List<DbObjectDefinition> listTables(DatabaseCredentials databaseCredentials) throws SQLException {
        List<String> objects = listOracleObjects(databaseCredentials, "TABLE", "  and SUBSTR(ao.object_name, 0, 3) in ('ITC', 'CFG', 'LOG', 'SIT', 'SEG', 'OFC', 'PTB', 'QDS', 'LOC')", ROWS);
        return listObjectDefinitions(databaseCredentials, objects, "TABLE");
    }

    @Override
    public List<DbObjectDefinition> listViews(DatabaseCredentials databaseCredentials) throws SQLException {
        List<String> objects = listOracleObjects(databaseCredentials, "VIEW", "  and ao.object_name like 'VW_NG_%'", null);
        return listObjectDefinitions(databaseCredentials, objects, "VIEW");
    }

    @Override
    public List<DbObjectDefinition> listFunctions(DatabaseCredentials databaseCredentials) throws SQLException {
        List<String> objects = listOracleObjects(databaseCredentials, "FUNCTION", "  and ao.object_name like 'FN_NG_%'", null);
        return listObjectDefinitions(databaseCredentials, objects, "FUNCTION");
    }

    @Override
    public List<DbObjectDefinition> listIndexes(DatabaseCredentials databaseCredentials) throws SQLException {
        List<String> objects = listOracleObjects(databaseCredentials, "INDEX", "  and SUBSTR(ao.object_name, 0, 3) in ('ITC', 'CFG', 'LOG', 'SIT', 'SEG', 'OFC', 'PTB', 'QDS', 'LOC')", ROWS);
        return listObjectDefinitions(databaseCredentials, objects, "INDEX");
    }

    @Override
    public List<DbObjectDefinition> listProcedures(DatabaseCredentials databaseCredentials) throws SQLException {
        return List.of();
    }

    @Override
    public List<DbObjectDefinition> listPackageBodies(DatabaseCredentials databaseCredentials) throws SQLException {
        List<String> objects = listOracleObjects(databaseCredentials, "PACKAGE BODY", "  and SUBSTR(ao.object_name, 0, 4) in ('PITC', 'PCFG', 'PLOG', 'PSIT', 'PSEG', 'POFC', 'PPTB', 'PQDS', 'PLOC')", null);
        return listObjectDefinitions(databaseCredentials, objects, "PACKAGE_BODY");
    }

    private List<String> listOracleObjects(DatabaseCredentials databaseCredentials, String objectType, String where, Integer rows) throws SQLException {
        String sql =
            "select ao.owner, ao.object_name\n" +
            "from all_objects ao\n" +
            "where ao.owner like 'VIGIANG_" + ((DatabaseCredentialsVigiaNG) databaseCredentials).getCarrier().toString() + "'\n" + // TODO:
            "  and ao.object_type = '" + objectType + "'\n" +
            where + "\n" +
            "order by ao.owner, ao.object_name";

        if (rows != null) {
            sql += "\nfetch first " + rows + " rows only";
        }

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
