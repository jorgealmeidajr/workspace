package workspace.home.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LocalFile {

    private final Path path;

    public LocalFile(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public FileTime getCreationTime() {
        FileTime creationTime;
        try {
            BasicFileAttributes attr = Files.readAttributes(this.path, BasicFileAttributes.class);
            creationTime = attr.creationTime();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return creationTime;
    }

    public FileTime getLastModifiedTime() {
        FileTime lastModifiedTime;
        try {
            BasicFileAttributes attr = Files.readAttributes(this.path, BasicFileAttributes.class);
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

    public String formatDateTime(FileTime fileTime) {
        LocalDateTime localDateTime = fileTime
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return localDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public String getFileName() {
        return this.path.getFileName().toString();
    }

}
