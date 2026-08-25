package workspace.vigiang.checkers;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import workspace.commons.service.SshService;
import workspace.vigiang.model.LaboratoryRef;
import workspace.vigiang.model.LaboratoryVigiaNg;
import workspace.vigiang.model.VersionLaboratories;
import workspace.vigiang.service.ContainersService;
import workspace.vigiang.service.EnvironmentService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

            updateVersionLaboratoriesFile(EnvironmentService.getVigiaNgPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n## END checking all containers.");
    }

    private static void updateVersionLaboratoriesFile(Path vigiaNgPath) throws Exception {
        List<String> frontendTags = ContainersService.getFrontendTags();

        Map<String, List<LaboratoryRef>> versionToLaboratories = new LinkedHashMap<>();

        List<LaboratoryVigiaNg> laboratories = EnvironmentService.getLaboratoriesVigiaNg();
        for (LaboratoryVigiaNg laboratory : laboratories) {
            List<String[]> containers = listDockerContainers(
                laboratory.getSshUsername(),
                laboratory.getSshPassword(),
                laboratory.getSshHost(),
                laboratory.getSshPort());

            List<String[]> frontendContainers = containers.stream()
                    .filter(row -> frontendTags.stream().anyMatch(row[0]::contains))
                    .toList();

            checkFrontendVersions(laboratory, frontendContainers);

            if (frontendContainers.isEmpty()) continue;

            String version = getMajorMinorVersion(frontendContainers.get(0)[1]);
            versionToLaboratories
                    .computeIfAbsent(version, k -> new ArrayList<>())
                    .add(new LaboratoryRef(laboratory.getName(), laboratory.getCarrier(), laboratory.getSshHost()));
        }

        List<VersionLaboratories> versionLaboratories = versionToLaboratories.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    List<LaboratoryRef> sortedLaboratories = entry.getValue().stream()
                            .sorted(Comparator.comparing(LaboratoryRef::getName))
                            .toList();
                    return new VersionLaboratories(entry.getKey(), sortedLaboratories);
                })
                .toList();

        Path outputPath = Paths.get(vigiaNgPath + "\\version_laboratories.json");

        DefaultIndenter indenter = new DefaultIndenter("  ", "\n");
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter()
                .withObjectIndenter(indenter)
                .withArrayIndenter(indenter);

        String json = new ObjectMapper()
                .writer(prettyPrinter)
                .writeValueAsString(versionLaboratories);
        writeString(outputPath, json);
    }

    private static void checkFrontendVersions(LaboratoryVigiaNg laboratory, List<String[]> frontendContainers) {
        // Only compare when there is more than one front-end container
        if (frontendContainers.size() <= 1) return;

        List<String> majorMinorVersions = frontendContainers.stream()
                .map(row -> getMajorMinorVersion(row[1]))
                .distinct()
                .toList();

        if (majorMinorVersions.size() > 1) {
            System.out.println("WARNING: front-end container versions do not match on "
                + laboratory.getName() + " (" + String.join(", ", majorMinorVersions) + ")");
        }
    }

    private static String getMajorMinorVersion(String version) {
        String[] parts = version.split("\\.");
        if (parts.length < 2) return version;
        return parts[0] + "." + parts[1];
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
        updateScriptFile(environmentPath, laboratoryVigiaNg, List.of("webviewer_docker_run.sh", "vigia_ng_webviewer_docker_run.sh"));
        updateScriptFile(environmentPath, laboratoryVigiaNg, List.of("workflow_docker_run.sh", "vigia_ng_workflow_docker_run.sh"));
    }

    private static void updateScriptFile(Path environmentPath, LaboratoryVigiaNg laboratoryVigiaNg, List<String> scripts) throws Exception {
        boolean anyFound = false;

        for (String script : scripts) {
            String result = readScriptFile(laboratoryVigiaNg, script);

            if (!result.isEmpty()) {
                Path outputPath = Paths.get(environmentPath + "\\" + script);
                writeString(outputPath, result);
                anyFound = true;
            }
        }

        if (!anyFound) {
            System.out.println("WARNING: none of [" + String.join(", ", scripts) + "] were found on " + laboratoryVigiaNg.getName());
        }
    }

    private static String readScriptFile(LaboratoryVigiaNg laboratoryVigiaNg, String script) throws Exception {
        var command = "cat /opt/vigiang/scripts/" + script;
        String sshResponse = SshService.execute(
                laboratoryVigiaNg.getSshUsername(),
                laboratoryVigiaNg.getSshPassword(),
                laboratoryVigiaNg.getSshHost(),
                laboratoryVigiaNg.getSshPort(),
                command);

        return sshResponse.trim();
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
