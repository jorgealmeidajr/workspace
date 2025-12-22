package workspace.commons.service;

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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MappersService {

    static String extractFunctionCall(String content) {
        String result;
        content = content.trim().toLowerCase();

        Pattern r = Pattern.compile("call\\s+(\\w+.\\w+)");
        Matcher m = r.matcher(content);
        if (m.find()) {
            result = m.group(1);
        } else {
            return "";
        }
        return result.toUpperCase() + "()";
    }

    static List<String> extractFunctionParams(String content) {
        var result = new ArrayList<String>();
        Pattern r = Pattern.compile("#\\{\\s*(\\w+)[^}]*mode=IN[^}]*\\}");
        Matcher m = r.matcher(content);
        while (m.find()) {
            result.add(m.group(1));
        }
        return result;
    }

    private static String getNamespace(Document document) {
        String namespace = "";
        Node node = document.getElementsByTagName("mapper").item(0);
        NamedNodeMap attributes = node.getAttributes();
        for (int a = 0; a < attributes.getLength(); a++) {
            Node attribute = attributes.item(a);
            if ("namespace".equals(attribute.getNodeName())) {
                namespace = attribute.getNodeValue();
                break;
            }
        }
        return namespace;
    }

    private static List<XmlResultMap> getResultMaps(Document document, String database, String namespace) {
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

            String id = getId(node);
            var temp = new XmlCallMapping(namespace, id, database, functionCall, functionParams);
            resultList.add(temp);
        }
        return resultList;
    }

    private static String getId(Node node) {
        NamedNodeMap attributes = node.getAttributes();
        String id = "";
        for (int a = 0; a < attributes.getLength(); a++) {
            Node attribute = attributes.item(a);
            if ("id".equals(attribute.getNodeName())) {
                id = attribute.getNodeValue();
                break;
            }
        }
        return id;
    }

}
