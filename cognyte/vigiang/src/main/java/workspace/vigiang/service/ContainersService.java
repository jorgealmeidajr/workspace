package workspace.vigiang.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import workspace.vigiang.model.DockerContainer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ContainersService {

    private static final String BACK = "back";
    private static final String FRONT = "front";

    private static final List<String> CONTROL_PROJECTS = List.of(
            "auth-service",
            "config-server",
            "eureka-server",
            "user-service",
            "zuul-server"
    );

    private static final List<String> MAIN_PROJECTS = List.of(
            "carrier-service",
            "dashboard-service",
            "event-service",
            "interception-service",
            "log-service",
            "message-service",
            "operation-service",
            "process-service",
            "report-service",
            "scheduler-service",
            "system-service",
            "warrant-service"
    );

    private static final List<String> OPTIONAL_PROJECTS = List.of(
            "block-service",
            "data-retention-service",
            "integration-service",
            "portability-service",
            "sittel-service",
            "tracking-service",
            "voucher-service",
            "websocket-service",
            "wms-service"
    );

    private static final List<String> SERVICES_PROJECTS = List.of(
            "claro-block-service",
            "vigiang_sync"
    );

    private static final List<String> FRONT_PROJECTS = List.of(
            "vigia_ng_webviewer",
            "vigia_ng_workflow",
            "webviewer",
            "workflow"
    );

    private static final Map<String, List<String>> BACK_GROUPS = new LinkedHashMap<>() {{
        put("control", CONTROL_PROJECTS);
        put("main", MAIN_PROJECTS);
        put("optional", OPTIONAL_PROJECTS);
        put("services", SERVICES_PROJECTS);
    }};

    private static final List<String> PROJECTS_TO_IGNORE = List.of(
            "kafka", "mock-smtp", "zookeeper", "admin-server", "objective_moser", "cadvisor", "docker_state_exporter",
            "node_exporter", "process_exporter", "quirky_shaw", "brave_wright", "schema-registry", "determined_rhodes",
            "mailhog", "redis", "boring_bartik"
    );

    public static List<String> getFrontendTags() {
        return List.of("webviewer", "workflow");
    }

    /**
     * Builds a JSON representation of the containers, grouped by project category.
     * Containers that do not match any known group are skipped, and empty groups are omitted.
     */
    public static String getContainersJsonContent(List<DockerContainer> data) throws JsonProcessingException {
        Map<String, List<Map<String, String>>> backGroups = new LinkedHashMap<>();
        List<Map<String, String>> frontContainers = new ArrayList<>();

        for (DockerContainer container : data) {
            String containerName = container.getName();

            String frontProject = findProject(containerName, FRONT_PROJECTS);

            String bestGroup = null;
            String bestProject = null;
            for (var group : BACK_GROUPS.entrySet()) {
                String project = findProject(containerName, group.getValue());
                if (project == null) continue;
                if (bestProject == null || project.length() > bestProject.length()) {
                    bestProject = project;
                    bestGroup = group.getKey();
                }
            }

            if (frontProject != null && (bestProject == null || frontProject.length() >= bestProject.length())) {
                frontContainers.add(toEntry(frontProject, container.getVersion()));
                continue;
            }

            if (bestProject == null) continue;

            backGroups.computeIfAbsent(bestGroup, key -> new ArrayList<>())
                    .add(toEntry(bestProject, container.getVersion()));
        }

        Map<String, Object> result = new LinkedHashMap<>();

        if (!backGroups.isEmpty()) {
            Map<String, List<Map<String, String>>> sortedBackGroups = new LinkedHashMap<>();
            for (String groupName : BACK_GROUPS.keySet()) {
                List<Map<String, String>> containers = backGroups.get(groupName);
                if (containers == null || containers.isEmpty()) continue;
                containers.sort(Comparator.comparing(entry -> entry.get("name")));
                sortedBackGroups.put(groupName, containers);
            }
            result.put(BACK, sortedBackGroups);
        }

        if (!frontContainers.isEmpty()) {
            frontContainers.sort(Comparator.comparing(entry -> entry.get("name")));
            result.put(FRONT, frontContainers);
        }

        DefaultIndenter indenter = new DefaultIndenter("  ", "\n");
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter()
                .withObjectIndenter(indenter)
                .withArrayIndenter(indenter);

        return new ObjectMapper()
                .writer(prettyPrinter)
                .writeValueAsString(result);
    }

    /**
     * Returns the longest known project name contained in the container name, or null when there is no match.
     */
    private static String findProject(String containerName, List<String> projects) {
        return projects.stream()
                .filter(containerName::contains)
                .max(Comparator.comparingInt(String::length))
                .orElse(null);
    }

    private static Map<String, String> toEntry(String name, String version) {
        Map<String, String> entry = new LinkedHashMap<>();
        entry.put("name", name);
        entry.put("version", version);
        return entry;
    }

    public static String getContainersContent(List<DockerContainer> data) {
        List<String> frontendTags = getFrontendTags();

        List<String> cloudControlTags = List.of(
                "auth-service",
                "config-server",
                "eureka-server",
                "user-service",
                "zuul-server"
        );

        List<String> cloudVigiangTags = List.of(
                "block-service",
                "carrier-service",
                "dashboard-service",
                "data-retention-service",
                "event-service",
                "interception-service",
                "log-service",
                "message-service",
                "operation-service",
                "portability-service",
                "process-service",
                "report-service",
                "scheduler-service",
                "system-service",
                "tracking-service",
                "voucher-service",
                "warrant-service",
                "websocket-service",
                "wms-service"
        );

        var tagGroups = getTagGroups(data, frontendTags, cloudControlTags, cloudVigiangTags);

        String content = "";
        content += getTagGroupContent(tagGroups, "frontend");
        content += getTagGroupContent(tagGroups, "cloud-control");
        content += getTagGroupContent(tagGroups, "cloud-vigiang");
        content += getTagGroupContent(tagGroups, "others");
        content = content.trim();
        content += "\n";

        return content;
    }

    private static HashMap<String, List<String>> getTagGroups(List<DockerContainer> data, List<String> frontendTags, List<String> cloudControlTags, List<String> cloudVigiangTags) {
        var tagGroups = new HashMap<String, List<String>>();
        tagGroups.put("frontend", new ArrayList<>());
        tagGroups.put("cloud-control", new ArrayList<>());
        tagGroups.put("cloud-vigiang", new ArrayList<>());
        tagGroups.put("others", new ArrayList<>());

        for (DockerContainer container : data) {
            String projectInitial = container.getName();
            if (PROJECTS_TO_IGNORE.stream().anyMatch(projectInitial::contains)) continue;

            String project = container.toString();

            if (frontendTags.stream().anyMatch(projectInitial::contains)) {
                tagGroups.get("frontend").add(project);
            } else if (cloudControlTags.stream().anyMatch(projectInitial::contains)) {
                tagGroups.get("cloud-control").add(project);
            } else if (cloudVigiangTags.stream().anyMatch(projectInitial::contains)) {
                tagGroups.get("cloud-vigiang").add(project);
            } else {
                tagGroups.get("others").add(project);
            }
        }
        return tagGroups;
    }

    private static String getTagGroupContent(HashMap<String, List<String>> tagGroups, String tagGroup) {
        String content = "";
        var tags = tagGroups.get(tagGroup);
        tags.sort(Comparator.naturalOrder());
        if (!tags.isEmpty()) {
            content += tagGroup + ":\n";
            for (var tag : tags) {
                content += "  " + tag + "\n";
            }
            content += "\n";
        }
        return content;
    }

}
