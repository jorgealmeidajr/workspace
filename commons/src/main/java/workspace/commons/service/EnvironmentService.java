package workspace.commons.service;

import workspace.commons.dao.DbSchemaDAO;
import workspace.commons.dao.OracleSchemaDAO;
import workspace.commons.dao.PostgresSchemaDAO;
import workspace.commons.model.Database;
import workspace.commons.model.DatabaseCredentials;
import workspace.commons.model.Laboratory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnvironmentService {

    public static DbSchemaDAO getDbSchemaDAO(DatabaseCredentials databaseCredentials) {
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) return new OracleSchemaDAO();
        if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) return new PostgresSchemaDAO();
        return null;
    }

    public static void validateLaboratories(List<? extends Laboratory> laboratories) {
        Set<String> names = new HashSet<>();
        Set<String> aliases = new HashSet<>();
        Set<String> hosts = new HashSet<>();

        for (Laboratory lab : laboratories) {
            if (!names.add(lab.getName())) {
                throw new IllegalArgumentException("Duplicate laboratory name found: " + lab.getName());
            }
            if (!aliases.add(lab.getAlias())) {
                throw new IllegalArgumentException("Duplicate laboratory alias found: " + lab.getAlias());
            }
            if (!hosts.add(lab.getSshHost())) {
                throw new IllegalArgumentException("Duplicate laboratory sshHost found: " + lab.getSshHost());
            }
        }
    }

}
