package workspace.commons.model;

import java.util.ArrayList;
import java.util.List;

public record XmlMyBatisMapping(
        String project,
        String namespace,
        String database,
        List<XmlCallMapping> selects,
        List<XmlCallMapping> inserts,
        List<XmlCallMapping> updates,
        List<XmlResultMap> resultMaps
    ) {

    public XmlMyBatisMapping {
        if (project == null || project.isBlank()) {
            throw new IllegalArgumentException("project cannot be null or empty");
        }
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("namespace cannot be null or empty");
        }
        if (database == null || database.isBlank()) {
            throw new IllegalArgumentException("database cannot be null or empty");
        }
    }

    public List<XmlCallMapping> getAllCalls() {
        List<XmlCallMapping> allCalls = new ArrayList<>();
        allCalls.addAll(selects);
        allCalls.addAll(inserts);
        allCalls.addAll(updates);
        return allCalls;
    }

}
