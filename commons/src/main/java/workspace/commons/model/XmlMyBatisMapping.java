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

    public List<XmlCallMapping> getAllCalls() {
        List<XmlCallMapping> allCalls = new ArrayList<>();
        allCalls.addAll(selects);
        allCalls.addAll(inserts);
        allCalls.addAll(updates);
        return allCalls;
    }

}
