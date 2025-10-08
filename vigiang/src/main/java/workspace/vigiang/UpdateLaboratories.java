package workspace.vigiang;

import workspace.vigiang.checkers.CheckContainers;
import workspace.vigiang.checkers.CheckLaboratories;
import workspace.vigiang.checkers.CheckProjectsVersions;

public class UpdateLaboratories {

    public static void main(String[] args) {
        CheckContainers.main(new String[] { });
        CheckLaboratories.main(new String[] { });
        CheckProjectsVersions.main(new String[] { });
    }

}
