package workspace.vigiang;

import workspace.commons.model.FileContent;
import workspace.commons.model.FileMatch;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static workspace.commons.model.FileMatch.getContentMd;
import static workspace.commons.model.FileMatch.getContentTxt;
import static workspace.commons.service.FileContentService.getFileContentsByExtensions;
import static workspace.commons.service.FileContentService.getMatches;
import static workspace.commons.service.FileService.writeMd;
import static workspace.commons.service.FileService.writeString;
import static workspace.vigiang.service.EnvironmentService.validateProjectDirectories;


public class UpdateProjectsByVersion {

    public static void main(String[] args) {
        var WORK_DIR = "C:\\work\\vigiang";

        for (String version : EnvironmentService.getVersions()) {
            validateProjectDirectories(WORK_DIR, version);
            Path backendPath = Paths.get(WORK_DIR + "\\" + version + "\\back-" + version);
            Path frontendPath = Paths.get(WORK_DIR + "\\" + version + "\\front-" + version);
            Path versionPath = Paths.get(EnvironmentService.getVigiaNgPath() + "\\versions\\" + version);

            final var backendFileContents = getFileContentsByExtensions(backendPath, List.of("java", "yaml"), List.of("commons", "target"));
            final var frontendFileContents = getFileContentsByExtensions(frontendPath, List.of("js", "tsx"), List.of("node_modules", "json-server", "tests"));
            VigiaFileContents vigiaFileContents = new VigiaFileContents(backendFileContents, frontendFileContents);

            try {
                updateConfigurations(versionPath, vigiaFileContents);
                updateFeatures(versionPath, vigiaFileContents);
                updatePrivileges(versionPath, vigiaFileContents);
                updateEnvironment(versionPath, vigiaFileContents);
                updateSetup(frontendPath, backendPath, versionPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        UpdateMybatis.main(new String[] {});
    }

    private static void updateConfigurations(Path versionPath, VigiaFileContents vigiaFileContents) throws IOException {
        List<Pattern> backendPatterns = List.of(
            Pattern.compile("\\.getConfiguration\\(['\"]([^'\"]+)['\"]"),
            Pattern.compile("\"(cnfg.*)\"")
        );

        List<Pattern> frontendPatterns = List.of(
            Pattern.compile("getConfiguration\\(['\"]([^'\"]+)['\"]")
        );

        List<FileMatch> frontendMatches = getMatches(vigiaFileContents.frontendFileContents(), frontendPatterns, List.of());

        List<FileMatch> backendMatches = getMatches(vigiaFileContents.backendFileContents(), backendPatterns, List.of());
        backendMatches = backendMatches.stream()
                .map(m -> {
                    var matchStr = m.getMatch().replaceAll(",", "");
                    return new FileMatch(m.getRelativeDir(), matchStr);
                })
                .collect(Collectors.toList());

        var vigiangMatches = new VigiaMatches(backendMatches, frontendMatches, VigiangMatchType.CONFIGURATION);

        updateTxt(versionPath, vigiangMatches, "configurations");
        updateMd(versionPath, vigiangMatches, "configurations");
    }

    private static void updateFeatures(Path versionPath, VigiaFileContents vigiaFileContents) throws IOException {
        List<Pattern> backendPatterns = List.of(
            Pattern.compile("\\.ifFeature\\([\"']([^\"']+)[\"']\\)")
        );

        List<Pattern> frontendPatterns = List.of(
            Pattern.compile("ifFeature\\(['\"]([^'\"]+)['\"]")
        );

        var vigiangMatches = new VigiaMatches(
            getMatches(vigiaFileContents.backendFileContents(), backendPatterns, List.of()),
            getMatches(vigiaFileContents.frontendFileContents(), frontendPatterns, List.of()),
            VigiangMatchType.FEATURE);

        updateTxt(versionPath, vigiangMatches, "features");
        updateMd(versionPath, vigiangMatches, "features");
    }

    private static void updatePrivileges(Path versionPath, VigiaFileContents vigiaFileContents) throws IOException {
        List<Pattern> backendPatterns = List.of(
            Pattern.compile("((LIST_|CREATE_|CHANGE_)[A-Z_]*)")
        );

        List<Pattern> frontendPatterns = List.of(
            Pattern.compile("hasRole\\((?:'|\")ROLE_([A-Z0-9_]+)(?:'|\")\\)"),
            Pattern.compile("(?:'|\")ROLE_([A-Z0-9_]+)(?:'|\")")
        );

        var vigiangMatches = new VigiaMatches(
            getMatches(vigiaFileContents.backendFileContents(), backendPatterns, List.of("LIST_TAG")),
            getMatches(vigiaFileContents.frontendFileContents(), frontendPatterns, List.of()),
            VigiangMatchType.PRIVILEGE);

        updateTxt(versionPath, vigiangMatches, "privileges");
        updateMd(versionPath, vigiangMatches, "privileges");
    }

    private static void updateEnvironment(Path versionPath, VigiaFileContents vigiaFileContents) throws IOException {
        List<Pattern> backendPatterns = List.of(
            Pattern.compile("[$][{](\\w+)[}]")
        );

        List<Pattern> frontendPatterns = List.of(
            Pattern.compile("import[.]meta[.]env[.](\\w+)"),
            Pattern.compile("process[.]env[.](\\w+)"),
            Pattern.compile("env[.](\\w+)")
        );

        var vigiangMatches = new VigiaMatches(
            getMatches(vigiaFileContents.backendFileContents(), backendPatterns, List.of()),
            getMatches(vigiaFileContents.frontendFileContents(), frontendPatterns, List.of("NODE_ENV", "globals")),
            VigiangMatchType.ENVIRONMENT);

        updateTxt(versionPath, vigiangMatches, "environment");
        updateMd(versionPath, vigiangMatches, "environment");
    }

    private static void updateSetup(Path frontendPath, Path backendPath, Path versionPath) throws IOException {
        List<FileContent> fileContents = new ArrayList<>();
        fileContents.addAll(getFileContentsByExtensions(frontendPath, List.of("dockerfile"), List.of("node_modules", "json-server", "tests")));
        fileContents.addAll(getFileContentsByExtensions(backendPath, List.of("Dockerfile", "yaml", "yml"), List.of("node_modules", "commons", "target")));

        writeMd(fileContents, Paths.get(versionPath + "\\setup.md"));
    }

    private static void updateTxt(Path versionPath, VigiaMatches vigiaMatches, String output) throws IOException {
        String result = "";

        if (!vigiaMatches.frontendMatches().isEmpty()) {
            List<FileMatch> matchesFiltered = vigiaMatches.frontendMatches().stream()
                    .filter(m -> m.getRelativeDir().contains("webviewer"))
                    .collect(Collectors.toList());
            if (!matchesFiltered.isEmpty()) {
                result += "webviewer:\n";
                result += getContentTxt(matchesFiltered);
                result += "\n";
            }

            matchesFiltered = vigiaMatches.frontendMatches().stream()
                    .filter(m -> m.getRelativeDir().contains("workflow"))
                    .collect(Collectors.toList());
            if (!matchesFiltered.isEmpty()) {
                result += "workflow:\n";
                result += getContentTxt(matchesFiltered);
                result += "\n";
            }
        }

        if (!vigiaMatches.backendMatches().isEmpty()) {
            result += "backend:\n";
            result += getContentTxt(vigiaMatches.backendMatches());
            result += "\n";
        }

        result = result.trim() + "\n";

        Path outputPath = Paths.get(versionPath + "\\" + output + ".txt");
        writeString(outputPath, result);
    }

    private static void updateMd(Path versionPath, VigiaMatches vigiaMatches, String output) throws IOException {
        String result = "";

        if (!vigiaMatches.frontendMatches().isEmpty()) {
            List<FileMatch> matchesFiltered = vigiaMatches.frontendMatches().stream()
                    .filter(m -> m.getRelativeDir().contains("webviewer"))
                    .collect(Collectors.toList());
            if (!matchesFiltered.isEmpty()) {
                result += "# webviewer:\n";
                result += "```\n";
                result += getContentMd(matchesFiltered);
                result += "```\n\n";
            }

            matchesFiltered = vigiaMatches.frontendMatches().stream()
                    .filter(m -> m.getRelativeDir().contains("workflow"))
                    .collect(Collectors.toList());
            if (!matchesFiltered.isEmpty()) {
                result += "# workflow:\n";
                result += "```\n";
                result += getContentMd(matchesFiltered);
                result += "```\n\n";
            }
        }

        if (!vigiaMatches.backendMatches().isEmpty()) {
            result += "# backend:\n";
            result += "```\n";
            result += getContentMd(vigiaMatches.backendMatches());
            result += "```\n";
        }

        result = result.trim() + "\n";

        Path outputPath = Paths.get(versionPath + "\\" + output + ".md");
        writeString(outputPath, result);
    }

}

record VigiaFileContents(List<FileContent> backendFileContents, List<FileContent> frontendFileContents) { }

record VigiaMatches(List<FileMatch> backendMatches, List<FileMatch> frontendMatches, VigiangMatchType type) { }

enum VigiangMatchType {
    CONFIGURATION, FEATURE, PRIVILEGE, ENVIRONMENT
}
