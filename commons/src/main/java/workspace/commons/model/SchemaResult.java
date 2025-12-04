package workspace.commons.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SchemaResult {

    private DatabaseCredentials databaseCredentials;

    private List<String> tablesNames;
    private List<String> viewsNames;
    private List<String> functionsNames;
    private List<String> indexesNames;
    private List<String> proceduresNames;
    private List<String> packageBodiesNames;

    private List<DbObjectDefinition> tables;
    private List<DbObjectDefinition> views;
    private List<DbObjectDefinition> functions;
    private List<DbObjectDefinition> indexes;
    private List<DbObjectDefinition> procedures;
    private List<DbObjectDefinition> packageBodies;

}
