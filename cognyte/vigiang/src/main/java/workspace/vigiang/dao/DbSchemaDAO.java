package workspace.vigiang.dao;

import workspace.commons.model.DbObjectDefinition;
import workspace.vigiang.model.DatabaseCredentials;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public interface DbSchemaDAO {

    @Deprecated
    List<DbObjectDefinition> listTables(DatabaseCredentials databaseCredentials) throws SQLException;

    List<DbObjectDefinition> listViews(DatabaseCredentials databaseCredentials) throws SQLException;

    List<DbObjectDefinition> listFunctions(DatabaseCredentials databaseCredentials) throws SQLException;

    List<DbObjectDefinition> listIndexes(DatabaseCredentials databaseCredentials) throws SQLException;

    List<DbObjectDefinition> listProcedures(DatabaseCredentials databaseCredentials) throws SQLException;

    List<DbObjectDefinition> listPackageBodies(DatabaseCredentials databaseCredentials) throws SQLException;

    default Connection getConnection(DatabaseCredentials databaseCredentials) throws SQLException {
        return DriverManager.getConnection(
                databaseCredentials.getDatabaseUrl(),
                databaseCredentials.getDatabaseUsername(),
                databaseCredentials.getDatabasePassword());
    }

}
