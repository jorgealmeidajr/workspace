package workspace.home;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class FileToRename {

    private final Path path;

    FileToRename(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public FileTime getCreationTime() {
        FileTime creationTime;
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            creationTime = attr.creationTime();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return creationTime;
    }

    public FileTime getLastModifiedTime() {
        FileTime lastModifiedTime;
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            lastModifiedTime = attr.lastModifiedTime();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lastModifiedTime;
    }

    public String getExtension() {
        String filename = this.path.toString();
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

}
