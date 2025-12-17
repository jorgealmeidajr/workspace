package workspace.commons.model;

import lombok.Getter;

@Getter
public class FileContent {

    private final String fullName;
    private final String relativeDir;
    private final String name;
    private final String content;

    public FileContent(String fullName, String relativeDir, String name, String content) {
        if (fullName == null || fullName.isEmpty()) {
            throw new IllegalArgumentException("fullName cannot be empty or null");
        }
        if (relativeDir == null || relativeDir.isEmpty()) {
            throw new IllegalArgumentException("relativeDir cannot be empty or null");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be empty or null");
        }
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("content cannot be empty or null");
        }
        this.fullName = fullName;
        this.relativeDir = relativeDir;
        this.name = name;
        this.content = content;
    }

}
