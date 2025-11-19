package workspace.commons.service;

import workspace.commons.dao.DbSchemaDAO;
import workspace.commons.dao.OracleSchemaDAO;
import workspace.commons.dao.PostgresSchemaDAO;
import workspace.commons.model.Database;
import workspace.commons.model.DatabaseCredentials;

public class EnvironmentService {

    public static DbSchemaDAO getDbSchemaDAO(DatabaseCredentials databaseCredentials) {
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) return new OracleSchemaDAO();
        if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) return new PostgresSchemaDAO();
        return null;
    }

}
