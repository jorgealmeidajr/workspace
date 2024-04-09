package workspace.vigiang;

import workspace.vigiang.model.Environment;
import workspace.vigiang.model.VigiaNgDAO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CheckFeatures {

    static final VigiaNgDAO VIGIA_NG_DAO = new VigiaNgDAO();

    public static void main(String[] args) {
        var vigiangPathStr = "C:\\Users\\jjunior\\OneDrive - COGNYTE\\Documents\\COGNYTE\\VIGIANG";
        Path vigiangPath = Paths.get(vigiangPathStr);

        if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
            throw new IllegalArgumentException("o diretorio do vigiang nao existe ou nao eh um diretorio");
        }

        for (Environment env : Environment.values()) {
            System.out.println("######");
            System.out.println(env);

            try {
                updateLocalFeatureFiles(vigiangPath, env);
                updateLocalConfigurationFiles(vigiangPath, env);
                updateLocalModuleFiles(vigiangPath, env);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            System.out.println("######\n");
        }
    }

    private static void updateLocalModuleFiles(Path vigiangPath, Environment env) throws IOException, SQLException {
        String[] columns = new String[] { "ID_CHAVE", "ID_STATUS", "ID_TIPO" };
        List<String[]> data = VIGIA_NG_DAO.listModules(env);

        var finalLines = new ArrayList<String>();
        int columnWidth = calculateColumnWidth(columns);

        for (String[] row : data) {
            String line = "";
            for (int i = 0; i < columns.length; i++) {
                var column = rightPad(columns[i], columnWidth, " ");
                line += column + ": " + row[i] + "\n";
            }
            finalLines.add(line);
        }

        var finalContent = "# " + env + " | CFG_MODULO\n" +
            "```\n" +
            String.join(System.lineSeparator(), finalLines) +
            "```\n";

        Path finalFilePath = Paths.get(vigiangPath + "\\envs\\" + env + "\\DEV\\database\\CFG_MODULO.md");
        System.out.println("updating file: " + finalFilePath);
        Files.writeString(finalFilePath, finalContent, StandardCharsets.UTF_8);
        System.out.println("file updated");
    }

    private static void updateLocalFeatureFiles(Path vigiangPath, Environment env) throws SQLException, IOException {
        String[] columns = new String[] { "ID_FEATURE", "ID_STATUS", "ID_DESCRICAO" };
        List<String[]> data = VIGIA_NG_DAO.listFeatures(env, columns);

        var finalLines = new ArrayList<String>();
        int columnWidth = calculateColumnWidth(columns);

        for (String[] row : data) {
            String line = "";
            for (int i = 0; i < columns.length; i++) {
                var column = rightPad(columns[i], columnWidth, " ");
                line += column + ": " + row[i] + "\n";
            }
            finalLines.add(line);
        }

        var finalContent = "# " + env + " | CFG_NG_FEATURE\n" +
            "```\n" +
            String.join(System.lineSeparator(), finalLines) +
            "```\n";

        Path finalFilePath = Paths.get(vigiangPath + "\\envs\\" + env + "\\DEV\\database\\CFG_NG_FEATURE.md");
        System.out.println("updating file: " + finalFilePath);
        Files.writeString(finalFilePath, finalContent, StandardCharsets.UTF_8);
        System.out.println("file updated");
    }

    private static int calculateColumnWidth(String[] headers) {
        int maxValue = headers[0].length();
        for (int i = 0; i < headers.length; i++) {
            int headerLength = headers[i].length();
            if (headerLength > maxValue) {
                maxValue = headerLength;
            }
        }
        return maxValue;
    }

    public static String rightPad(String input, int length, String padStr) {
        if(input == null || padStr == null){
            return null;
        }

        if(input.length() >= length){
            return input;
        }

        int padLength = length - input.length();

        StringBuilder paddedString = new StringBuilder();
        paddedString.append(input);
        paddedString.append(padStr.repeat(padLength));

        return paddedString.toString();
    }

    private static void updateLocalConfigurationFiles(Path vigiangPath, Environment env) throws SQLException, IOException {
        String[] headers = new String[] { "ID_PARAMETRO", "DE_PARAMETRO", "VL_PARAMETRO" };
        List<String[]> data = VIGIA_NG_DAO.listConfigurationValues(env);

        var finalLines = new ArrayList<String>();
        int columnWidth = calculateColumnWidth(headers);

        for (String[] row : data) {
            String line = "";
            for (int i = 0; i < headers.length; i++) {
                var column = rightPad(headers[i], columnWidth, " ");
                line += column + ": " + row[i] + "\n";
            }
            finalLines.add(line);
        }

        var finalContent = "# " + env + " | CFG_NG_SITE\n" +
            "```\n" +
            String.join(System.lineSeparator(), finalLines) +
            "```\n";

        Path finalFilePath = Paths.get(vigiangPath + "\\envs\\" + env + "\\DEV\\database\\CFG_NG_SITE.md");
        System.out.println("updating file: " + finalFilePath);
        Files.writeString(finalFilePath, finalContent, StandardCharsets.UTF_8);
        System.out.println("file updated");
    }

}
