package workspace.vigiang.model;

import java.util.HashMap;
import java.util.Map;

public class CredentialsSSH {

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
        }
        return new HashMap<>();
    }

    private static Map<String, String> getDefaultCredentials() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "root");
        credentials.put("password", "jacare1");
        credentials.put("port", "22");
        return credentials;
    }

    private static Map<String, String> getAlgarCredentials() {
        Map<String, String> credentials = getDefaultCredentials();
        credentials.put("host", "10.50.240.161");
        return credentials;
    }

    private static Map<String, String> getClaroCredentials() {
        Map<String, String> credentials = getDefaultCredentials();
        credentials.put("host", "10.50.150.101");
        return credentials;
    }

    private static Map<String, String> getLiggaCredentials() {
        Map<String, String> credentials = getDefaultCredentials();
        credentials.put("host", "10.50.150.77");
        return credentials;
    }

    private static Map<String, String> getOiCredentials() {
        Map<String, String> credentials = getDefaultCredentials();
        credentials.put("host", "10.50.240.148");
        return credentials;
    }

    private static Map<String, String> getSkyCredentials() {
        Map<String, String> credentials = getDefaultCredentials();
        credentials.put("host", "10.50.153.226");
        return credentials;
    }

    private static Map<String, String> getSurfCredentials() {
        Map<String, String> credentials = getDefaultCredentials();
        credentials.put("host", "10.50.150.78");
        return credentials;
    }

    private static Map<String, String> getTimCredentials() {
        Map<String, String> credentials = getDefaultCredentials();
        credentials.put("host", "10.50.150.59");
        return credentials;
    }

    private static Map<String, String> getVivoCredentials() {
        Map<String, String> credentials = getDefaultCredentials();
        credentials.put("host", "10.50.240.124");
        return credentials;
    }

    private static Map<String, String> getVtalCredentials() {
        Map<String, String> credentials = getDefaultCredentials();
        credentials.put("host", "10.50.153.249");
        return credentials;
    }

    private static Map<String, String> getWomCredentials() {
        Map<String, String> credentials = getDefaultCredentials();
        credentials.put("host", "10.50.150.57");
        return credentials;
    }

}
