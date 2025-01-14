package workspace.vigiang;

import com.microsoft.playwright.*;

import java.awt.*;
import java.util.ArrayList;
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

            {
                var tags = getTags(frontendRepositoryUrl, page);

                var firstTag = tags.get(0);
                var commits = getCommits(frontendRepositoryUrl, page, firstTag);
                System.out.println(commits.size());
            }

            for (String backendUrl : backendRepositoryUrls) {
                var tags = getTags(backendUrl, page);

                var firstTag = tags.get(0);
                var commits = getCommits(backendUrl, page, firstTag);
                System.out.println(commits.size());
            }

            {
                for (String url : databaseRepositoryUrls) {
                    page.navigate(url + "/-/issues/?sort=created_date&state=all&first_page_size=100");
                    await();
                    var selector = "#content-body > div.js-issues-list-app > div > ul > li";
                    for (Locator li : page.locator(selector).all()) {
                        String text = li.textContent();
                        System.out.println(text);
                    }
                }
            }

            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("## END: get git changes\n");
    }

    private static ArrayList<String> getCommits(String url, Page page, String tag) {
        page.navigate(url + "/-/commits/" + tag + "?ref_type=tags");
        var commitSelector = "div.commit-sha-group.btn-group.gl-hidden.sm\\:gl-flex > div";

        var commits = new ArrayList<String>();
        for (Locator li : page.locator(commitSelector).all()) {
            String commit = li.textContent();
            commits.add(commit);
        }
        return commits;
    }

    private static List<String> getTags(String url, Page page) {
        page.navigate(url + "/-/tags");
        String selector = "#content-body > ul > li > div.row-main-content > a";

        var tags = new ArrayList<String>();
        for (Locator li : page.locator(selector).all()) {
            tags.add(li.textContent());
        }
        return tags;
    }

    private static List<String> getDatabaseRepositoryUrls() {
        var initialUrl = "https://flngit01.cognyte.local/dev/vigiang/database/";
        return Arrays.asList(
//            initialUrl + "algar",
//            initialUrl + "claro",
//            initialUrl + "ligga",
//            initialUrl + "oi",
//            initialUrl + "sky",
            initialUrl + "surf"
//            initialUrl + "tim",
//            initialUrl + "vivo",
//            initialUrl + "vtal",
//            initialUrl + "wom"
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
