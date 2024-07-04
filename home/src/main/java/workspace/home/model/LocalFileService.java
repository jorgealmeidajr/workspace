package workspace.home.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalFileService {

    public static String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public static void sortByLastModifiedTime(List<LocalFile> filesToRename) {
        Collections.sort(filesToRename, (image1, image2) -> {
            try {
                BasicFileAttributes attr1 = Files.readAttributes(image1.getPath(), BasicFileAttributes.class);
                BasicFileAttributes attr2 = Files.readAttributes(image2.getPath(), BasicFileAttributes.class);
                return attr2.lastModifiedTime().compareTo(attr1.lastModifiedTime());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static List<LocalFile> listFilesFilterByExtensions(String dir, List<String> extensions) throws IOException {
        return listFilesUsingFileWalk(dir, 1).stream()
                .filter((path) -> {
                    var extension = getExtension(path.toString());
                    return extensions.contains(extension.toLowerCase());
                })
                .map(LocalFile::new)
                .collect(Collectors.toList());
    }

    public static List<Path> listFilesUsingFileWalk(String dir, int depth) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(dir), depth)) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .collect(Collectors.toList());
        }
    }

}
