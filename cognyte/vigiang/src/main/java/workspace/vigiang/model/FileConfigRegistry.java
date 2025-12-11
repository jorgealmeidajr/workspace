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
