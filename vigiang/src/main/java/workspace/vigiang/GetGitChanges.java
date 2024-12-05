package workspace.vigiang;

import com.microsoft.playwright.*;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GetGitChanges {

    public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        System.out.println("## START: get git changes\n");

        var frontendRepositoryUrl = "https://flngit01.cognyte.local/dev/vigiang/front-end/vigia_ng_app";
        var backendRepositoryUrls = getBackendRepositoryUrls();
        var databaseRepositoryUrls = getDatabaseRepositoryUrls();

        try (Playwright playwright = Playwright.create()) {
            var launchOptions = new BrowserType.LaunchOptions()
                    .setChannel("chrome") // "chrome", "msedge", "chrome-beta", "msedge-beta" or "msedge-dev"
                    .setHeadless(false)
                    .setSlowMo(500);

            Browser browser = playwright.chromium().launch(launchOptions);
            BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(width, height));

            Page page = context.newPage();
            page.navigate("https://flngit01.cognyte.local/dev");

            page.locator("#ldapmain_username").pressSequentially("jjunior");
            page.locator("#ldapmain_password").pressSequentially("Floripa2024#");
            page.locator("#ldapmain > form > button").click();
            await();



            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("## END: get git changes\n");
    }

    private static List<String> getDatabaseRepositoryUrls() {
        var initialUrl = "https://flngit01.cognyte.local/dev/vigiang/database/";
        return Arrays.asList(
            initialUrl + "algar",
            initialUrl + "claro",
            initialUrl + "ligga",
            initialUrl + "oi",
            initialUrl + "sky",
            initialUrl + "surf",
            initialUrl + "tim",
            initialUrl + "vivo",
            initialUrl + "vtal"
        );
    }

    private static List<String> getBackendRepositoryUrls() {
        return Arrays.asList(
            // cloud-control
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-control/admin-server",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-control/config-server",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-control/eureka-server",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-control/zuul-server",
            // cloud-vigiang
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/auth-service"
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/block-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/carrier-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/dashboard-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/data-retention-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/event-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/interception-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/log-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/message-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/operation-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/portability-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/process-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/report-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/scheduler-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/sittel-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/system-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/tracking-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/user-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/voucher-service",
//            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/warrant-service"
        );
    }

    private static void await() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

}
