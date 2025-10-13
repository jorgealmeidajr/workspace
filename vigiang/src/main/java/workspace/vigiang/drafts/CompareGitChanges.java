package workspace.vigiang.drafts;

import com.microsoft.playwright.*;
import workspace.vigiang.service.GitLabService;

import java.awt.*;

@Deprecated
public class CompareGitChanges {

    public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        System.out.println("## START: compare git changes\n");

        var launchOptions = new BrowserType.LaunchOptions()
                .setChannel("chrome")
                .setHeadless(true)
                .setSlowMo(500);

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(launchOptions)) {
            BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(width, height));
            Page page = context.newPage();
            GitLabService.login(page);

            String gitlab = "https://flngit01.cognyte.local";
            String branchSource = "1.0.76";
            String branchTarget = "1.0.59";
            String url = gitlab + "/dev/vigiang/back-end/cloud-vigiang/interception-service/-/compare/" + branchTarget + "..." + branchSource;
            page.navigate(url);

            for (Locator locator1 : page.locator("div.diff-file.file-holder").all()) {
                for (Locator locator2 : locator1.locator("strong.file-title-name").all()) {
                    var fileName = locator2.textContent().trim();
                    if (fileName.contains("SRC/src/main/resources/repository")) {
                        for (Locator locator3 : locator1.locator("tr.line_holder.new").all()) {
                            String newLineNumberStr = locator3.locator("td.new_line.diff-line-num.new").all().get(0).getAttribute("data-linenumber");
                            int newLineNumber = Integer.parseInt(newLineNumberStr);
                            String newLineContent = locator3.locator("td.line_content.new").all().get(0).textContent().trim();
                        }

                        {
                            var href = locator1.locator("a.gl-button").all().get(0).getAttribute("href");
                            var toVisit = gitlab + href;

                            Page codePage = context.newPage();
                            codePage.navigate(toVisit);

                            String code = codePage.locator("div.blob-viewer").all().get(0).textContent().trim();
                            System.out.println(code);

                            codePage.close();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("## END: compare git changes\n");
    }

}
