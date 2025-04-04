package workspace.vigiang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DbObjectDefinition {
    private final String name;
    private final String definition;
}
