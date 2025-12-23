package workspace.commons.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MyBatisMappings {

    private final List<XmlMyBatisMapping> mappings;
    private final List<String> projectsKeys;
    private final Map<String, Project> projects;

    record Project (String key, List<XmlCallMapping> mappings, List<String> namespaces) { }

    public MyBatisMappings(List<XmlMyBatisMapping> mappings) {
        this.mappings = mappings;
        this.projectsKeys = new ArrayList<>();
        this.projects = new HashMap<>();
    }

}


