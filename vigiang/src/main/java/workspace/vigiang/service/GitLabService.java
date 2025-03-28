package workspace.vigiang.service;

import java.util.Arrays;
import java.util.List;

public class GitLabService {

    public static class VigiaNG {
        private static final String VIGIANG = "https://flngit01.cognyte.local/dev/vigiang/";

        public static String getFrontEndUrl() {
            return VIGIANG + "front-end/vigia_ng_app";
        }

        public static List<String> getBackendRepositoryUrls() {
            var cloudControlInitialUrl = VIGIANG + "back-end/cloud-control/";
            var cloudVigiangInitialUrl = VIGIANG + "back-end/cloud-vigiang/";

            return Arrays.asList(
                cloudControlInitialUrl + "admin-server",
                cloudControlInitialUrl + "config-server",
                cloudControlInitialUrl + "eureka-server",
                cloudControlInitialUrl + "zuul-server",

                cloudVigiangInitialUrl + "auth-service",
                cloudVigiangInitialUrl + "block-service",
                cloudVigiangInitialUrl + "carrier-service",
                cloudVigiangInitialUrl + "dashboard-service",
                cloudVigiangInitialUrl + "data-retention-service",
                cloudVigiangInitialUrl + "event-service",
                cloudVigiangInitialUrl + "interception-service",
                cloudVigiangInitialUrl + "log-service",
                cloudVigiangInitialUrl + "message-service",
                cloudVigiangInitialUrl + "operation-service",
                cloudVigiangInitialUrl + "portability-service",
                cloudVigiangInitialUrl + "process-service",
                cloudVigiangInitialUrl + "report-service",
                cloudVigiangInitialUrl + "scheduler-service",
                cloudVigiangInitialUrl + "sittel-service",
                cloudVigiangInitialUrl + "system-service",
                cloudVigiangInitialUrl + "tracking-service",
                cloudVigiangInitialUrl + "user-service",
                cloudVigiangInitialUrl + "voucher-service",
                cloudVigiangInitialUrl + "warrant-service"
            );
        }

        public static List<String> getDatabaseRepositoryUrls() {
            var initialUrl = VIGIANG + "database/";
            return Arrays.asList(
                initialUrl + "algar",
                initialUrl + "claro",
                initialUrl + "ligga",
                initialUrl + "oi",
                initialUrl + "sky",
                initialUrl + "surf",
                initialUrl + "tim",
                initialUrl + "vivo",
                initialUrl + "vtal",
                initialUrl + "wom"
            );
        }
    }

}
