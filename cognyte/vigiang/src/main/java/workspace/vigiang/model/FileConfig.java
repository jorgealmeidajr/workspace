package workspace.vigiang.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FileConfig {

    private final String fileName;
    private final String[] columns;

}
