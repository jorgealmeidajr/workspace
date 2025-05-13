package workspace.vigiang.scripts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GetVersionsFromCompose {

    public static void main(String[] args) {
        Path scriptsPath = getScriptsPath();
        Path inputPath = Paths.get(scriptsPath.toString(), "get-versions-from-compose-input.txt");

        try {
            List<ProjectVersion> versionsFromCompose = getVersionsFromCompose(inputPath);
            List<ProjectVersion> versionsFromContainersTxt = getVersionsFromContainersTxt();

            for (ProjectVersion project1 : versionsFromCompose) {
                for (ProjectVersion project2 : versionsFromContainersTxt) {

                    if (project1.getName().equals(project2.getName())
                            && project1.getVersion().compareTo(project2.getVersion()) < 0) {
                        System.out.println(project1.getName() + ": " + project1.getVersion() + " to " + project2.getVersion());
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<ProjectVersion> getVersionsFromContainersTxt() throws IOException {
        Path vigiangPath = EnvironmentService.getVigiaNgPath();
        Path containersPath = Paths.get(vigiangPath + "\\containers.txt");

        List<ProjectVersion> versions = new ArrayList<>();

        var fileLines = Files.readAllLines(containersPath);
        for (var line : fileLines) {
            String[] lineSplit = line.split("[|]");
            ProjectVersion projectVersion = new ProjectVersion(lineSplit[1].trim(), lineSplit[2].trim());
            versions.add(projectVersion);
        }
        return versions;
    }

    @AllArgsConstructor
    @Getter
    static class ProjectVersion {
        private final String name;
        private final String version;

        @Override
        public String toString() {
            return name + ":" + version;
        }
    }

    private static List<ProjectVersion> getVersionsFromCompose(Path inputPath) throws IOException {
        String input = new String(Files.readAllBytes(inputPath));
        List<ProjectVersion> versions = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\s*image:\\s*.*[/](.*)");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String version = matcher.group(1);

            if (!version.contains("admin-server:") && !version.contains("integration-service")) {
                String[] versionSplit = version.split(":");
                ProjectVersion projectVersion = new ProjectVersion(versionSplit[0], versionSplit[1]);
                versions.add(projectVersion);
            }
        }

        versions = versions.stream()
                .sorted(Comparator.comparing(ProjectVersion::getName))
                .collect(Collectors.toList());
        return versions;
    }

    public static Path getScriptsPath() {
        Path scriptsPath = Paths.get("scripts");
        if (!Files.exists(scriptsPath) || !Files.isDirectory(scriptsPath)) {
            throw new IllegalArgumentException("o diretorio 'scripts' nao existe ou nao eh um diretorio");
        }
        return scriptsPath;
    }

}
