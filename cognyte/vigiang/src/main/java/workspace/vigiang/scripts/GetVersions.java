package workspace.vigiang.scripts;

import workspace.vigiang.model.ProjectVersion;
import workspace.vigiang.service.ContainersService;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GetVersions {

    static Path INPUT_TXT_PATH = Paths.get(getVigiangTempPath().toString(), "input.txt");
    static Path OUTPUT_TXT_PATH = Paths.get(getVigiangTempPath().toString(), "output.txt");
    static Path OUTPUT_CSV_PATH = Paths.get(getVigiangTempPath().toString(), "output.csv");

    static class FromCompose {
        public static void main(String[] args) {
            try {
                List<ProjectVersion> versionsFromCompose = getVersionsFromCompose(INPUT_TXT_PATH);
                List<String[]> data = versionsFromCompose.stream()
                        .map(ProjectVersion::toArray)
                        .collect(Collectors.toList());
                String content = ContainersService.getContainersContent(data);
                Files.writeString(OUTPUT_TXT_PATH, content, StandardCharsets.UTF_8);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class FromComposeCompareContainersTxt {
        public static void main(String[] args) {
            try {
                List<ProjectVersion> versionsFromCompose = getVersionsFromCompose(INPUT_TXT_PATH);
                List<ProjectVersion> versionsFromContainersTxt = getVersionsFromContainersTxt();

                String csv = "\"Tag Atual\";\"Nova Tag\"\n";
                for (ProjectVersion project1 : versionsFromCompose) {
                    for (ProjectVersion project2 : versionsFromContainersTxt) {
                        if (project1.getName().equals(project2.getName())) {
                            if (!project1.getVersion().equals(project2.getVersion())) {
                                csv += "\"" + project1 + "\";\"" + project2 + "\"\n";
                            }
                            break;
                        }
                    }
                }

                Files.writeString(OUTPUT_CSV_PATH, csv.trim(), StandardCharsets.UTF_8);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static List<ProjectVersion> getVersionsFromCompose(Path inputPath) throws IOException {
        String input = new String(Files.readAllBytes(inputPath));
        List<ProjectVersion> versions = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\s*image:\\s*.*[/](.*)");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String version = matcher.group(1);

            if (!version.contains("integration-service")) {
                String[] versionSplit = version.split(":");
                ProjectVersion projectVersion = new ProjectVersion(versionSplit[0].trim(), versionSplit[1].trim().replace("'", ""));
                versions.add(projectVersion);
            }
        }

        versions = versions.stream()
                .sorted(Comparator.comparing(ProjectVersion::getName))
                .collect(Collectors.toList());
        return versions;
    }

    static List<ProjectVersion> getVersionsFromContainersTxt() throws IOException {
        Path vigiangPath = EnvironmentService.getVigiaNgPath();
        Path containersPath = Paths.get(vigiangPath + "\\containers.txt");

        List<ProjectVersion> versions = new ArrayList<>();

        var fileLines = Files.readAllLines(containersPath);
        fileLines = fileLines.stream()
                .filter(line -> line.matches("^\\s+\\S.*"))
                .map(String::trim)
                .collect(Collectors.toList());
        for (var line : fileLines) {
            String[] lineSplit = line.split(":");
            ProjectVersion projectVersion = new ProjectVersion(lineSplit[0].trim(), lineSplit[1].trim());
            versions.add(projectVersion);
        }
        return versions;
    }

    static Path getVigiangTempPath() {
        Path scriptsPath = Paths.get("C:\\Users\\jjunior\\MyDocuments\\vigiang-temp");
        if (!Files.exists(scriptsPath) || !Files.isDirectory(scriptsPath)) {
            throw new IllegalArgumentException("o diretorio 'vigiang-temp' nao existe ou nao eh um diretorio");
        }
        return scriptsPath;
    }

}
