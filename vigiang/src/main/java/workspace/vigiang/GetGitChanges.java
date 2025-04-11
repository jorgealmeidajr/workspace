package workspace.vigiang;

import com.microsoft.playwright.*;
import workspace.vigiang.service.GitLabService;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GetGitChanges {

    public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        System.out.println("## START: get git changes\n");

        var frontendRepositoryUrl = GitLabService.VigiaNG.getFrontEndUrl();

        try (Playwright playwright = Playwright.create()) {
            var launchOptions = new BrowserType.LaunchOptions()
                    .setChannel("chrome") // "chrome", "msedge", "chrome-beta", "msedge-beta" or "msedge-dev"
                    .setHeadless(false)
                    .setSlowMo(500);

            Browser browser = playwright.chromium().launch(launchOptions);
            BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(width, height));

            Page page = context.newPage();
            GitLabService.login(page);

            {
                var tags = getTags(frontendRepositoryUrl, page);

                var firstTag = tags.get(0);
                var commits = getCommits(frontendRepositoryUrl, page, firstTag);
                System.out.println(commits.size());
            }

            for (String backendUrl : GitLabService.VigiaNG.getBackendRepositoryUrls()) {
                var tags = getTags(backendUrl, page);

                var firstTag = tags.get(0);
                var commits = getCommits(backendUrl, page, firstTag);
                System.out.println(commits.size());
            }

            {
                for (String url : GitLabService.VigiaNG.getDatabaseRepositoryUrls()) {
                    page.navigate(url + "/-/issues/?sort=created_date&state=all&first_page_size=100");
                    GitLabService.await();
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

}
