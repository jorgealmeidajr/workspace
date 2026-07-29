package workspace.vigiang.checkers;

import workspace.commons.service.SshService;
import workspace.vigiang.model.LaboratoryVigiaNg;
import workspace.vigiang.service.ContainersService;
import workspace.vigiang.service.EnvironmentService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static workspace.commons.service.FileService.writeString;

public class CheckContainers {

    public static void main(String[] args) {
        System.out.println("## START checking all containers\n");
        try {
            for (LaboratoryVigiaNg laboratoryVigiaNg : EnvironmentService.getLaboratoriesVigiaNg()) {
                System.out.println(laboratoryVigiaNg.getName() + ":");
                Path laboratoryPath = EnvironmentService.getLaboratoryPath(laboratoryVigiaNg);

                updateContainersFile(laboratoryPath, laboratoryVigiaNg);
                updateDockerComposeFile(laboratoryPath, laboratoryVigiaNg);
                updateFrontendScriptFiles(laboratoryPath, laboratoryVigiaNg);

                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n## END checking all containers.");
    }

    private static void updateDockerComposeFile(Path laboratoryPath, LaboratoryVigiaNg laboratoryVigiaNg) throws Exception {
        Path outputPath = Paths.get(laboratoryPath + "\\docker-compose.yml");

        var result = getDockerCompose(
                laboratoryVigiaNg.getSshUsername(),
                laboratoryVigiaNg.getSshPassword(),
                laboratoryVigiaNg.getSshHost(),
                laboratoryVigiaNg.getSshPort());

        writeString(outputPath, result);
    }

    private static void updateContainersFile(Path laboratoryPath, LaboratoryVigiaNg laboratoryVigiaNg) throws Exception {
        Path outputPath = Paths.get(laboratoryPath + "\\containers.txt");

        var result = listContainers(
                laboratoryVigiaNg.getSshUsername(),
                laboratoryVigiaNg.getSshPassword(),
                laboratoryVigiaNg.getSshHost(),
                laboratoryVigiaNg.getSshPort());

        writeString(outputPath, result);
    }

    private static void updateFrontendScriptFiles(Path environmentPath, LaboratoryVigiaNg laboratoryVigiaNg) throws Exception {
        updateScriptFile(environmentPath, laboratoryVigiaNg, "webviewer_docker_run.sh");
        updateScriptFile(environmentPath, laboratoryVigiaNg, "workflow_docker_run.sh");
    }

    private static void updateScriptFile(Path environmentPath, LaboratoryVigiaNg laboratoryVigiaNg, String script) throws Exception {
        var command = "cat /opt/vigiang/scripts/" + script;
        String sshResponse = SshService.execute(
                laboratoryVigiaNg.getSshUsername(),
                laboratoryVigiaNg.getSshPassword(),
                laboratoryVigiaNg.getSshHost(),
                laboratoryVigiaNg.getSshPort(),
                command);

        Path outputPath = Paths.get(environmentPath + "\\" + script);

        String result = sshResponse.trim();

        writeString(outputPath, result);
    }

    private static String listContainers(String username, String password, String host, int port) throws Exception {
        List<String[]> data = listDockerContainers(username, password, host, port);
        return ContainersService.getContainersContent(data);
    }

    private static List<String[]> listDockerContainers(String username, String password, String host, int port) throws Exception {
        var command = "docker ps -a --format 'table {{.Names}}\\t{{.Image}}'";
        String sshResponse = SshService.execute(username, password, host, port, command);
        List<String> initialLines = new ArrayList<>(Arrays.asList(sshResponse.split("\\R")));
        initialLines.remove(0);
        initialLines.sort(Comparator.naturalOrder());

        List<String[]> data = new ArrayList<>();
        for (String line : initialLines) {
            String[] firstSplit = line.split(" ");
            String project = firstSplit[0];

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
        return SshService.execute(username, password, host, port, command);
    }

}
