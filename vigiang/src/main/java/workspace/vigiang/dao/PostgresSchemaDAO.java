package workspace.vigiang.dao;

import workspace.vigiang.model.Environment;

import java.sql.SQLException;
import java.util.List;

public class PostgresSchemaDAO implements DbSchemaDAO {

    @Override
    public List<String> listTables(Environment env) throws SQLException {
        return List.of();
    }

    @Override
    public List<String> listViews(Environment env) throws SQLException {
        return List.of();
    }

}
