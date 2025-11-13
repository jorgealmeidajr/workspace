package workspace.vigiang.checkers;

import workspace.commons.model.Database;
import workspace.vigiang.model.Configuration;
import workspace.vigiang.model.Feature;
import workspace.vigiang.service.EnvironmentService;
import workspace.vigiang.service.FileService;
import workspace.vigiang.model.DatabaseCredentials;
import workspace.vigiang.dao.VigiaNgDAO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class CheckDatabases {

    public static void main(String[] args) {
        System.out.println("## START checking all environment databases\n");
        try {
            for (DatabaseCredentials databaseCredentials : EnvironmentService.getVigiangDatabases()) {
                VigiaNgDAO dao = EnvironmentService.getVigiaNgDAO(databaseCredentials);
                System.out.println(databaseCredentials.getName() + ":");

                updateLocalFeatureFiles(databaseCredentials, dao);
                updateLocalConfigurationFiles(databaseCredentials, dao);

                updateLocalPrivilegeFiles(databaseCredentials, dao);
                updateLocalProfileFiles(databaseCredentials, dao);

                updateLocalFilterQueryFiles(databaseCredentials, dao);
                updateLocalZoneInterceptionFiles(databaseCredentials, dao);
                updateLocalValidationRuleFiles(databaseCredentials, dao);
                updateLocalQdsValidationRuleFiles(databaseCredentials, dao);
                updateLocalCarriersFiles(databaseCredentials, dao);
                updateLocalZonesFiles(databaseCredentials, dao);

                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n## END checking all environment databases.");
    }

    private static void updateLocalFeatureFiles(DatabaseCredentials databaseCredentials, VigiaNgDAO dao) throws SQLException, IOException {
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
            fileName = "CFG_NG_FEATURE";
            columns = new String[] { "ID_FEATURE", "ID_STATUS", "ID_DESCRICAO" };
        } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
            fileName = "conf.feature";
            columns = new String[] { "feature", "status", "description" };
        }

        List<String[]> data = dao.listFeatures().stream()
                .map(Feature::toArray)
                .collect(Collectors.toList());
        FileService.updateLocalFiles(databaseCredentials, fileName, columns, data);
    }

    private static void updateLocalConfigurationFiles(DatabaseCredentials databaseCredentials, VigiaNgDAO dao) throws SQLException, IOException {
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
            fileName = "CFG_NG_SITE";
            columns = new String[] { "CD_OPERADORA", "ID_PARAMETRO", "DE_PARAMETRO", "VL_PARAMETRO" };
        } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
            fileName = "conf.site";
            columns = new String[] { "carrier_id", "parameter_id", "parameter_description", "value" };
        }

        List<String[]> data = dao.listConfigurationValues().stream()
                .map(Configuration::toArray)
                .collect(Collectors.toList());
        FileService.updateLocalFiles(databaseCredentials, fileName, columns, data);
    }

    private static void updateLocalPrivilegeFiles(DatabaseCredentials databaseCredentials, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
            fileName = "SEG_PRIVILEGIO";
            columns = new String[] { "CD_PRIVILEGIO", "ID_PRIVILEGIO", "NM_PRIVILEGIO" };
        } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
            fileName = "sec.privilege";
            columns = new String[] { "id", "privilegeid", "name" };
        }

        List<String[]> data = dao.listPrivileges();
        FileService.updateLocalFiles(databaseCredentials, fileName, columns, data);
    }

    private static void updateLocalProfileFiles(DatabaseCredentials databaseCredentials, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
            fileName = "SEG_PERFIL_PRIVILEGIO";
            columns = new String[] { "CD_OPERADORA", "NM_PERFIL", "NM_PRIVILEGIO" };
        } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
            fileName = "sec.profile_privilege";
            columns = new String[] { "carrier_id", "profile_name", "privilege_name" };
        }

        List<String[]> data = dao.listProfiles();
        FileService.updateLocalFiles(databaseCredentials, fileName, columns, data);
    }

    private static void updateLocalFilterQueryFiles(DatabaseCredentials databaseCredentials, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
            fileName = "CFG_NG_FILTERQUERY";
            columns = new String[] { "MODULE", "LABEL", "VALUE" };
        } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
            fileName = "conf.filterquery";
            columns = new String[] { "module", "label", "value" };
        }

        List<String[]> data = dao.listFilterQueries();
        FileService.updateLocalFiles(databaseCredentials, fileName, columns, data);
    }

    private static void updateLocalZoneInterceptionFiles(DatabaseCredentials databaseCredentials, VigiaNgDAO dao) {
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
            fileName = "CFG_TP_ZONA_TP_VL_ITC";
            columns = new String[] {
                "CD_OPERADORA", "NM_OPERADORA",
                "NM_ZONA_MONIT", "NM_TIPO_VALOR_INTERCEPTADO",
                "SN_VISIVEL_CAD_ITC", "SN_VISIVEL_LOTE", "NM_REGRAS"
            };
        } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
            fileName = "conf.tp_zone_tp_vl_itc";
            columns = new String[] {
                "carrier_id", "carrier_name",
                "network_element_type_name", "target_type_name",
                "itc_form_visible", "visible", "rules"
            };
        }

        try {
            List<String[]> data = dao.listZoneInterceptions();
            FileService.updateLocalFiles(databaseCredentials, fileName, columns, data);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalValidationRuleFiles(DatabaseCredentials databaseCredentials, VigiaNgDAO dao) {
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
            fileName = "CFG_NG_VALIDATRULES";
            columns = new String[] { "CD_OPERADORA", "NM_OPERADORA", "MODULO", "VALID_RULES" };
        } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
            fileName = "conf.validatrules";
            columns = new String[] { "carrier_id", "carrier_name", "module", "valid_rules" };
        }

        try {
            List<String[]> data = dao.listValidationRules();
            FileService.updateLocalFiles(databaseCredentials, fileName, columns, data);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalQdsValidationRuleFiles(DatabaseCredentials databaseCredentials, VigiaNgDAO dao) {
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
            fileName = "CFG_TIPO_NUMERO_QDS";
            columns = new String[] { "ID_TIPO_NUMERO_QDS", "NM_CHAVE", "TP_CONSULTA", "SN_VOUCHER_DATE", "VALID_RULES" };
        } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
            return;
        }

        try {
            List<String[]> data = dao.listQdsValidationRules();
            FileService.updateLocalFiles(databaseCredentials, fileName, columns, data);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalCarriersFiles(DatabaseCredentials databaseCredentials, VigiaNgDAO dao) throws SQLException, IOException {
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
            fileName = "CFG_OPERADORA";
            columns = new String[] {
                "CD_OPERADORA", "NM_OPERADORA",
                "ID_SMTP_HOST", "ID_SMTP_USER", "ID_SMPT_PASSWORD", "ID_SMPT_ACCOUNT", "ID_SMTP_PORT",
                "DE_COMENTARIO", "SN_MULTIOPERADORA", "ID_REGEX", "DS_LOCAL_IMAGENS",  "DS_REGEX", "ID_API_KEY_MAPS", "SN_TOKEN"
            };
        } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
            fileName = "conf.carrier";
            columns = new String[] {
                "id", "name",
                "smtp_host", "smtp_user", "smpt_password", "smpt_account", "smtp_port",
                "comments", "muti_carrier", "regex", "local_images", "ds_regex", "api_key_maps", "token"
            };
        }

        List<String[]> data = dao.listCarriers();
        FileService.updateLocalFiles(databaseCredentials, fileName, columns, data);

        writeLogos(databaseCredentials, data);
    }

    private static void writeLogos(DatabaseCredentials databaseCredentials, List<String[]> data) throws IOException {
        Path databaseDataPath = EnvironmentService.getDatabasePath(databaseCredentials);
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
            FileService.writeLogo(logosPath, carrier);
        }
    }

    private static void updateLocalZonesFiles(DatabaseCredentials databaseCredentials, VigiaNgDAO dao) throws SQLException, IOException {
        String fileName = null;
        String[] columns = null;
        if (Database.ORACLE.equals(databaseCredentials.getDatabase())) {
            fileName = "CFG_ZONA_MONIT";
            columns = new String[] {
                "CD_OPERADORA", "NM_OPERADORA",
                "CD_ZONA_MONIT", "NM_ZONA_MONIT", "DE_COMENTARIOS"//, "IN_ATIVO"
            };
        } else if (Database.POSTGRES.equals(databaseCredentials.getDatabase())) {
            fileName = "conf.zone_monit";
            columns = new String[] {
                "carrier_id", "carrier_name",
                "zone_monit_id", "zone_monit_name", "comments", "active"
            };
        }

        List<String[]> data = dao.listZones();
        FileService.updateLocalFiles(databaseCredentials, fileName, columns, data);
    }

}
