package workspace.commons.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FileServiceTest {

    @Nested
    class IsSvgXml {
        @Test
        void testNullInput() {
            assertFalse(FileService.isSvgXml(null));
        }

        @Test
        void testValidSvgXml() {
            String svg = "<svg><circle/></svg>";
            assertTrue(FileService.isSvgXml(svg));
        }

        @Test
        void testValidSvgXmlWithAttributes() {
            String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\"><rect/></svg>";
            assertTrue(FileService.isSvgXml(svg));
        }

        @Test
        void testSvgWithWhitespace() {
            String svg = "  <svg>  </svg>  ";
            assertTrue(FileService.isSvgXml(svg));
        }

        @Test
        void testSvgCaseInsensitive() {
            assertTrue(FileService.isSvgXml("<SVG></SVG>"));
            assertTrue(FileService.isSvgXml("<Svg></Svg>"));
        }

        @Test
        void testMissingSvgOpenTag() {
            assertFalse(FileService.isSvgXml("</svg>"));
        }

        @Test
        void testMissingSvgCloseTag() {
            assertFalse(FileService.isSvgXml("<svg>"));
        }

        @Test
        void testEmptyString() {
            assertFalse(FileService.isSvgXml(""));
        }

        @Test
        void testNonSvgXml() {
            assertFalse(FileService.isSvgXml("<div></div>"));
        }

        @Test
        void testSvgWithNewlines() {
            String svg = "<svg>\n  <circle/>\n</svg>";
            assertTrue(FileService.isSvgXml(svg));
        }
    }

}
