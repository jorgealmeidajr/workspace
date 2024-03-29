package workspace.vigiang;

import com.microsoft.playwright.*;
import workspace.vigiang.model.TablePrinter;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static workspace.vigiang.model.TablePrinter.calculateColumnWidths;

public class CheckProjectsVersions {

    public static void main(String[] args) {
        var vigiangPathStr = "C:\\Users\\jjunior\\OneDrive - COGNYTE\\Documents\\COGNYTE\\VIGIANG";
        Path vigiangPath = Paths.get(vigiangPathStr);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        try (Playwright playwright = Playwright.create()) {
            var launchOptions = new BrowserType.LaunchOptions()
                    .setChannel("chrome") // "chrome", "msedge", "chrome-beta", "msedge-beta" or "msedge-dev"
                    .setHeadless(true)
                    .setSlowMo(500);

            Browser browser = playwright.chromium().launch(launchOptions);
            BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(width, height));

            Page page = context.newPage();
            page.navigate("https://flngit01.cognyte.local/dev");

            page.locator("#username").pressSequentially("jjunior");
            page.locator("#password").pressSequentially("Floripa2024#");
            page.locator("#new_ldap_user > div.submit-container.move-submit-down.gl-px-5.gl-pb-5 > input").click();
            await();

            {
                var containersPathStr = vigiangPath + "\\containers.txt";
                Path containersPath = Paths.get(containersPathStr);
                System.out.println("updating file: " + containersPath);

                var response = checkBackEndTags(page);
                Files.writeString(containersPath, response, StandardCharsets.UTF_8);
                System.out.println("file updated");
            }

//            checkFrontEndTags(page);

            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String checkBackEndTags(Page page) {
        var data = new ArrayList<String[]>();
        var urls = Arrays.asList(
            // cloud-control
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-control/admin-server",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-control/config-server",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-control/eureka-server",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-control/zuul-server",
            // cloud-vigiang
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/auth-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/block-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/carrier-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/dashboard-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/data-retention-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/event-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/interception-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/log-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/message-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/operation-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/portability-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/process-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/report-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/scheduler-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/sittel-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/system-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/tracking-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/user-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/voucher-service",
            "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/warrant-service"
        );

        for (String url : urls) {
            page.navigate(url + "/-/tags");

            var project = page.locator("div > ul > li.context-header.has-tooltip > a > span.sidebar-context-title").textContent();
            var content = page.locator("div.tags > ul > li:nth-child(1) > div.row-main-content > a").textContent();
            data.add(new String[] { project.trim(), content.trim() });

            await();
        }

        var lines = new ArrayList<String>();
        int[] columnWidths = TablePrinter.calculateColumnWidths(data);
        for (String[] row : data) {
            lines.add(TablePrinter.printRow(row, columnWidths));
        }

        lines.sort(Comparator.naturalOrder());
        return String.join(System.lineSeparator(), lines);
    }

    private static void checkFrontEndTags(Page page) {
        page.navigate("https://flngit01.cognyte.local/dev/vigiang/front-end/vigia_ng_app/-/tags");

        Locator tags = page.locator("#content-body > div.flex-list > div.tags > ul > li > div.row-main-content > a");

        for (int i = 0; i < tags.count(); i++) {
            System.out.println(tags.nth(i).textContent().trim());
        }

        await();
    }

    private static void await() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

}
