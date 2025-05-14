package workspace.vigiang.scripts;

import workspace.vigiang.model.ProjectVersion;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GetVersionsFromText {

    public static void main(String[] args) {
        Path vigiangTempPath = getVigiangTempPath();
        Path inputPath = Paths.get(vigiangTempPath.toString(), "get-versions-from-text-input.txt");
        Path outputPath = Paths.get(vigiangTempPath.toString(), "get-versions-from-text-output.csv");

        try {
            List<ProjectVersion> versionsFromText = getVersionsFromText(inputPath);
            List<ProjectVersion> versionsFromContainersTxt = getVersionsFromContainersTxt();

            String csv = "\"Tag Atual\";\"Nova Tag\"\n";
            for (ProjectVersion project1 : versionsFromText) {
                for (ProjectVersion project2 : versionsFromContainersTxt) {
                    if (project1.getName().equals(project2.getName())) {
                        if (!project1.getVersion().equals(project2.getVersion())) {
                            csv += "\"" + project1 + "\";\"" + project2 + "\";\"\"\n";
                        }
                        break;
                    }
                }
            }

            String csvFileContent = csv.trim();
            Files.writeString(outputPath, csvFileContent, StandardCharsets.UTF_8);
            System.out.println(outputPath.toAbsolutePath() + " file updated successfully");

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

    private static List<ProjectVersion> getVersionsFromText(Path inputPath) throws IOException {
        List<ProjectVersion> versions = new ArrayList<>();
        var fileLines = Files.readAllLines(inputPath);

        for (var line : fileLines) {
            if (!line.contains("admin-server:") && !line.contains("integration-service")) {
                String[] versionSplit = line.split(":");
                ProjectVersion projectVersion = new ProjectVersion(versionSplit[0].trim(), versionSplit[1].trim());
                versions.add(projectVersion);
            }
        }

        versions = versions.stream()
                .sorted(Comparator.comparing(ProjectVersion::getName))
                .collect(Collectors.toList());
        return versions;
    }

    public static Path getVigiangTempPath() {
        Path scriptsPath = Paths.get("C:\\Users\\jjunior\\MyDocuments\\vigiang-temp");
        if (!Files.exists(scriptsPath) || !Files.isDirectory(scriptsPath)) {
            throw new IllegalArgumentException("o diretorio 'vigiang-temp' nao existe ou nao eh um diretorio");
        }
        return scriptsPath;
    }

}
