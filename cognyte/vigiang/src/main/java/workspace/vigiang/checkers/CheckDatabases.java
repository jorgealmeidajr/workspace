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
            for (DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG : EnvironmentService.getVigiangDatabases()) {
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
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentialsVigiaNG.getDatabase())) {
            fileName = "CFG_TP_ZONA_TP_VL_ITC";
            columns = new String[] {
                "CD_OPERADORA", "NM_OPERADORA",
                "NM_ZONA_MONIT", "NM_TIPO_VALOR_INTERCEPTADO",
                "SN_VISIVEL_CAD_ITC", "SN_VISIVEL_LOTE", "NM_REGRAS"
            };
        } else if (Database.POSTGRES.equals(databaseCredentialsVigiaNG.getDatabase())) {
            fileName = "conf.tp_zone_tp_vl_itc";
            columns = new String[] {
                "carrier_id", "carrier_name",
                "network_element_type_name", "target_type_name",
                "itc_form_visible", "visible", "rules"
            };
        }

        try {
            List<String[]> data = dao.listZoneInterceptions();
            Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
            FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileName, columns, data, databaseDataPath);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalValidationRuleFiles(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, VigiaNgDAO dao) {
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentialsVigiaNG.getDatabase())) {
            fileName = "CFG_NG_VALIDATRULES";
            columns = new String[] { "CD_OPERADORA", "NM_OPERADORA", "MODULO", "VALID_RULES" };
        } else if (Database.POSTGRES.equals(databaseCredentialsVigiaNG.getDatabase())) {
            fileName = "conf.validatrules";
            columns = new String[] { "carrier_id", "carrier_name", "module", "valid_rules" };
        }

        try {
            List<String[]> data = dao.listValidationRules();
            Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
            FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileName, columns, data, databaseDataPath);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalQdsValidationRuleFiles(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, VigiaNgDAO dao) {
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentialsVigiaNG.getDatabase())) {
            fileName = "CFG_TIPO_NUMERO_QDS";
            columns = new String[] { "ID_TIPO_NUMERO_QDS", "NM_CHAVE", "TP_CONSULTA", "SN_VOUCHER_DATE", "VALID_RULES" };
        } else if (Database.POSTGRES.equals(databaseCredentialsVigiaNG.getDatabase())) {
            return;
        }

        try {
            List<String[]> data = dao.listQdsValidationRules();
            Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
            FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileName, columns, data, databaseDataPath);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalCarriersFiles(DatabaseCredentialsVigiaNG databaseCredentialsVigiaNG, VigiaNgDAO dao) throws SQLException, IOException {
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentialsVigiaNG.getDatabase())) {
            fileName = "CFG_OPERADORA";
            columns = new String[] {
                "CD_OPERADORA", "NM_OPERADORA",
                "ID_SMTP_HOST", "ID_SMTP_USER", "ID_SMPT_PASSWORD", "ID_SMPT_ACCOUNT", "ID_SMTP_PORT",
                "DE_COMENTARIO", "SN_MULTIOPERADORA", "ID_REGEX", "DS_LOCAL_IMAGENS",  "DS_REGEX", "ID_API_KEY_MAPS", "SN_TOKEN"
            };
        } else if (Database.POSTGRES.equals(databaseCredentialsVigiaNG.getDatabase())) {
            fileName = "conf.carrier";
            columns = new String[] {
                "id", "name",
                "smtp_host", "smtp_user", "smpt_password", "smpt_account", "smtp_port",
                "comments", "muti_carrier", "regex", "local_images", "ds_regex", "api_key_maps", "token"
            };
        }

        List<String[]> data = dao.listCarriers();
        Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
        FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileName, columns, data, databaseDataPath);

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
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentialsVigiaNG.getDatabase())) {
            fileName = "CFG_ZONA_MONIT";
            columns = new String[] {
                "CD_OPERADORA", "NM_OPERADORA",
                "CD_ZONA_MONIT", "NM_ZONA_MONIT", "DE_COMENTARIOS"//, "IN_ATIVO"
            };
        } else if (Database.POSTGRES.equals(databaseCredentialsVigiaNG.getDatabase())) {
            fileName = "conf.zone_monit";
            columns = new String[] {
                "carrier_id", "carrier_name",
                "zone_monit_id", "zone_monit_name", "comments", "active"
            };
        }

        List<String[]> data = dao.listZones();
        Path databaseDataPath = EnvironmentService.getDatabaseDataPath(databaseCredentialsVigiaNG);
        FileService.updateLocalFiles(databaseCredentialsVigiaNG.getName(), fileName, columns, data, databaseDataPath);
    }

}
