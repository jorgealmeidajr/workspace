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

    @Nested
    class RightPad {
        @Test
        void testNullInput() {
            assertNull(FileService.rightPad(null, 10, " "));
        }

        @Test
        void testNullPadString() {
            assertNull(FileService.rightPad("hello", 10, null));
        }

        @Test
        void testBothNull() {
            assertNull(FileService.rightPad(null, 10, null));
        }

        @Test
        void testInputLongerThanLength() {
            assertEquals("hello", FileService.rightPad("hello", 3, " "));
        }

        @Test
        void testInputEqualToLength() {
            assertEquals("hello", FileService.rightPad("hello", 5, " "));
        }

        @Test
        void testSimplePaddingWithSpace() {
            assertEquals("hello     ", FileService.rightPad("hello", 10, " "));
        }

        @Test
        void testPaddingWithMultipleCharacters() {
            assertEquals("hello*-*-*-", FileService.rightPad("hello", 11, "*-"));
        }

        @Test
        void testPaddingWithSingleCharacter() {
            assertEquals("hello0000", FileService.rightPad("hello", 9, "0"));
        }

        @Test
        void testEmptyStringPadding() {
            assertEquals("     ", FileService.rightPad("", 5, " "));
        }

        @Test
        void testPaddingWithEmptyLength() {
            assertEquals("", FileService.rightPad("", 0, " "));
        }

        @Test
        void testLengthOneNeedsPadding() {
            assertEquals("a  ", FileService.rightPad("a", 3, " "));
        }

        @Test
        void testLargeLength() {
            String result = FileService.rightPad("x", 100, " ");
            assertEquals(100, result.length());
            assertTrue(result.startsWith("x"));
            assertTrue(result.endsWith("                                                                                                   "));
        }

        @Test
        void testPaddingWithComplexString() {
            assertEquals("helloXYZXYZ", FileService.rightPad("hello", 11, "XYZ"));
        }
    }

    @Nested
    class CalculateColumnWidth {
        @Test
        void testSingleHeaderWithShortString() {
            String[] headers = {"Name"};
            assertEquals(4, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testSingleHeaderWithLongString() {
            String[] headers = {"VeryLongHeaderName"};
            assertEquals(18, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testMultipleHeadersWithSameLength() {
            String[] headers = {"Name", "City", "Type"};
            assertEquals(4, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testMultipleHeadersWithDifferentLengths() {
            String[] headers = {"Name", "CityName", "C"};
            assertEquals(8, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testMultipleHeadersFirstIsLongest() {
            String[] headers = {"VeryLongHeaderName", "City", "Age"};
            assertEquals(18, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testMultipleHeadersLastIsLongest() {
            String[] headers = {"Name", "City", "VeryLongHeaderName"};
            assertEquals(18, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testMultipleHeadersMiddleIsLongest() {
            String[] headers = {"Name", "VeryLongHeaderName", "City"};
            assertEquals(18, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testSingleCharacterHeader() {
            String[] headers = {"A"};
            assertEquals(1, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testEmptyStringHeader() {
            String[] headers = {""};
            assertEquals(0, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testMultipleEmptyStringHeaders() {
            String[] headers = {"", "", ""};
            assertEquals(0, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testHeadersWithSpaces() {
            String[] headers = {"First Name", "Last Name", "Age"};
            assertEquals(10, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testHeadersWithSpecialCharacters() {
            String[] headers = {"Name@User", "Email#Domain", "ID-Number"};
            assertEquals(12, FileService.calculateColumnWidth(headers));
        }

        @Test
        void testLargeNumberOfHeaders() {
            String[] headers = {"A", "BB", "CCC", "DDDD", "EEEEE", "FFFFFF"};
            assertEquals(6, FileService.calculateColumnWidth(headers));
        }
    }

}
