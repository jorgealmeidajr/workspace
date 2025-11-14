package workspace.vigiang.model;

import lombok.Getter;
import workspace.vigiang.dao.VigiaNgDAO;
import workspace.vigiang.service.EnvironmentService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class VigiaNgData {

    private final List<String> featuresIds;
    private final Map<String, Feature> featuresMap;

    private final List<String> configurationsIds;
    private final Map<String, Configuration> configurationsMap;

    public VigiaNgData(String databaseName) throws Exception {
        DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG = EnvironmentService.getDatabaseCredentials(databaseName);
        VigiaNgDAO vigiaNgDAO = EnvironmentService.getVigiaNgDAO(databaseCredentialsVigiaNG);

        List<Feature> features = vigiaNgDAO.listFeatures().stream()
                .sorted(Comparator.comparing(Feature::getCode).reversed())
                .collect(Collectors.toList());
        this.featuresIds = features.stream()
                .map(Feature::getId)
                .collect(Collectors.toList());
        this.featuresMap = features.stream()
                .collect(Collectors.toMap(Feature::getId, Function.identity()));

        List<Configuration> configurations = vigiaNgDAO.listConfigurationValues().stream()
                .sorted(Comparator.comparing(Configuration::getCode).reversed())
                .collect(Collectors.toList());
        this.configurationsIds = configurations.stream()
                .map(Configuration::getId)
                .collect(Collectors.toList());
        this.configurationsMap = configurations.stream()
                .collect(Collectors.toMap(Configuration::getId, Function.identity()));
    }

}
