package workspace.vigiang.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigServiceTest {

    @Test
    @DisplayName("keeps backslashes in windows paths")
    void shouldKeepWindowsPathsIntact() {
        Map<String, String> values = ConfigService.parse(List.of(
                "WORK_PATH=C:\\work",
                "USER_BASHRC_PATH=C:\\Users\\jjunior\\.bashrc"
        ));

        assertThat(values)
                .containsEntry("WORK_PATH", "C:\\work")
                .containsEntry("USER_BASHRC_PATH", "C:\\Users\\jjunior\\.bashrc");
    }

    @Test
    @DisplayName("ignores blank lines and comments")
    void shouldIgnoreBlankLinesAndComments() {
        Map<String, String> values = ConfigService.parse(List.of(
                "",
                "   ",
                "# a comment",
                "   # an indented comment",
                "WORK_PATH=C:\\work"
        ));

        assertThat(values).containsExactly(Map.entry("WORK_PATH", "C:\\work"));
    }

    @Test
    @DisplayName("trims keys and values and removes surrounding quotes")
    void shouldTrimAndUnquote() {
        Map<String, String> values = ConfigService.parse(List.of(
                "  WORK_PATH  =   C:\\work  ",
                "DOUBLE=\"C:\\work\"",
                "SINGLE='C:\\work'"
        ));

        assertThat(values)
                .containsEntry("WORK_PATH", "C:\\work")
                .containsEntry("DOUBLE", "C:\\work")
                .containsEntry("SINGLE", "C:\\work");
    }

    @Test
    @DisplayName("splits on the first '=' only")
    void shouldSplitOnTheFirstSeparator() {
        Map<String, String> values = ConfigService.parse(List.of("KEY=a=b=c"));

        assertThat(values).containsEntry("KEY", "a=b=c");
    }

    @Test
    @DisplayName("skips lines without a valid key")
    void shouldSkipInvalidLines() {
        Map<String, String> values = ConfigService.parse(List.of(
                "no-separator",
                "=no-key",
                "VALID=value"
        ));

        assertThat(values).containsExactly(Map.entry("VALID", "value"));
    }

    @Test
    @DisplayName("keeps an empty value")
    void shouldKeepEmptyValue() {
        Map<String, String> values = ConfigService.parse(List.of("WORK_PATH="));

        assertThat(values).containsEntry("WORK_PATH", "");
    }

}

