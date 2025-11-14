package workspace.vigiang.dao;

import workspace.commons.model.DbObjectDefinition;
import workspace.vigiang.model.DatabaseCredentialsVigiaNG;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public interface DbSchemaDAO {

    @Deprecated
    List<DbObjectDefinition> listTables(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws SQLException;

    List<DbObjectDefinition> listViews(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws SQLException;

    List<DbObjectDefinition> listFunctions(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws SQLException;

    List<DbObjectDefinition> listIndexes(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws SQLException;

    List<DbObjectDefinition> listProcedures(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws SQLException;

    List<DbObjectDefinition> listPackageBodies(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws SQLException;

    default Connection getConnection(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG) throws SQLException {
        return DriverManager.getConnection(
                databaseCredentialsVigiaNG.getDatabaseUrl(),
                databaseCredentialsVigiaNG.getDatabaseUsername(),
                databaseCredentialsVigiaNG.getDatabasePassword());
    }

}
