package workspace.vigiang;

import workspace.vigiang.checkers.CheckContainers;
import workspace.vigiang.checkers.CheckLaboratories;
import workspace.vigiang.checkers.CheckWork;
import workspace.vigiang.model.LaboratoryVigiaNg;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class UpdateLaboratories {

    public static void main(String[] args) {
        CheckContainers.main(new String[] { });
        CheckLaboratories.main(new String[] { });
        CheckWork.main(new String[] { });

        removeUnusedLaboratories();
    }

    private static void removeUnusedLaboratories() {
        System.out.println("## START removing unused laboratories\n");
        try {
            // remove laboratories that are not active from the laboratories.json file
            for (LaboratoryVigiaNg laboratoryVigiaNg : EnvironmentService.getLaboratoriesVigiaNgInactive()) {
                System.out.println("🗑️ Removing inactive laboratory: " + laboratoryVigiaNg.getName());
                Path laboratoryPath = EnvironmentService.getLaboratoryPath(laboratoryVigiaNg);
                deleteRecursively(laboratoryPath);
            }

            // todo: remove laboratories by version...

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n## END removing unused laboratories.");
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (var walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

}
