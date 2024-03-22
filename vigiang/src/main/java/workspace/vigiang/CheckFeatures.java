package workspace.vigiang;

import workspace.vigiang.model.CredentialsOracle;
import workspace.vigiang.model.Environment;
import workspace.vigiang.model.TablePrinter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CheckFeatures {

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
                String[] headers = new String[] { "ID_FEATURE", "ID_STATUS", "ID_DESCRICAO" };
                List<String[]> data = listFeatures(env);

                var finalLines = new ArrayList<String>();
                int[] columnWidths = TablePrinter.calculateColumnWidths(headers, data);

                finalLines.add(TablePrinter.printRow(headers, columnWidths));
                finalLines.add(TablePrinter.printHorizontalLine(columnWidths));

                for (String[] row : data) {
                    finalLines.add(TablePrinter.printRow(row, columnWidths));
                }

                var finalContent = String.join(System.lineSeparator(), finalLines);

//                TablePrinter.printTable(headers, data);

                Path finalFilePath = Paths.get(vigiangPath + "\\envs\\" + env + "\\DEV\\database\\CFG_NG_FEATURE.md");
                System.out.println("updating file: " + finalFilePath);
                Files.writeString(finalFilePath, finalContent, StandardCharsets.UTF_8);
                System.out.println("file updated");

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            System.out.println("######\n");
        }
    }

    private static List<String[]> listFeatures(Environment env) throws SQLException {
        var credentials = CredentialsOracle.getCredentials(env);

        String sql =
            "select ID_FEATURE, ID_STATUS, ID_DESCRICAO\n" +
            "from CFG_NG_FEATURE\n" +
            "order by ID_FEATURE";

        List<String[]> data = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(credentials.get("url"), credentials.get("username"), credentials.get("password"));
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                var descricao = (rs.getString("ID_DESCRICAO") == null) ? "NULL" : rs.getString("ID_DESCRICAO");
                String[] row = new String[] {
                        rs.getString("ID_FEATURE"),
                        rs.getString("ID_STATUS"),
                        descricao
                };
                data.add(row);
            }
        }
        return data;
    }

}
