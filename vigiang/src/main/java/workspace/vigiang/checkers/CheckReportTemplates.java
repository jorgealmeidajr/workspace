package workspace.vigiang.checkers;

import workspace.vigiang.FilesService;
import workspace.vigiang.model.Environment;
import workspace.vigiang.model.ReportTemplate;
import workspace.vigiang.model.VigiaNgDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class CheckReportTemplates {

    public static void main(String[] args) {
        var vigiangPathStr = "C:\\Users\\jjunior\\MyDocuments\\COGNYTE\\VIGIANG";
        Path vigiangPath = Paths.get(vigiangPathStr);

        if (!Files.exists(vigiangPath) || !Files.isDirectory(vigiangPath)) {
            throw new IllegalArgumentException("o diretorio do vigiang nao existe ou nao eh um diretorio");
        }

        System.out.println("#".repeat(3 * 2));
        System.out.println("## START checking all report templates\n");

        for (Environment env : Environment.values()) {
            VigiaNgDAO dao = env.getVigiaNgDAO();
            System.out.println(env + ":");

            try {
                updateLocalReportFiles(vigiangPath, env, dao);
                updateLocalConfigReportFiles(vigiangPath, env, dao);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

            System.out.println();
        }

        System.out.println("## END checking all report templates.");
        System.out.println("#".repeat(3 * 2));
    }

    private static void updateLocalReportFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_RELATORIO";
            columns = new String[] { "CD_RELATORIO", "ID_RELATORIO", "TP_RELATORIO", "CD_OPERADORA", "NM_OPERADORA" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.report";
            columns = new String[] { "id", "report_id", "report_type", "carrier_id", "carrier_name" };
        }

        List<ReportTemplate> reportTemplates = dao.listReports(env);
        List<String[]> data = reportTemplates.stream()
                .map(ReportTemplate::toArray)
                .collect(Collectors.toList());

        FilesService.updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

    private static void updateLocalConfigReportFiles(Path vigiangPath, Environment env, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_SITE_RELATORIO";
            columns = new String[] { "CD_OPERADORA", "ID_PARAMETRO", "DE_PARAMETRO", "VL_PARAMETRO", "CD_RELATORIO", "ID_RELATORIO" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.site_report";
            columns = new String[] { "carrier_id", "parameter_id", "parameter_description", "value", "id", "report_id" };
        }

        List<String[]> data = dao.listConfigurationReports(env);
        FilesService.updateLocalFiles(vigiangPath, env, fileName, columns, data);
    }

}
