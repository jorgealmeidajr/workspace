package workspace.vigiang.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class XmlResultMap {
    private final String namespace;
    private final String id;
    private final String database;
    private final List<XmlResult> results;

    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class XmlResult {
        private final String property;
        private final String column;
    }
}

