package workspace.vigiang.dao;

import workspace.vigiang.model.DbObjectDefinition;
import workspace.vigiang.model.Environment;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class OracleSchemaDAO implements DbSchemaDAO {

    @Override
    public List<DbObjectDefinition> listTables(Environment env) throws SQLException {
        List<String> objects = listOracleObjects(env, "TABLE", "  and SUBSTR(ao.object_name, 0, 3) in ('ITC', 'CFG', 'LOG', 'SIT', 'SEG', 'OFC', 'PTB', 'QDS', 'LOC')");
        return listObjectDefinitions(env, objects, "TABLE");
    }

    @Override
    public List<DbObjectDefinition> listViews(Environment env) throws SQLException {
        List<String> objects = listOracleObjects(env, "VIEW", "  and ao.object_name like 'VW_NG_%'");;
        return listObjectDefinitions(env, objects, "VIEW");
    }

    @Override
    public List<DbObjectDefinition> listFunctions(Environment env) throws SQLException {
        List<String> objects = listOracleObjects(env, "FUNCTION", "  and ao.object_name like 'FN_NG_%'");
        return listObjectDefinitions(env, objects, "FUNCTION");
    }

    @Override
    public List<DbObjectDefinition> listIndexes(Environment env) throws SQLException {
        List<String> objects = listOracleObjects(env, "INDEX", "  and SUBSTR(ao.object_name, 0, 3) in ('ITC', 'CFG', 'LOG', 'SIT', 'SEG', 'OFC', 'PTB', 'QDS', 'LOC')");
        return listObjectDefinitions(env, objects, "INDEX");
    }

    @Override
    public List<DbObjectDefinition> listProcedures(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<DbObjectDefinition> listPackageBodies(Environment env) throws SQLException {
        List<String> objects = listOracleObjects(env, "PACKAGE BODY", "  and SUBSTR(ao.object_name, 0, 4) in ('PITC', 'PCFG', 'PLOG', 'PSIT', 'PSEG', 'POFC', 'PPTB', 'PQDS', 'PLOC')");
        return listObjectDefinitions(env, objects, "PACKAGE_BODY");
    }

    private List<String> listOracleObjects(Environment env, String objectType, String where) throws SQLException {
        String sql =
            "select ao.owner, ao.object_name\n" +
            "from all_objects ao\n" +
            "where ao.owner like 'VIGIANG_" + env.getCarrier().toString() + "'\n" +
            "  and ao.object_type = '" + objectType + "'\n" +
            where + "\n" +
            "order by ao.owner, ao.object_name";

        List<String> result = new ArrayList<>();
        try (Connection conn = getConnection(env);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var name = rs.getString("owner") + "." + rs.getString("object_name");
                result.add(name);
            }
        }
        return result;
    }

    private List<DbObjectDefinition> listObjectDefinitions(Environment env, List<String> objects, String objectType) throws SQLException {
        List<DbObjectDefinition> result = new ArrayList<>();

        try (Connection conn = getConnection(env)) {
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
                    TimeUnit.MILLISECONDS.sleep(250);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return result;
    }

}
