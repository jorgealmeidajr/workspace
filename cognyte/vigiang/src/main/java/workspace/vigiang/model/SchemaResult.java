package workspace.vigiang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import workspace.commons.model.DbObjectDefinition;

import java.util.List;

@AllArgsConstructor
@Getter
public class SchemaResult {
    private final DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG;
    private final List<DbObjectDefinition> tables;
    private final List<DbObjectDefinition> views;
    private final List<DbObjectDefinition> functions;
    private final List<DbObjectDefinition> indexes;
    private final List<DbObjectDefinition> procedures;
    private final List<DbObjectDefinition> packageBodies;
}
