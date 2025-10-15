package workspace.vigiang;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import workspace.vigiang.service.EnvironmentService;
import workspace.vigiang.service.MappersService;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static workspace.vigiang.service.EnvironmentService.getVigiaNgPath;
import static workspace.vigiang.service.MappersService.extractFunctionCall;

public class UpdateProjectsByVersion {

    static final List<VigiangMatches> MATCHES = new ArrayList<>();

    public static void main(String[] args) {
        var WORK_DIR = "C:\\work\\vigiang";

        for (String version : EnvironmentService.getVersions()) {
            validateProjectDirectories(WORK_DIR, version);
            Path backendPath = Paths.get(WORK_DIR + "\\" + version + "\\back-" + version);
            Path frontendPath = Paths.get(WORK_DIR + "\\" + version + "\\front-" + version);
            Path versionPath = Paths.get(getVigiaNgPath() + "\\versions\\" + version);

            var backendFileContents = getFileContentsByExtensions(backendPath, List.of("java", "yaml"), List.of("commons", "target"));
            var frontendFileContents = getFileContentsByExtensions(frontendPath, List.of("js"), List.of("node_modules", "json-server", "tests"));
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
            Pattern.compile("hasRole\\((?:'|\")ROLE_([A-Z0-9_]+)(?:'|\")\\)")
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
            getMatches(vigiangFileContents.getFrontendFileContents(), frontendPatterns, List.of("NODE_ENV")),
            VigiangMatchType.ENVIRONMENT);
        MATCHES.add(vigiangMatches);

        updateTxt(versionPath, vigiangMatches, "environment");
        updateMd(versionPath, vigiangMatches, "environment");
    }

    private static void updateTxt(Path versionPath, VigiangMatches vigiangMatches, String output) throws IOException {
        String resultTxt = "";

        if (!vigiangMatches.getFrontendMatches().isEmpty()) {
            List<FileMatch> matchesFiltered = vigiangMatches.getFrontendMatches().stream()
                    .filter(m -> m.getRelativeDir() != null && m.getRelativeDir().contains("webviewer"))
                    .collect(Collectors.toList());
            if (!matchesFiltered.isEmpty()) {
                resultTxt += "webviewer:\n";
                resultTxt = getFileContentsTxt(matchesFiltered, resultTxt);
                resultTxt += "\n";
            }

            matchesFiltered = vigiangMatches.getFrontendMatches().stream()
                    .filter(m -> m.getRelativeDir() != null && m.getRelativeDir().contains("workflow"))
                    .collect(Collectors.toList());
            if (!matchesFiltered.isEmpty()) {
                resultTxt += "workflow:\n";
                resultTxt = getFileContentsTxt(matchesFiltered, resultTxt);
                resultTxt += "\n";
            }
        }

        if (!vigiangMatches.getBackendMatches().isEmpty()) {
            resultTxt += "backend:\n";
            resultTxt = getFileContentsTxt(vigiangMatches.getBackendMatches(), resultTxt);
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

    private static void updateMappers(Path versionPath, List<FileContent> backendFileContents) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        var resultList = new ArrayList<MappingResult>();
        for (FileContent backendFileContent : backendFileContents) {
            Document document = builder.parse(new InputSource(new StringReader(backendFileContent.getContent())));

            String database = null;
            if (backendFileContent.getRelativeDir().endsWith("\\oracle")) {
                database = "oracle";
            } else if (backendFileContent.getRelativeDir().endsWith("\\postgres")) {
                database = "postgres";
            }

            resultList.addAll(getXmlMappings(document, database));
        }

        Set<MappingResult> uniqueSet = new HashSet<>(resultList);
        List<MappingResult> listWithoutDuplicates = new ArrayList<>(uniqueSet);

        Map<String, List<MappingResult>> byNamespace = listWithoutDuplicates.stream()
                .collect(Collectors.groupingBy(MappingResult::getNamespace));
        List<String> byNamespaceKeys = new ArrayList<>(byNamespace.keySet());
        Collections.sort(byNamespaceKeys);

        Map<String, List<MappingResult>> byId = listWithoutDuplicates.stream()
                .collect(Collectors.groupingBy(MappingResult::getId));

        String resultTxt = "";
        for (String key : byNamespaceKeys) {
            List<MappingResult> result = byNamespace.get(key);
            result.sort(Comparator.comparing(MappingResult::getId)
                    .thenComparing(MappingResult::getDatabase));

            resultTxt += "# " + key + ":\n";
            resultTxt += "```\n";
            String currentId = null;
            for (MappingResult mappingResult : result) {
                if ("()".equals(mappingResult.getFunctionCall()) || "".equals(mappingResult.getId().trim())) {
                    System.out.println("case to check: " + key + ", " + mappingResult.getId() + ", " + mappingResult.getDatabase());
                    continue;
                }

                if (currentId == null || !currentId.equals(mappingResult.getId())) {
                    currentId = mappingResult.getId();
                    resultTxt += currentId + "():\n";

                    var byIdList = byId.get(currentId);
                    var oracleCall = byIdList.stream().filter(r -> "oracle".equals(r.getDatabase())).findFirst().orElse(null);
                    var postgresCall = byIdList.stream().filter(r -> "postgres".equals(r.getDatabase())).findFirst().orElse(null);

                    if (oracleCall != null) {
                        resultTxt += "  oracle: " + oracleCall.getFunctionCall() + "\n";
                    } else {
                        resultTxt += "  oracle: _UNDEFINED_\n";
                    }

                    if (postgresCall != null) {
                        resultTxt += "  postgres: " + postgresCall.getFunctionCall() + "\n";
                    } else {
                        resultTxt += "  postgres: _UNDEFINED_\n";
                    }
                }
            }
            resultTxt += "```\n\n";
        }
        System.out.println();

        String newFileContent = resultTxt;
        Path allConfigurationsPath = Paths.get(versionPath + "\\mappers.md");

        var initialFileContent = "";
        if (Files.exists(allConfigurationsPath)) {
            initialFileContent = new String(Files.readAllBytes(allConfigurationsPath));
        }

        if (!initialFileContent.equals(newFileContent)) {
            System.out.println("updating file: " + allConfigurationsPath);
            Files.writeString(allConfigurationsPath, newFileContent);
        }
    }

    private static List<MappingResult> getXmlMappings(Document document, String database) {
        var result = new ArrayList<MappingResult>();

        final String namespace = MappersService.getNamespace(document);

        result.addAll(getMappings(document, database, namespace, "select"));
        result.addAll(getMappings(document, database, namespace, "insert"));
        result.addAll(getMappings(document, database, namespace, "update"));

        // TODO: get params
        // TODO: get id result

        return result;
    }

    private static List<MappingResult> getMappings(Document document, String database, String namespace, String tagName) {
        NodeList nodeList = document.getElementsByTagName(tagName);

        var resultList = new ArrayList<MappingResult>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String content = node.getTextContent();
            String functionCall = extractFunctionCall(content);

            NamedNodeMap attributes = node.getAttributes();
            String id = "";
            for (int a = 0; a < attributes.getLength(); a++) {
                Node attribute = attributes.item(a);
                if ("id".equals(attribute.getNodeName())) id = attribute.getNodeValue();

                var temp = new MappingResult(namespace, id, database, functionCall);
                resultList.add(temp);
            }
        }
        return resultList;
    }

    private static void updateMd(Path versionPath, VigiangMatches vigiangMatches, String output) throws IOException {
        String resultTxt = "";

        if (!vigiangMatches.getFrontendMatches().isEmpty()) {
            List<FileMatch> matchesFiltered = vigiangMatches.getFrontendMatches().stream()
                    .filter(m -> m.getRelativeDir() != null && m.getRelativeDir().contains("webviewer"))
                    .collect(Collectors.toList());
            if (!matchesFiltered.isEmpty()) {
                resultTxt += "# webviewer:\n";
                resultTxt += "```\n";
                resultTxt = getFileContentsMd(matchesFiltered, resultTxt);
                resultTxt += "```\n\n";
            }

            matchesFiltered = vigiangMatches.getFrontendMatches().stream()
                    .filter(m -> m.getRelativeDir() != null && m.getRelativeDir().contains("workflow"))
                    .collect(Collectors.toList());
            if (!matchesFiltered.isEmpty()) {
                resultTxt += "# workflow:\n";
                resultTxt += "```\n";
                resultTxt = getFileContentsMd(matchesFiltered, resultTxt);
                resultTxt += "```\n\n";
            }
        }

        if (!vigiangMatches.getBackendMatches().isEmpty()) {
            resultTxt += "# backend:\n";
            resultTxt += "```\n";
            resultTxt = getFileContentsMd(vigiangMatches.getBackendMatches(), resultTxt);
            resultTxt += "```\n";
        }

        String newFileContent = resultTxt;
        Path allConfigurationsPath = Paths.get(versionPath + "\\" + output + ".md");

        var initialFileContent = "";
        if (Files.exists(allConfigurationsPath)) {
            initialFileContent = new String(Files.readAllBytes(allConfigurationsPath));
        }

        if (!initialFileContent.equals(newFileContent)) {
            System.out.println("updating file: " + allConfigurationsPath);
            Files.writeString(allConfigurationsPath, newFileContent);
        }
    }

    private static String getFileContentsTxt(List<FileMatch> matches, String resultTxt) {
        List<String> sortedMatches = matches.stream()
                .map(FileMatch::getMatch)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        for (String match : sortedMatches) {
            resultTxt += "  " + match + "\n";
        }
        return resultTxt;
    }

    private static String getFileContentsMd(List<FileMatch> matches, String resultTxt) {
        Map<String, List<FileMatch>> grouped = matches.stream()
                .collect(Collectors.groupingBy(fm -> fm.getRelativeDir() == null ? "" : fm.getRelativeDir()));

        List<String> dirs = new ArrayList<>(grouped.keySet());
        dirs.sort(String::compareTo);

        for (String dir : dirs) {
            resultTxt += dir + ":\n";

            List<FileMatch> sortedUnique = grouped.get(dir).stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(FileMatch::getMatch, fm -> fm, (a, b) -> a, LinkedHashMap::new),
                            m -> m.values().stream()
                                    .sorted(Comparator.comparing(FileMatch::getMatch))
                                    .collect(Collectors.toList())
                    ));

            for (FileMatch fm : sortedUnique) {
                resultTxt += "  " + fm.getMatch() + "\n";
            }
            resultTxt += "\n";
        }
        return resultTxt;
    }

    private static List<FileMatch> getMatches(List<FileContent> fileContents, List<Pattern> patterns, List<String> ignore) {
        Set<FileMatch> matchesSet = new LinkedHashSet<>();

        for (FileContent fileContent : fileContents) {
            String input = fileContent.getContent().trim();

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(input);

                while (matcher.find()) {
                    var matchStr = matcher.group(1);
                    if (ignore.contains(matchStr)) continue;

                    var match = new FileMatch(fileContent.getRelativeDir(), matchStr);
                    matchesSet.add(match);
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

    private static List<FileContent> getFileContentsByExtensions(Path dirPath, List<String> extensions, List<String> ignoreDirs) {
        List<FileContent> fileContents = List.of();
        try (var stream = Files.walk(dirPath)) {
            fileContents = stream
                    .filter(p -> Files.isRegularFile(p) &&
                            extensions.stream().anyMatch(ext -> p.toString().endsWith(ext)) &&
                            ignoreDirs.stream().noneMatch(dir -> p.toString().contains("\\" + dir + "\\")))
                    .map(p -> {
                        Path parent = p.getParent();
                        Path relativeDir = (parent == null) ? Paths.get("") : dirPath.relativize(parent);
                        try {
                            return new FileContent(relativeDir.toString(), p.getFileName().toString(), Files.readString(p));
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
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

@AllArgsConstructor
@Getter
class FileContent {
    private final String relativeDir;
    private final String name;
    private final String content;
}

@AllArgsConstructor
@Getter
@EqualsAndHashCode
class FileMatch {
    private final String relativeDir;
    private final String match;
}

@AllArgsConstructor
@Getter
@EqualsAndHashCode
class MappingResult {
    private final String namespace;
    private final String id;
    private final String database;
    private final String functionCall;

    @Override
    public String toString() {
        return "MappingResult{" +
                "namespace='" + namespace + '\'' +
                ", id='" + id + '\'' +
                ", database='" + database + '\'' +
                ", functionCall='" + functionCall + '\'' +
                '}';
    }
}
