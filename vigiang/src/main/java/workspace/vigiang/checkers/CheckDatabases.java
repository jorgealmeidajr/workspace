package workspace.vigiang.checkers;

import workspace.vigiang.model.Configuration;
import workspace.vigiang.model.Feature;
import workspace.vigiang.service.EnvironmentService;
import workspace.vigiang.service.FileService;
import workspace.vigiang.model.Environment;
import workspace.vigiang.dao.VigiaNgDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class CheckDatabases {

    public static void main(String[] args) {
        System.out.println("## START checking all environment databases\n");
        try {
            for (Environment env : EnvironmentService.getEnvironments()) {
                VigiaNgDAO dao = EnvironmentService.getVigiaNgDAO(env);
                System.out.println(env.getName() + ":");

                updateLocalFeatureFiles(env, dao);
                updateLocalConfigurationFiles(env, dao);

                updateLocalModuleFiles(env, dao);
                updateLocalPrivilegeFiles(env, dao);
                updateLocalProfileFiles(env, dao);

                updateLocalFilterQueryFiles(env, dao);
                updateLocalZoneInterceptionFiles(env, dao);
                updateLocalValidationRuleFiles(env, dao);
                updateLocalQdsValidationRuleFiles(env, dao);
                updateLocalCarriersFiles(env, dao);
                updateLocalZonesFiles(env, dao);

                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n## END checking all environment databases.");
    }

    private static void updateLocalFeatureFiles(Environment env, VigiaNgDAO dao) throws SQLException, IOException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_NG_FEATURE";
            columns = new String[] { "ID_FEATURE", "ID_STATUS", "ID_DESCRICAO" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.feature";
            columns = new String[] { "feature", "status", "description" };
        }

        List<String[]> data = dao.listFeatures(env).stream()
                .map(Feature::toArray)
                .collect(Collectors.toList());
        FileService.updateLocalFiles(env, fileName, columns, data);
    }

    private static void updateLocalConfigurationFiles(Environment env, VigiaNgDAO dao) throws SQLException, IOException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_NG_SITE";
            columns = new String[] { "CD_OPERADORA", "ID_PARAMETRO", "DE_PARAMETRO", "VL_PARAMETRO" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.site";
            columns = new String[] { "carrier_id", "parameter_id", "parameter_description", "value" };
        }

        List<String[]> data = dao.listConfigurationValues(env).stream()
                .map(Configuration::toArray)
                .collect(Collectors.toList());
        FileService.updateLocalFiles(env, fileName, columns, data);
    }

    private static void updateLocalModuleFiles(Environment env, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_MODULO";
            columns = new String[] { "ID_CHAVE", "ID_STATUS", "ID_TIPO" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            return;
        }

        List<String[]> data = dao.listModules(env);
        FileService.updateLocalFiles(env, fileName, columns, data);
    }

    private static void updateLocalPrivilegeFiles(Environment env, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "SEG_PRIVILEGIO";
            columns = new String[] { "CD_PRIVILEGIO", "ID_PRIVILEGIO", "NM_PRIVILEGIO" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "sec.privilege";
            columns = new String[] { "id", "privilegeid", "name" };
        }

        List<String[]> data = dao.listPrivileges(env);
        FileService.updateLocalFiles(env, fileName, columns, data);
    }

    private static void updateLocalProfileFiles(Environment env, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "SEG_PERFIL_PRIVILEGIO";
            columns = new String[] { "CD_OPERADORA", "NM_PERFIL", "NM_PRIVILEGIO", "CD_MODULO", "NM_MODULO" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "sec.profile_privilege";
            columns = new String[] { "carrier_id", "profile_name", "privilege_name", "module_id" };
        }

        List<String[]> data = dao.listProfiles(env);
        FileService.updateLocalFiles(env, fileName, columns, data);
    }

    private static void updateLocalFilterQueryFiles(Environment env, VigiaNgDAO dao) throws IOException, SQLException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_NG_FILTERQUERY";
            columns = new String[] { "MODULE", "LABEL", "VALUE" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.filterquery";
            columns = new String[] { "module", "label", "value" };
        }

        List<String[]> data = dao.listFilterQueries(env);
        FileService.updateLocalFiles(env, fileName, columns, data);
    }

    private static void updateLocalZoneInterceptionFiles(Environment env, VigiaNgDAO dao) {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_TP_ZONA_TP_VL_ITC";
            columns = new String[] {
                "CD_OPERADORA", "NM_OPERADORA",
                "NM_ZONA_MONIT", "NM_TIPO_VALOR_INTERCEPTADO",
                "SN_VISIVEL_CAD_ITC", "SN_VISIVEL_LOTE", "NM_REGRAS"
            };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.tp_zone_tp_vl_itc";
            columns = new String[] {
                "carrier_id", "carrier_name",
                "network_element_type_name", "target_type_name",
                "itc_form_visible", "visible", "rules"
            };
        }

        try {
            List<String[]> data = dao.listZoneInterceptions(env);
            FileService.updateLocalFiles(env, fileName, columns, data);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalValidationRuleFiles(Environment env, VigiaNgDAO dao) {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_NG_VALIDATRULES";
            columns = new String[] { "CD_OPERADORA", "NM_OPERADORA", "MODULO", "VALID_RULES" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.validatrules";
            columns = new String[] { "carrier_id", "carrier_name", "module", "valid_rules" };
        }

        try {
            List<String[]> data = dao.listValidationRules(env);
            FileService.updateLocalFiles(env, fileName, columns, data);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalQdsValidationRuleFiles(Environment env, VigiaNgDAO dao) {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_TIPO_NUMERO_QDS";
            columns = new String[] { "ID_TIPO_NUMERO_QDS", "NM_CHAVE", "TP_CONSULTA", "SN_VOUCHER_DATE", "VALID_RULES" };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            return;
        }

        try {
            List<String[]> data = dao.listQdsValidationRules(env);
            FileService.updateLocalFiles(env, fileName, columns, data);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void updateLocalCarriersFiles(Environment env, VigiaNgDAO dao) throws SQLException, IOException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_OPERADORA";
            columns = new String[] {
                "CD_OPERADORA", "NM_OPERADORA",
                "ID_SMTP_HOST", "ID_SMTP_USER", "ID_SMPT_PASSWORD", "ID_SMPT_ACCOUNT", "ID_SMTP_PORT",
                "DE_COMENTARIO", "SN_MULTIOPERADORA", "ID_REGEX", "DS_LOCAL_IMAGENS",  "DS_REGEX", "ID_API_KEY_MAPS", "SN_TOKEN",
                "IM_LOGO", "IM_LOGO_FOOTER"
            };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.carrier";
            columns = new String[] {
                "id", "name",
                "smtp_host", "smtp_user", "smpt_password", "smpt_account", "smtp_port",
                "comments", "muti_carrier", "regex", "local_images", "ds_regex", "api_key_maps", "token",
                "im_logo", "im_logo_footer"
            };
        }

        List<String[]> data = dao.listCarriers(env);
        FileService.updateLocalFiles(env, fileName, columns, data);
    }

    private static void updateLocalZonesFiles(Environment env, VigiaNgDAO dao) throws SQLException, IOException {
        String fileName = null;
        String[] columns = null;
        if (Environment.Database.ORACLE.equals(env.getDatabase())) {
            fileName = "CFG_ZONA_MONIT";
            columns = new String[] {
                "CD_OPERADORA", "NM_OPERADORA",
                "CD_ZONA_MONIT", "NM_ZONA_MONIT", "DE_COMENTARIOS"//, "IN_ATIVO"
            };
        } else if (Environment.Database.POSTGRES.equals(env.getDatabase())) {
            fileName = "conf.zone_monit";
            columns = new String[] {
                "carrier_id", "carrier_name",
                "zone_monit_id", "zone_monit_name", "comments", "active"
            };
        }

        List<String[]> data = dao.listZones(env);
        FileService.updateLocalFiles(env, fileName, columns, data);
    }

}
