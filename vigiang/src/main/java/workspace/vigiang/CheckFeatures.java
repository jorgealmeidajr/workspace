package workspace.vigiang;

import workspace.vigiang.model.Environment;
import workspace.vigiang.model.TablePrinter;
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
                updateLocalFeaturesFile(vigiangPath, env);
                updateLocalConfigurationFile(vigiangPath, env);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            System.out.println("######\n");
        }
    }

    private static void updateLocalFeaturesFile(Path vigiangPath, Environment env) throws SQLException, IOException {
        String[] headers = new String[] { "ID_FEATURE", "ID_STATUS" }; // , "ID_DESCRICAO"
        List<String[]> data = VIGIA_NG_DAO.listFeatures(env);

        var finalLines = new ArrayList<String>();
        int[] columnWidths = TablePrinter.calculateColumnWidths(headers, data);

        finalLines.add(TablePrinter.printRow(headers, columnWidths));
        finalLines.add(TablePrinter.printHorizontalLine(columnWidths));

        for (String[] row : data) {
            finalLines.add(TablePrinter.printRow(row, columnWidths));
        }

        var finalContent = String.join(System.lineSeparator(), finalLines);

        Path finalFilePath = Paths.get(vigiangPath + "\\envs\\" + env + "\\DEV\\database\\CFG_NG_FEATURE.md");
        System.out.println("updating file: " + finalFilePath);
        Files.writeString(finalFilePath, finalContent, StandardCharsets.UTF_8);
        System.out.println("file updated");
    }

    private static void updateLocalConfigurationFile(Path vigiangPath, Environment env) throws SQLException, IOException {
        String[] headers = new String[] { "ID_PARAMETRO" };
        List<String[]> data = VIGIA_NG_DAO.listConfigurationValues(env);

        var finalLines = new ArrayList<String>();
        int[] columnWidths = TablePrinter.calculateColumnWidths(headers, data);

        finalLines.add(TablePrinter.printRow(headers, columnWidths));
        finalLines.add(TablePrinter.printHorizontalLine(columnWidths));

        for (String[] row : data) {
            finalLines.add(TablePrinter.printRow(row, columnWidths));
        }

        var finalContent = String.join(System.lineSeparator(), finalLines);

        Path finalFilePath = Paths.get(vigiangPath + "\\envs\\" + env + "\\DEV\\database\\CFG_NG_SITE.md");
        System.out.println("updating file: " + finalFilePath);
        Files.writeString(finalFilePath, finalContent, StandardCharsets.UTF_8);
        System.out.println("file updated");
    }

}
