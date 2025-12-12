package workspace.vigiang.model;

import workspace.commons.model.Database;

import java.util.HashMap;
import java.util.Map;

public class FileConfigRegistry {

    private static final Map<String, Map<Database, FileConfig>> CONFIGS = getConfigurations();

    private static Map<String, Map<Database, FileConfig>> getConfigurations() {
        var configurations = new HashMap<String, Map<Database, FileConfig>>();

        configurations.put("feature", Map.of(
            Database.ORACLE, new FileConfig("CFG_NG_FEATURE", new String[] { "ID_FEATURE", "ID_STATUS", "ID_DESCRICAO" }),
            Database.POSTGRES, new FileConfig("conf.feature", new String[] { "feature", "status", "description" })
        ));

        configurations.put("configuration", Map.of(
            Database.ORACLE, new FileConfig("CFG_NG_SITE", new String[] { "CD_OPERADORA", "ID_PARAMETRO", "DE_PARAMETRO", "VL_PARAMETRO" }),
            Database.POSTGRES, new FileConfig("conf.site", new String[] { "carrier_id", "parameter_id", "parameter_description", "value" })
        ));

        configurations.put("privilege", Map.of(
            Database.ORACLE, new FileConfig("SEG_PRIVILEGIO", new String[] { "CD_PRIVILEGIO", "ID_PRIVILEGIO", "NM_PRIVILEGIO" }),
            Database.POSTGRES, new FileConfig("sec.privilege", new String[] { "id", "privilegeid", "name" })
        ));

        configurations.put("profile", Map.of(
            Database.ORACLE, new FileConfig("SEG_PERFIL_PRIVILEGIO", new String[] { "CD_OPERADORA", "NM_PERFIL", "NM_PRIVILEGIO" }),
            Database.POSTGRES, new FileConfig("sec.profile_privilege", new String[] { "carrier_id", "profile_name", "privilege_name" })
        ));

        configurations.put("filterQuery", Map.of(
            Database.ORACLE, new FileConfig("CFG_NG_FILTERQUERY", new String[] { "MODULE", "LABEL", "VALUE" }),
            Database.POSTGRES, new FileConfig("conf.filterquery", new String[] { "module", "label", "value" })
        ));

        configurations.put("zoneInterception", Map.of(
            Database.ORACLE, new FileConfig("CFG_TP_ZONA_TP_VL_ITC", new String[] {
                "CD_OPERADORA", "NM_OPERADORA", "NM_ZONA_MONIT", "NM_TIPO_VALOR_INTERCEPTADO", "SN_VISIVEL_CAD_ITC", "SN_VISIVEL_LOTE", "NM_REGRAS"
            }),
            Database.POSTGRES, new FileConfig("conf.tp_zone_tp_vl_itc", new String[] {
                "carrier_id", "carrier_name", "network_element_type_name", "target_type_name", "itc_form_visible", "visible", "rules"
            })
        ));

        configurations.put("validationRule", Map.of(
            Database.ORACLE, new FileConfig("CFG_NG_VALIDATRULES", new String[] { "CD_OPERADORA", "NM_OPERADORA", "MODULO", "VALID_RULES" }),
            Database.POSTGRES, new FileConfig("conf.validatrules", new String[] { "carrier_id", "carrier_name", "module", "valid_rules" })
        ));

        configurations.put("carrier", Map.of(
            Database.ORACLE, new FileConfig("CFG_OPERADORA", new String[] {
                "CD_OPERADORA", "NM_OPERADORA",
                "ID_SMTP_HOST", "ID_SMTP_USER", "ID_SMPT_PASSWORD", "ID_SMPT_ACCOUNT", "ID_SMTP_PORT",
                "DE_COMENTARIO", "SN_MULTIOPERADORA", "ID_REGEX", "DS_LOCAL_IMAGENS", "DS_REGEX", "ID_API_KEY_MAPS", "SN_TOKEN"
            }),
            Database.POSTGRES, new FileConfig("conf.carrier", new String[] {
                "id", "name",
                "smtp_host", "smtp_user", "smpt_password", "smpt_account", "smtp_port",
                "comments", "muti_carrier", "regex", "local_images", "ds_regex", "api_key_maps", "token"
            })
        ));

        configurations.put("zone", Map.of(
            Database.ORACLE, new FileConfig("CFG_ZONA_MONIT", new String[] {
                "CD_OPERADORA", "NM_OPERADORA",
                "CD_ZONA_MONIT", "NM_ZONA_MONIT", "DE_COMENTARIOS"
            }),
            Database.POSTGRES, new FileConfig("conf.zone_monit", new String[] {
                "carrier_id", "carrier_name",
                "zone_monit_id", "zone_monit_name", "comments", "active"
            })
        ));

        configurations.put("qdsValidationRule", Map.of(
            Database.ORACLE, new FileConfig("CFG_TIPO_NUMERO_QDS", new String[] {
                "ID_TIPO_NUMERO_QDS", "NM_CHAVE", "TP_CONSULTA", "SN_VOUCHER_DATE", "VALID_RULES"
            })
        ));

        configurations.put("report", Map.of(
            Database.ORACLE, new FileConfig("CFG_RELATORIO", new String[] {
                "CD_RELATORIO", "ID_RELATORIO", "TP_RELATORIO", "CD_OPERADORA", "NM_OPERADORA"
            }),
            Database.POSTGRES, new FileConfig("conf.report", new String[] {
                "id", "report_id", "report_type", "carrier_id", "carrier_name"
            })
        ));

        configurations.put("configReport", Map.of(
            Database.ORACLE, new FileConfig("CFG_SITE_RELATORIO", new String[] {
                "CD_OPERADORA", "ID_PARAMETRO", "DE_PARAMETRO", "VL_PARAMETRO", "CD_RELATORIO", "ID_RELATORIO"
            }),
            Database.POSTGRES, new FileConfig("conf.site_report", new String[] {
                "carrier_id", "parameter_id", "parameter_description", "value", "id", "report_id"
            })
        ));

        configurations.put("emailTemplate", Map.of(
            Database.ORACLE, new FileConfig("CFG_EMAIL_SERVICOS", new String[] {
                "CD_OPERADORA", "NM_OPERADORA", "ID_TIPO_SERVICO", "DE_ASSUNTO", "DE_NOME", "DE_NOME_ARQUIVO", "DE_REMETENTE", "DE_DESTINATARIO"
            }),
            Database.POSTGRES, new FileConfig("conf.service_email", new String[] {
                "carrier_id", "carrier_name", "service_type", "email_subject", "service_name", "attach_name", "email_from", "email_to"
            })
        ));

        return configurations;
    }

    public static FileConfig getConfig(String type, Database db) {
        Map<Database, FileConfig> byDb = CONFIGS.get(type);
        if (byDb == null) throw new IllegalArgumentException("Unknown file type: " + type);
        FileConfig config = byDb.get(db);
        if (config == null) throw new IllegalArgumentException("Unknown database: " + db);
        return config;
    }

}
