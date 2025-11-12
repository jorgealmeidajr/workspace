package workspace.vigiang;

import workspace.vigiang.checkers.*;

public class UpdateDatabases {

    public static void main(String[] args) {
        CheckDatabases.main(new String[] { });
        CheckEmailTemplates.main(new String[] { });
        CheckReportTemplates.main(new String[] { });
    }

}
