package workspace.vigiang;

import workspace.vigiang.model.Environment;
import workspace.vigiang.model.VigiaNgDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

public class ReportTemplates {

    static final VigiaNgDAO VIGIA_NG_DAO = new VigiaNgDAO();

    public static void main(String[] args) {
        try {
            Path path = Paths.get("C:\\Users\\jjunior\\MyDocuments\\REPORT_TEMPLATES");

            if (!Files.exists(path) || !Files.isDirectory(path)) {
                throw new IllegalArgumentException("template folder does not exist or it is not a directory");
            }

            System.out.println("Start to download report templates from dev databases");
            downloadReportTemplates(path);
            System.out.println("End to download report templates");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void downloadReportTemplates(Path path) throws IOException, SQLException {
        for (Environment env : Environment.values()) {
            if (env.equals(Environment.SURF)) continue; // TODO: this environment uses postgres

            System.out.println("#########");
            System.out.println(env);

            Path tempPath = Paths.get(path + "\\" + env + "\\_temp");
            if (!Files.exists(tempPath)) {
                Files.createDirectories(tempPath);
            }

            List<Object[]> data = VIGIA_NG_DAO.listReportTemplates(env);

            for (Object[] reportTemplate : data){
                writeReportTemplate(tempPath, reportTemplate);
            }

            System.out.println("#########\n");
        }
    }

    private static void writeReportTemplate(Path tempPath, Object[] reportTemplate) throws IOException {
        var code = Integer.parseInt((String) reportTemplate[0]);
        var codeFormatted = String.format("%02d", code);
        var id = reportTemplate[1];

        var extension = reportTemplate[2];
        var templateExtension = "";
        if ("pdf".equals(extension)) {
            templateExtension = "odt";
        } else if ("xls".equals(extension)) {
            templateExtension = "xlsx";
        }

        var bytes = (byte[]) reportTemplate[3];
        var templateName = codeFormatted + "_" + id + "." + templateExtension;

        Path templatePath = Paths.get(tempPath + "\\" + templateName);
        Files.write(templatePath, bytes);
    }

}
