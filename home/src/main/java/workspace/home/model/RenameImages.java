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
    private final int maxCount;
    private final int increment;

    public RenameImages(String folder, boolean forceRename, int maxCount, int increment) throws IOException {
        var folderPath = Paths.get(folder);
        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            throw new IOException("The folder does not exist or it is not a directory");
        }

        this.folder = folder;
        this.folderPath = folderPath;
        this.forceRename = forceRename;
        this.maxCount = maxCount;
        this.increment = increment;
    }

    public void execute() throws IOException {
        var imageExtensions = List.of("jpg", "jpeg", "png", "webp", "jfif");

        var localFilesToRename = LocalFileService.listFilesFilterByExtensions(this.folder, imageExtensions);
        LocalFileService.sortByLastModifiedTime(localFilesToRename);
        Collections.reverse(localFilesToRename);

        var initial = "I";
        renameFilesByCounter(localFilesToRename, initial);
    }

    private void renameFilesByCounter(List<LocalFile> localFilesToRename, String initial) throws IOException {
        var count = 1;

        if (!this.forceRename) {
            count = LocalFileService.getAvailableCount(count, this.increment, initial, localFilesToRename);
        }

        for (var localFile : localFilesToRename) {
            var fileNameNoExtension = LocalFileService.removeFileExtension(localFile.getFileName(), true);

            // ignore previous renamed files
            if (!this.forceRename && fileNameNoExtension.matches(initial + "\\d{3}")) {
                continue;
            }

            String newName = LocalFileService.generateNewName(localFile, initial, count);
            System.out.println("original file name [" + localFile.getFileName() + "] creation time [" + localFile.formatDateTime(localFile.getLastModifiedTime()) + "]");

            var newNamePath = localFile.getPath().resolveSibling(newName);
            Files.move(localFile.getPath(), newNamePath);
            System.out.println("file renamed to [" + newName + "]");

            count = count + this.increment;
            if (count >= this.maxCount) throw new IOException("The count value cant be larger than " + this.maxCount);
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
