package workspace.vigiang.scripts;

import lombok.Getter;
import workspace.vigiang.dao.ManagementDAO;
import workspace.vigiang.model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompareManagementToLab {

    public static void main(String[] args) {
        String carrierManagementName = "CLARO?"; // should be the carrier name in management system
        String databaseName = "CLARO?"; // should be the name in databases.json

        try {
            ManagementData managementData = new ManagementData(carrierManagementName);
            VigiaNgData vigiaNgData = new VigiaNgData(databaseName);

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
