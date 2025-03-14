package workspace.vigiang.model;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class CredentialsOracle {

    public static Map<String, String> getCredentials(Environment environment) {
        switch (environment) {
            case ALGAR: return getAlgarCredentials();
            case CLARO: return getClaroCredentials();
            case LIGGA: return getLiggaCredentials();
            case OI: return getOiCredentials();
            case SKY: return getSkyCredentials();
            case SURF: return getSurfCredentials();
            case TIM: return getTimCredentials();
            case VIVO: return getVivoCredentials();
            case VTAL: return getVtalCredentials();
            case WOM: return getWomCredentials();
            case WOM2: return getWomCredentials2();
        }
        return new HashMap<>();
    }

    private static Map<String, String> getSurfCredentials() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "vigiang");
        credentials.put("password", "vigiang-sun");
        credentials.put("url", "jdbc:postgresql://10.50.153.19:5432/vigiang_surf");
        return credentials;
    }

    private static Map<String, String> getWomCredentials2() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "vigiang");
        credentials.put("password", "vigiang-sun");
        credentials.put("url", "jdbc:postgresql://10.50.153.19:5432/vigiang_wom");
        return credentials;
    }

    private static Map<String, String> getAlgarCredentials() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "VIGIANG_ALGAR");
        credentials.put("password", "VIGIANG_ALGAR-sun");
        credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
        return credentials;
    }

    private static Map<String, String> getClaroCredentials() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "VIGIANG_CLARO");
        credentials.put("password", "VIGIANG_CLARO-sun");
        credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
        return credentials;
    }

    private static Map<String, String> getLiggaCredentials() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "VIGIANG_LIGGA");
        credentials.put("password", "VIGIANG_LIGGA-sun");
        credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
        return credentials;
    }

    private static Map<String, String> getOiCredentials() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "VIGIANG_OI");
        credentials.put("password", "VIGIANG_OI-sun");
        credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
        return credentials;
    }

    private static Map<String, String> getSkyCredentials() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "VIGIANG_SKY");
        credentials.put("password", "VIGIANG_SKY-sun");
        credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
        return credentials;
    }

    private static Map<String, String> getTimCredentials() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "VIGIANG_TIM");
        credentials.put("password", "VIGIANG_TIM-sun");
        credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
        return credentials;
    }

    private static Map<String, String> getVivoCredentials() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "VIGIANG_VIVO");
        credentials.put("password", "VIGIANG_VIVO-sun");
        credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
        return credentials;
    }

    private static Map<String, String> getVtalCredentials() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "VIGIANG_VTAL");
        credentials.put("password", "VIGIANG_VTAL-sun");
        credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
        return credentials;
    }

    private static Map<String, String> getWomCredentials() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "VIGIANG_WOM");
        credentials.put("password", "VIGIANG_WOM-sun");
        credentials.put("url", "jdbc:oracle:thin:@10.50.150.236:1521:ORADEV");
        return credentials;
    }

}
