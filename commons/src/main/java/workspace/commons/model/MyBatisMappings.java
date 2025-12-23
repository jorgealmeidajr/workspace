package workspace.commons.model;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class MyBatisMappings {

    private final List<XmlMyBatisMapping> mappings;
    private final Map<String, Project> projects;

    record Project (String name, List<XmlMyBatisMapping> mappings) { }

    public MyBatisMappings(List<XmlMyBatisMapping> mappings) {
        validateUniqueMappings(mappings);
        sort(mappings);
        this.mappings = mappings;

        this.projects = new HashMap<>();

        Map<String, List<XmlMyBatisMapping>> byProject = mappings.stream()
            .collect(Collectors.groupingBy(XmlMyBatisMapping::project));

        for (String projectName : byProject.keySet()) {
            List<XmlMyBatisMapping> projectMappings = byProject.get(projectName);

            sort(projectMappings);

            List<XmlCallMapping> allCalls = projectMappings.stream()
                .flatMap(mapping -> mapping.getAllCalls().stream())
                .toList();

            validateUniqueCalls(allCalls);

            Project project = new Project(projectName, projectMappings);
            this.projects.put(projectName, project);
        }
    }

    private static void sort(List<XmlMyBatisMapping> mappings) {
        mappings.sort((m1, m2) -> {
            int projectCompare = m1.project().compareTo(m2.project());
            if (projectCompare != 0) return projectCompare;
            int namespaceCompare = m1.namespace().compareTo(m2.namespace());
            if (namespaceCompare != 0) return namespaceCompare;
            return m1.database().compareTo(m2.database());
        });
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

}
