package workspace.vigiang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class SchemaResult {
    private final Environment environment;
    private final List<String> tables;
    private final List<DbObjectDefinition> views;
    private final List<DbObjectDefinition> functions;
    private final List<DbObjectDefinition> indexes;
}
