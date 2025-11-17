package workspace.commons.dao;

import workspace.commons.model.DatabaseCredentials;
import workspace.commons.model.DbObjectDefinition;

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

}
