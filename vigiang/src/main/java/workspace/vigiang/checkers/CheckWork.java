package workspace.vigiang.checkers;

import workspace.vigiang.service.EnvironmentService;

public class CheckWork {

    public static void main(String[] args) {
        var WORK_DIR = "/c/work/vigiang";
        String result = "";
        result += "VIGIANG_ROOT=\"" + WORK_DIR + "\"\n";
        result += "alias work='cd SRC/ && yarn run workflow'\n";
        result += "alias web='cd SRC/ && yarn run webviewer'\n\n";

        for (String version : EnvironmentService.getVersions()) {
            String frontendPath = WORK_DIR + "/" + version + "/front-" + version;
            String backendPath = WORK_DIR + "/" + version + "/back-" + version;

            String front = String.format("alias front%s='cd \"%s\"'", version, frontendPath);
            String back = String.format("alias back%s='cd \"%s\"'", version, backendPath);
            //alias runReport='vigiab && cd report-service/SRC/ && yarn start'

            result += front + "\n";
            result += back + "\n\n";
        }

        System.out.println(result);
    }

}
