package workspace.vigiang.checkers;

import workspace.vigiang.model.Environment;
import workspace.vigiang.model.SshExecutor;
import workspace.vigiang.service.EnvironmentService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CheckContainers {

    public static void main(String[] args) {
        System.out.println("## START checking all containers\n");
        try {
            for (Environment environment : EnvironmentService.getEnvironments()) {
                System.out.println(environment.getName() + ":");
                Path environmentPath = EnvironmentService.getEnvironmentPath(environment);

                updateContainersFile(environmentPath, environment);
                updateDockerComposeFile(environmentPath, environment);
                updateFrontendScriptFiles(environmentPath, environment);

                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n## END checking all containers.");
    }

    private static void updateDockerComposeFile(Path environmentPath, Environment env) throws Exception {
        Path dockerComposePath = Paths.get(environmentPath + "\\docker-compose.yml");

        var initialFileContent = "";
        if (Files.exists(dockerComposePath)) {
            initialFileContent = new String(Files.readAllBytes(dockerComposePath));
        }

        var newFileContent = getDockerCompose(env.getSshUsername(), env.getSshPassword(), env.getSshHost(), env.getSshPort());

        if (!initialFileContent.equals(newFileContent)) {
            System.out.println("updating file: " + dockerComposePath);
            Files.writeString(dockerComposePath, newFileContent, StandardCharsets.UTF_8);
        }
    }

    private static void updateContainersFile(Path environmentPath, Environment env) throws Exception {
        Path containersPath = Paths.get(environmentPath + "\\containers.txt");

        var initialFileContent = "";
        if (Files.exists(containersPath)) {
            initialFileContent = new String(Files.readAllBytes(containersPath));
        }

        var newFileContent = listContainers(env.getSshUsername(), env.getSshPassword(), env.getSshHost(), env.getSshPort());

        if (!initialFileContent.equals(newFileContent)) {
            System.out.println("updating file: " + containersPath);
            Files.writeString(containersPath, newFileContent, StandardCharsets.UTF_8);
        }
    }

    private static void updateFrontendScriptFiles(Path environmentPath, Environment environment) throws Exception {
        updateScriptFile(environmentPath, environment, "webviewer_docker_run.sh");
        updateScriptFile(environmentPath, environment, "workflow_docker_run.sh");
    }

    private static void updateScriptFile(Path environmentPath, Environment environment, String script) throws Exception {
        var command = "cat /opt/vigiang/scripts/" + script;
        String sshResponse = SshExecutor.execute(environment.getSshUsername(), environment.getSshPassword(), environment.getSshHost(), environment.getSshPort(), command);

        Path inputPath = Paths.get(environmentPath + "\\" + script);
        String newFileContent = sshResponse.trim();

        var initialFileContent = "";
        if (Files.exists(inputPath)) {
            initialFileContent = new String(Files.readAllBytes(inputPath));
        }

        if (!initialFileContent.equals(newFileContent)) {
            System.out.println("updating file: " + inputPath);
            Files.writeString(inputPath, newFileContent, StandardCharsets.UTF_8);
        }
    }

    private static String listContainers(String username, String password, String host, int port) throws Exception {
        List<String[]> data = listDockerContainers(username, password, host, port);

        List<String> frontendTags = List.of("webviewer", "workflow");

        List<String> cloudControlTags = List.of(
                "auth-service",
                "config-server",
                "eureka-server",
                "user-service",
                "zuul-server"
        );

        List<String> cloudVigiangTags = List.of(
                "block-service",
                "carrier-service",
                "dashboard-service",
                "data-retention-service",
                "event-service",
                "interception-service",
                "log-service",
                "message-service",
                "operation-service",
                "portability-service",
                "process-service",
                "report-service",
                "scheduler-service",
                "system-service",
                "tracking-service",
                "voucher-service",
                "warrant-service"
        );

        var tagGroups = new HashMap<String, List<String>>();
        tagGroups.put("frontend", new ArrayList<>());
        tagGroups.put("cloud-control", new ArrayList<>());
        tagGroups.put("cloud-vigiang", new ArrayList<>());
        tagGroups.put("others", new ArrayList<>());

        for (String[] row : data) {
            String projectInitial = row[0];
            String project = row[0] + ":" + row[1];

            if (frontendTags.stream().anyMatch(projectInitial::contains)) {
                tagGroups.get("frontend").add(project);
            } else if (cloudControlTags.stream().anyMatch(projectInitial::contains)) {
                tagGroups.get("cloud-control").add(project);
            } else if (cloudVigiangTags.stream().anyMatch(projectInitial::contains)) {
                tagGroups.get("cloud-vigiang").add(project);
            } else {
                tagGroups.get("others").add(project);
            }
        }

        String content = "";
        content += getTagGroupContent(tagGroups, "frontend");
        content += getTagGroupContent(tagGroups, "cloud-control");
        content += getTagGroupContent(tagGroups, "cloud-vigiang");
        content += getTagGroupContent(tagGroups, "others");
        content = content.trim();
        content += "\n";

        return content;
    }

    private static String getTagGroupContent(HashMap<String, List<String>> tagGroups, String tagGroup) {
        String content = "";
        var tags = tagGroups.get(tagGroup);
        tags.sort(Comparator.naturalOrder());
        if (!tags.isEmpty()) {
            content += tagGroup + ":\n";
            for (var tag : tags) {
                content += "  " + tag + "\n";
            }
            content += "\n";
        }
        return content;
    }

    private static List<String[]> listDockerContainers(String username, String password, String host, int port) throws Exception {
        var command = "docker ps -a --format 'table {{.Names}}\\t{{.Image}}'";
        String sshResponse = SshExecutor.execute(username, password, host, port, command);
        List<String> initialLines = new ArrayList<>(Arrays.asList(sshResponse.split("\\R")));
        initialLines.remove(0);
        initialLines.sort(Comparator.naturalOrder());

        List<String> projectsToIgnore = List.of(
            "kafka", "mock-smtp", "zookeeper", "admin-server", "objective_moser", "cadvisor", "docker_state_exporter",
            "node_exporter", "process_exporter", "quirky_shaw"
        );

        List<String[]> data = new ArrayList<>();
        for (String line : initialLines) {
            String[] firstSplit = line.split(" ");
            String project = firstSplit[0];

            boolean shouldIgnore = projectsToIgnore.stream()
                    .anyMatch(project::contains);
            if (shouldIgnore) continue;

            String lastString = firstSplit[firstSplit.length - 1];

            String[] secondSplit = lastString.split(":");
            String version = secondSplit[secondSplit.length - 1];

            String[] row = new String[] { project, version };
            data.add(row);
        }

        return data;
    }

    private static String getDockerCompose(String username, String password, String host, int port) throws Exception {
        var command = "cat /opt/vigiang/scripts/docker-compose.yml";
        return SshExecutor.execute(username, password, host, port, command);
    }

}
