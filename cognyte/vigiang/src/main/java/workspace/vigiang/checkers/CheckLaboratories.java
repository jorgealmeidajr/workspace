package workspace.vigiang.checkers;

import workspace.vigiang.model.LaboratoryVigiaNg;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static workspace.commons.service.FileService.replaceRegion;
import static workspace.commons.service.FileService.writeString;

public class CheckLaboratories {

    public static void main(String[] args) {
        System.out.println("## START checking laboratories hosts\n");
        try {
            Path laboratoriesPath = EnvironmentService.getVigiaNgLaboratoriesPath();
            updateMd(laboratoriesPath);
            updateBashrc();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n## END checking laboratories hosts.");
    }

    private static void updateMd(Path laboratoriesPath) throws IOException {
        String result = "# DEPLOY_HOSTS:\n";
        result += "```\n";
        for (LaboratoryVigiaNg laboratoryVigiaNg : EnvironmentService.getLaboratoriesVigiaNg()) {
            result += laboratoryVigiaNg.getSshHost() + " " + laboratoryVigiaNg.getName() + "\n";
        }
        result += "```\n\n";

        result += "# vite deploy string:\n";
        result += "```\n";
        String oneline = "";
        for (LaboratoryVigiaNg laboratoryVigiaNg : EnvironmentService.getLaboratoriesVigiaNg()) {
            oneline += laboratoryVigiaNg.getSshHost() + "-" + laboratoryVigiaNg.getAlias() + " ";
        }
        result += oneline.trim() + "\n";
        result += "```\n";

        Path outputPath = Paths.get(laboratoriesPath + "\\laboratories.md");

        writeString(outputPath, result);
    }

    private static void updateBashrc() throws IOException {
        List<String> aliasList = new ArrayList<>();

        for (LaboratoryVigiaNg laboratoryVigiaNg : EnvironmentService.getLaboratoriesVigiaNg()) {
            String alias = getSshAliasFormat(laboratoryVigiaNg);
            aliasList.add(alias);
        }

        if (!aliasList.isEmpty()) {
            Path bashrcPath = Paths.get(EnvironmentService.getCognytePath() + "\\.bashrc");
            replaceRegion(bashrcPath, "# vigiang_labs begin", "# vigiang_labs end", aliasList);
            String content = Files.readString(bashrcPath, StandardCharsets.UTF_8);

            Path userBashrcPath = Paths.get("C:\\Users\\jjunior\\.bashrc");
            if (Files.exists(userBashrcPath)) {
                Files.writeString(userBashrcPath, content, StandardCharsets.UTF_8);
                System.out.println("updating file: " + userBashrcPath);
            }
        }
    }

    private static String getSshAliasFormat(LaboratoryVigiaNg laboratoryVigiaNg) {
        return String.format("alias ssh%s='ssh %s@%s'", laboratoryVigiaNg.getAlias(), laboratoryVigiaNg.getSshUsername(), laboratoryVigiaNg.getSshHost());
    }

}
