package workspace.vigiang.scripts;

import com.microsoft.playwright.*;
import workspace.vigiang.service.GitLabService;

import java.awt.*;

public class GitLabScriptDraft {

    public static void main(String[] args) {
        System.out.println("## START: updating laboratories.\n");

        try {
            Playwright playwright = Playwright.create();
            var launchOptions = new BrowserType.LaunchOptions()
                    .setChannel("chrome") // "chrome", "msedge", "chrome-beta", "msedge-beta" or "msedge-dev"
                    .setHeadless(false)
                    .setSlowMo(1500);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = (int) screenSize.getWidth();
            int height = (int) screenSize.getHeight();

            Browser browser = playwright.chromium().launch(launchOptions);
            BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(width, height));

            Page page = context.newPage();

            // list of laboratories
            // list of backend
            // frontend?
            execute(page);

//            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n## END: updating laboratories.");
    }

    private static void execute(Page page) {
        GitLabService.login(page);

        String backendUrl = GitLabService.VigiaNG.getBackendRepositoryUrls().stream()
                .filter(url -> url.contains("warrant-service"))
                .findFirst()
                .orElseThrow();

        page.navigate(backendUrl);

        String initialValue = getVariableValue(page, backendUrl);
        System.out.println("Initial value: " + initialValue);

        updateVariableValue(page, backendUrl, "TEST");
        System.out.println("Updated value: " + getVariableValue(page, backendUrl));

        updateVariableValue(page, backendUrl, initialValue);
        System.out.println("Updated value: " + getVariableValue(page, backendUrl));
    }

    private static String getVariableValue(Page page, String backendUrl) {
        page.navigate(backendUrl + "/-/settings/ci_cd");

        // Variables
        page.click("#js-cicd-variables-settings");

        // DEPLOY_HOSTS
        page.click("#__BVID__678 > tbody > tr > td:nth-child(4) > div > div > button:nth-child(1)");

        return page.inputValue("#ci-variable-value");
    }

    private static void updateVariableValue(Page page, String backendUrl, String value) {
        page.navigate(backendUrl + "/-/settings/ci_cd");

        // Variables
        page.click("#js-cicd-variables-settings");

        // DEPLOY_HOSTS
        page.click("#__BVID__678 > tbody > tr > td:nth-child(4) > div > div > button:nth-child(1)");

        page.fill("#ci-variable-value", value);
        page.click("#js-cicd-variables-settings > div.settings-content > div > div.row > div > div:nth-child(2) > aside > div.gl-drawer-body.gl-drawer-body-scrim > div.gl-mb-5.gl-flex.gl-gap-3 > button.btn.btn-confirm.btn-md.gl-button");
    }

}
