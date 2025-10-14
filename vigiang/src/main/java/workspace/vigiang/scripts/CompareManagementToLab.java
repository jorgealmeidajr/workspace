package workspace.vigiang.scripts;

import lombok.Getter;
import workspace.vigiang.dao.ManagementDAO;
import workspace.vigiang.dao.VigiaNgDAO;
import workspace.vigiang.model.*;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Deprecated
public class CompareManagementToLab {

    public static void main(String[] args) {
        // TODO: this will not work anymore
        String carrierManagementName = "CLARO";
        String vigiaNgLabName = "CLARO_ORACLE_DEV";

        try {
            ManagementData managementData = new ManagementData(carrierManagementName);
            VigiaNgData vigiaNgData = new VigiaNgData(vigiaNgLabName);

            checkNewFeaturesToManagement(managementData, vigiaNgData);
            checkNewConfigurationsToManagement(managementData, vigiaNgData);
            // TODO: missing implementation

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkNewFeaturesToManagement(ManagementData managementData, VigiaNgData vigiaNgData) {
        for (String featureId : vigiaNgData.getFeaturesIds()) {
            if (!managementData.getAllFeaturesIds().contains(featureId)) {
                System.out.println("Feature " + featureId + " not found in management data");
            }
        }
    }

    private static void checkNewConfigurationsToManagement(ManagementData managementData, VigiaNgData vigiaNgData) {
        for (String configurationId : vigiaNgData.getConfigurationsIds()) {
            if (!managementData.getAllConfigurationsIds().contains(configurationId)) {
                System.out.println("Configuration " + configurationId + " not found in management data");
            }
        }
    }

}

@Getter
class ManagementData {
    private final Integer carrierId;
    private final String carrierName;

    private final List<String> allConfigurationsIds;
    private final Map<String, ManagementConfiguration> allConfigurationsMap;

    private final List<String> allFeaturesIds;
    private final Map<String, ManagementFeature> allFeaturesMap;

    ManagementData(String carrierManagementName) throws SQLException {
        ManagementDAO managementDAO = new ManagementDAO();
        Map<String, Integer> carriers = managementDAO.listCarriers();
        this.carrierId = carriers.get(carrierManagementName.toUpperCase());
        this.carrierName = carrierManagementName.toUpperCase();

        List<ManagementConfiguration> configurations = managementDAO.listConfigurations();
        this.allConfigurationsMap = configurations.stream()
                .collect(Collectors.toMap(ManagementConfiguration::getName, Function.identity()));
        this.allConfigurationsIds = configurations.stream()
                .map(ManagementConfiguration::getName)
                .collect(Collectors.toList());

        List<ManagementFeature> features = managementDAO.listFeatures();
        this.allFeaturesMap = features.stream()
                .collect(Collectors.toMap(ManagementFeature::getName, Function.identity()));
        this.allFeaturesIds = features.stream()
                .map(ManagementFeature::getName)
                .collect(Collectors.toList());
    }
}

@Getter
class VigiaNgData {
    private final List<String> featuresIds;
    private final Map<String, Feature> featuresMap;

    private final List<String> configurationsIds;
    private final Map<String, Configuration> configurationsMap;

    VigiaNgData(String vigiaNgLabName) throws Exception {
        DatabaseCredentials databaseCredentials = getEnvironmentByName(vigiaNgLabName);
        VigiaNgDAO vigiaNgDAO = EnvironmentService.getVigiaNgDAO(databaseCredentials);

        List<Feature> features = vigiaNgDAO.listFeatures().stream()
                .sorted(Comparator.comparing(Feature::getCode).reversed())
                .collect(Collectors.toList());
        this.featuresIds = features.stream()
                .map(Feature::getId)
                .collect(Collectors.toList());
        this.featuresMap = features.stream()
                .collect(Collectors.toMap(Feature::getId, Function.identity()));

        List<Configuration> configurations = vigiaNgDAO.listConfigurationValues(databaseCredentials).stream()
                .sorted(Comparator.comparing(Configuration::getCode).reversed())
                .collect(Collectors.toList());
        this.configurationsIds = configurations.stream()
                .map(Configuration::getId)
                .collect(Collectors.toList());
        this.configurationsMap = configurations.stream()
                .collect(Collectors.toMap(Configuration::getId, Function.identity()));
    }

    private static DatabaseCredentials getEnvironmentByName(String vigiaNgLabName) throws IOException {
        return EnvironmentService.getVigiangDatabases().stream()
                .filter((databaseCredentials) -> databaseCredentials.getName().toString().equalsIgnoreCase(vigiaNgLabName))
                .findFirst()
                .orElseThrow();
    }
}
