package workspace.home;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RenameImages {

    public static void main(String[] args) {
        var folder = "FOLDER_PATH...";
        var forceRename = false;

        try {
            var folderPath = Paths.get(folder);
            if (!Files.exists(folderPath) || !Files.isDirectory(folderPath))
                throw new IOException("The folder does not exist or it is not a directory");

            var filesToRename = listFilesToRename(folder);
            sortByLastModifiedTime(filesToRename);
            Collections.reverse(filesToRename);

            // no log esta creation time, tem de trocar para last modified time...

            // experimental: execute this routine in a folder, in a different day
//            renameFilesDateStart(filesToRename, false);

            // * rename all files, overwrite based on the date execution and a new count 0 begin
//            renameFilesDateStart(filesToRename);

            // * rename all files by counter
            renameFilesByCounter(filesToRename, forceRename);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sortByLastModifiedTime(List<FileToRename> filesToRename) {
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

    private static void sortByCreationTime(List<FileToRename> filesToRename) {
        Collections.sort(filesToRename,
            (image1, image2) -> image2.getCreationTime().compareTo(image1.getCreationTime()));
    }

    private static void renameFilesDateStart(List<FileToRename> filesToRename) throws IOException {
        var maxCount = 995;
        var count = 0;
        var nowDate = LocalDateTime.now();

        for (var imageFile : filesToRename) {
            var fileName = imageFile.getPath().getFileName().toString();
            var extension = imageFile.getExtension();
            String newName = getNewName(count, extension, nowDate);

            System.out.println("current file [" + fileName + "] creation time [" + formatDateTime(imageFile.getLastModifiedTime()) + "]");
            var newNamePath = imageFile.getPath().resolveSibling(newName);
            Files.move(imageFile.getPath(), newNamePath);
            System.out.println("renamed to [" + newName + "]");

            count = count + 5;
            if (count >= maxCount) throw new IOException("The count value cant be larger than " + maxCount);
        }
    }

    private static void renameFilesByCounter(List<FileToRename> filesToRename, boolean forceRename) throws IOException {
        var initial = "I";
        var count = 5;

        for (var imageFile : filesToRename) {
            var fileName = imageFile.getPath().getFileName().toString();
            var extension = imageFile.getExtension();
            String newName = String.format("%s%03d.%s", initial, count, extension);
            System.out.println("original file name [" + fileName + "] creation time [" + formatDateTime(imageFile.getLastModifiedTime()) + "]");

            var newNamePath = imageFile.getPath().resolveSibling(newName);
            Files.move(imageFile.getPath(), newNamePath);
            System.out.println("file renamed to [" + newName + "]");

            count = count + 5;

            await();
        }
    }

    private static void await() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private static void renameFilesDateStart(List<FileToRename> filesToRename, boolean forceRename) throws IOException {
        var count = 0;
        for (var imageFile : filesToRename) {
            var fileName = imageFile.getPath().getFileName().toString();
            var fileNameNoExtension = removeFileExtension(fileName, true);

            // ignore previous renamed files
            if (!forceRename && fileNameNoExtension.matches("\\d{6}_\\d{4}")) {
                continue;
            }

            var extension = imageFile.getExtension();
            var nowDate = LocalDateTime.now();
            String newName = getNewName(count, extension, nowDate);

            System.out.println("current file [" + fileName + "] creation time [" + formatDateTime(imageFile.getCreationTime()) + "]");
//            Files.move(imageFile.getPath(), imageFile.getPath().resolveSibling(newName));
            System.out.println("renamed to [" + newName + "]");

            count = count + 5;
        }
    }

    static String getNewName(int count, String extension, LocalDateTime nowDate) {
        var lastTwoDigitsYear = String.valueOf(nowDate.getYear()).substring(2);
        var newName = String.format("%s%02d%02d_%03d.%s",
                lastTwoDigitsYear, nowDate.getMonthValue(), nowDate.getDayOfMonth(), count, extension);
        return newName;
    }

    public static List<Path> listFilesUsingFileWalk(String dir, int depth) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(dir), depth)) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .collect(Collectors.toList());
        }
    }

    public static List<FileToRename> listFilesToRename(String dir) throws IOException {
        var imageExtensions = List.of("jpg", "jpeg", "png", "webp");
        return listFilesUsingFileWalk(dir, 1).stream()
                .filter((path) -> {
                    var extension = getExtension(path.toString());
                    return imageExtensions.contains(extension.toLowerCase());
                })
                .map(FileToRename::new)
                .collect(Collectors.toList());
    }

    public static String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public static String removeFileExtension(String filename, boolean removeAllExtensions) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }

        String extPattern = "(?<!^)[.]" + (removeAllExtensions ? ".*" : "[^.]*$");
        return filename.replaceAll(extPattern, "");
    }

    public static String formatDateTime(FileTime fileTime) {
        LocalDateTime localDateTime = fileTime
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return localDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

}
