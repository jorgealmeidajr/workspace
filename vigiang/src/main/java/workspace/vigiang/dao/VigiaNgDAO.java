package workspace.vigiang.dao;

import workspace.vigiang.model.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public interface VigiaNgDAO {

    List<Feature> listFeatures(Environment env) throws SQLException;

    List<Configuration> listConfigurationValues(Environment env) throws SQLException;

    List<String[]> listModules(Environment env) throws SQLException;

    List<String[]> listPrivileges(Environment env) throws SQLException;

    List<String[]> listProfiles(Environment env) throws SQLException;

    List<String[]> listFilterQueries(Environment env) throws SQLException;

    List<String[]> listZoneInterceptions(Environment env) throws SQLException;

    List<String[]> listValidationRules(Environment env) throws SQLException;

    List<String[]> listQdsValidationRules(Environment env) throws SQLException;

    List<EmailTemplate> listEmailTemplates(Environment env) throws SQLException;

    List<ReportTemplate> listReportTemplates(Environment env) throws SQLException;

    List<String[]> listConfigurationReports(Environment env) throws SQLException;

    List<String[]> listCarriers(Environment env) throws SQLException;

    List<String[]> listZones(Environment env) throws SQLException;

    default Connection getConnection(Environment environment) throws SQLException {
        return DriverManager.getConnection(
                environment.getDatabaseUrl(),
                environment.getDatabaseUsername(),
                environment.getDatabasePassword());
    }

}
