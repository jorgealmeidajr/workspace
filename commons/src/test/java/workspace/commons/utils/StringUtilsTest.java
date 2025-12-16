package workspace.commons.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StringUtilsTest {

    @Nested
    class GetValueAfterDotTest {
        @Test
        public void testGetValueAfterDot_WithDot() {
            String result = StringUtils.getValueAfterDot("schema.table");
            assertEquals("table", result);
        }

        @Test
        public void testGetValueAfterDot_MultipleDots() {
            String result = StringUtils.getValueAfterDot("schema.table.column");
            assertEquals("table.column", result);
        }

        @Test
        public void testGetValueAfterDot_NoDot() {
            String result = StringUtils.getValueAfterDot("table");
            assertEquals("table", result);
        }

        @Test
        public void testGetValueAfterDot_EmptyString() {
            String result = StringUtils.getValueAfterDot("");
            assertEquals("", result);
        }

        @Test
        public void testGetValueAfterDot_DotAtEnd() {
            String result = StringUtils.getValueAfterDot("schema.");
            assertEquals("", result);
        }

        @Test
        public void testGetValueAfterDot_DotAtStart() {
            String result = StringUtils.getValueAfterDot(".table");
            assertEquals("table", result);
        }
    }

    @Nested
    class GetValueBeforeDotTest {
        @Test
        public void testGetValueBeforeDot_WithDot() {
            String result = StringUtils.getValueBeforeDot("schema.table");
            assertEquals("schema", result);
        }

        @Test
        public void testGetValueBeforeDot_MultipleDots() {
            String result = StringUtils.getValueBeforeDot("schema.table.column");
            assertEquals("schema", result);
        }

        @Test
        public void testGetValueBeforeDot_NoDot() {
            String result = StringUtils.getValueBeforeDot("table");
            assertEquals("table", result);
        }

        @Test
        public void testGetValueBeforeDot_EmptyString() {
            String result = StringUtils.getValueBeforeDot("");
            assertEquals("", result);
        }

        @Test
        public void testGetValueBeforeDot_DotAtEnd() {
            String result = StringUtils.getValueBeforeDot("schema.");
            assertEquals("schema", result);
        }

        @Test
        public void testGetValueBeforeDot_DotAtStart() {
            String result = StringUtils.getValueBeforeDot(".table");
            assertEquals("", result);
        }
    }

}
