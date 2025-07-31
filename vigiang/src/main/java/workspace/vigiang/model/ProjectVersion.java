package workspace.vigiang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProjectVersion {
    private final String name;
    private final String version;

    @Override
    public String toString() {
        return name + ":" + version;
    }

    public String[] toArray() {
        return new String[] { name , version };
    }
}
