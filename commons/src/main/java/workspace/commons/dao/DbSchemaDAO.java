package workspace.commons.dao;

import workspace.commons.model.DbObjectDefinition;

import java.sql.SQLException;
import java.util.List;

public interface DbSchemaDAO {

    List<String> listTablesNames() throws SQLException;

    List<DbObjectDefinition> listTables(String filter) throws SQLException;

    List<String> listViewsNames() throws SQLException;

    List<DbObjectDefinition> listViews(String filter) throws SQLException;

    List<String> listFunctionsNames() throws SQLException;

    List<DbObjectDefinition> listFunctions(String filter) throws SQLException;

    List<String> listIndexesNames() throws SQLException;

    List<DbObjectDefinition> listIndexes(String filter) throws SQLException;

    List<String> listProceduresNames() throws SQLException;

    List<DbObjectDefinition> listProcedures(String filter) throws SQLException;

    List<String> listPackageBodiesNames() throws SQLException;

    List<DbObjectDefinition> listPackageBodies(String filter) throws SQLException;

}
