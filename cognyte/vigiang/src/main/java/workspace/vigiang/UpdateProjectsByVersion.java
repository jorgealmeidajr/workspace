package workspace.vigiang;

import workspace.vigiang.service.EnvironmentService;

import java.nio.file.Path;
import java.nio.file.Paths;

import static workspace.vigiang.service.EnvironmentService.validateProjectDirectories;


public class UpdateProjectsByVersion {

    public static void main(String[] args) {
        var WORK_DIR = EnvironmentService.getWorkVigiaDir();

        for (String version : EnvironmentService.getVersions()) {
            validateProjectDirectories(WORK_DIR, version);
            Path backendPath = Paths.get(WORK_DIR + "\\" + version + "\\back-" + version);
            Path frontendPath = Paths.get(WORK_DIR + "\\" + version + "\\front-" + version);
            Path versionPath = Paths.get(EnvironmentService.getVigiaNgPath() + "\\versions\\" + version);

            try {
                UpdateFrontendProjects.run(frontendPath, versionPath);
                UpdateBackendProjects.run(backendPath, versionPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        UpdateMybatis.main(new String[]{});
    }

}
