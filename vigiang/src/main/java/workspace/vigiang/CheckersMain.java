package workspace.vigiang;

import workspace.vigiang.checkers.CheckContainers;
import workspace.vigiang.checkers.CheckDatabases;
import workspace.vigiang.checkers.CheckEmailTemplates;
import workspace.vigiang.checkers.CheckProjectsVersions;

public class CheckersMain {

    public static void main(String[] args) {
        CheckContainers.main(new String[] { });
        CheckDatabases.main(new String[] { });
        CheckEmailTemplates.main(new String[] { });
        CheckProjectsVersions.main(new String[] { });
    }

}
