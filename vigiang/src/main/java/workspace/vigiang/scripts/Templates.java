package workspace.vigiang.scripts;

import workspace.vigiang.dao.VigiaNgDAO;
import workspace.vigiang.model.Configuration;
import workspace.vigiang.model.Environment;
import workspace.vigiang.model.ReportTemplate;
import workspace.vigiang.service.EnvironmentService;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Templates {

    static class UpdateReports {
        public static void main(String[] args) {
            String ENVIRONMENT_NAME = "?"; // this name should be in environments.json
            Integer CARRIER_ID = 0; // this id is from database

            try {
                Environment environment = EnvironmentService.getEnvironments().stream()
                        .filter(env -> env.getName().equals(ENVIRONMENT_NAME))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Environment not found: " + ENVIRONMENT_NAME));
                VigiaNgDAO dao = EnvironmentService.getVigiaNgDAO(environment);

                Path reportTemplatesPath = EnvironmentService.getReportTemplatesPath(environment);
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(reportTemplatesPath)) {
                    for (Path entry : stream) {
                        String fullFileName = entry.getFileName().toString();
                        String[] split = fullFileName.split("_");
                        String carrierId = split[0];
                        String reportId = split[1];

                        if (!CARRIER_ID.equals(Integer.parseInt(carrierId))) continue;

                        int firstUnderscore = fullFileName.indexOf('_');
                        int secondUnderscore = fullFileName.indexOf('_', firstUnderscore + 1);
                        int lastDot = fullFileName.lastIndexOf('.');
                        String reportName = fullFileName.substring(secondUnderscore + 1, lastDot);

                        byte[] fileBytes = Files.readAllBytes(entry);
                        dao.updateTemplateReport(environment, carrierId, reportId, reportName, fileBytes);
                    }
                }

                System.out.println("execution finished...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class UpdateReportsConfig {
        public static void main(String[] args) {
            String ENVIRONMENT_NAME = "?"; // this name should be in environments.json
            String CARRIER_ID = "0"; // this id is from database

            try {
                Environment environment = EnvironmentService.getEnvironments().stream()
                        .filter(env -> env.getName().equals(ENVIRONMENT_NAME))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Environment not found: " + ENVIRONMENT_NAME));
                VigiaNgDAO dao = EnvironmentService.getVigiaNgDAO(environment);

                List<Configuration> configurations = dao.listConfigurationValues(environment).stream()
                        .filter(configuration -> configuration.getCarrierId().equals(CARRIER_ID))
                        .filter(configuration -> configuration.getId().toLowerCase().contains("report") && configuration.getId().toLowerCase().contains("id"))
                        .collect(Collectors.toList());

                List<ReportTemplate> reportTemplates = dao.listReportTemplates(environment).stream()
                        .filter(reportTemplate -> reportTemplate.getCarrierCode().equals(CARRIER_ID))
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
                                dao.updateConfigurarionValue(environment, configuration, newValue);
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
//                configMap.put("cnfg.cnj.report.pdf.id", "");
//                configMap.put("cnfg.cnj.report.xls.id", "");
            configurationsMap.put("cnfg.customer.pdf.report.id", "report_events_customer_pdf_template");
            configurationsMap.put("cnfg.customer.report.id", "report_events_customer_template");
            configurationsMap.put("cnfg.roaming.report.id", "roaming_template");
            return configurationsMap;
        }
    }

}
