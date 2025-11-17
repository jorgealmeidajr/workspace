package workspace.vigiang.dao;

import workspace.vigiang.model.Configuration;
import workspace.vigiang.model.EmailTemplate;
import workspace.vigiang.model.Feature;
import workspace.vigiang.model.ReportTemplate;

import java.sql.SQLException;
import java.util.List;

public interface VigiaNgDAO {

    List<Feature> listFeatures() throws SQLException;

    List<Configuration> listConfigurationValues() throws SQLException;

    List<String[]> listPrivileges() throws SQLException;

    List<String[]> listProfiles() throws SQLException;

    List<String[]> listFilterQueries() throws SQLException;

    List<String[]> listZoneInterceptions() throws SQLException;

    List<String[]> listValidationRules() throws SQLException;

    List<String[]> listQdsValidationRules() throws SQLException;

    List<EmailTemplate> listEmailTemplates() throws SQLException;

    List<ReportTemplate> listReportTemplates() throws SQLException;

    List<String[]> listConfigurationReports() throws SQLException;

    List<String[]> listCarriers() throws SQLException;

    List<String[]> listZones() throws SQLException;

    void updateTemplateReport(String carrierId, String reportId, String reportName, byte[] fileBytes) throws SQLException;

    void updateConfigurationValue(Configuration configuration, String newValue) throws SQLException;

    void updateTemplateEmail(String carrierId, String templateEmailName, byte[] fileBytes) throws SQLException;

    void insertPrivileges(List<String> privilegeIds) throws SQLException;

    void associatePrivileges(int targetPrivilegeId) throws SQLException;

}
