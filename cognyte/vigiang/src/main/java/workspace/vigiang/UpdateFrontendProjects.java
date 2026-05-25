package workspace.vigiang;

import workspace.commons.model.FileContent;
import workspace.commons.model.FileMatch;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static workspace.commons.model.FileMatch.getContentMd;
import static workspace.commons.model.FileMatch.getContentTxt;
import static workspace.commons.service.FileContentService.getFileContentsByExtensions;
import static workspace.commons.service.FileContentService.getMatches;
import static workspace.commons.service.FileService.writeMd;
import static workspace.commons.service.FileService.writeString;


public class UpdateFrontendProjects {

    public static void run(Path frontendPath, Path versionPath) throws IOException {
        List<FileContent> frontendFileContents = getFileContentsByExtensions(
                frontendPath, List.of("js", "tsx"), List.of("node_modules", "json-server", "tests"));

        updateConfigurations(versionPath, frontendFileContents);
        updateFeatures(versionPath, frontendFileContents);
        updatePrivileges(versionPath, frontendFileContents);
        updateEnvironment(versionPath, frontendFileContents);
        updateSetup(frontendPath, versionPath);
    }

    private static void updateConfigurations(Path versionPath, List<FileContent> frontendFileContents) throws IOException {
        List<Pattern> patterns = List.of(
                Pattern.compile("getConfiguration\\(['\"]([^'\"]+)['\"]")
        );

        List<FileMatch> matches = getMatches(frontendFileContents, patterns, List.of());

        updateTxt(versionPath, matches, "configurations");
        updateMd(versionPath, matches, "configurations");
    }

    private static void updateFeatures(Path versionPath, List<FileContent> frontendFileContents) throws IOException {
        List<Pattern> patterns = List.of(
                Pattern.compile("ifFeature\\(['\"]([^'\"]+)['\"]")
        );

        List<FileMatch> matches = getMatches(frontendFileContents, patterns, List.of());

        updateTxt(versionPath, matches, "features");
        updateMd(versionPath, matches, "features");
    }

    private static void updatePrivileges(Path versionPath, List<FileContent> frontendFileContents) throws IOException {
        List<Pattern> patterns = List.of(
                Pattern.compile("hasRole\\((?:'|\")ROLE_([A-Z0-9_]+)(?:'|\")\\)"),
                Pattern.compile("(?:'|\")ROLE_([A-Z0-9_]+)(?:'|\")")
        );

        List<FileMatch> matches = getMatches(frontendFileContents, patterns, List.of());

        updateTxt(versionPath, matches, "privileges");
        updateMd(versionPath, matches, "privileges");
    }

    private static void updateEnvironment(Path versionPath, List<FileContent> frontendFileContents) throws IOException {
        List<Pattern> patterns = List.of(
                Pattern.compile("import[.]meta[.]env[.](\\w+)"),
                Pattern.compile("process[.]env[.](\\w+)"),
                Pattern.compile("env[.](\\w+)")
        );

        List<FileMatch> matches = getMatches(frontendFileContents, patterns, List.of("NODE_ENV", "globals"));

        updateTxt(versionPath, matches, "environment");
        updateMd(versionPath, matches, "environment");
    }

    private static void updateSetup(Path frontendPath, Path versionPath) throws IOException {
        List<FileContent> fileContents = getFileContentsByExtensions(
                frontendPath, List.of("dockerfile"), List.of("node_modules", "json-server", "tests"));

        writeMd(fileContents, Paths.get(versionPath + "\\setup.md"));
    }

    private static void updateTxt(Path versionPath, List<FileMatch> matches, String output) throws IOException {
        String result = "";

        if (!matches.isEmpty()) {
            List<FileMatch> matchesFiltered = matches.stream()
                    .filter(m -> m.getRelativeDir().contains("webviewer"))
                    .collect(Collectors.toList());
            if (!matchesFiltered.isEmpty()) {
                result += "webviewer:\n";
                result += getContentTxt(matchesFiltered);
                result += "\n";
            }

            matchesFiltered = matches.stream()
                    .filter(m -> m.getRelativeDir().contains("workflow"))
                    .collect(Collectors.toList());
            if (!matchesFiltered.isEmpty()) {
                result += "workflow:\n";
                result += getContentTxt(matchesFiltered);
                result += "\n";
            }
        }

        result = result.trim() + "\n";

        Path outputPath = Paths.get(versionPath + "\\" + output + ".txt");
        writeString(outputPath, result);
    }

    private static void updateMd(Path versionPath, List<FileMatch> matches, String output) throws IOException {
        String result = "";

        if (!matches.isEmpty()) {
            List<FileMatch> matchesFiltered = matches.stream()
                    .filter(m -> m.getRelativeDir().contains("webviewer"))
                    .collect(Collectors.toList());
            if (!matchesFiltered.isEmpty()) {
                result += "# webviewer:\n";
                result += "```\n";
                result += getContentMd(matchesFiltered);
                result += "```\n\n";
            }

            matchesFiltered = matches.stream()
                    .filter(m -> m.getRelativeDir().contains("workflow"))
                    .collect(Collectors.toList());
            if (!matchesFiltered.isEmpty()) {
                result += "# workflow:\n";
                result += "```\n";
                result += getContentMd(matchesFiltered);
                result += "```\n\n";
            }
        }

        result = result.trim() + "\n";

        Path outputPath = Paths.get(versionPath + "\\" + output + ".md");
        writeString(outputPath, result);
    }

}

