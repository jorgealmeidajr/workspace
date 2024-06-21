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
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class CheckProjectsVersions {

    public static void main(String[] args) {
        var vigiangPathStr = "C:\\Users\\jjunior\\OneDrive - COGNYTE\\Documents\\COGNYTE\\VIGIANG";
        Path vigiangPath = Paths.get(vigiangPathStr);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        System.out.println("#".repeat(3 * 2));
        System.out.println("## START checking all projects versions\n");

        try (Playwright playwright = Playwright.create()) {
            var launchOptions = new BrowserType.LaunchOptions()
                    .setChannel("chrome") // "chrome", "msedge", "chrome-beta", "msedge-beta" or "msedge-dev"
                    .setHeadless(true)
                    .setSlowMo(500);

            Browser browser = playwright.chromium().launch(launchOptions);
            BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(width, height));

            Page page = context.newPage();
            page.navigate("https://flngit01.cognyte.local/dev");

            page.locator("#ldapmain_username").pressSequentially("jjunior");
            page.locator("#ldapmain_password").pressSequentially("Floripa2024#");
            page.locator("#ldapmain > form > button").click();
            await();

            updateContainersFile(vigiangPath, page);
//            checkFrontEndTags(page);

            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("## END checking all projects versions.");
        System.out.println("#".repeat(3 * 2));
    }

    private static void updateContainersFile(Path vigiangPath, Page page) throws IOException {
        Path containersPath = Paths.get(vigiangPath + "\\containers.txt");

        var initialFileContent = "";
        if (Files.exists(containersPath)) {
            initialFileContent = new String(Files.readAllBytes(containersPath));
        }

        var newFileContent = checkBackEndTags(page);

        if (!initialFileContent.equals(newFileContent)) {
            System.out.println("updating file: " + containersPath);
            Files.writeString(containersPath, newFileContent, StandardCharsets.UTF_8);
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

            String projectSelector = "#super-sidebar > div.contextual-nav.gl-display-flex.gl-flex-direction-column.gl-flex-grow-1.gl-overflow-hidden > div.gl-scroll-scrim.gl-overflow-auto.gl-flex-grow-1.bottom-scrim-visible.gl-border-b > div.gl-p-2.gl-relative > ul.gl-list-none.gl-p-0.gl-m-0 > li > a > div.gl-flex-grow-1.gl-text-gray-900";
            var project = page.locator(projectSelector).textContent();
            String firstVersionSelector = "#content-body > div.flex-list > div.tags > ul > li:nth-child(1) > div.row-main-content > a";
            var version = page.locator(firstVersionSelector).textContent();
            data.add(new String[] { project.trim(), version.trim() });

            await();
        }

        Collections.sort(data, (data1, data2) -> data1[0].compareTo(data2[0]));
        return getTableStr(data);
    }

    private static String getTableStr(ArrayList<String[]> data) {
        var lines = new ArrayList<String>();
        int[] columnWidths = TablePrinter.calculateColumnWidths(data);
        columnWidths = new int[] { 35, 35 };
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
