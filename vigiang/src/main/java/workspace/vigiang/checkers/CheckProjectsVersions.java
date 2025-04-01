package workspace.vigiang.checkers;

import com.microsoft.playwright.*;
import workspace.vigiang.service.EnvironmentService;
import workspace.vigiang.service.GitLabService;
import workspace.vigiang.model.TablePrinter;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class CheckProjectsVersions {

    public static void main(String[] args) {
        System.out.println("## START checking all projects versions\n");
        try (Playwright playwright = Playwright.create()) {
            var launchOptions = new BrowserType.LaunchOptions()
                    .setChannel("chrome") // "chrome", "msedge", "chrome-beta", "msedge-beta" or "msedge-dev"
                    .setHeadless(true)
                    .setSlowMo(500);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = (int) screenSize.getWidth();
            int height = (int) screenSize.getHeight();

            Browser browser = playwright.chromium().launch(launchOptions);
            BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(width, height));

            Page page = context.newPage();
            page.navigate("https://flngit01.cognyte.local/dev");

            page.locator("#ldapmain_username").pressSequentially("jjunior");
            page.locator("#ldapmain_password").pressSequentially("Floripa2025#");
            page.locator("#ldapmain > form > button").click();
            await();

            updateContainersFile(page);
//            checkFrontEndTags(page);

            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n## END checking all projects versions.");
    }

    private static void updateContainersFile(Page page) throws IOException {
        Path vigiangPath = EnvironmentService.getVigiaNgPath();
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
        var urls = GitLabService.VigiaNG.getBackendRepositoryUrls();

        for (String url : urls) {
            page.navigate(url + "/-/tags");

            // copy selector from page
            String projectSelector = "#super-sidebar > div.contextual-nav.gl-flex.gl-grow.gl-flex-col.gl-overflow-hidden > div.gl-scroll-scrim.gl-overflow-auto.gl-grow.bottom-scrim-visible.gl-border-b > div.gl-relative.gl-p-2 > ul.gl-m-0.gl-list-none.gl-p-0 > li > a > div.gl-grow.gl-text-gray-900";
            var project = page.locator(projectSelector).textContent();

            // copy selector from page
            String firstVersionSelector = "#content-body > ul > li:nth-child(1) > div.row-main-content > a";
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
