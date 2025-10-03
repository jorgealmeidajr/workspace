package workspace.vigiang.drafts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static workspace.vigiang.service.EnvironmentService.getVigiaNgPath;

public class UpdateProjectsByVersion {

    public static void main(String[] args) throws IOException {
        var VERSION = "1.7";
        var WORK_DIR = "C:\\work\\vigiang";

        Path backendPath = Paths.get(WORK_DIR + "\\" + VERSION + "\\back-" + VERSION);
        if (!Files.exists(backendPath) || !Files.isDirectory(backendPath)) {
            throw new IllegalArgumentException("o diretorio backendPath nao existe ou nao eh um diretorio");
        }
        Path frontendPath = Paths.get(WORK_DIR + "\\" + VERSION + "\\front-" + VERSION);
        if (!Files.exists(frontendPath) || !Files.isDirectory(frontendPath)) {
            throw new IllegalArgumentException("o diretorio frontendPath nao existe ou nao eh um diretorio");
        }

//        List<String> javaFileContents = getJavaFileContents(backendPath);

        List<String> javascriptFileContents = getJavascriptFileContents(frontendPath);

        Set<String> matchesSet = new LinkedHashSet<>();

        for (String javaFileContent : javascriptFileContents) {
            String input = javaFileContent.trim();

            Pattern pattern = Pattern.compile("getConfiguration\\(['\"]([^'\"]+)['\"]");
            Matcher matcher = pattern.matcher(input);

            while (matcher.find()) {
                matchesSet.add(matcher.group(1));
            }
        }

        List<String> matches = new java.util.ArrayList<>(matchesSet);
        matches.sort(String::compareTo);
        String matchesString = String.join("\n", matches);

        Path vigiaNgPath = getVigiaNgPath();
        Path versionPath = Paths.get(vigiaNgPath + "\\versions\\" + VERSION);
        if (!Files.exists(versionPath) || !Files.isDirectory(versionPath)) {
            throw new IllegalArgumentException("o diretorio versionPath nao existe ou nao eh um diretorio");
        }

        Path allConfigurationsPath = Paths.get(versionPath + "\\configurations.txt");
        Files.writeString(allConfigurationsPath, matchesString);
    }

    private static List<String> getJavaFileContents(Path backendPath) {
        List<String> javaFileContents = List.of();
        try (var stream = Files.walk(backendPath)) {
            javaFileContents = stream
                .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"))
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
        return javaFileContents;
    }

    private static List<String> getJavascriptFileContents(Path frontendPath) {
        List<String> javascriptFileContents = List.of();
        try (var stream = Files.walk(frontendPath)) {
            javascriptFileContents = stream
                    .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".js"))
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
        return javascriptFileContents;
    }

}
