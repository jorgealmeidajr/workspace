package workspace.commons.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class XmlCallMapping {

    private final String project;
    private final String namespace;
    private final String id;
    private final String database;
    private final String functionCall;
    private final List<String> functionParams;

    @Override
    public String toString() {
        return "XmlCallMapping{" +
                "namespace='" + namespace + '\'' +
                ", id='" + id + '\'' +
                ", database='" + database + '\'' +
                ", functionCall='" + functionCall + '\'' +
                '}';
    }

}
