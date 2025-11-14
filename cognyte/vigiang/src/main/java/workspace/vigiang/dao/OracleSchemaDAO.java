package workspace.vigiang.dao;

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
    public List<DbObjectDefinition> listTables(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws SQLException {
        List<String> objects = listOracleObjects(databaseCredentialsVigiaNG, "TABLE", "  and SUBSTR(ao.object_name, 0, 3) in ('ITC', 'CFG', 'LOG', 'SIT', 'SEG', 'OFC', 'PTB', 'QDS', 'LOC')", ROWS);
        return listObjectDefinitions(databaseCredentialsVigiaNG, objects, "TABLE");
    }

    @Override
    public List<DbObjectDefinition> listViews(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws SQLException {
        List<String> objects = listOracleObjects(databaseCredentialsVigiaNG, "VIEW", "  and ao.object_name like 'VW_NG_%'", null);
        return listObjectDefinitions(databaseCredentialsVigiaNG, objects, "VIEW");
    }

    @Override
    public List<DbObjectDefinition> listFunctions(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws SQLException {
        List<String> objects = listOracleObjects(databaseCredentialsVigiaNG, "FUNCTION", "  and ao.object_name like 'FN_NG_%'", null);
        return listObjectDefinitions(databaseCredentialsVigiaNG, objects, "FUNCTION");
    }

    @Override
    public List<DbObjectDefinition> listIndexes(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws SQLException {
        List<String> objects = listOracleObjects(databaseCredentialsVigiaNG, "INDEX", "  and SUBSTR(ao.object_name, 0, 3) in ('ITC', 'CFG', 'LOG', 'SIT', 'SEG', 'OFC', 'PTB', 'QDS', 'LOC')", ROWS);
        return listObjectDefinitions(databaseCredentialsVigiaNG, objects, "INDEX");
    }

    @Override
    public List<DbObjectDefinition> listProcedures(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws SQLException {
        return List.of();
    }

    @Override
    public List<DbObjectDefinition> listPackageBodies(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws SQLException {
        List<String> objects = listOracleObjects(databaseCredentialsVigiaNG, "PACKAGE BODY", "  and SUBSTR(ao.object_name, 0, 4) in ('PITC', 'PCFG', 'PLOG', 'PSIT', 'PSEG', 'POFC', 'PPTB', 'PQDS', 'PLOC')", null);
        return listObjectDefinitions(databaseCredentialsVigiaNG, objects, "PACKAGE_BODY");
    }

    private List<String> listOracleObjects(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, String objectType, String where, Integer rows) throws SQLException {
        String sql =
            "select ao.owner, ao.object_name\n" +
            "from all_objects ao\n" +
            "where ao.owner like 'VIGIANG_" + databaseCredentialsVigiaNG.getCarrier().toString() + "'\n" +
            "  and ao.object_type = '" + objectType + "'\n" +
            where + "\n" +
            "order by ao.owner, ao.object_name";

        if (rows != null) {
            sql += "\nfetch first " + rows + " rows only";
        }

        List<String> result = new ArrayList<>();
        try (Connection conn = getConnection(databaseCredentialsVigiaNG);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("owner") + "." + rs.getString("object_name");
                result.add(name);
            }
        }
        return result;
    }

    private List<DbObjectDefinition> listObjectDefinitions(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, List<String> objects, String objectType) throws SQLException {
        List<DbObjectDefinition> result = new ArrayList<>();

        try (Connection conn = getConnection(databaseCredentialsVigiaNG)) {
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
