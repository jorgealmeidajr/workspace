package workspace.home.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RenameImages {

    private final String folder;
    private final Path folderPath;
    private final boolean forceRename;

    public RenameImages(String folder, boolean forceRename) throws IOException {
        var folderPath = Paths.get(folder);
        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            throw new IOException("The folder does not exist or it is not a directory");
        }

        this.folder = folder;
        this.folderPath = folderPath;
        this.forceRename = forceRename;
    }

    public void execute() throws IOException {
        var imageExtensions = List.of("jpg", "jpeg", "png", "webp", "jfif");
        var filesToRename = LocalFileService.listFilesFilterByExtensions(folder, imageExtensions);
        LocalFileService.sortByLastModifiedTime(filesToRename);
        Collections.reverse(filesToRename);
        renameFilesByCounter(filesToRename);
    }

    private void renameFilesByCounter(List<LocalFile> filesToRename) throws IOException {
        var initial = "I";
        var count = 5;

        for (var imageFile : filesToRename) {
            var fileName = imageFile.getPath().getFileName().toString();
            var extension = imageFile.getExtension();
            String newName = String.format("%s%03d.%s", initial, count, extension);
            System.out.println("original file name [" + fileName + "] creation time [" + imageFile.formatDateTime(imageFile.getLastModifiedTime()) + "]");

            var newNamePath = imageFile.getPath().resolveSibling(newName);
            Files.move(imageFile.getPath(), newNamePath);
            System.out.println("file renamed to [" + newName + "]");

            count = count + 5;

            await();
        }
    }

    private void await() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

}
