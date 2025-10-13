package workspace.vigiang.service;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

}
