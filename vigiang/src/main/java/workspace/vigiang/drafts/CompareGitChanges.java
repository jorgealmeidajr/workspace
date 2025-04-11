package workspace.vigiang.drafts;

import com.microsoft.playwright.*;
import workspace.vigiang.service.GitLabService;

import java.awt.*;
import java.util.ArrayList;

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

            String branchSource = "1.0.76";
            String branchTarget = "1.0.59";
            String url = "https://flngit01.cognyte.local/dev/vigiang/back-end/cloud-vigiang/interception-service/-/compare/" + branchTarget + "..." + branchSource;
            page.navigate(url);

            String selector = "div.file-header-content > a > strong";
            var filesChanged = new ArrayList<String>();
            for (Locator li : page.locator(selector).all()) {
                filesChanged.add(li.textContent().trim());
//                System.out.println(li.textContent().trim());
            }

            Locator rows1 = page.locator(selector);
            int count = rows1.count();
            for (int i = 0; i < count; ++i) {
                Locator l = rows1.nth(i);
                var fileName = l.textContent().trim();
                if (fileName.contains("SRC/src/main/resources/repository")) {
                    System.out.println(i + " >> nth= " + fileName);

//                    var sel2 = "div.diff-file >> nth=" + i + " > td.line_content.new";
                    var sel2 = "div.diff-file >> nth=" + i;
                    Locator l2 = page.locator(sel2);

                    for (Locator li : l2.locator("td.line_content.new").all()) {
                        System.out.println(li.textContent().trim());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("## END: compare git changes\n");
    }

}
