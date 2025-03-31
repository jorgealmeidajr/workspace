package workspace.vigiang.checkers;

import workspace.vigiang.model.Environment;
import workspace.vigiang.model.SshExecutor;
import workspace.vigiang.model.TablePrinter;
import workspace.vigiang.service.EnvironmentService;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CheckContainers {

    public static void main(String[] args) {
        var vigiangPathStr = "C:\\Users\\jjunior\\MyDocuments\\COGNYTE\\VIGIANG";
        Path vigiangPath = Paths.get(vigiangPathStr);

        System.out.println("## START checking all containers\n");
        try {
            if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
                throw new IllegalArgumentException("o diretorio do vigiang nao existe ou nao eh um diretorio");
            }

            for (Environment environment : EnvironmentService.getEnvironments()) {
                System.out.println(environment.getName() + ":");

                Path environmentPath = Paths.get(vigiangPath + "\\environments\\" + environment.getName());
                if (!Files.exists(environmentPath)) {
                    Files.createDirectories(environmentPath);
                }

                updateContainersFile(environmentPath, environment);
                updateDockerComposeFile(environmentPath, environment);

                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        System.out.println("## END checking all containers.");
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

    private static String listContainers(String username, String password, String host, int port) throws Exception {
        List<String[]> data = listDockerContainers(username, password, host, port);

        var finalLines = new ArrayList<String>();
        int[] columnWidths = new int[] { 35, 35 };
        for (String[] row : data) {
            finalLines.add(TablePrinter.printRow(row, columnWidths));
        }

        finalLines.sort(Comparator.naturalOrder());
        return String.join(System.lineSeparator(), finalLines);
    }

    private static List<String[]> listDockerContainers(String username, String password, String host, int port) throws Exception {
        var command = "docker ps -a --format 'table {{.Names}}\\t{{.Image}}'";
        String sshResponse = SshExecutor.execute(username, password, host, port, command);
        List<String> initialLines = new ArrayList<>(Arrays.asList(sshResponse.split("\\R")));
        initialLines.remove(0);
        initialLines.sort(Comparator.naturalOrder());

        Predicate<String> linesToIgnore = (line) -> !(line.startsWith("kafka") || line.startsWith("mock-smtp") || line.startsWith("zookeeper"));
        initialLines = initialLines.stream()
                .filter(linesToIgnore)
                .collect(Collectors.toList());

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
        return SshExecutor.execute(username, password, host, port, command);
    }

}
