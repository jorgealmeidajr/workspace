package workspace.commons.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DockerComposeServiceTest {

    @Nested
    class ParseDockerCompose {
        @Test
        void shouldParseDockerComposeWithMapEnvironment() {
            String dockerCompose = """
                    services:
                      web:
                        environment:
                          DEBUG: 'true'
                          PORT: '8080'
                    """;

            String result = DockerComposeService.parseDockerCompose(dockerCompose);

            assertThat(result).contains("web:").contains("DEBUG=true").contains("PORT=8080");
        }

        @Test
        void shouldParseDockerComposeWithListEnvironment() {
            String dockerCompose = """
                    services:
                      api:
                        environment:
                          - DATABASE_URL=postgres://localhost
                          - API_KEY=secret123
                    """;

            String result = DockerComposeService.parseDockerCompose(dockerCompose);

            assertThat(result).contains("api:").contains("DATABASE_URL=postgres://localhost").contains("API_KEY=secret123");
        }

        @Test
        void shouldHandleMultipleServices() {
            String dockerCompose = """
                    services:
                      web:
                        environment:
                          PORT: '3000'
                      db:
                        environment:
                          PASSWORD: 'secret'
                    """;

            String result = DockerComposeService.parseDockerCompose(dockerCompose);

            assertThat(result).contains("db:").contains("PASSWORD=secret").contains("web:").contains("PORT=3000");
        }

        @Test
        void shouldReturnBlankStringWhenNoServicesSection() {
            String dockerCompose = """
                    version: '3'
                    """;

            String result = DockerComposeService.parseDockerCompose(dockerCompose);

            assertThat(result).isBlank();
        }

        @Test
        void shouldSkipServicesWithoutEnvironment() {
            String dockerCompose = """
                    services:
                      web:
                        image: nginx
                      api:
                        environment:
                          DEBUG: 'true'
                    """;

            String result = DockerComposeService.parseDockerCompose(dockerCompose);

            assertThat(result).contains("api:").contains("DEBUG=true").doesNotContain("web:");
        }

        @Test
        void shouldHandleEmptyYaml() {
            String dockerCompose = "";

            String result = DockerComposeService.parseDockerCompose(dockerCompose);

            assertThat(result).isBlank();
        }

        @Test
        void shouldSortServiceNamesAlphabetically() {
            String dockerCompose = """
                    services:
                      zebra:
                        environment:
                          VAR: '1'
                      alpha:
                        environment:
                          VAR: '2'
                    """;

            String result = DockerComposeService.parseDockerCompose(dockerCompose);

            int alphaIndex = result.indexOf("alpha:");
            int zebraIndex = result.indexOf("zebra:");
            assertThat(alphaIndex).isLessThan(zebraIndex);
        }

        @Test
        void shouldSortEnvironmentVariablesAlphabetically() {
            String dockerCompose = """
                    services:
                      web:
                        environment:
                          ZEBRA: '1'
                          ALPHA: '2'
                    """;

            String result = DockerComposeService.parseDockerCompose(dockerCompose);

            int alphaIndex = result.indexOf("ALPHA=");
            int zebraIndex = result.indexOf("ZEBRA=");
            assertThat(alphaIndex).isLessThan(zebraIndex);
        }

        @Test
        void shouldHandleNullEnvironmentValues() {
            String dockerCompose = "services:\n" +
                    "  web:\n" +
                    "    environment:\n" +
                    "      EMPTY_VAR:\n";

            String result = DockerComposeService.parseDockerCompose(dockerCompose);

            assertThat(result).contains("web:").contains("EMPTY_VAR=");
        }

        @Test
        void shouldHandleNumericEnvironmentValues() {
            String dockerCompose = "services:\n" +
                    "  web:\n" +
                    "    environment:\n" +
                    "      PORT: 8080\n" +
                    "      REPLICAS: 3\n";

            String result = DockerComposeService.parseDockerCompose(dockerCompose);

            assertThat(result).contains("PORT=8080").contains("REPLICAS=3");
        }

        @Test
        void shouldHandleMixedMapAndListEnvironment() {
            String dockerCompose = "services:\n" +
                    "  web:\n" +
                    "    environment:\n" +
                    "      VAR1: value1\n" +
                    "  api:\n" +
                    "    environment:\n" +
                    "      - VAR2=value2\n";

            String result = DockerComposeService.parseDockerCompose(dockerCompose);

            assertThat(result).contains("api:").contains("VAR2=value2").contains("web:").contains("VAR1=value1");
        }

        @Test
        void shouldHandleServiceWithoutEnvironmentInMultipleServices() {
            String dockerCompose = "services:\n" +
                    "  web:\n" +
                    "    image: nginx\n" +
                    "  api:\n" +
                    "    environment:\n" +
                    "      DEBUG: 'true'\n" +
                    "  db:\n" +
                    "    image: postgres\n";

            String result = DockerComposeService.parseDockerCompose(dockerCompose);

            assertThat(result).contains("api:").contains("DEBUG=true");
            assertThat(result).doesNotContain("web:").doesNotContain("db:");
        }

        @Test
        void shouldHandleComplexYamlStructure() {
            String dockerCompose = "version: '3.8'\n" +
                    "services:\n" +
                    "  web:\n" +
                    "    image: nginx:latest\n" +
                    "    ports:\n" +
                    "      - '80:80'\n" +
                    "    environment:\n" +
                    "      NGINX_HOST: example.com\n" +
                    "      NGINX_PORT: 80\n";

            String result = DockerComposeService.parseDockerCompose(dockerCompose);

            assertThat(result).contains("web:").contains("NGINX_HOST=example.com").contains("NGINX_PORT=80");
        }

        @Test
        void shouldReturnStringWithNewlineAtEnd() {
            String dockerCompose = "services:\n" +
                    "  web:\n" +
                    "    environment:\n" +
                    "      VAR: value\n";

            String result = DockerComposeService.parseDockerCompose(dockerCompose);

            assertThat(result).endsWith("\n");
        }

        @Test
        void shouldHandleEmptyEnvironmentMap() {
            String dockerCompose = """
                    services:
                      web:
                        environment:
                    """;

            String result = DockerComposeService.parseDockerCompose(dockerCompose);

            assertThat(result).isEqualTo("\n");
        }

        @Test
        void shouldHandleSpecialCharactersInValues() {
            String dockerCompose = "services:\n" +
                    "  web:\n" +
                    "    environment:\n" +
                    "      DATABASE_URL: 'postgres://user:pass@localhost:5432/db'\n" +
                    "      SPECIAL_CHARS: 'value-with-dashes_and_underscores'\n";

            String result = DockerComposeService.parseDockerCompose(dockerCompose);

            assertThat(result).contains("DATABASE_URL=postgres://user:pass@localhost:5432/db")
                    .contains("SPECIAL_CHARS=value-with-dashes_and_underscores");
        }
    }

}
