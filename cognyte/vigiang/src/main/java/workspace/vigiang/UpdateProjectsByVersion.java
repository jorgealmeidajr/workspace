package workspace.vigiang;

import workspace.vigiang.service.EnvironmentService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static workspace.vigiang.service.EnvironmentService.validateProjectDirectories;


public class UpdateProjectsByVersion {

    public static void main(String[] args) {
        for (String version : EnvironmentService.getVersions()) {
            validateProjectDirectories(version);
            Path backendPath = EnvironmentService.getBackendPath(version);
            Path frontendPath = EnvironmentService.getFrontendPath(version);
            Path versionPath = EnvironmentService.getVersionPath(version);

            Path backendOutputPath = Paths.get(versionPath + "\\back");
            Path frontendOutputPath = Paths.get(versionPath + "\\front");

            try {
                if (!Files.exists(backendOutputPath)) {
                    Files.createDirectories(backendOutputPath);
                }

                if (!Files.exists(frontendOutputPath)) {
                    Files.createDirectories(frontendOutputPath);
                }

                UpdateBackendProjects.run(backendPath, backendOutputPath);
                UpdateFrontendProjects.run(frontendPath, frontendOutputPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        UpdateMybatis.main(new String[]{});
    }

}
