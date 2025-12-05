package workspace.commons.dao;

import workspace.commons.model.DbObjectDefinition;

import java.sql.SQLException;
import java.util.List;

public interface DbSchemaDAO {

    List<String> listTablesNames() throws SQLException;

    List<DbObjectDefinition> listTablesDefinitions(List<String> names) throws SQLException;

    List<String> listViewsNames() throws SQLException;

    List<DbObjectDefinition> listViewsDefinitions(List<String> names) throws SQLException;

    List<String> listFunctionsNames() throws SQLException;

    List<DbObjectDefinition> listFunctionsDefinitions(List<String> names) throws SQLException;

    List<String> listIndexesNames() throws SQLException;

    List<DbObjectDefinition> listIndexesDefinitions(List<String> names) throws SQLException;

    List<String> listProceduresNames() throws SQLException;

    List<DbObjectDefinition> listProceduresDefinitions(List<String> names) throws SQLException;

    List<String> listPackageBodiesNames() throws SQLException;

    List<DbObjectDefinition> listPackageBodiesDefinitions(List<String> names) throws SQLException;

}
