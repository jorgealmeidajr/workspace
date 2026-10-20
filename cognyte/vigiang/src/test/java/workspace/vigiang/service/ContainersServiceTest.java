package workspace.vigiang.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import workspace.vigiang.model.DockerContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class ContainersServiceTest {

    @Nested
    class GetContainersJsonContent {
        @Test
        void testSingleControlContainer() throws JsonProcessingException {
            List<DockerContainer> data = List.of(new DockerContainer("auth-service", "1.0.0"));

            String json = ContainersService.getContainersJsonContent(data);

            JsonNode expected = new ObjectMapper().readTree("""
                    {
                      "back": {
                        "control": [
                          { "name": "auth-service", "version": "1.0.0" }
                        ]
                      }
                    }""");

            assertThat(new ObjectMapper().readTree(json)).isEqualTo(expected);
        }

        @Test
        void testAllGroups() throws JsonProcessingException {
            List<DockerContainer> data = List.of(
                    new DockerContainer("auth-service", "1.0.0"),
                    new DockerContainer("carrier-service", "2.0.0"),
                    new DockerContainer("block-service", "3.0.0"),
                    new DockerContainer("claro-block-service", "4.0.0"),
                    new DockerContainer("vigia_ng_webviewer", "5.0.0"));

            String json = ContainersService.getContainersJsonContent(data);

            assertThat(json).contains("\"control\"", "\"main\"", "\"optional\"", "\"services\"", "\"front\"");
            assertThat(json).contains("\"claro-block-service\"", "\"vigia_ng_webviewer\"");
        }

        @Test
        void testLongestProjectNameWins() throws JsonProcessingException {
            List<DockerContainer> data = List.of(new DockerContainer("claro-block-service", "1.2.3"));

            JsonNode json = new ObjectMapper().readTree(ContainersService.getContainersJsonContent(data));

            assertThat(json.at("/back/optional").isMissingNode()).isTrue();
            assertThat(json.at("/back/services/0/name").asText()).isEqualTo("claro-block-service");
            assertThat(json.at("/back/services/0/version").asText()).isEqualTo("1.2.3");
        }

        @Test
        void testContainerNameIsNormalizedToProjectName() throws JsonProcessingException {
            List<DockerContainer> data = List.of(new DockerContainer("vigiang_auth-service_1", "1.0.0"));

            JsonNode json = new ObjectMapper().readTree(ContainersService.getContainersJsonContent(data));

            assertThat(json.at("/back/control/0/name").asText()).isEqualTo("auth-service");
        }

        @Test
        void testUnknownContainerIsSkipped() throws JsonProcessingException {
            List<DockerContainer> data = List.of(new DockerContainer("some-unknown-container", "1.0.0"));

            String json = ContainersService.getContainersJsonContent(data);

            assertThat(json.replace(" ", "")).isEqualTo("{}");
        }

        @Test
        void testIgnoredContainerIsSkipped() throws JsonProcessingException {
            List<DockerContainer> data = List.of(
                    new DockerContainer("kafka", "1.0.0"),
                    new DockerContainer("redis", "2.0.0"),
                    new DockerContainer("zookeeper", "3.0.0"));

            String json = ContainersService.getContainersJsonContent(data);

            assertThat(json.replace(" ", "")).isEqualTo("{}");
        }

        @Test
        void testEmptyGroupsAreOmitted() throws JsonProcessingException {
            List<DockerContainer> data = List.of(new DockerContainer("webviewer", "1.0.0"));

            JsonNode json = new ObjectMapper().readTree(ContainersService.getContainersJsonContent(data));

            assertThat(json.has("back")).isFalse();
            assertThat(json.at("/front/0/name").asText()).isEqualTo("webviewer");
        }

        @Test
        void testBackGroupsAreSortedByName() throws JsonProcessingException {
            List<DockerContainer> data = List.of(
                    new DockerContainer("user-service", "1.0.0"),
                    new DockerContainer("auth-service", "2.0.0"),
                    new DockerContainer("config-server", "3.0.0"));

            JsonNode json = new ObjectMapper().readTree(ContainersService.getContainersJsonContent(data));

            assertThat(json.at("/back/control/0/name").asText()).isEqualTo("auth-service");
            assertThat(json.at("/back/control/1/name").asText()).isEqualTo("config-server");
            assertThat(json.at("/back/control/2/name").asText()).isEqualTo("user-service");
        }

        @Test
        void testEmptyInput() throws JsonProcessingException {
            String json = ContainersService.getContainersJsonContent(List.of());

            assertThat(json.replace(" ", "")).isEqualTo("{}");
        }
    }

}
