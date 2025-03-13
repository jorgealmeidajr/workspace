package workspace.vigiang.model;

import java.sql.SQLException;
import java.util.List;

public interface VigiaNgDAO {

    List<String[]> listFeatures(Environment env, String[] columns) throws SQLException;

    List<String[]> listConfigurationValues(Environment env) throws SQLException;

    List<String[]> listModules(Environment env) throws SQLException;

    List<String[]> listPrivileges(Environment env) throws SQLException;

    List<String[]> listProfiles(Environment env) throws SQLException;

    List<String[]> listFilterQueries(Environment env) throws SQLException;

    List<String[]> listZoneInterceptions(Environment env) throws SQLException;

    List<String[]> listValidationRules(Environment env) throws SQLException;

    List<String[]> listQdsValidationRules(Environment env) throws SQLException;

    List<String[]> listEmailTemplates(Environment env) throws SQLException;

    List<String[]> listReports(Environment env) throws SQLException;

    List<String[]> listConfigurationReports(Environment env) throws SQLException;

    List<Object[]> listReportTemplates(Environment env) throws SQLException;

    List<String> listObjects(Environment env, String objectType) throws SQLException;

    List<String[]> listDdlStatements(Environment env, List<String> objectNames, String objectType) throws SQLException;

}
