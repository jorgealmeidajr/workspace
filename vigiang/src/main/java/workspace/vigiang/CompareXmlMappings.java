package workspace.vigiang;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Deprecated
public class CompareXmlMappings {

    public static void main(String[] args) {
        Path vigiangPath = Paths.get("C:\\Users\\jjunior\\Workspace\\vigiang");
        List<String> vigiangProjects = getVigiangProjects();

        try {
            if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
                throw new IllegalArgumentException("o diretorio do vigiang nao existe ou nao eh um diretorio");
            }

            for (String project : vigiangProjects) {
                Path repositoryPath = Paths.get(vigiangPath + "\\" + project + "\\SRC\\src\\main\\resources\\repository");
                if (!Files.exists(repositoryPath) || !Files.isDirectory(repositoryPath)) {
                    throw new IllegalArgumentException("o diretorio repository nao existe no projeto " + project);
                }

                Path oraclePath = Paths.get(repositoryPath + "\\oracle");
                List<Path> oracleXmls = listXmlFilesUsingFileWalk(oraclePath.toString());

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                List<XmlMapping> oracleXmlMappingsList = new ArrayList<>();

                for(Path oracleXmlPath : oracleXmls) {
                    Document document = builder.parse(oracleXmlPath.toFile());
                    oracleXmlMappingsList.addAll(getXmlMappings(document));
                }

                List<XmlMapping> postgresXmlMappingsList = new ArrayList<>();

                Path postgresPath = Paths.get(repositoryPath + "\\postgres");
                if (Files.exists(postgresPath) && Files.isDirectory(postgresPath)) {
                    System.out.println("o projeto \"" + project + "\" possui mapeamentos em postgres");
                    List<Path> postgresXmls = listXmlFilesUsingFileWalk(postgresPath.toString());

                    for(Path postgresXmlPath : postgresXmls) {
                        Document document = builder.parse(postgresXmlPath.toFile());
                        postgresXmlMappingsList.addAll(getXmlMappings(document));
                    }
                }


                if (!oracleXmlMappingsList.isEmpty() && !postgresXmlMappingsList.isEmpty()) {
                    System.out.println("comparando oracle com postgres...");
                    List<String> postgresIds = postgresXmlMappingsList.stream().map(XmlMapping::getId).collect(Collectors.toList());
                    List<XmlMapping> missingMappingsInPostgres = oracleXmlMappingsList.stream()
                            .filter((xmlMapping) -> !postgresIds.contains(xmlMapping.getId()))
                            .sorted(Comparator.comparing(XmlMapping::getId))
                            .collect(Collectors.toList());

                    missingMappingsInPostgres.forEach((xmlMapping) -> System.out.println(xmlMapping.getId()));

                    System.out.println();
                }

                break;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static List<XmlMapping> getXmlMappings(Document document) {
        List<XmlMapping> result = new ArrayList<>();
        NodeList nodeList = document.getElementsByTagName("select");

        final String namespace = getNamespace(document);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String content = node.getTextContent();

            NamedNodeMap attributes = node.getAttributes();
            String id = "";
            String resultMap = "";
            String statementType = "";
            for (int a = 0; a < attributes.getLength(); a++) {
                Node attribute = attributes.item(a);
                if ("id".equals(attribute.getNodeName())) id = attribute.getNodeValue();
                if ("resultMap".equals(attribute.getNodeName())) resultMap = attribute.getNodeValue();
                if ("statementType".equals(attribute.getNodeName())) statementType = attribute.getNodeValue();
            }

            result.add(new XmlMapping(namespace, id.trim(), content, resultMap.trim(), statementType));
        }
        return result;
    }

    private static String getNamespace(Document document) {
        String namespace = "";
        Node node = document.getElementsByTagName("mapper").item(0);
        NamedNodeMap attributes = node.getAttributes();
        for (int a = 0; a < attributes.getLength(); a++) {
            Node attribute = attributes.item(a);
            if ("namespace".equals(attribute.getNodeName())) namespace = attribute.getNodeValue();
        }
        return namespace;
    }

    static String extractFunctionCall(String content) {
        String result = "";
        content = content.trim();

        Pattern r = Pattern.compile("call\\s+(\\w+.\\w+)");
        Matcher m = r.matcher(content);
        if (m.find()) {
            result = m.group(1);
        }
        return result.toUpperCase() + "()";
    }

    static class XmlMapping {
        final String namespace;
        final String id;
        final String content;
        String functionCall;
        final String resultMap;
        final String statementType;

        XmlMapping(String namespace, String id, String content, String resultMap, String statementType) {
            if ("".equals(id)) throw new IllegalArgumentException("id cannot be an empty string");
            this.namespace = namespace;
            this.id = id;
            this.content = content;
            this.resultMap = resultMap;
            this.statementType = statementType;
        }

        public String getId() {
            return id;
        }
    }

    private static List<String> getVigiangProjects() {
        List<String> vigiangProjects = List.of(
            "auth-service",
            "block-service",
            "carrier-service",
            "config-server",
            "dashboard-service",
            "data-retention-service",
            "event-service",
            "interception-service",
            "operation-service",
            "portability-service",
            "process-service",
            "system-service",
            "tracking-service",
            "user-service",
            "voucher-service",
            "warrant-service");
        return vigiangProjects;
    }

    static List<Path> listXmlFilesUsingFileWalk(String dir) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(dir), 1)) {
            return stream
                .filter(file -> !Files.isDirectory(file))
                .filter((path) -> path.getFileName().toString().endsWith(".xml"))
                .collect(Collectors.toList());
        }
    }

}
