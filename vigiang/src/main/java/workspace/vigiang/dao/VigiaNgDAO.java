package workspace.vigiang.dao;

import workspace.vigiang.model.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public interface VigiaNgDAO {

    List<Feature> listFeatures() throws SQLException;

    List<Configuration> listConfigurationValues() throws SQLException;

    List<String[]> listPrivileges(DatabaseCredentials databaseCredentials) throws SQLException;

    List<String[]> listProfiles(DatabaseCredentials databaseCredentials) throws SQLException;

    List<String[]> listFilterQueries(DatabaseCredentials databaseCredentials) throws SQLException;

    List<String[]> listZoneInterceptions(DatabaseCredentials databaseCredentials) throws SQLException;

    List<String[]> listValidationRules(DatabaseCredentials databaseCredentials) throws SQLException;

    List<String[]> listQdsValidationRules(DatabaseCredentials databaseCredentials) throws SQLException;

    List<EmailTemplate> listEmailTemplates(DatabaseCredentials databaseCredentials) throws SQLException;

    List<ReportTemplate> listReportTemplates(DatabaseCredentials databaseCredentials) throws SQLException;

    List<String[]> listConfigurationReports(DatabaseCredentials databaseCredentials) throws SQLException;

    List<String[]> listCarriers(DatabaseCredentials databaseCredentials) throws SQLException;

    List<String[]> listZones(DatabaseCredentials databaseCredentials) throws SQLException;

    void updateTemplateReport(DatabaseCredentials databaseCredentials, String carrierId, String reportId, String reportName, byte[] fileBytes) throws SQLException;

    void updateConfigurationValue(DatabaseCredentials databaseCredentials, Configuration configuration, String newValue) throws SQLException;

    void insertPrivileges(DatabaseCredentials databaseCredentials, List<String> privilegeIds) throws SQLException;

    void associatePrivileges(DatabaseCredentials targetDb, int targetPrivilegeId) throws SQLException;

    default Connection getConnection(DatabaseCredentials databaseCredentials) throws SQLException {
        return DriverManager.getConnection(
                databaseCredentials.getDatabaseUrl(),
                databaseCredentials.getDatabaseUsername(),
                databaseCredentials.getDatabasePassword());
    }

}
