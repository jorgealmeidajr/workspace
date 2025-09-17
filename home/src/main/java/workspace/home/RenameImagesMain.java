package workspace.home;

import workspace.home.model.RenameImages;

import java.io.IOException;

public class RenameImagesMain {

    public static void main(String[] args) {
        var folder = "FOLDER_PATH...";
        var forceRename = false;

        try {
            RenameImages renameImages = new RenameImages(folder, forceRename, 999, 1);
            renameImages.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
