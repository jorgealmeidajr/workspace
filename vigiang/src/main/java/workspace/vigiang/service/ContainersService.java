package workspace.vigiang.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class ContainersService {

    public static String getContainersContent(List<String[]> data) {
        List<String> frontendTags = List.of("webviewer", "workflow");

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
                "warrant-service"
        );

        var tagGroups = new HashMap<String, List<String>>();
        tagGroups.put("frontend", new ArrayList<>());
        tagGroups.put("cloud-control", new ArrayList<>());
        tagGroups.put("cloud-vigiang", new ArrayList<>());
        tagGroups.put("others", new ArrayList<>());

        for (String[] row : data) {
            String projectInitial = row[0];
            String project = row[0] + ":" + row[1];

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

        String content = "";
        content += getTagGroupContent(tagGroups, "frontend");
        content += getTagGroupContent(tagGroups, "cloud-control");
        content += getTagGroupContent(tagGroups, "cloud-vigiang");
        content += getTagGroupContent(tagGroups, "others");
        content = content.trim();
        content += "\n";

        return content;
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
