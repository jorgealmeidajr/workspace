package workspace.vigiang.drafts;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static workspace.vigiang.service.EnvironmentService.getVigiaNgPath;

public class UpdateProjectsByVersion {

    public static void main(String[] args) throws IOException {
        var WORK_DIR = "C:\\work\\vigiang";

        for (String version : List.of("1.5", "1.7", "2.2")) {
            validateProjectDirectories(WORK_DIR, version);
            Path backendPath = Paths.get(WORK_DIR + "\\" + version + "\\back-" + version);
            Path frontendPath = Paths.get(WORK_DIR + "\\" + version + "\\front-" + version);
            Path versionPath = Paths.get(getVigiaNgPath() + "\\versions\\" + version);

            var backendFileContents = getFileContentsByExtensions(backendPath, List.of("java", "yaml"), List.of("commons"));
            var frontendFileContents = getFileContentsByExtensions(frontendPath, List.of("js"), List.of("node_modules"));
            VigiangFileContents vigiangFileContents = new VigiangFileContents(backendFileContents, frontendFileContents);

            updateConfigurations(versionPath, vigiangFileContents);
            updateFeatures(versionPath, vigiangFileContents);
            updatePrivileges(versionPath, vigiangFileContents);
            updateEnvironment(versionPath, vigiangFileContents);
        }
    }

    private static void updateConfigurations(Path versionPath, VigiangFileContents vigiangFileContents) throws IOException {
        List<Pattern> backendPatterns = List.of(
            Pattern.compile("\\.getConfiguration\\(['\"]([^'\"]+)['\"]")
        );

        List<Pattern> frontendPatterns = List.of(
            Pattern.compile("getConfiguration\\(['\"]([^'\"]+)['\"]")
        );

        update(versionPath, vigiangFileContents, "configurations", backendPatterns, frontendPatterns);
    }

    private static void updateFeatures(Path versionPath, VigiangFileContents vigiangFileContents) throws IOException {
        List<Pattern> backendPatterns = List.of(
            Pattern.compile("\\.ifFeature\\([\"']([^\"']+)[\"']\\)")
        );

        List<Pattern> frontendPatterns = List.of(
            Pattern.compile("ifFeature\\(['\"]([^'\"]+)['\"]")
        );

        update(versionPath, vigiangFileContents, "features", backendPatterns, frontendPatterns);
    }

    private static void updatePrivileges(Path versionPath, VigiangFileContents vigiangFileContents) throws IOException {
        List<Pattern> backendPatterns = List.of(
            Pattern.compile("((LIST_|CREATE_|CHANGE_)[A-Z_]*)")
        );

        List<Pattern> frontendPatterns = List.of(
            Pattern.compile("hasRole\\((?:'|\")ROLE_([A-Z0-9_]+)(?:'|\")\\)")
        );

        update(versionPath, vigiangFileContents, "privileges", backendPatterns, frontendPatterns);
    }

    private static void updateEnvironment(Path versionPath, VigiangFileContents vigiangFileContents) throws IOException {
        List<Pattern> backendPatterns = List.of(
            Pattern.compile("[$][{](\\w+)[}]")
        );

        List<Pattern> frontendPatterns = List.of(
            Pattern.compile("import[.]meta[.]env[.](\\w+)"),
            Pattern.compile("process[.]env[.](\\w+)"),
            Pattern.compile("env[.](\\w+)")
        );

        update(versionPath, vigiangFileContents, "environment", backendPatterns, frontendPatterns);
    }

    private static void update(Path versionPath, VigiangFileContents vigiangFileContents, String output,
                               List<Pattern> backendPatterns, List<Pattern> frontendPatterns) throws IOException {
        String resultTxt = "";

        List<String> matches = getMatches(vigiangFileContents.getFrontendFileContents(), frontendPatterns);
        if (!matches.isEmpty()) {
            resultTxt += "frontend:\n";
            resultTxt = getFileContentsTxt(matches, resultTxt);
            resultTxt += "\n";
        }

        matches = getMatches(vigiangFileContents.getBackendFileContents(), backendPatterns);
        if (!matches.isEmpty()) {
            resultTxt += "backend:\n";
            resultTxt = getFileContentsTxt(matches, resultTxt);
            resultTxt += "\n";
        }

        String newFileContent = resultTxt;
        Path allConfigurationsPath = Paths.get(versionPath + "\\" + output + ".txt");

        var initialFileContent = "";
        if (Files.exists(allConfigurationsPath)) {
            initialFileContent = new String(Files.readAllBytes(allConfigurationsPath));
        }

        if (!initialFileContent.equals(newFileContent)) {
            System.out.println("updating file: " + allConfigurationsPath);
            Files.writeString(allConfigurationsPath, newFileContent);
        }
    }

    private static String getFileContentsTxt(List<String> matches, String resultTxt) {
        Set<String> uniqueMatches = new LinkedHashSet<>(matches);
        List<String> sortedMatches = new ArrayList<>(uniqueMatches);
        sortedMatches.sort(String::compareTo);

        for (String match : sortedMatches) {
            resultTxt += "  " + match + "\n";
        }
        return resultTxt;
    }

    private static List<String> getMatches(List<String> fileContents, List<Pattern> patterns) {
        Set<String> matchesSet = new LinkedHashSet<>();

        for (String fileContent : fileContents) {
            String input = fileContent.trim();

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(input);

                while (matcher.find()) {
                    matchesSet.add(matcher.group(1));
                }
            }
        }

        return new ArrayList<>(matchesSet);
    }

    private static void validateProjectDirectories(String workDir, String version) {
        Path backendPath = Paths.get(workDir + "\\" + version + "\\back-" + version);
        if (!Files.exists(backendPath) || !Files.isDirectory(backendPath)) {
            throw new IllegalArgumentException("o diretorio backendPath nao existe ou nao eh um diretorio");
        }

        Path frontendPath = Paths.get(workDir + "\\" + version + "\\front-" + version);
        if (!Files.exists(frontendPath) || !Files.isDirectory(frontendPath)) {
            throw new IllegalArgumentException("o diretorio frontendPath nao existe ou nao eh um diretorio");
        }

        Path versionPath = Paths.get(getVigiaNgPath() + "\\versions\\" + version);
        if (!Files.exists(versionPath) || !Files.isDirectory(versionPath)) {
            throw new IllegalArgumentException("o diretorio versionPath nao existe ou nao eh um diretorio");
        }
    }

    private static List<String> getFileContentsByExtensions(Path dirPath, List<String> extensions, List<String> ignoreDirs) {
        List<String> fileContents = List.of();
        try (var stream = Files.walk(dirPath)) {
            fileContents = stream
                    .filter(p -> Files.isRegularFile(p) &&
                            extensions.stream().anyMatch(ext -> p.toString().endsWith(ext)) &&
                            ignoreDirs.stream().noneMatch(dir -> p.toString().contains("\\" + dir + "\\")))
                    .map(p -> {
                        try {
                            return Files.readString(p);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "";
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileContents;
    }

}

@AllArgsConstructor
@Getter
class VigiangFileContents {
    private final List<String> backendFileContents;
    private final List<String> frontendFileContents;
}
