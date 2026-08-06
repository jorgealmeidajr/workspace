package workspace.commons.model;

import java.util.*;
import java.util.stream.Collectors;

public class MyBatisMappings {

    private final Map<String, Project> projects;
    private final List<XmlMyBatisMapping> mappings;

    record Project (String name, List<XmlMyBatisMapping> mappings) {
        List<XmlCallMapping> getAllCalls() {
            return mappings.stream()
                .flatMap(mapping -> mapping.getAllCalls().stream())
                .toList();
        }

        List<XmlResultMap> getAllResultMaps() {
            return mappings.stream()
                .flatMap(mapping -> mapping.resultMaps().stream())
                .toList();
        }
    }

    public MyBatisMappings(List<XmlMyBatisMapping> mappings) {
        validateUniqueMappings(mappings);
        this.mappings = mappings;

        this.projects = new HashMap<>();

        Map<String, List<XmlMyBatisMapping>> byProject = mappings.stream()
            .collect(Collectors.groupingBy(XmlMyBatisMapping::project));

        for (String projectName : byProject.keySet()) {
            List<XmlMyBatisMapping> projectMappings = byProject.get(projectName);

            projectMappings = sort(projectMappings);

            List<XmlCallMapping> allCalls = projectMappings.stream()
                .flatMap(mapping -> mapping.getAllCalls().stream())
                .toList();

            validateUniqueCalls(allCalls);
            validateCalls(allCalls);

            Project project = new Project(projectName, projectMappings);
            this.projects.put(projectName, project);
        }
    }

    static List<XmlMyBatisMapping> sort(List<XmlMyBatisMapping> mappings) {
        List<XmlMyBatisMapping> copy = new ArrayList<>(mappings);
        copy.sort((m1, m2) -> {
            int projectCompare = m1.project().compareTo(m2.project());
            if (projectCompare != 0) return projectCompare;
            int namespaceCompare = m1.namespace().compareTo(m2.namespace());
            if (namespaceCompare != 0) return namespaceCompare;
            return m1.database().compareTo(m2.database());
        });
        return copy;
    }

    private void validateUniqueMappings(List<XmlMyBatisMapping> mappings) {
        Map<String, XmlMyBatisMapping> seen = new HashMap<>();

        for (XmlMyBatisMapping mapping : mappings) {
            String key = mapping.project() + "|" + mapping.namespace() + "|" + mapping.database();

            if (seen.containsKey(key)) {
                throw new IllegalArgumentException(
                    "Duplicate mapping found: project=" + mapping.project() +
                    ", namespace=" + mapping.namespace() +
                    ", database=" + mapping.database()
                );
            }
            seen.put(key, mapping);
        }
    }

    private void validateUniqueCalls(List<XmlCallMapping> calls) {
        Map<String, XmlCallMapping> seen = new HashMap<>();

        for (XmlCallMapping call : calls) {
            String key = call.getNamespace() + "|" + call.getId() + "|" + call.getDatabase();

            if (seen.containsKey(key)) {
                throw new IllegalArgumentException(
                    "Duplicate call found: namespace=" + call.getNamespace() +
                    ", id=" + call.getId() +
                    ", database=" + call.getDatabase()
                );
            }
            seen.put(key, call);
        }
    }

    static void validateCalls(List<XmlCallMapping> allCalls) {
        for (XmlCallMapping xmlCallMapping : allCalls) {
            if ("".equals(xmlCallMapping.getFunctionCall()) || xmlCallMapping.getId().trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Function call or ID cannot be empty: namespace=" + xmlCallMapping.getNamespace() +
                    ", id=" + xmlCallMapping.getId() +
                    ", database=" + xmlCallMapping.getDatabase()
                );
            }
        }
    }

    List<String> getProjectsKeys() {
        List<String> projectKeys = new ArrayList<>(projects.keySet());
        Collections.sort(projectKeys);
        return projectKeys;
    }

    public String getMappersMd(String database) {
        if (mappings.isEmpty()) return "";
        String resultMd = "";

        for (String projectKey : getProjectsKeys()) {
            Project project = projects.get(projectKey);

            Map<String, List<XmlCallMapping>> byNamespace = project.getAllCalls().stream()
                .filter(call -> database.equals(call.getDatabase()))
                .collect(Collectors.groupingBy(XmlCallMapping::getNamespace));
            List<String> byNamespaceKeys = new ArrayList<>(byNamespace.keySet());
            Collections.sort(byNamespaceKeys);

            var resultsByNamespace = project.getAllResultMaps().stream()
                .filter(resultMap -> database.equals(resultMap.getDatabase()))
                .collect(Collectors.groupingBy(XmlResultMap::getNamespace));

            for (String key : byNamespaceKeys) {
                List<XmlCallMapping> callsByNamespace = byNamespace.get(key);
                callsByNamespace.sort(Comparator.comparing(XmlCallMapping::getId));

                resultMd += "# " + key + ":\n";
                resultMd += "```\n";
                String currentId = null;
                for (XmlCallMapping xmlCallMapping : callsByNamespace) {
                    if (currentId == null || !currentId.equals(xmlCallMapping.getId())) {
                        currentId = xmlCallMapping.getId();
                        resultMd += currentId + "():\n";
                        resultMd += getCallMd(xmlCallMapping);
                    }
                }

                resultMd += getResultMaps(key, resultsByNamespace);
                resultMd += "```\n\n";
            }
        }

        return resultMd.trim() + "\n";
    }

    static String getCallMd(XmlCallMapping xmlCallMapping) {
        String result = "";
        result += "  " + xmlCallMapping.getFunctionCall() + "\n";
        if (!xmlCallMapping.getFunctionParams().isEmpty()) {
            result += "    params:\n";
            for (String param : xmlCallMapping.getFunctionParams()) {
                result += "      - " + param + "\n";
            }
            result += "\n";
        }
        return result;
    }

    static String getResultMaps(String key, Map<String, List<XmlResultMap>> resultsByNamespace) {
        List<XmlResultMap> resultMapsForNamespace = resultsByNamespace.get(key);
        String result = "";
        if (resultMapsForNamespace != null && !resultMapsForNamespace.isEmpty()) {
            // Create a copy to avoid modifying the original list
            resultMapsForNamespace = new ArrayList<>(resultMapsForNamespace);
            resultMapsForNamespace.sort(
                Comparator.comparing(XmlResultMap::getId, Comparator.nullsLast(String::compareTo)));
            result += "result_maps:\n".toUpperCase();

            for (XmlResultMap xmlResultMap : resultMapsForNamespace) {
                result += "  " + xmlResultMap.getId() + "\n";
                for (XmlResultMap.XmlResult xmlResult : xmlResultMap.getResults()) {
                    result += "    - property: " + xmlResult.getProperty() + ", column: " + xmlResult.getColumn() + "\n";
                }
                result += "\n";
            }
        }
        return result;
    }

}
