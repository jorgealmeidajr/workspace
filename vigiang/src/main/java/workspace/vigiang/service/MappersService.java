package workspace.vigiang.service;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import workspace.vigiang.model.MappingResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MappersService {

    public static String extractFunctionCall(String content) {
        String result = "";
        content = content.trim();

        Pattern r = Pattern.compile("call\\s+(\\w+.\\w+)");
        Matcher m = r.matcher(content);
        if (m.find()) {
            result = m.group(1);
        }
        return result.toUpperCase() + "()";
    }

    public static List<String> extractFunctionParams(String content) {
        var result = new java.util.ArrayList<String>();
        Pattern r = Pattern.compile("#\\{\\s*(\\w+)[^}]*mode=IN[^}]*\\}");
        Matcher m = r.matcher(content);
        while (m.find()) {
            result.add(m.group(1));
        }
        return result;
    }

    public static String getNamespace(Document document) {
        String namespace = "";
        Node node = document.getElementsByTagName("mapper").item(0);
        NamedNodeMap attributes = node.getAttributes();
        for (int a = 0; a < attributes.getLength(); a++) {
            Node attribute = attributes.item(a);
            if ("namespace".equals(attribute.getNodeName())) namespace = attribute.getNodeValue();
        }
        return namespace;
    }

    public static void writeMappersTxt(Path versionPath, List<MappingResult> listWithoutDuplicates) {

    }

    public static void writeMappersMd(Path versionPath, List<MappingResult> listWithoutDuplicates) throws IOException {
        Map<String, List<MappingResult>> byNamespace = listWithoutDuplicates.stream()
                .collect(Collectors.groupingBy(MappingResult::getNamespace));
        List<String> byNamespaceKeys = new ArrayList<>(byNamespace.keySet());
        Collections.sort(byNamespaceKeys);

        Map<String, List<MappingResult>> byId = listWithoutDuplicates.stream()
                .collect(Collectors.groupingBy(MappingResult::getId));

        String resultMd = "";
        for (String key : byNamespaceKeys) {
            List<MappingResult> result = byNamespace.get(key);
            result.sort(Comparator.comparing(MappingResult::getId)
                    .thenComparing(MappingResult::getDatabase));

            resultMd += "# " + key + ":\n";
            resultMd += "```\n";
            String currentId = null;
            for (MappingResult mappingResult : result) {
                if ("()".equals(mappingResult.getFunctionCall()) || "".equals(mappingResult.getId().trim())) {
                    System.out.println("case to check: " + key + ", " + mappingResult.getId() + ", " + mappingResult.getDatabase());
                    continue;
                }

                if (currentId == null || !currentId.equals(mappingResult.getId())) {
                    currentId = mappingResult.getId();
                    resultMd += currentId + "():\n";

                    var byIdList = byId.get(currentId);
                    var oracleCall = byIdList.stream().filter(r -> "oracle".equals(r.getDatabase())).findFirst().orElse(null);
                    var postgresCall = byIdList.stream().filter(r -> "postgres".equals(r.getDatabase())).findFirst().orElse(null);

                    if (oracleCall != null) {
                        resultMd += "  oracle: " + oracleCall.getFunctionCall() + "\n";
                        if (!mappingResult.getFunctionParams().isEmpty()) {
                            resultMd += "    params:\n";
                            for (String param : mappingResult.getFunctionParams()) {
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
                        if (!mappingResult.getFunctionParams().isEmpty()) {
                            resultMd += "    params:\n";
                            for (String param : mappingResult.getFunctionParams()) {
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
            resultMd += "```\n\n";
        }
        System.out.println();

        String newFileContent = resultMd;
        Path mappersMdPath = Paths.get(versionPath + "\\mappers.md");

        var initialFileContent = "";
        if (Files.exists(mappersMdPath)) {
            initialFileContent = new String(Files.readAllBytes(mappersMdPath));
        }

        if (!initialFileContent.equals(newFileContent)) {
            System.out.println("updating file: " + mappersMdPath);
            Files.writeString(mappersMdPath, newFileContent);
        }
    }

}
