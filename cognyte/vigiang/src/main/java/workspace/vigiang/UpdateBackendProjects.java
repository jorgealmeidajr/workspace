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


public class UpdateBackendProjects {

    public static void run(Path backendPath, Path versionPath) throws IOException {
        List<FileContent> backendFileContents = getFileContentsByExtensions(
                backendPath, List.of("java", "yaml"), List.of("commons", "target"));

        updateConfigurations(versionPath, backendFileContents);
        updateFeatures(versionPath, backendFileContents);
        updatePrivileges(versionPath, backendFileContents);
        updateEnvironment(versionPath, backendFileContents);
        updateSetup(backendPath, versionPath);
    }

    private static void updateConfigurations(Path versionPath, List<FileContent> backendFileContents) throws IOException {
        List<Pattern> patterns = List.of(
                Pattern.compile("\\.getConfiguration\\(['\"]([^'\"]+)['\"]"),
                Pattern.compile("\"(cnfg.*)\"")
        );

        List<FileMatch> matches = getMatches(backendFileContents, patterns, List.of());
        matches = matches.stream()
                .map(m -> {
                    var matchStr = m.getMatch().replaceAll(",", "");
                    return new FileMatch(m.getRelativeDir(), matchStr);
                })
                .collect(Collectors.toList());

        updateTxt(versionPath, matches, "configurations");
        updateMd(versionPath, matches, "configurations");
    }

    private static void updateFeatures(Path versionPath, List<FileContent> backendFileContents) throws IOException {
        List<Pattern> patterns = List.of(
                Pattern.compile("\\.ifFeature\\([\"']([^\"']+)[\"']\\)")
        );

        List<FileMatch> matches = getMatches(backendFileContents, patterns, List.of());

        updateTxt(versionPath, matches, "features");
        updateMd(versionPath, matches, "features");
    }

    private static void updatePrivileges(Path versionPath, List<FileContent> backendFileContents) throws IOException {
        List<Pattern> patterns = List.of(
                Pattern.compile("((LIST_|CREATE_|CHANGE_)[A-Z_]*)")
        );

        List<FileMatch> matches = getMatches(backendFileContents, patterns, List.of("LIST_TAG"));

        updateTxt(versionPath, matches, "privileges");
        updateMd(versionPath, matches, "privileges");
    }

    private static void updateEnvironment(Path versionPath, List<FileContent> backendFileContents) throws IOException {
        List<Pattern> patterns = List.of(
                Pattern.compile("[$][{](\\w+)[}]")
        );

        List<FileMatch> matches = getMatches(backendFileContents, patterns, List.of());

        updateTxt(versionPath, matches, "environment");
        updateMd(versionPath, matches, "environment");
    }

    private static void updateSetup(Path backendPath, Path versionPath) throws IOException {
        List<FileContent> fileContents = getFileContentsByExtensions(
                backendPath, List.of("Dockerfile", "yaml", "yml"), List.of("node_modules", "commons", "target"));

        writeMd(fileContents, Paths.get(versionPath + "\\setup.md"));
    }

    private static void updateTxt(Path versionPath, List<FileMatch> matches, String output) throws IOException {
        String result = "";

        if (!matches.isEmpty()) {
            result += "backend:\n";
            result += getContentTxt(matches);
            result += "\n";
        }

        result = result.trim() + "\n";

        Path outputPath = Paths.get(versionPath + "\\" + output + ".txt");
        writeString(outputPath, result);
    }

    private static void updateMd(Path versionPath, List<FileMatch> matches, String output) throws IOException {
        String result = "";

        if (!matches.isEmpty()) {
            result += "# backend:\n";
            result += "```\n";
            result += getContentMd(matches);
            result += "```\n";
        }

        result = result.trim() + "\n";

        Path outputPath = Paths.get(versionPath + "\\" + output + ".md");
        writeString(outputPath, result);
    }

}


