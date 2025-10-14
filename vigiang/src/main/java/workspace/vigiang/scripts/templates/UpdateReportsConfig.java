package workspace.vigiang.scripts.templates;

import workspace.vigiang.dao.VigiaNgDAO;
import workspace.vigiang.model.Carrier;
import workspace.vigiang.model.Configuration;
import workspace.vigiang.model.DatabaseCredentials;
import workspace.vigiang.model.ReportTemplate;
import workspace.vigiang.service.EnvironmentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class UpdateReportsConfig {

    public static void main(String[] args) {
        // the parameters bellow must match in file databases.json
        String DATABASE_NAME = "?";
        Carrier CARRIER = null;
        DatabaseCredentials.Database DATABASE = null;

        Integer CARRIER_ID = 0; // this id is from database

        try {
            Predicate<DatabaseCredentials> databaseCredentialsPredicate = (credentials) ->
                    credentials.getName().equals(DATABASE_NAME)
                            && credentials.getCarrier().equals(CARRIER)
                            && credentials.getDatabase().equals(DATABASE);
            DatabaseCredentials databaseCredentials = EnvironmentService.getVigiangDatabases().stream()
                    .filter(databaseCredentialsPredicate)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Database credentials not found, check the parameters..."));
            VigiaNgDAO dao = EnvironmentService.getVigiaNgDAO(databaseCredentials);
            String carrierId = String.valueOf(CARRIER_ID);

            List<Configuration> configurations = dao.listConfigurationValues().stream()
                    .filter(configuration -> configuration.getCarrierId().equals(carrierId))
                    .filter(configuration -> configuration.getId().toLowerCase().contains("report") && configuration.getId().toLowerCase().contains("id"))
                    .collect(Collectors.toList());

            List<ReportTemplate> reportTemplates = dao.listReportTemplates().stream()
                    .filter(reportTemplate -> reportTemplate.getCarrierCode().equals(carrierId))
                    .collect(Collectors.toList());

            Map<String, String> configurationsMap = getConfigurationsMap();

            var configurationsNotMapped = new ArrayList<String>();
            var reportTemplatesMissing = new ArrayList<String>();

            for (Configuration configuration : configurations) {
                if (configurationsMap.containsKey(configuration.getId())) {
                    String reportId = configurationsMap.get(configuration.getId());

                    ReportTemplate matchingTemplate = reportTemplates.stream()
                            .filter(template -> template.getReportId().equals(reportId))
                            .findFirst()
                            .orElse(null);

                    if (matchingTemplate != null) {
                        String originalValue = configuration.getValue();
                        String newValue = matchingTemplate.getReportCode();
                        System.out.println(configuration.getId() + " -> " + matchingTemplate.getReportId());

                        if (!originalValue.equals(newValue)) {
                            dao.updateConfigurationValue(configuration, newValue);
                            System.out.println("  original value: " + originalValue + " -> new value: " + newValue);
                        }

                    } else {
                        reportTemplatesMissing.add(reportId);
                    }

                } else {
                    configurationsNotMapped.add(configuration.getId());
                }
            }

            if (!configurationsNotMapped.isEmpty()) {
                System.out.println("\nConfigurations not mapped:");
                configurationsNotMapped.forEach(System.out::println);
            }

            if (!reportTemplatesMissing.isEmpty()) {
                System.out.println("\nReport templates missing from database:");
                reportTemplatesMissing.forEach(System.out::println);
            }

            System.out.println("\nExecution finished...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> getConfigurationsMap() {
        Map<String, String> configurationsMap = new HashMap<>();
        configurationsMap.put("cnfg.operation.password.report.id", "report_operation_password_xls");
        configurationsMap.put("cnfg.event.pdf.report.id", "operation_events_pdf_template");
        configurationsMap.put("cnfg.event.report.id", "operation_events_template");

        // workflow warrant extract
        configurationsMap.put("cnfg.extract.call.pdf.report.id", "events_call_mesage_pdf_template");
        configurationsMap.put("cnfg.extract.call.report.id", "events_call_message_template");
        configurationsMap.put("cnfg.extract.connection.pdf.report.id", "events_connection_pdf_template");
        configurationsMap.put("cnfg.extract.connection.report.id", "events_connection_template");
        configurationsMap.put("cnfg.extract.customer.pdf.report.id", "events_customer_pdf_template");
        configurationsMap.put("cnfg.extract.customer.report.id", "events_customer_template");
        configurationsMap.put("cnfg.extract.web.pdf.report.id", "events_web_pdf_template");
        configurationsMap.put("cnfg.extract.web.report.id", "events_web_template");

        configurationsMap.put("cnfg.cell.pdf.report.id", "report_events_cell_pdf_template");
        configurationsMap.put("cnfg.cell.report.id", "report_events_cell_template");
        configurationsMap.put("cnfg.customer.pdf.report.id", "report_events_customer_pdf_template");
        configurationsMap.put("cnfg.customer.report.id", "report_events_customer_template");
        configurationsMap.put("cnfg.roaming.report.id", "roaming_template");
//        configurationsMap.put("cnfg.cnj.report.pdf.id", "?");
//        configurationsMap.put("cnfg.cnj.report.xls.id", "?");

        configurationsMap.put("cnfg.voucher.report.id", "voucher_unattended_template");

        // voucher request
        configurationsMap.put("cnfg.voucher.request.report.crm.report.id", "voucher_crm_data_template");
        configurationsMap.put("cnfg.voucher.request.report.interception.call.report.id", "voucher_interception_call_template");
        configurationsMap.put("cnfg.voucher.request.report.interception.call.sms.report.id", "vocuher_interception_call_sms_template");
        configurationsMap.put("cnfg.voucher.request.report.tracking.call.report.id", "voucher_tracking_call_template");
        configurationsMap.put("cnfg.voucher.request.report.tracking.event.report.id", "voucher_tracking_event_template");
        return configurationsMap;
    }

}
