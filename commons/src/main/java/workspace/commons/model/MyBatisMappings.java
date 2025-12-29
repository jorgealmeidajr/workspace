package workspace.commons.model;

import java.util.*;
import java.util.stream.Collectors;

public class MyBatisMappings {

    private final List<XmlMyBatisMapping> mappings;
    private final Map<String, Project> projects;

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
        this.mappings = sort(mappings);

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
        String resultTxt = "";

        for (String projectKey : getProjectsKeys()) {
            Project project = projects.get(projectKey);

            Map<String, List<XmlCallMapping>> byNamespace = project.getAllCalls().stream()
                .collect(Collectors.groupingBy(XmlCallMapping::getNamespace));
            List<String> byNamespaceKeys = new ArrayList<>(byNamespace.keySet());
            Collections.sort(byNamespaceKeys);

            for (String namespace : byNamespaceKeys) {
                List<XmlCallMapping> result = byNamespace.get(namespace);
                result.sort(Comparator.comparing(XmlCallMapping::getId)
                    .thenComparing(XmlCallMapping::getDatabase));

                Map<String, List<XmlCallMapping>> byId = project.getAllCalls().stream()
                    .collect(Collectors.groupingBy(XmlCallMapping::getId));

                resultTxt += namespace + ":\n";

                String currentId = null;
                for (XmlCallMapping xmlCallMapping : result) {
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
        }

        return resultTxt;
    }

    public String getMappersMd() {
        String resultMd = "";

        for (String projectKey : getProjectsKeys()) {
            Project project = projects.get(projectKey);

            Map<String, List<XmlCallMapping>> byNamespace = project.getAllCalls().stream()
                .collect(Collectors.groupingBy(XmlCallMapping::getNamespace));
            List<String> byNamespaceKeys = new ArrayList<>(byNamespace.keySet());
            Collections.sort(byNamespaceKeys);

            Map<String, List<XmlCallMapping>> byId = project.getAllCalls().stream()
                .collect(Collectors.groupingBy(XmlCallMapping::getId));

            var resultsByNamespace = project.getAllResultMaps().stream()
                .collect(Collectors.groupingBy(XmlResultMap::getNamespace));

            for (String key : byNamespaceKeys) {
                List<XmlCallMapping> result = byNamespace.get(key);
                result.sort(Comparator.comparing(XmlCallMapping::getId)
                    .thenComparing(XmlCallMapping::getDatabase));

                resultMd += "# " + key + ":\n";
                resultMd += "```\n";
                String currentId = null;
                for (XmlCallMapping xmlCallMapping : result) {
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
                    resultMapsForNamespace.sort(
                        Comparator.comparing(XmlResultMap::getId, Comparator.nullsLast(String::compareTo)));
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
        }

        return resultMd;
    }

}
