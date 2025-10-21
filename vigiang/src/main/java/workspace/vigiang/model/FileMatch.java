package workspace.vigiang.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class FileMatch {

    private final String relativeDir;
    private final String match;

}
