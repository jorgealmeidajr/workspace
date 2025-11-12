package workspace.vigiang.checkers;

import com.microsoft.playwright.*;
import workspace.vigiang.service.ContainersService;
import workspace.vigiang.service.EnvironmentService;
import workspace.vigiang.service.GitLabService;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

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
            GitLabService.login(page);

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
            String projectSelector = "#super-sidebar > div.contextual-nav.gl-flex.gl-grow.gl-flex-col.gl-overflow-hidden > div.gl-scroll-scrim.gl-overflow-auto.gl-grow.bottom-scrim-visible.gl-border-b > div.gl-relative.gl-p-2 > ul.gl-m-0.gl-list-none.gl-p-0 > li > a > div.gl-grow.gl-text-default.gl-break-anywhere";
            var project = page.locator(projectSelector).textContent().trim();

            String version = getLastMainTag(page);
            data.add(new String[] { project, version });
            GitLabService.await();
        }

        return ContainersService.getContainersContent(data);
    }

    private static String getLastMainTag(Page page) {
        String lastTag = "";
        for (Locator locator1 : page.locator("div.row-main-content > a").all()) {
            String tag = locator1.textContent().trim().toLowerCase();
            if (!tag.contains("-") && !tag.matches(".*rc\\d*$")) {
                lastTag = tag;
                break;
            }
        }
        return lastTag;
    }

    private static void checkFrontEndTags(Page page) {
        var url = GitLabService.VigiaNG.getFrontEndUrl();
        page.navigate(url + "/-/tags");

        var selector = "#content-body > ul > li > div.row-main-content > a";
        Locator tags = page.locator(selector);

        String lastTag = "";
        for (int i = 0; i < tags.count(); i++) {
            String tag = tags.nth(i).textContent().trim();
            if (!tag.contains("-") && !tag.matches(".*rc\\d*$")) {
                lastTag = tag;
                break;
            }
        }
        System.out.println(lastTag);

        GitLabService.await();
    }

}
