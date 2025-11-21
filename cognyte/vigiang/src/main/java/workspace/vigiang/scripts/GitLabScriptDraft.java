package workspace.vigiang.scripts;

import workspace.commons.model.Laboratory;
import workspace.vigiang.service.EnvironmentService;
import workspace.vigiang.service.GitLabService;

import java.util.stream.Collectors;

public class GitLabScriptDraft {

    public static void main(String[] args) {
        System.out.println("## START: updating laboratories.\n");

        String[] laboratoriesList = "CLARO-01,ENTEL,MOVISTAR,TIM,VIVO".split(",");
        String[] backendServices = "warrant-service,operation-service".split(",");
        var frontend = true;

        try {
            var laboratories = EnvironmentService.getVigiangLaboratories().stream()
                    .filter(lab -> {
                        for (var labName : laboratoriesList) {
                            if (lab.getName().equalsIgnoreCase(labName.trim())) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());

            String backendDeployHosts = laboratories.stream()
                    .map(Laboratory::getSshHost)
                    .collect(Collectors.joining(" "));

            String frontendDeployHosts = laboratories.stream()
                    .map(laboratory -> laboratory.getSshHost() + "-" + laboratory.getAlias())
                    .collect(Collectors.joining(" "));

            for (var backendService : backendServices) {
                updateBackendDeployHosts(backendService, backendDeployHosts);
            }

            if (frontend) {
                updateFrontendDeployHosts(frontendDeployHosts);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n## END: updating laboratories.");
    }

    private static void updateBackendDeployHosts(String backendService, String backendDeployHosts) {
        String backendUrl = GitLabService.VigiaNG.getBackendRepositoryUrls().stream()
                .filter(url -> url.contains(backendService))
                .findFirst()
                .orElseThrow();

        System.out.println("Updating DEPLOY_HOSTS for: " + backendService);

        System.out.println();
    }

    private static void updateFrontendDeployHosts(String frontendDeployHosts) {
        String frontendUrl = GitLabService.VigiaNG.getFrontEndUrl();

        System.out.println("Updating DEPLOY_HOSTS_WEBVIEWER for frontend");

        System.out.println();

        System.out.println("Updating DEPLOY_HOSTS_WORKFLOW for frontend");

        System.out.println();
    }

}
