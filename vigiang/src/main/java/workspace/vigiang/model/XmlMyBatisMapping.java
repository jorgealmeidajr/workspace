package workspace.vigiang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Getter
public class XmlMyBatisMapping {

    private final String namespace;
    private final String database;
    private final List<XmlCallMapping> selects;
    private final List<XmlCallMapping> inserts;
    private final List<XmlCallMapping> updates;
    private final List<XmlResultMap> resultMaps;

    public List<XmlCallMapping> getAllCalls() {
        List<XmlCallMapping> allCalls = new java.util.ArrayList<>();
        allCalls.addAll(selects);
        allCalls.addAll(inserts);
        allCalls.addAll(updates);
        return allCalls;
    }

}
