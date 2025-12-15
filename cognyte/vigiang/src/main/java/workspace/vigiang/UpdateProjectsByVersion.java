package workspace.vigiang;

import lombok.AllArgsConstructor;
import lombok.Getter;
import workspace.commons.model.FileContent;
import workspace.commons.model.FileMatch;
import workspace.commons.model.XmlMyBatisMapping;
import workspace.commons.service.MappersService;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static workspace.commons.service.FileService.*;


public class UpdateProjectsByVersion {

    static final List<VigiangMatches> MATCHES = new ArrayList<>();

    public static void main(String[] args) {
        var WORK_DIR = "C:\\work\\vigiang";

        for (String version : EnvironmentService.getVersions()) {
            validateProjectDirectories(WORK_DIR, version);
            Path backendPath = Paths.get(WORK_DIR + "\\" + version + "\\back-" + version);
            Path frontendPath = Paths.get(WORK_DIR + "\\" + version + "\\front-" + version);
            Path versionPath = Paths.get(EnvironmentService.getVigiaNgPath() + "\\versions\\" + version);

            var backendFileContents = getFileContentsByExtensions(backendPath, List.of("java", "yaml"), List.of("commons", "target"));
            var frontendFileContents = getFileContentsByExtensions(frontendPath, List.of("js", "tsx"), List.of("node_modules", "json-server", "tests"));
            VigiangFileContents vigiangFileContents = new VigiangFileContents(backendFileContents, frontendFileContents);

            try {
                updateConfigurations(versionPath, vigiangFileContents);
                updateFeatures(versionPath, vigiangFileContents);
                updatePrivileges(versionPath, vigiangFileContents);
                updateEnvironment(versionPath, vigiangFileContents);

                backendFileContents = getFileContentsByExtensions(backendPath, List.of("xml"), List.of("commons", "target")).stream()
                    .filter(f -> f.getRelativeDir().contains("\\repository\\"))
                    .collect(Collectors.toList());
                updateMappers(versionPath, backendFileContents);
                writeMd(backendFileContents, Paths.get(versionPath + "\\mybatis.md"));

                List<FileContent> setupFileContents = new ArrayList<>();
                setupFileContents.addAll(getFileContentsByExtensions(frontendPath, List.of("dockerfile"), List.of("node_modules", "json-server", "tests")));
                setupFileContents.addAll(getFileContentsByExtensions(backendPath, List.of("Dockerfile", "yaml", "yml"), List.of("node_modules", "commons", "target")));
                writeMd(setupFileContents, Paths.get(versionPath + "\\setup.md"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            MATCHES.clear();
        }
    }

    private static void updateConfigurations(Path versionPath, VigiangFileContents vigiangFileContents) throws IOException {
        List<Pattern> backendPatterns = List.of(
            Pattern.compile("\\.getConfiguration\\(['\"]([^'\"]+)['\"]"),
            Pattern.compile("\"(cnfg.*)\"")
        );

        List<Pattern> frontendPatterns = List.of(
            Pattern.compile("getConfiguration\\(['\"]([^'\"]+)['\"]")
        );

        List<FileMatch> frontendMatches = getMatches(vigiangFileContents.getFrontendFileContents(), frontendPatterns, List.of());

        List<FileMatch> backendMatches = getMatches(vigiangFileContents.getBackendFileContents(), backendPatterns, List.of());
        backendMatches = backendMatches.stream()
                .map(m -> {
                    var matchStr = m.getMatch().replaceAll(",", "");
                    return new FileMatch(m.getRelativeDir(), matchStr);
                })
                .collect(Collectors.toList());

        var vigiangMatches = new VigiangMatches(backendMatches, frontendMatches, VigiangMatchType.CONFIGURATION);
        MATCHES.add(vigiangMatches);

        updateTxt(versionPath, vigiangMatches, "configurations");
        updateMd(versionPath, vigiangMatches, "configurations");
    }

    private static void updateFeatures(Path versionPath, VigiangFileContents vigiangFileContents) throws IOException {
        List<Pattern> backendPatterns = List.of(
            Pattern.compile("\\.ifFeature\\([\"']([^\"']+)[\"']\\)")
        );

        List<Pattern> frontendPatterns = List.of(
            Pattern.compile("ifFeature\\(['\"]([^'\"]+)['\"]")
        );

        var vigiangMatches = new VigiangMatches(
            getMatches(vigiangFileContents.getBackendFileContents(), backendPatterns, List.of()),
            getMatches(vigiangFileContents.getFrontendFileContents(), frontendPatterns, List.of()),
            VigiangMatchType.FEATURE);
        MATCHES.add(vigiangMatches);

        updateTxt(versionPath, vigiangMatches, "features");
        updateMd(versionPath, vigiangMatches, "features");
    }

    private static void updatePrivileges(Path versionPath, VigiangFileContents vigiangFileContents) throws IOException {
        List<Pattern> backendPatterns = List.of(
            Pattern.compile("((LIST_|CREATE_|CHANGE_)[A-Z_]*)")
        );

        List<Pattern> frontendPatterns = List.of(
            Pattern.compile("hasRole\\((?:'|\")ROLE_([A-Z0-9_]+)(?:'|\")\\)"),
            Pattern.compile("(?:'|\")ROLE_([A-Z0-9_]+)(?:'|\")")
        );

        var vigiangMatches = new VigiangMatches(
            getMatches(vigiangFileContents.getBackendFileContents(), backendPatterns, List.of("LIST_TAG")),
            getMatches(vigiangFileContents.getFrontendFileContents(), frontendPatterns, List.of()),
            VigiangMatchType.PRIVILEGE);
        MATCHES.add(vigiangMatches);

        updateTxt(versionPath, vigiangMatches, "privileges");
        updateMd(versionPath, vigiangMatches, "privileges");
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

        var vigiangMatches = new VigiangMatches(
            getMatches(vigiangFileContents.getBackendFileContents(), backendPatterns, List.of()),
            getMatches(vigiangFileContents.getFrontendFileContents(), frontendPatterns, List.of("NODE_ENV", "globals")),
            VigiangMatchType.ENVIRONMENT);
        MATCHES.add(vigiangMatches);

        updateTxt(versionPath, vigiangMatches, "environment");
        updateMd(versionPath, vigiangMatches, "environment");
    }

    private static void updateTxt(Path versionPath, VigiangMatches vigiangMatches, String output) throws IOException {
        String result = "";

        if (!vigiangMatches.getFrontendMatches().isEmpty()) {
            List<FileMatch> matchesFiltered = vigiangMatches.getFrontendMatches().stream()
                    .filter(m -> m.getRelativeDir() != null && m.getRelativeDir().contains("webviewer"))
                    .collect(Collectors.toList());
            if (!matchesFiltered.isEmpty()) {
                result += "webviewer:\n";
                result += getFileContentsTxt(matchesFiltered);
                result += "\n";
            }

            matchesFiltered = vigiangMatches.getFrontendMatches().stream()
                    .filter(m -> m.getRelativeDir() != null && m.getRelativeDir().contains("workflow"))
                    .collect(Collectors.toList());
            if (!matchesFiltered.isEmpty()) {
                result += "workflow:\n";
                result += getFileContentsTxt(matchesFiltered);
                result += "\n";
            }
        }

        if (!vigiangMatches.getBackendMatches().isEmpty()) {
            result += "backend:\n";
            result += getFileContentsTxt(vigiangMatches.getBackendMatches());
            result += "\n";
        }

        result = result.trim() + "\n";

        Path outputPath = Paths.get(versionPath + "\\" + output + ".txt");
        writeString(outputPath, result);
    }

    private static void updateMappers(Path versionPath, List<FileContent> backendFileContents) {
        try {
            var mappings = new ArrayList<XmlMyBatisMapping>();
            for (FileContent backendFileContent : backendFileContents) {
                String database = null;
                if (backendFileContent.getRelativeDir().endsWith("\\oracle")) {
                    database = "oracle";
                } else if (backendFileContent.getRelativeDir().endsWith("\\postgres")) {
                    database = "postgres";
                }

                var mapping = MappersService.getXmlMappings(backendFileContent.getContent(), database);
                mappings.add(mapping);
            }

            MappersService.writeMappers(versionPath, mappings);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void updateMd(Path versionPath, VigiangMatches vigiangMatches, String output) throws IOException {
        String result = "";

        if (!vigiangMatches.getFrontendMatches().isEmpty()) {
            List<FileMatch> matchesFiltered = vigiangMatches.getFrontendMatches().stream()
                    .filter(m -> m.getRelativeDir() != null && m.getRelativeDir().contains("webviewer"))
                    .collect(Collectors.toList());
            if (!matchesFiltered.isEmpty()) {
                result += "# webviewer:\n";
                result += "```\n";
                result += getFileContentsMd(matchesFiltered);
                result += "```\n\n";
            }

            matchesFiltered = vigiangMatches.getFrontendMatches().stream()
                    .filter(m -> m.getRelativeDir() != null && m.getRelativeDir().contains("workflow"))
                    .collect(Collectors.toList());
            if (!matchesFiltered.isEmpty()) {
                result += "# workflow:\n";
                result += "```\n";
                result += getFileContentsMd(matchesFiltered);
                result += "```\n\n";
            }
        }

        if (!vigiangMatches.getBackendMatches().isEmpty()) {
            result += "# backend:\n";
            result += "```\n";
            result += getFileContentsMd(vigiangMatches.getBackendMatches());
            result += "```\n";
        }

        result = result.trim() + "\n";

        Path outputPath = Paths.get(versionPath + "\\" + output + ".md");
        writeString(outputPath, result);
    }

    private static String getFileContentsTxt(List<FileMatch> matches) {
        List<String> sortedMatches = matches.stream()
                .map(FileMatch::getMatch)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        String result = "";
        for (String match : sortedMatches) {
            result += "  " + match + "\n";
        }
        return result;
    }

    private static String getFileContentsMd(List<FileMatch> matches) {
        Map<String, List<FileMatch>> grouped = matches.stream()
                .collect(Collectors.groupingBy(fm -> fm.getRelativeDir() == null ? "" : fm.getRelativeDir()));

        List<String> dirs = new ArrayList<>(grouped.keySet());
        dirs.sort(String::compareTo);

        String result = "";
        for (String dir : dirs) {
            result += dir + ":\n";

            List<FileMatch> sortedUnique = grouped.get(dir).stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(FileMatch::getMatch, fm -> fm, (a, b) -> a, LinkedHashMap::new),
                            m -> m.values().stream()
                                    .sorted(Comparator.comparing(FileMatch::getMatch))
                                    .collect(Collectors.toList())
                    ));

            for (FileMatch fm : sortedUnique) {
                result += "  " + fm.getMatch() + "\n";
            }
            result += "\n";
        }
        return result;
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

        Path versionPath = Paths.get(EnvironmentService.getVigiaNgPath() + "\\versions\\" + version);
        if (!Files.exists(versionPath) || !Files.isDirectory(versionPath)) {
            throw new IllegalArgumentException("o diretorio versionPath nao existe ou nao eh um diretorio");
        }
    }

}

@AllArgsConstructor
@Getter
class VigiangFileContents {
    private final List<FileContent> backendFileContents;
    private final List<FileContent> frontendFileContents;
}

@AllArgsConstructor
@Getter
class VigiangMatches {
    private final List<FileMatch> backendMatches;
    private final List<FileMatch> frontendMatches;
    private final VigiangMatchType type;
}

enum VigiangMatchType {
    CONFIGURATION, FEATURE, PRIVILEGE, ENVIRONMENT
}
