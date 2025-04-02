package workspace.vigiang.dao;

import workspace.vigiang.model.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public interface DbSchemaDAO {

    List<String> listTables(Environment env) throws SQLException;

    List<String> listViews(Environment env) throws SQLException;

    default Connection getConnection(Environment environment) throws SQLException {
        return DriverManager.getConnection(
                environment.getDatabaseUrl(),
                environment.getDatabaseUsername(),
                environment.getDatabasePassword());
    }

}
