package workspace.vigiang.scripts;

import workspace.vigiang.dao.VigiaNgDAO;
import workspace.vigiang.model.Carrier;
import workspace.vigiang.model.DatabaseCredentials;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CopyData {

    public static void main(String[] args) {
        // the parameters bellow must match in file databases.json
        String SOURCE_DATABASE_NAME = "?";
        Carrier SOURCE_CARRIER = null;
        DatabaseCredentials.Database SOURCE_DATABASE = null;

        String TARGET_DATABASE_NAME = "?";
        Carrier TARGET_CARRIER = null;
        DatabaseCredentials.Database TARGET_DATABASE = null;

        Integer TARGET_PROFILE_ID = 0; // this id is from database

        try {
            DatabaseCredentials sourceDb = getDatabaseCredentials(SOURCE_DATABASE_NAME, SOURCE_CARRIER, SOURCE_DATABASE);
            DatabaseCredentials targetDb = getDatabaseCredentials(TARGET_DATABASE_NAME, TARGET_CARRIER, TARGET_DATABASE);

            VigiaNgDAO sourceDao = EnvironmentService.getVigiaNgDAO(sourceDb);
            VigiaNgDAO targetDao = EnvironmentService.getVigiaNgDAO(targetDb);

            copyPrivileges(sourceDao, sourceDb, targetDao, targetDb);

            targetDao.associatePrivileges(targetDb, TARGET_PROFILE_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyPrivileges(VigiaNgDAO sourceDao, DatabaseCredentials sourceDb, VigiaNgDAO targetDao, DatabaseCredentials targetDb) throws SQLException {
        List<String> sourcePrivileges = sourceDao.listPrivileges(sourceDb).stream()
                .map(p -> p[1]).collect(Collectors.toList());
        List<String> targetPrivileges = targetDao.listPrivileges(targetDb).stream()
                .map(p -> p[1]).collect(Collectors.toList());
        List<String> missingPrivileges = new ArrayList<>();

        for (String sourcePrivilege : sourcePrivileges) {
            if (!targetPrivileges.contains(sourcePrivilege)) {
                missingPrivileges.add(sourcePrivilege);
            }
        }

        missingPrivileges.sort(String::compareTo);
        targetDao.insertPrivileges(targetDb, missingPrivileges);
    }

    private static DatabaseCredentials getDatabaseCredentials(String databaseName, Carrier carrier, DatabaseCredentials.Database database) throws IOException {
        Predicate<DatabaseCredentials> databaseCredentialsPredicate = (credentials) ->
                credentials.getName().equals(databaseName)
                        && credentials.getCarrier().equals(carrier)
                        && credentials.getDatabase().equals(database);

        return EnvironmentService.getVigiangDatabases().stream()
                .filter(databaseCredentialsPredicate)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Database credentials not found, check the parameters..."));

        //return EnvironmentService.getVigiaNgDAO(databaseCredentials); // TODO
    }

}
