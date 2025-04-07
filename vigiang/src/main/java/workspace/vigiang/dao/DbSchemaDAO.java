package workspace.vigiang.dao;

import workspace.vigiang.model.DbObjectDefinition;
import workspace.vigiang.model.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public interface DbSchemaDAO {

    List<DbObjectDefinition> listTables(Environment env) throws SQLException;

    List<DbObjectDefinition> listViews(Environment env) throws SQLException;

    List<DbObjectDefinition> listFunctions(Environment env) throws SQLException;

    List<DbObjectDefinition> listIndexes(Environment env) throws SQLException;

    List<DbObjectDefinition> listProcedures(Environment env) throws SQLException;

    List<DbObjectDefinition> listPackageBodies(Environment env) throws SQLException;

    default Connection getConnection(Environment environment) throws SQLException {
        return DriverManager.getConnection(
                environment.getDatabaseUrl(),
                environment.getDatabaseUsername(),
                environment.getDatabasePassword());
    }

}
