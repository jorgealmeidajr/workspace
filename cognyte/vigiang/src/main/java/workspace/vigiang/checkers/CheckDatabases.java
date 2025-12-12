package workspace.vigiang.checkers;

import workspace.commons.model.Database;
import workspace.commons.service.FileService;
import workspace.vigiang.model.Configuration;
import workspace.vigiang.model.Feature;
import workspace.vigiang.model.FileConfigRegistry;
import workspace.vigiang.service.EnvironmentService;
import workspace.vigiang.model.DatabaseCredentialsVigiaNG;
import workspace.vigiang.dao.VigiaNgDAO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CheckDatabases {

    public static void main(String[] args) {
        System.out.println("## START checking all environment databases\n");
        try {
            for (DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG : EnvironmentService.getDatabasesVigiaNg()) {
                VigiaNgDAO dao = EnvironmentService.getVigiaNgDAO(databaseCredentialsVigiaNG);
                System.out.println(databaseCredentialsVigiaNG.getName() + ":");

                updateLocalFeatureFiles(databaseCredentialsVigiaNG, dao);
                updateLocalConfigurationFiles(databaseCredentialsVigiaNG, dao);

                updateLocalPrivilegeFiles(databaseCredentialsVigiaNG, dao);
                updateLocalProfileFiles(databaseCredentialsVigiaNG, dao);

                updateLocalFilterQueryFiles(databaseCredentialsVigiaNG, dao);
                updateLocalZoneInterceptionFiles(databaseCredentialsVigiaNG, dao);
                updateLocalValidationRuleFiles(databaseCredentialsVigiaNG, dao);
                updateLocalQdsValidationRuleFiles(databaseCredentialsVigiaNG, dao);
                updateLocalCarriersFiles(databaseCredentialsVigiaNG, dao);
                updateLocalZonesFiles(databaseCredentialsVigiaNG, dao);

                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n## END checking all environment databases.");
    }

    private static void updateLocalFeatureFiles(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, VigiaNgDAO dao) throws SQLException, IOException {
        var fileConfig = FileConfigRegistry.getConfig("feature", databaseCredentialsVigiaNG.getDatabase());

        List<String[]> data = dao.listFeatures().stream()
                .map(Feature::toArray)
                .collect(Collectors.toList());

        Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
        FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileConfig.getFileName(), fileConfig.getColumns(), data, databaseDataPath);
    }

    private static void updateLocalConfigurationFiles(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, VigiaNgDAO dao) throws SQLException, IOException {
        var fileConfig = FileConfigRegistry.getConfig("configuration", databaseCredentialsVigiaNG.getDatabase());

        List<String[]> data = dao.listConfigurationValues().stream()
                .map(Configuration::toArray)
                .collect(Collectors.toList());

        Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
        FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileConfig.getFileName(), fileConfig.getColumns(), data, databaseDataPath);
    }

    private static void updateLocalPrivilegeFiles(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, VigiaNgDAO dao) throws IOException, SQLException {
        var fileConfig = FileConfigRegistry.getConfig("privilege", databaseCredentialsVigiaNG.getDatabase());
        List<String[]> data = dao.listPrivileges();
        Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
        FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileConfig.getFileName(), fileConfig.getColumns(), data, databaseDataPath);
    }

    private static void updateLocalProfileFiles(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, VigiaNgDAO dao) throws IOException, SQLException {
        var fileConfig = FileConfigRegistry.getConfig("profile", databaseCredentialsVigiaNG.getDatabase());
        List<String[]> data = dao.listProfiles();
        Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
        FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileConfig.getFileName(), fileConfig.getColumns(), data, databaseDataPath);
    }

    private static void updateLocalFilterQueryFiles(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, VigiaNgDAO dao) throws IOException, SQLException {
        var fileConfig = FileConfigRegistry.getConfig("filterQuery", databaseCredentialsVigiaNG.getDatabase());
        List<String[]> data = dao.listFilterQueries();
        Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
        FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileConfig.getFileName(), fileConfig.getColumns(), data, databaseDataPath);
    }

    private static void updateLocalZoneInterceptionFiles(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, VigiaNgDAO dao) {
        var fileConfig = FileConfigRegistry.getConfig("zoneInterception", databaseCredentialsVigiaNG.getDatabase());

        try {
            List<String[]> data = dao.listZoneInterceptions();
            Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
            FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileConfig.getFileName(), fileConfig.getColumns(), data, databaseDataPath);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalValidationRuleFiles(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, VigiaNgDAO dao) {
        var fileConfig = FileConfigRegistry.getConfig("validationRule", databaseCredentialsVigiaNG.getDatabase());

        try {
            List<String[]> data = dao.listValidationRules();
            Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
            FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileConfig.getFileName(), fileConfig.getColumns(), data, databaseDataPath);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalQdsValidationRuleFiles(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, VigiaNgDAO dao) {
        if (Database.POSTGRES.equals(databaseCredentialsVigiaNG.getDatabase())) {
            return;
        }

        var fileConfig = FileConfigRegistry.getConfig("qdsValidationRule", databaseCredentialsVigiaNG.getDatabase());

        try {
            List<String[]> data = dao.listQdsValidationRules();
            Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
            FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileConfig.getFileName(), fileConfig.getColumns(), data, databaseDataPath);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalCarriersFiles(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, VigiaNgDAO dao) throws SQLException, IOException {
        var fileConfig = FileConfigRegistry.getConfig("carrier", databaseCredentialsVigiaNG.getDatabase());

        List<String[]> data = dao.listCarriers();
        Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
        FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileConfig.getFileName(), fileConfig.getColumns(), data, databaseDataPath);

        writeLogos(databaseCredentialsVigiaNG, data);
    }

    private static void writeLogos(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, List<String[]> data) throws IOException {
        Path databaseDataPath = EnvironmentService.getDatabasePath(databaseCredentialsVigiaNG);
        Path logosPath = Paths.get(databaseDataPath + "\\logos");

        if (Files.exists(logosPath) && Files.isDirectory(logosPath)) {
            try (var filesStream = Files.list(logosPath)) {
                filesStream.forEach(file -> {
                    try {
                        Files.deleteIfExists(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } else {
            Files.createDirectories(logosPath);
        }

        for (String[] carrier : data) {
            writeLogo(logosPath, carrier);
        }
    }

    private static void writeLogo(Path logosPath, String[] carrier) throws IOException {
        int carrierId = Integer.parseInt(carrier[0]);
        String carrierCode = String.format("%02d", carrierId);
        String carrierName = Arrays.stream(carrier[1].trim().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));
        carrierName = carrierName.trim().toUpperCase().replaceAll("\\s+", "_");
        String logoName = carrierCode + "-" + carrierName;

        String logo = carrier[14];
        if (logo == null || logo.trim().isEmpty()) return;

        if (FileService.isSvgXml(logo)) {
            Path logoPath = Paths.get(logosPath + "\\" + logoName + ".svg");
            Files.writeString(logoPath, logo, StandardCharsets.UTF_8);
        } else {
            System.out.println("Logo is not in SVG format=" + logo);
        }
    }

    private static void updateLocalZonesFiles(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, VigiaNgDAO dao) throws SQLException, IOException {
        var fileConfig = FileConfigRegistry.getConfig("zone", databaseCredentialsVigiaNG.getDatabase());

        List<String[]> data = dao.listZones();
        Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
        FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileConfig.getFileName(), fileConfig.getColumns(), data, databaseDataPath);
    }

}
