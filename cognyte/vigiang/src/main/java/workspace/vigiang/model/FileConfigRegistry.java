package workspace.vigiang.model;

import workspace.commons.model.Database;

import java.util.Map;

public class FileConfigRegistry {

    public static final Map<String, Map<Database, FileConfig>> CONFIGS = Map.of(
        "feature", Map.of(
            Database.ORACLE, new FileConfig("CFG_NG_FEATURE", new String[] { "ID_FEATURE", "ID_STATUS", "ID_DESCRICAO" }),
            Database.POSTGRES, new FileConfig("conf.feature", new String[] { "feature", "status", "description" })
        ),
        "configuration", Map.of(
            Database.ORACLE, new FileConfig("CFG_NG_SITE", new String[] { "CD_OPERADORA", "ID_PARAMETRO", "DE_PARAMETRO", "VL_PARAMETRO" }),
            Database.POSTGRES, new FileConfig("conf.site", new String[] { "carrier_id", "parameter_id", "parameter_description", "value" })
        ),
        "privilege", Map.of(
            Database.ORACLE, new FileConfig("SEG_PRIVILEGIO", new String[] { "CD_PRIVILEGIO", "ID_PRIVILEGIO", "NM_PRIVILEGIO" }),
            Database.POSTGRES, new FileConfig("sec.privilege", new String[] { "id", "privilegeid", "name" })
        ),
        "profile", Map.of(
            Database.ORACLE, new FileConfig("SEG_PERFIL_PRIVILEGIO", new String[] { "CD_OPERADORA", "NM_PERFIL", "NM_PRIVILEGIO" }),
            Database.POSTGRES, new FileConfig("sec.profile_privilege", new String[] { "carrier_id", "profile_name", "privilege_name" })
        ),
        "filterQuery", Map.of(
            Database.ORACLE, new FileConfig("CFG_NG_FILTERQUERY", new String[] { "MODULE", "LABEL", "VALUE" }),
            Database.POSTGRES, new FileConfig("conf.filterquery", new String[] { "module", "label", "value" })
        ),
        "zoneInterception", Map.of(
            Database.ORACLE, new FileConfig("CFG_TP_ZONA_TP_VL_ITC", new String[] {
                "CD_OPERADORA", "NM_OPERADORA", "NM_ZONA_MONIT", "NM_TIPO_VALOR_INTERCEPTADO", "SN_VISIVEL_CAD_ITC", "SN_VISIVEL_LOTE", "NM_REGRAS"
            }),
            Database.POSTGRES, new FileConfig("conf.tp_zone_tp_vl_itc", new String[] {
                "carrier_id", "carrier_name", "network_element_type_name", "target_type_name", "itc_form_visible", "visible", "rules"
            })
        ),
        "validationRule", Map.of(
            Database.ORACLE, new FileConfig("CFG_NG_VALIDATRULES", new String[] { "CD_OPERADORA", "NM_OPERADORA", "MODULO", "VALID_RULES" }),
            Database.POSTGRES, new FileConfig("conf.validatrules", new String[] { "carrier_id", "carrier_name", "module", "valid_rules" })
        )
    );

    public static FileConfig getConfig(String type, Database db) {
        Map<Database, FileConfig> byDb = CONFIGS.get(type);
        if (byDb == null) throw new IllegalArgumentException("Unknown file type: " + type);
        FileConfig config = byDb.get(db);
        if (config == null) throw new IllegalArgumentException("Unknown database: " + db);
        return config;
    }

}
