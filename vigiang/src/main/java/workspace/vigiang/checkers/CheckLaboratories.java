package workspace.vigiang.checkers;

import workspace.vigiang.model.Laboratory;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CheckLaboratories {

    public static void main(String[] args) {
        System.out.println("## START checking laboratories hosts\n");
        try {
            Path laboratoriesPath = EnvironmentService.getVigiaNgLaboratoriesPath();
            updateSh(laboratoriesPath);
            updateMd(laboratoriesPath);
            updateBashrc();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n## END checking laboratories hosts.");
    }

    private static void updateSh(Path laboratoriesPath) throws IOException {
        String result = "";
        for (Laboratory laboratory : EnvironmentService.getVigiangLaboratories()) {
            String alias = getSshAliasFormat(laboratory);
            result += alias + "\n";
        }

        Path laboratoriesShPath = Paths.get(laboratoriesPath + "\\laboratories.sh");

        var initialFileContent = "";
        if (Files.exists(laboratoriesShPath)) {
            initialFileContent = new String(Files.readAllBytes(laboratoriesShPath));
        }

        var newFileContent = result;

        if (!initialFileContent.equals(newFileContent)) {
            System.out.println("updating file: " + laboratoriesShPath);
            Files.writeString(laboratoriesShPath, newFileContent, StandardCharsets.UTF_8);
        }
    }

    private static void updateMd(Path laboratoriesPath) throws IOException {
        String result = "# DEPLOY_HOSTS:\n";
        result += "```\n";
        for (Laboratory laboratory : EnvironmentService.getVigiangLaboratories()) {
            result += laboratory.getSshHost() + " " + laboratory.getName() + "\n";
        }
        result += "```\n\n";

        result += "# vite deploy string:\n";
        result += "```\n";
        String oneline = "";
        for (Laboratory laboratory : EnvironmentService.getVigiangLaboratories()) {
            oneline += laboratory.getSshHost() + "-" + laboratory.getAlias() + " ";
        }
        result += oneline.trim() + "\n";
        result += "```\n";

        Path laboratoriesShPath = Paths.get(laboratoriesPath + "\\laboratories.md");

        var initialFileContent = "";
        if (Files.exists(laboratoriesShPath)) {
            initialFileContent = new String(Files.readAllBytes(laboratoriesShPath));
        }

        var newFileContent = result;

        if (!initialFileContent.equals(newFileContent)) {
            System.out.println("updating file: " + laboratoriesShPath);
            Files.writeString(laboratoriesShPath, newFileContent, StandardCharsets.UTF_8);
        }
    }

    private static void updateBashrc() throws IOException {
        List<String> aliasList = new ArrayList<>();

        for (Laboratory laboratory : EnvironmentService.getVigiangLaboratories()) {
            String alias = getSshAliasFormat(laboratory);
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

    private static String getSshAliasFormat(Laboratory laboratory) {
        return String.format("alias ssh%s='ssh %s@%s'", laboratory.getAlias(), laboratory.getSshUsername(), laboratory.getSshHost());
    }

    public static void replaceRegion(Path file, String beginMarker, String endMarker, List<String> replacementLines) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int beginIdx = -1, endIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains(beginMarker)) {
                beginIdx = i;
                break;
            }
        }
        if (beginIdx == -1) throw new IllegalStateException("Begin marker not found: " + beginMarker);
        for (int i = beginIdx + 1; i < lines.size(); i++) {
            if (lines.get(i).contains(endMarker)) {
                endIdx = i;
                break;
            }
        }
        if (endIdx == -1) throw new IllegalStateException("End marker not found after begin: " + endMarker);

        List<String> out = new ArrayList<>();
        out.addAll(lines.subList(0, beginIdx + 1));
        out.addAll(replacementLines);
        out.addAll(lines.subList(endIdx, lines.size()));

        Files.write(file, out, StandardCharsets.UTF_8);
    }

}
