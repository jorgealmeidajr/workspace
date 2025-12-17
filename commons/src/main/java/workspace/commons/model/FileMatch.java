package workspace.commons.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class FileMatch {

    private final String relativeDir;
    private final String match;

    public FileMatch(String relativeDir, String match) {
        if (relativeDir == null || relativeDir.isEmpty()) {
            throw new IllegalArgumentException("relativeDir cannot be empty or null");
        }
        if (match == null || match.isEmpty()) {
            throw new IllegalArgumentException("match cannot be empty or null");
        }
        this.relativeDir = relativeDir;
        this.match = match;
    }

}
