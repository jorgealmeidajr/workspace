package workspace.vigiang.scripts;

import workspace.vigiang.dao.VigiaNgDAO;
import workspace.vigiang.model.DatabaseCredentials;
import workspace.vigiang.service.EnvironmentService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static workspace.vigiang.service.EnvironmentService.getDatabaseCredentials;

public class CopyData {

    public static void main(String[] args) {
        // the parameters bellow must match in file databases.json
        String SOURCE_DATABASE_NAME = "?";
        String TARGET_DATABASE_NAME = "?";
        Integer TARGET_PROFILE_ID = 0; // this id is from database

        try {
            DatabaseCredentials sourceDb = getDatabaseCredentials(SOURCE_DATABASE_NAME);
            DatabaseCredentials targetDb = getDatabaseCredentials(TARGET_DATABASE_NAME);

            VigiaNgDAO sourceDao = EnvironmentService.getVigiaNgDAO(sourceDb);
            VigiaNgDAO targetDao = EnvironmentService.getVigiaNgDAO(targetDb);

            copyPrivileges(sourceDao, targetDao);

            targetDao.associatePrivileges(TARGET_PROFILE_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyPrivileges(VigiaNgDAO sourceDao, VigiaNgDAO targetDao) throws SQLException {
        List<String> sourcePrivileges = sourceDao.listPrivileges().stream()
                .map(p -> p[1]).collect(Collectors.toList());
        List<String> targetPrivileges = targetDao.listPrivileges().stream()
                .map(p -> p[1]).collect(Collectors.toList());
        List<String> missingPrivileges = new ArrayList<>();

        for (String sourcePrivilege : sourcePrivileges) {
            if (!targetPrivileges.contains(sourcePrivilege)) {
                missingPrivileges.add(sourcePrivilege);
            }
        }

        missingPrivileges.sort(String::compareTo);
        targetDao.insertPrivileges(missingPrivileges);
    }

}
