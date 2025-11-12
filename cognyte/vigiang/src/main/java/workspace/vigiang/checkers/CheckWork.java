package workspace.vigiang.checkers;

import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static workspace.vigiang.checkers.CheckLaboratories.replaceRegion;

public class CheckWork {

    public static void main(String[] args) {
        String result = "";
        for (String version : EnvironmentService.getVersions()) {
            String frontendPath = "$VIGIANG_ROOT/" + version + "/front-" + version;
            String backendPath = "$VIGIANG_ROOT/" + version + "/back-" + version;

            String front = String.format("alias front%s='cd \"%s\"'", version, frontendPath);
            String back = String.format("alias back%s='cd \"%s\"'", version, backendPath);

            result += front + "\n";
            result += back + "\n\n";
        }

        try {
            List<String> lines = Arrays.asList(result.trim().split("\\R"));
            updateBashrc(lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateBashrc(List<String> lines) throws IOException {
        Path bashrcPath = Paths.get(EnvironmentService.getCognytePath() + "\\.bashrc");
        replaceRegion(bashrcPath, "# vigiang_work begin", "# vigiang_work end", lines);
        String content = Files.readString(bashrcPath, StandardCharsets.UTF_8);

        Path userBashrcPath = Paths.get("C:\\Users\\jjunior\\.bashrc");
        if (Files.exists(userBashrcPath)) {
            Files.writeString(userBashrcPath, content, StandardCharsets.UTF_8);
            System.out.println("updating file: " + userBashrcPath);
        }
    }

}
