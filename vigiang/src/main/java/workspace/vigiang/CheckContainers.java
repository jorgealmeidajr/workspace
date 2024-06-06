package workspace.vigiang;

import workspace.vigiang.model.CredentialsSSH;
import workspace.vigiang.model.Environment;
import workspace.vigiang.model.SshExecutor;
import workspace.vigiang.model.TablePrinter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CheckContainers {

    public static void main(String[] args) {
        var vigiangPathStr = "C:\\Users\\jjunior\\OneDrive - COGNYTE\\Documents\\COGNYTE\\VIGIANG";
        Path vigiangPath = Paths.get(vigiangPathStr);

        try {
            if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
                throw new IllegalArgumentException("o diretorio do vigiang nao existe ou nao eh um diretorio");
            }

            for (Environment env : Environment.values()) {
                System.out.println("######");
                System.out.println(env);

                var credentials = CredentialsSSH.getCredentials(env);
                var port = Integer.parseInt(credentials.get("port"));

                {
                    var containersPathStr = vigiangPath + "\\envs\\" + env + "\\DEV\\containers.txt";
                    Path containersPath = Paths.get(containersPathStr);
                    System.out.println("updating file: " + containersPath);

                    var response = listContainers(credentials.get("username"), credentials.get("password"), credentials.get("host"), port);
                    Files.writeString(containersPath, response, StandardCharsets.UTF_8);
                    System.out.println("file updated");
                }

                {
                    Path dockerComposePath = Paths.get(vigiangPath + "\\envs\\" + env + "\\DEV\\docker-compose.yml");
                    System.out.println("updating file: " + dockerComposePath);
                    var response = getDockerCompose(credentials.get("username"), credentials.get("password"), credentials.get("host"), port);
                    Files.writeString(dockerComposePath, response, StandardCharsets.UTF_8);
                    System.out.println("file updated");
                }

                System.out.println("######\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String listContainers(String username, String password, String host, int port) throws Exception {
        List<String[]> data = listDockerContainers(username, password, host, port);

        var finalLines = new ArrayList<String>();
        int[] columnWidths = TablePrinter.calculateColumnWidths(data);
        columnWidths = new int[] { 35, 35 };
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

        var patternsToIgnore = Arrays.asList("kafka", "mock-smtp", "zookeeper", "vigiang_claro_block", "integration-service");
        initialLines = initialLines.stream()
                .filter((line) -> !(line.startsWith("kafka") || line.startsWith("mock-smtp") || line.startsWith("zookeeper")
                    || line.startsWith("vigiang_claro_block") || line.startsWith("integration-service")))
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

    public static String getDockerCompose(String username, String password, String host, int port) throws Exception {
        var command = "cat /opt/vigiang/scripts/docker-compose.yml";
        return SshExecutor.execute(username, password, host, port, command);
    }

}
