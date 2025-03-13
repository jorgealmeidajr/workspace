package workspace.vigiang;

import workspace.vigiang.model.Environment;
import workspace.vigiang.model.OracleVigiaNgDAO;
import workspace.vigiang.model.VigiaNgDAO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CheckOracleDatabases {

    static final VigiaNgDAO VIGIA_NG_DAO = new OracleVigiaNgDAO();

    public static void main(String[] args) {
        var vigiangPathStr = "C:\\Users\\jjunior\\MyDocuments\\COGNYTE\\VIGIANG";
        Path vigiangPath = Paths.get(vigiangPathStr);

        if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
            throw new IllegalArgumentException("o diretorio do vigiang nao existe ou nao eh um diretorio");
        }

        System.out.println("#".repeat(3 * 2));
        System.out.println("## START checking ORACLE databases\n");

        for (Environment env : Environment.values()) {
            if (env.equals(Environment.SURF)) continue; // TODO: this environment uses postgres

            System.out.println("#".repeat(3 * 1));
            System.out.println(env);

            try {
                updateFunctions(vigiangPath, env);
                updateIndexes(vigiangPath, env);
                updatePackages(vigiangPath, env);
                updatePackageBodies(vigiangPath, env);
                updateProcedures(vigiangPath, env);
                updateSequences(vigiangPath, env);
                updateTables(vigiangPath, env);
                updateTriggers(vigiangPath, env);
                updateViews(vigiangPath, env);

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

            System.out.println("#".repeat(3 * 1) + "\n");
            break;
        }

        System.out.println("## END checking all ORACLE databases.");
        System.out.println("#".repeat(3 * 2));
    }

    private static void updateFunctions(Path vigiangPath, Environment env) {
        update(vigiangPath, env, "FUNCTION", "FUNCTIONS");
    }

    private static void updateIndexes(Path vigiangPath, Environment env) {
        update(vigiangPath, env, "INDEX", "INDEXES");
    }

    private static void updatePackages(Path vigiangPath, Environment env) {
        update(vigiangPath, env, "PACKAGE", "PACKAGES");
    }

    private static void updatePackageBodies(Path vigiangPath, Environment env) {
        try {
            List<String> packageNames = VIGIA_NG_DAO.listObjects(env, "PACKAGE BODY");
            List<String[]> ddlStatements = VIGIA_NG_DAO.listDdlStatements(env, packageNames, "PACKAGE_BODY");
            updateLocalFiles(vigiangPath, env, "PACKAGE_BODIES", ddlStatements);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateProcedures(Path vigiangPath, Environment env) {
        update(vigiangPath, env, "PROCEDURE", "PROCEDURES");
    }

    private static void updateSequences(Path vigiangPath, Environment env) {
        update(vigiangPath, env, "SEQUENCE", "SEQUENCES");
    }

    private static void updateTables(Path vigiangPath, Environment env) {
        update(vigiangPath, env, "TABLE", "TABLES");
    }

    private static void updateTriggers(Path vigiangPath, Environment env) {
        update(vigiangPath, env, "TRIGGER", "TRIGGERS");
    }

    private static void updateViews(Path vigiangPath, Environment env) {
        update(vigiangPath, env, "VIEW", "VIEWS");
    }

    private static void update(Path vigiangPath, Environment env, String objectType, String fileName) {
        try {
            List<String> objectNames = VIGIA_NG_DAO.listObjects(env, objectType);
            List<String[]> ddlStatements = VIGIA_NG_DAO.listDdlStatements(env, objectNames, objectType);
            updateLocalFiles(vigiangPath, env, fileName, ddlStatements);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalFiles(Path vigiangPath, Environment env, String fileName, List<String[]> data) throws IOException {
        var finalLines = new ArrayList<String>();

        for (String[] row : data) {
            String line = "```\n" + row[1].trim() + "\n```\n";
            finalLines.add(line);
        }

        var newFileContent = String.join(System.lineSeparator(), finalLines);

        Path finalFilePath = Paths.get(vigiangPath + "\\envs\\" + env + "\\DEV\\oracle\\" + fileName + ".md");

        var initialFileContent = "";
        if (Files.exists(finalFilePath)) {
            initialFileContent = new String(Files.readAllBytes(finalFilePath));
        }

        if (!initialFileContent.equals(newFileContent)) {
            System.out.println("updating file: " + finalFilePath);
            Files.writeString(finalFilePath, newFileContent, StandardCharsets.UTF_8);
        }
    }

}
