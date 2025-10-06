package workspace.vigiang.drafts;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

            {
                List<String> backendFileContents = getFileContentsByExtensions(backendPath, List.of("java"), List.of("commons"));
                List<String> frontendFileContents = getFileContentsByExtensions(frontendPath, List.of("js"), List.of(""));
                updateConfigurations(versionPath, backendFileContents, frontendFileContents);
            }

            {
                List<String> backendFileContents = getFileContentsByExtensions(backendPath, List.of("java"), List.of("commons"));
                List<String> frontendFileContents = getFileContentsByExtensions(frontendPath, List.of("js"), List.of(""));
                updateFeatures(versionPath, backendFileContents, frontendFileContents);
            }
        }
    }

    private static void updateConfigurations(Path versionPath, List<String> backendFileContents, List<String> frontendFileContents) throws IOException {
        List<Pattern> backendPatterns = List.of(
            Pattern.compile("\\.getConfiguration\\(['\"]([^'\"]+)['\"]")
        );

        List<Pattern> frontendPatterns = List.of(
            Pattern.compile("getConfiguration\\(['\"]([^'\"]+)['\"]")
        );

        update(versionPath, backendFileContents, frontendFileContents, "configurations", backendPatterns, frontendPatterns);
    }

    private static void updateFeatures(Path versionPath, List<String> backendFileContents, List<String> frontendFileContents) throws IOException {
        List<Pattern> backendPatterns = List.of(
            Pattern.compile("\\.ifFeature\\([\"']([^\"']+)[\"']\\)")
        );

        List<Pattern> frontendPatterns = List.of(
            Pattern.compile("ifFeature\\(['\"]([^'\"]+)['\"]")
        );

        update(versionPath, backendFileContents, frontendFileContents, "features", backendPatterns, frontendPatterns);
    }

    private static void update(Path versionPath, List<String> backendFileContents, List<String> frontendFileContents,
                               String output, List<Pattern> backendPatterns, List<Pattern> frontendPatterns) throws IOException {
        List<String> allMatches = new ArrayList<>();
        allMatches.addAll(getMatches(backendFileContents, backendPatterns));
        allMatches.addAll(getMatches(frontendFileContents, frontendPatterns));

        Set<String> uniqueMatches = new LinkedHashSet<>(allMatches);
        List<String> sortedMatches = new ArrayList<>(uniqueMatches);
        sortedMatches.sort(String::compareTo);
        String newFileContent = String.join("\n", sortedMatches);

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

    private static List<String> getMatches(List<String> fileContents, List<Pattern> patterns) {
        Set<String> matchesSet = new LinkedHashSet<>();

        for (String javaFileContent : fileContents) {
            String input = javaFileContent.trim();

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
