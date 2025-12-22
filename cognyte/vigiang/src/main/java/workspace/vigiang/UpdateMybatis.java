package workspace.vigiang;

import workspace.commons.model.FileContent;
import workspace.commons.model.XmlCallMapping;
import workspace.commons.model.XmlMyBatisMapping;
import workspace.commons.model.XmlResultMap;
import workspace.commons.service.MappersService;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static workspace.commons.service.FileContentService.getFileContentsByExtensions;
import static workspace.commons.service.FileService.writeMd;
import static workspace.commons.service.FileService.writeString;
import static workspace.vigiang.service.EnvironmentService.validateProjectDirectories;


public class UpdateMybatis {

    public static void main(String[] args) {
        var WORK_DIR = "C:\\work\\vigiang";

        for (String version : EnvironmentService.getVersions()) {
            validateProjectDirectories(WORK_DIR, version);
            Path backendPath = Paths.get(WORK_DIR + "\\" + version + "\\back-" + version);
            Path versionPath = Paths.get(EnvironmentService.getVigiaNgPath() + "\\versions\\" + version);

            try {
                updateMybatis(backendPath, versionPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void updateMybatis(Path backendPath, Path versionPath) throws IOException {
        var fileContents = getFileContentsByExtensions(backendPath, List.of("xml"), List.of("commons", "target")).stream()
                .filter(f -> f.getRelativeDir().contains("\\repository\\"))
                .collect(Collectors.toList());

        updateMappers(versionPath, fileContents);
        writeMd(fileContents, Paths.get(versionPath + "\\mybatis.md"));
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

            writeMappers(versionPath, mappings);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeMappers(Path versionPath, List<XmlMyBatisMapping> mappings) throws IOException {
        List<XmlCallMapping> allCalls = mappings.stream()
                .flatMap(mapping -> mapping.getAllCalls().stream())
                .collect(Collectors.toList());

        Set<XmlCallMapping> uniqueSet = new HashSet<>(allCalls);
        List<XmlCallMapping> listWithoutDuplicates = new ArrayList<>(uniqueSet); // TODO: ?

        writeMappersTxt(versionPath, listWithoutDuplicates);

        var allResultMaps = mappings.stream()
                .flatMap(mapping -> mapping.getResultMaps().stream())
                .collect(Collectors.toList());
        writeMappersMd(versionPath, listWithoutDuplicates, allResultMaps);
    }

    private static void writeMappersTxt(Path versionPath, List<XmlCallMapping> listWithoutDuplicates) throws IOException {
        Map<String, List<XmlCallMapping>> byNamespace = listWithoutDuplicates.stream()
                .collect(Collectors.groupingBy(XmlCallMapping::getNamespace));
        List<String> byNamespaceKeys = new ArrayList<>(byNamespace.keySet());
        Collections.sort(byNamespaceKeys);

        String resultTxt = "";
        for (String namespace : byNamespaceKeys) {
            List<XmlCallMapping> result = byNamespace.get(namespace);
            result.sort(Comparator.comparing(XmlCallMapping::getId)
                    .thenComparing(XmlCallMapping::getDatabase));

            Map<String, List<XmlCallMapping>> byId = listWithoutDuplicates.stream()
                    .collect(Collectors.groupingBy(XmlCallMapping::getId));

            resultTxt += namespace + ":\n";

            String currentId = null;
            for (XmlCallMapping xmlCallMapping : result) {
                if ("".equals(xmlCallMapping.getFunctionCall()) || "".equals(xmlCallMapping.getId().trim())) {
                    System.out.println("case to check: " + namespace + ", " + xmlCallMapping.getId() + ", " + xmlCallMapping.getDatabase());
                    continue;
                }

                if (currentId == null || !currentId.equals(xmlCallMapping.getId())) {
                    currentId = xmlCallMapping.getId();
                    resultTxt += "  " + currentId + "():\n";

                    var byIdList = byId.get(currentId);
                    var oracleCall = byIdList.stream().filter(r -> "oracle".equals(r.getDatabase())).findFirst().orElse(null);
                    var postgresCall = byIdList.stream().filter(r -> "postgres".equals(r.getDatabase())).findFirst().orElse(null);

                    if (oracleCall != null) {
                        resultTxt += "    oracle: " + oracleCall.getFunctionCall() + "\n";
                    } else {
                        resultTxt += "    oracle: _UNDEFINED_\n";
                    }

                    if (postgresCall != null) {
                        resultTxt += "    postgres: " + postgresCall.getFunctionCall() + "\n";
                    } else {
                        resultTxt += "    postgres: _UNDEFINED_\n";
                    }
                }
            }
            resultTxt += "\n";
        }

        writeContentToFile(resultTxt, versionPath, "\\mappers.txt");
    }

    private static void writeContentToFile(String result, Path filePath, String fileName) throws IOException {
        result = result.trim() + "\n";
        Path outputPath = Paths.get(filePath + fileName);
        writeString(outputPath, result);
    }

    private static void writeMappersMd(Path versionPath, List<XmlCallMapping> listWithoutDuplicates, List<XmlResultMap> allResultMaps) throws IOException {
        Map<String, List<XmlCallMapping>> byNamespace = listWithoutDuplicates.stream()
                .collect(Collectors.groupingBy(XmlCallMapping::getNamespace));
        List<String> byNamespaceKeys = new ArrayList<>(byNamespace.keySet());
        Collections.sort(byNamespaceKeys);

        Map<String, List<XmlCallMapping>> byId = listWithoutDuplicates.stream()
                .collect(Collectors.groupingBy(XmlCallMapping::getId));

        var resultsByNamespace = allResultMaps.stream()
                .collect(Collectors.groupingBy(XmlResultMap::getNamespace));

        String resultMd = "";
        for (String key : byNamespaceKeys) {
            List<XmlCallMapping> result = byNamespace.get(key);
            result.sort(Comparator.comparing(XmlCallMapping::getId)
                    .thenComparing(XmlCallMapping::getDatabase));

            resultMd += "# " + key + ":\n";
            resultMd += "```\n";
            String currentId = null;
            for (XmlCallMapping xmlCallMapping : result) {
                if ("".equals(xmlCallMapping.getFunctionCall()) || "".equals(xmlCallMapping.getId().trim())) {
                    System.out.println("case to check: " + key + ", " + xmlCallMapping.getId() + ", " + xmlCallMapping.getDatabase());
                    continue;
                }

                if (currentId == null || !currentId.equals(xmlCallMapping.getId())) {
                    currentId = xmlCallMapping.getId();
                    resultMd += currentId + "():\n";

                    var byIdList = byId.get(currentId);
                    var oracleCall = byIdList.stream().filter(r -> "oracle".equals(r.getDatabase())).findFirst().orElse(null);
                    var postgresCall = byIdList.stream().filter(r -> "postgres".equals(r.getDatabase())).findFirst().orElse(null);

                    if (oracleCall != null) {
                        resultMd += "  oracle: " + oracleCall.getFunctionCall() + "\n";
                        if (!xmlCallMapping.getFunctionParams().isEmpty()) {
                            resultMd += "    params:\n";
                            for (String param : xmlCallMapping.getFunctionParams()) {
                                resultMd += "      - " + param + "\n";
                            }
                            resultMd += "\n";
                        }
                    } else {
                        resultMd += "  oracle: _UNDEFINED_\n";
                        resultMd += "\n";
                    }

                    if (postgresCall != null) {
                        resultMd += "  postgres: " + postgresCall.getFunctionCall() + "\n";
                        if (!xmlCallMapping.getFunctionParams().isEmpty()) {
                            resultMd += "    params:\n";
                            for (String param : xmlCallMapping.getFunctionParams()) {
                                resultMd += "      - " + param + "\n";
                            }
                            resultMd += "\n";
                        }
                    } else {
                        resultMd += "  postgres: _UNDEFINED_\n";
                        resultMd += "\n";
                    }
                }
            }

            List<XmlResultMap> resultMapsForNamespace = resultsByNamespace.get(key);

            if (resultMapsForNamespace != null) {
                resultMapsForNamespace.sort(Comparator.comparing(XmlResultMap::getId, Comparator.nullsLast(String::compareTo)));
                resultMd += "result_maps:\n".toUpperCase();

                for (XmlResultMap xmlResultMap : resultMapsForNamespace) {
                    resultMd += "  " + xmlResultMap.getDatabase() + ": " + xmlResultMap.getId() + "\n";
                    for (XmlResultMap.XmlResult xmlResult : xmlResultMap.getResults()) {
                        resultMd += "    - property: " + xmlResult.getProperty() + ", column: " + xmlResult.getColumn() + "\n";
                    }
                    resultMd += "\n";
                }
            }

            resultMd += "```\n\n";
        }

        writeContentToFile(resultMd, versionPath, "\\mappers.md");
    }

}
