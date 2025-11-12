package workspace.vigiang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FileContent {

    private final String relativeDir;
    private final String name;
    private final String content;

}
