package workspace.vigiang;

import workspace.commons.service.FileService;
import workspace.vigiang.checkers.CheckDatabases;
import workspace.vigiang.checkers.CheckEmailTemplates;
import workspace.vigiang.checkers.CheckReportTemplates;
import workspace.vigiang.model.DatabaseCredentialsVigiaNG;
import workspace.vigiang.service.EnvironmentService;

import java.nio.file.Path;

public class UpdateDatabases {

    public static void main(String[] args) {
        CheckDatabases.main(new String[] { });
        CheckEmailTemplates.main(new String[] { });
        CheckReportTemplates.main(new String[] { });

        removeUnusedDatabases();
    }

    private static void removeUnusedDatabases() {
        System.out.println("## START removing unused databases\n");
        try {
            for (DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG : EnvironmentService.getDatabasesVigiaNgInactive()) {
                System.out.println("🗑️ Removing inactive database: " + databaseCredentialsVigiaNG.getName());
                Path databasePath = EnvironmentService.getDatabasePath(databaseCredentialsVigiaNG);
                FileService.deleteRecursively(databasePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n## END removing unused databases.");
    }

}
