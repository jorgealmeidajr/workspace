package workspace.vigiang.service;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import workspace.commons.model.XmlCallMapping;
import workspace.commons.model.XmlMyBatisMapping;
import workspace.commons.model.XmlResultMap;

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

    public static void writeMappers(Path versionPath, List<XmlMyBatisMapping> mappings) throws IOException {
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
                if ("()".equals(xmlCallMapping.getFunctionCall()) || "".equals(xmlCallMapping.getId().trim())) {
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

    private static void writeContentToFile(String newFileContent, Path filePath, String fileName) throws IOException {
        newFileContent = newFileContent.trim() + "\n";
        Path mappersMdPath = Paths.get(filePath + fileName);

        var initialFileContent = "";
        if (Files.exists(mappersMdPath)) {
            initialFileContent = new String(Files.readAllBytes(mappersMdPath));
        }

        if (!initialFileContent.equals(newFileContent)) {
            System.out.println("updating file: " + mappersMdPath);
            Files.writeString(mappersMdPath, newFileContent);
        }
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
                if ("()".equals(xmlCallMapping.getFunctionCall()) || "".equals(xmlCallMapping.getId().trim())) {
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
        System.out.println();

        writeContentToFile(resultMd, versionPath, "\\mappers.md");
    }

    public static List<XmlResultMap> getResultMaps(Document document, String database, String namespace) {
        var result = new ArrayList<XmlResultMap>();

        NodeList nodeList1 = document.getElementsByTagName("resultMap");
        for (int i = 0; i < nodeList1.getLength(); i++) {
            Node node1 = nodeList1.item(i);

            NamedNodeMap attributes = node1.getAttributes();
            String id = "";
            for (int a = 0; a < attributes.getLength(); a++) {
                Node attribute = attributes.item(a);
                if ("id".equals(attribute.getNodeName())) id = attribute.getNodeValue();
            }

            var xmlResults = new ArrayList<XmlResultMap.XmlResult>();
            var xmlResultMap = new XmlResultMap(namespace, id, database, xmlResults);
            result.add(xmlResultMap);

            getXmlResults(node1, xmlResults);
        }
        return result;
    }

    private static void getXmlResults(Node node1, List<XmlResultMap.XmlResult> xmlResults) {
        NodeList nodeList2 = node1.getChildNodes();
        for (int j = 0; j < nodeList2.getLength(); j++) {
            Node node2 = nodeList2.item(j);
            if (node2.getNodeType() == Node.ELEMENT_NODE && "result".equals(node2.getNodeName())) {
                NamedNodeMap attributes2 = node2.getAttributes();
                String property = "";
                String column = "";
                for (int a = 0; a < attributes2.getLength(); a++) {
                    Node attribute = attributes2.item(a);
                    if ("property".equals(attribute.getNodeName())) property = attribute.getNodeValue();
                    if ("column".equals(attribute.getNodeName())) column = attribute.getNodeValue();
                }
                xmlResults.add(new XmlResultMap.XmlResult(property, column));
            }
        }
    }

    public static XmlMyBatisMapping getXmlMappings(String content, String database) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(content)));

        final String namespace = getNamespace(document);

        List<XmlCallMapping> selects = getMappings(document, database, namespace, "select");
        List<XmlCallMapping> inserts = getMappings(document, database, namespace, "insert");
        List<XmlCallMapping> updates = getMappings(document, database, namespace, "update");
        var resultMaps = getResultMaps(document, database, namespace);

        return new XmlMyBatisMapping(namespace, database, selects, inserts, updates, resultMaps);
    }

    private static List<XmlCallMapping> getMappings(Document document, String database, String namespace, String tagName) {
        NodeList nodeList = document.getElementsByTagName(tagName);

        var resultList = new ArrayList<XmlCallMapping>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String content = node.getTextContent();

            String functionCall = extractFunctionCall(content);
            List<String> functionParams = extractFunctionParams(content);

            NamedNodeMap attributes = node.getAttributes();
            String id = "";
            for (int a = 0; a < attributes.getLength(); a++) {
                Node attribute = attributes.item(a);
                if ("id".equals(attribute.getNodeName())) id = attribute.getNodeValue();

                var temp = new XmlCallMapping(namespace, id, database, functionCall, functionParams);
                resultList.add(temp);
            }
        }
        return resultList;
    }

}
