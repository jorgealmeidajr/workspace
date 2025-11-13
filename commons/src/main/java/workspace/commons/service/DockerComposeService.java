package workspace.commons.service;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class DockerComposeService {

    public static String parseDockerCompose(String fileContent) {
        StringBuilder result = new StringBuilder();
        try {
            InputStream inputStream = new java.io.ByteArrayInputStream(fileContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(inputStream);

            if (data == null) {
                System.err.println("Error: Could not parse the YAML file or file is empty.");
                return result.toString();
            }

            Object servicesObj = data.get("services");
            if (servicesObj == null) {
                System.err.println("Error: No 'services' section found in the docker-compose file.");
                return result.toString();
            }

            if (!(servicesObj instanceof Map)) {
                System.err.println("Error: 'services' section is not in the expected format.");
                return result.toString();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> services = (Map<String, Object>) servicesObj;
            List<String> serviceNames = new ArrayList<>(services.keySet());
            Collections.sort(serviceNames);

            for (String serviceName : serviceNames) {
                Object serviceConfigObj = services.get(serviceName);
                if (!(serviceConfigObj instanceof Map)) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> serviceConfig = (Map<String, Object>) serviceConfigObj;

                Object environmentObj = serviceConfig.get("environment");
                if (environmentObj != null) {
                    result.append(serviceName).append(":\n");
                    result.append(parseEnvironmentVariables(environmentObj));
                    result.append("\n");
                }
            }

            inputStream.close();

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing YAML: " + e.getMessage());
        }
        return result.toString().trim() + "\n";
    }

    private static String parseEnvironmentVariables(Object environmentObj) {
        StringBuilder result = new StringBuilder();

        if (environmentObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> envMap = (Map<String, Object>) environmentObj;
            List<String> variables = new ArrayList<>(envMap.keySet());
            Collections.sort(variables);

            for (String variable : variables) {
                Object value = envMap.get(variable);
                String valueStr = (value != null) ? value.toString() : "";
                result.append("  ").append(variable).append("=").append(valueStr).append("\n");
            }
        } else if (environmentObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> envList = (List<String>) environmentObj;
            Collections.sort(envList);

            for (String envVar : envList) {
                result.append("  ").append(envVar).append("\n");
            }
        }

        return result.toString();
    }

}
