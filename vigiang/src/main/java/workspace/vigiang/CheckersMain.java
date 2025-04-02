package workspace.vigiang;

import workspace.vigiang.checkers.*;

public class CheckersMain {

    public static void main(String[] args) {
        CheckContainers.main(new String[] { });
        CheckDatabases.main(new String[] { });
        CheckEmailTemplates.main(new String[] { });
//        CheckReportTemplates.main(new String[] { });
        CheckProjectsVersions.main(new String[] { });
    }

}
