package workspace.commons.model;

import java.util.*;
import java.util.stream.Collectors;

public class MyBatisMappings {

    private final Map<String, Project> projects;
    private final List<String> databases;
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

        this.databases = mappings.stream()
            .map(XmlMyBatisMapping::database)
            .distinct()
            .sorted()
            .toList();

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
            if ("".equals(xmlCallMapping.getFunctionCall()) || "".equals(xmlCallMapping.getId().trim())) {
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

    public String getMappersTxt() {
        if (mappings.isEmpty()) return "";
        String resultTxt = "";

        for (String projectKey : getProjectsKeys()) {
            Project project = projects.get(projectKey);

            Map<String, List<XmlCallMapping>> callsByNamespace = project.getAllCalls().stream()
                .collect(Collectors.groupingBy(XmlCallMapping::getNamespace));
            List<String> byNamespaceKeys = new ArrayList<>(callsByNamespace.keySet());
            Collections.sort(byNamespaceKeys);

            for (String namespace : byNamespaceKeys) {
                List<XmlCallMapping> result = callsByNamespace.get(namespace);
                result.sort(Comparator.comparing(XmlCallMapping::getId)
                    .thenComparing(XmlCallMapping::getDatabase));

                Map<String, List<XmlCallMapping>> byId = result.stream()
                    .collect(Collectors.groupingBy(XmlCallMapping::getId));

                resultTxt += namespace + ":\n";

                String currentId = null;
                for (XmlCallMapping xmlCallMapping : result) {
                    if (currentId == null || !currentId.equals(xmlCallMapping.getId())) {
                        currentId = xmlCallMapping.getId();
                        resultTxt += "  " + currentId + "():\n";

                        var byIdList = byId.get(currentId);

                        for (String database : databases) {
                            var call = byIdList.stream().filter(r -> database.equals(r.getDatabase())).findFirst().orElse(null);
                            if (call != null) {
                                resultTxt += "    " + database + ": " + call.getFunctionCall() + "\n";
                            } else {
                                resultTxt += "    " + database + ": _UNDEFINED_\n";
                            }
                        }
                    }
                }
                resultTxt += "\n";
            }
        }

        return resultTxt.trim() + "\n";
    }

    public String getMappersMd() {
        if (mappings.isEmpty()) return "";
        String resultMd = "";

        for (String projectKey : getProjectsKeys()) {
            Project project = projects.get(projectKey);

            Map<String, List<XmlCallMapping>> byNamespace = project.getAllCalls().stream()
                .collect(Collectors.groupingBy(XmlCallMapping::getNamespace));
            List<String> byNamespaceKeys = new ArrayList<>(byNamespace.keySet());
            Collections.sort(byNamespaceKeys);

            var resultsByNamespace = project.getAllResultMaps().stream()
                .collect(Collectors.groupingBy(XmlResultMap::getNamespace));

            for (String key : byNamespaceKeys) {
                List<XmlCallMapping> callsByNamespace = byNamespace.get(key);
                callsByNamespace.sort(Comparator.comparing(XmlCallMapping::getId)
                    .thenComparing(XmlCallMapping::getDatabase));

                Map<String, List<XmlCallMapping>> callsById = callsByNamespace.stream()
                    .collect(Collectors.groupingBy(XmlCallMapping::getId));

                resultMd += "# " + key + ":\n";
                resultMd += "```\n";
                String currentId = null;
                for (XmlCallMapping xmlCallMapping : callsByNamespace) {
                    if (currentId == null || !currentId.equals(xmlCallMapping.getId())) {
                        currentId = xmlCallMapping.getId();
                        resultMd += currentId + "():\n";

                        var byIdList = callsById.get(currentId);

                        for (String database : databases) {
                            resultMd += getCallMd(xmlCallMapping, database, byIdList);
                        }
                    }
                }

                resultMd += getResultMaps(key, resultsByNamespace);
                resultMd += "```\n\n";
            }
        }

        return resultMd.trim() + "\n";
    }

    private static String getCallMd(XmlCallMapping xmlCallMapping, String database, List<XmlCallMapping> byIdList) {
        var call = byIdList.stream().filter(r -> database.equals(r.getDatabase())).findFirst().orElse(null);
        String result = "";

        if (call != null) {
            result += "  " + database + ": " + call.getFunctionCall() + "\n";
            if (!xmlCallMapping.getFunctionParams().isEmpty()) {
                result += "    params:\n";
                for (String param : xmlCallMapping.getFunctionParams()) {
                    result += "      - " + param + "\n";
                }
                result += "\n";
            }
        } else {
            result += "  " + database + ": _UNDEFINED_\n";
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
                result += "  " + xmlResultMap.getDatabase() + ": " + xmlResultMap.getId() + "\n";
                for (XmlResultMap.XmlResult xmlResult : xmlResultMap.getResults()) {
                    result += "    - property: " + xmlResult.getProperty() + ", column: " + xmlResult.getColumn() + "\n";
                }
                result += "\n";
            }
        }
        return result;
    }

}
