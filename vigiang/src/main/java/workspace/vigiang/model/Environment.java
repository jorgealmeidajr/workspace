package workspace.vigiang.model;

import workspace.vigiang.dao.OracleVigiaNgDAO;
import workspace.vigiang.dao.PostgresVigiaNgDAO;
import workspace.vigiang.dao.VigiaNgDAO;

import java.util.HashMap;
import java.util.Map;

public enum Environment {

    ALGAR(Database.ORACLE) {
        @Override
        public Map<String, String> getDatabaseCredentials() {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "VIGIANG_ALGAR");
            credentials.put("password", "VIGIANG_ALGAR-sun");
            credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
            return credentials;
        }
    },

    CLARO(Database.ORACLE) {
        @Override
        public Map<String, String> getDatabaseCredentials() {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "VIGIANG_CLARO");
            credentials.put("password", "VIGIANG_CLARO-sun");
            credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
            return credentials;
        }
    },

    LIGGA(Database.ORACLE) {
        @Override
        public Map<String, String> getDatabaseCredentials() {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "VIGIANG_LIGGA");
            credentials.put("password", "VIGIANG_LIGGA-sun");
            credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
            return credentials;
        }
    },

    OI(Database.ORACLE) {
        @Override
        public Map<String, String> getDatabaseCredentials() {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "VIGIANG_OI");
            credentials.put("password", "VIGIANG_OI-sun");
            credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
            return credentials;
        }
    },

    SKY(Database.ORACLE) {
        @Override
        public Map<String, String> getDatabaseCredentials() {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "VIGIANG_SKY");
            credentials.put("password", "VIGIANG_SKY-sun");
            credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
            return credentials;
        }
    },

    SURF(Database.POSTGRES) {
        @Override
        public Map<String, String> getDatabaseCredentials() {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "vigiang");
            credentials.put("password", "vigiang-sun");
            credentials.put("url", "jdbc:postgresql://10.50.153.19:5432/vigiang_surf");
            return credentials;
        }
    },

    TIM(Database.ORACLE) {
        @Override
        public Map<String, String> getDatabaseCredentials() {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "VIGIANG_TIM");
            credentials.put("password", "VIGIANG_TIM-sun");
            credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
            return credentials;
        }
    },

    VIVO(Database.ORACLE) {
        @Override
        public Map<String, String> getDatabaseCredentials() {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "VIGIANG_VIVO");
            credentials.put("password", "VIGIANG_VIVO-sun");
            credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
            return credentials;
        }
    },

    VTAL(Database.ORACLE) {
        @Override
        public Map<String, String> getDatabaseCredentials() {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "VIGIANG_VTAL");
            credentials.put("password", "VIGIANG_VTAL-sun");
            credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
            return credentials;
        }
    },

    WOM(Database.ORACLE) {
        @Override
        public Map<String, String> getDatabaseCredentials() {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "VIGIANG_WOM");
            credentials.put("password", "VIGIANG_WOM-sun");
            credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
            return credentials;
        }
    },

    WOM2(Database.POSTGRES) {
        @Override
        public Map<String, String> getDatabaseCredentials() {
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", "vigiang");
            credentials.put("password", "vigiang-sun");
            credentials.put("url", "jdbc:postgresql://10.50.153.19:5432/vigiang_wom");
            return credentials;
        }
    };

    private final Database database;

    Environment(Database database) {
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    public VigiaNgDAO getVigiaNgDAO() {
        if (Database.ORACLE.equals(database)) return new OracleVigiaNgDAO();
        if (Database.POSTGRES.equals(database)) return new PostgresVigiaNgDAO();
        return null;
    }

    abstract public Map<String, String> getDatabaseCredentials();

    public enum Database {
        ORACLE, POSTGRES
    }

}