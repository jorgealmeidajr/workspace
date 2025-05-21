package workspace.vigiang.scripts;

import lombok.Getter;
import workspace.vigiang.dao.ManagementDAO;
import workspace.vigiang.dao.VigiaNgDAO;
import workspace.vigiang.model.*;
import workspace.vigiang.service.EnvironmentService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompareManagementToLab {

    public static void main(String[] args) {
        String carrierManagementName = "CLARO";
        String vigiaNgLabName = "CLARO_ORACLE_DEV";

        try {
            ManagementData managementData = new ManagementData(carrierManagementName);
            VigiaNgData vigiaNgData = new VigiaNgData(vigiaNgLabName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

@Getter
class ManagementData {
    private final Integer carrierId;
    private final String carrierName;
    private final Map<String, ManagementConfiguration> allConfigurationsMap;
    private final Map<String, ManagementFeature> allFeaturesMap;

    ManagementData(String carrierManagementName) throws SQLException {
        ManagementDAO managementDAO = new ManagementDAO();
        Map<String, Integer> carriers = managementDAO.listCarriers();
        this.carrierId = carriers.get(carrierManagementName.toUpperCase());
        this.carrierName = carrierManagementName.toUpperCase();

        List<ManagementConfiguration> configurations = managementDAO.listConfigurations();
        this.allConfigurationsMap = configurations.stream()
                .collect(Collectors.toMap(ManagementConfiguration::getName, Function.identity()));

        List<ManagementFeature> features = managementDAO.listFeatures();
        this.allFeaturesMap = features.stream()
                .collect(Collectors.toMap(ManagementFeature::getName, Function.identity()));
    }
}

@Getter
class VigiaNgData {
    private final List<String> featuresKeys;
    private final Map<String, Feature> featuresMap;
    private final List<String> configurationsKeys;
    private final Map<String, Configuration> configurationsMap;

    VigiaNgData(String vigiaNgLabName) throws Exception {
        Environment environment = getEnvironmentByName(vigiaNgLabName);
        VigiaNgDAO vigiaNgDAO = EnvironmentService.getVigiaNgDAO(environment);

        List<Feature> features = vigiaNgDAO.listFeatures(environment);
        this.featuresKeys = features.stream()
                .map(Feature::getId)
                .collect(Collectors.toList());
        this.featuresMap = features.stream()
                .collect(Collectors.toMap(Feature::getId, Function.identity()));

        List<Configuration> configurations = vigiaNgDAO.listConfigurationValues(environment);
        this.configurationsKeys = configurations.stream()
                .map(Configuration::getId)
                .collect(Collectors.toList());
        this.configurationsMap = configurations.stream()
                .collect(Collectors.toMap(Configuration::getId, Function.identity()));
    }

    private static Environment getEnvironmentByName(String vigiaNgLabName) throws IOException {
        return EnvironmentService.getEnvironments().stream()
                .filter((env) -> env.getName().toString().equalsIgnoreCase(vigiaNgLabName))
                .findFirst()
                .orElseThrow();
    }
}
